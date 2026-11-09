package com.tavola.data.firestore

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.fake.FakeGroupRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupWritesTest {
    private val groups = FakeGroupRepository()
    private val groupWrites = GroupWrites(groups)

    private fun seedGroupOfTheUser() = groups.seedGroup("Casa", memberIds = listOf(FakeAuthRepository.SampleUser.id))

    @Test
    fun `a write waits for a group that arrives a little later, then runs in it`() = runTest {
        val group = CompletableDeferred<String>()
        val write = async { groupWrites.write { groupId -> group.complete(groupId) } }

        advanceTimeBy(2_000)
        assertFalse(group.isCompleted)
        val seeded = seedGroupOfTheUser()
        write.await()

        assertEquals(seeded.id, group.await())
    }

    @Test
    fun `a write with no group gives up after the wait and does not run`() = runTest {
        var ran = false

        groupWrites.write { ran = true }

        assertFalse(ran)
        assertEquals(GroupWrites.GROUP_WAIT_MILLIS, testScheduler.currentTime)
    }

    @Test
    fun `a refusal of the server is not thrown at a fire-and-forget write`() = runTest {
        seedGroupOfTheUser()

        groupWrites.write { throw FirebaseFirestoreException("gone", Code.NOT_FOUND) }
        groupWrites.write { throw FirebaseFirestoreException("denied", Code.PERMISSION_DENIED) }
    }

    @Test
    fun `a write that never gets an answer stops waiting after a few seconds`() = runTest {
        seedGroupOfTheUser()
        var finished = false

        val write = async { groupWrites.write { delay(60_000).also { finished = true } } }
        advanceUntilIdle()
        write.await()

        assertFalse(finished)
        assertEquals(GroupWrites.ACKNOWLEDGEMENT_WAIT_MILLIS, testScheduler.currentTime)
    }

    @Test
    fun `acknowledged returns the result and, unlike write, throws a refusal at the caller`() = runTest {
        assertEquals("ok", groupWrites.acknowledged { "ok" })
        assertNull(groupWrites.acknowledged { delay(60_000) })

        val refusal = runCatching { groupWrites.acknowledged { throw FirebaseFirestoreException("denied", Code.PERMISSION_DENIED) } }
            .exceptionOrNull()
        assertEquals(Code.PERMISSION_DENIED, (refusal as FirebaseFirestoreException).code)
    }

    @Test
    fun `requireGroupId names what could not be saved when there is no group`() = runTest {
        val failure = runCatching { groupWrites.requireGroupId("item") }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
        assertTrue(failure?.message.orEmpty().contains("item"))
    }
}
