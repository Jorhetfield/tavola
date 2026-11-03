package com.tavola.domain.repository

import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.JoinGroupResult
import com.tavola.domain.model.MyGroupState
import kotlinx.coroutines.flow.Flow

/**
 * The group of the signed-in person. A person belongs to at most one, and every member has the same permissions. The real
 * implementation (Firestore) lives in `:data`. Every action works on the signed-in person and fails with `NotSignedIn` if there
 * is none.
 */
interface GroupRepository {
    /** Emits the group of the signed-in person with its members (or that there is none) now and every time it changes. */
    val myGroup: Flow<MyGroupState>

    /** Creates a group called [name] with the person as its only member. */
    suspend fun createGroup(name: String): GroupActionResult

    /** Joins the group that has [code], as typed by the person (any case, with or without dashes). */
    suspend fun joinGroup(code: String): JoinGroupResult

    suspend fun renameGroup(name: String): GroupActionResult

    /** Gives the group a new invite code; the old one stops working. */
    suspend fun renewInviteCode(): GroupActionResult

    /** The person stops belonging to the group. If they were the last one, the group stays as it is, without members. */
    suspend fun leaveGroup(): GroupActionResult
}
