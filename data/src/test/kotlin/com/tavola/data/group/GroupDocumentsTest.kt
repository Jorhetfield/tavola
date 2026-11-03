package com.tavola.data.group

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.Group
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.GroupMember
import com.tavola.domain.model.GroupRules
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupDocumentsTest {
    private val marta = AuthUser(id = "user-marta", displayName = "Marta Ruiz", email = "marta@example.com", photoUrl = "https://photo")

    @Test
    fun `a new group document has the shape the rules ask for`() {
        val document = newGroupDocument(name = "Casa", creatorId = marta.id, inviteCode = "K7M2QX4R")

        assertEquals(setOf("name", "memberIds", "inviteCode", "createdBy"), document.keys)
        assertEquals(listOf(marta.id), document["memberIds"])
        assertEquals(marta.id, document["createdBy"])
    }

    @Test
    fun `a group document is read back as the group that was written`() {
        val document = newGroupDocument(name = "Casa", creatorId = marta.id, inviteCode = "K7M2QX4R")

        assertEquals(Group("g1", "Casa", listOf(marta.id), "K7M2QX4R"), document.toGroup("g1"))
    }

    @Test
    fun `a document that lacks something a group needs is not a group`() {
        assertNull(mapOf<String, Any?>("name" to "Casa").toGroup("g1"))
        assertNull(mapOf<String, Any?>("name" to "Casa", "inviteCode" to "K7M2QX4R", "memberIds" to "not-a-list").toGroup("g1"))
        assertNull(emptyMap<String, Any?>().toGroup("g1"))
    }

    @Test
    fun `the member of someone who creates a group has no join code, and one who joins carries the code`() {
        val creator = memberDocument(marta, joinCode = null)
        val joiner = memberDocument(marta, joinCode = "K7M2QX4R")

        assertFalse(creator.containsKey("joinCode"))
        assertEquals("K7M2QX4R", joiner["joinCode"])
        assertEquals(setOf("displayName", "photoUrl", "colorIndex"), creator.keys)
    }

    @Test
    fun `a member document is read back with the name, the photo and the color of the person`() {
        val member = memberDocument(marta, joinCode = null).toGroupMember(marta.id)

        assertEquals(GroupMember(marta.id, "Marta Ruiz", "https://photo", GroupRules.memberColorIndex(marta.id)), member)
    }

    @Test
    fun `a member document without a color gets the color of the person`() {
        val member = mapOf<String, Any?>("displayName" to "Luis").toGroupMember("user-luis")

        assertEquals(GroupRules.memberColorIndex("user-luis"), member.colorIndex)
        assertNull(member.photoUrl)
    }

    @Test
    fun `the invite document only holds the group`() {
        assertEquals(mapOf("groupId" to "g1"), inviteDocument("g1"))
    }

    @Test
    fun `a lost connection is a network failure and anything else a generic one`() {
        assertEquals(GroupFailure.Network, FirebaseNetworkException("offline").toGroupFailure())
        assertEquals(
            GroupFailure.Network,
            FirebaseFirestoreException("unavailable", FirebaseFirestoreException.Code.UNAVAILABLE).toGroupFailure(),
        )
        assertEquals(
            GroupFailure.Other,
            FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED).toGroupFailure(),
        )
        assertEquals(GroupFailure.Other, IllegalStateException("boom").toGroupFailure())
    }

    @Test
    fun `only a permission error is told apart as denied`() {
        assertTrue(FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED).isPermissionDenied())
        assertFalse(FirebaseFirestoreException("unavailable", FirebaseFirestoreException.Code.UNAVAILABLE).isPermissionDenied())
        assertFalse(IllegalStateException("denied").isPermissionDenied())
    }
}
