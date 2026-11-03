package com.tavola.data.group

import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.Group
import com.tavola.domain.model.GroupMember
import com.tavola.domain.model.GroupRules

// How the groups are stored in Firestore. The security rules in `firestore.rules` depend on these names and shapes, so a change
// here has to be made there too.
//
//   users/{userId}                    { groupId }                  the group of a person, or null
//   groups/{groupId}                  { name, memberIds, inviteCode, createdBy }
//   groups/{groupId}/members/{userId} { displayName, photoUrl, colorIndex, joinCode? }
//   invites/{code}                    { groupId }                  lets a person who knows the code find the group

internal object GroupPaths {
    const val USERS = "users"
    const val GROUPS = "groups"
    const val MEMBERS = "members"
    const val INVITES = "invites"
}

internal object GroupFields {
    const val GROUP_ID = "groupId"
    const val NAME = "name"
    const val MEMBER_IDS = "memberIds"
    const val INVITE_CODE = "inviteCode"
    const val CREATED_BY = "createdBy"
    const val DISPLAY_NAME = "displayName"
    const val PHOTO_URL = "photoUrl"
    const val COLOR_INDEX = "colorIndex"

    /** The code a person joined with: it proves to the rules that they knew it. Only the members who joined carry it. */
    const val JOIN_CODE = "joinCode"
}

internal fun newGroupDocument(name: String, creatorId: String, inviteCode: String): Map<String, Any> = mapOf(
    GroupFields.NAME to name,
    GroupFields.MEMBER_IDS to listOf(creatorId),
    GroupFields.INVITE_CODE to inviteCode,
    GroupFields.CREATED_BY to creatorId,
)

internal fun inviteDocument(groupId: String): Map<String, Any> = mapOf(GroupFields.GROUP_ID to groupId)

/** The member document of [user]. [joinCode] is set when they join an existing group and left out for the person who creates it. */
internal fun memberDocument(user: AuthUser, joinCode: String?): Map<String, Any?> = buildMap {
    put(GroupFields.DISPLAY_NAME, user.displayName)
    put(GroupFields.PHOTO_URL, user.photoUrl)
    put(GroupFields.COLOR_INDEX, GroupRules.memberColorIndex(user.id))
    if (joinCode != null) put(GroupFields.JOIN_CODE, joinCode)
}

/** The group stored in a document, or `null` if the document is missing something it must have. */
internal fun Map<String, Any?>.toGroup(id: String): Group? {
    val name = this[GroupFields.NAME] as? String ?: return null
    val inviteCode = this[GroupFields.INVITE_CODE] as? String ?: return null
    val memberIds = (this[GroupFields.MEMBER_IDS] as? List<*>)?.filterIsInstance<String>() ?: return null
    return Group(id = id, name = name, memberIds = memberIds, inviteCode = inviteCode)
}

internal fun Map<String, Any?>.toGroupMember(userId: String): GroupMember = GroupMember(
    userId = userId,
    displayName = this[GroupFields.DISPLAY_NAME] as? String,
    photoUrl = this[GroupFields.PHOTO_URL] as? String,
    colorIndex = (this[GroupFields.COLOR_INDEX] as? Number)?.toInt() ?: GroupRules.memberColorIndex(userId),
)
