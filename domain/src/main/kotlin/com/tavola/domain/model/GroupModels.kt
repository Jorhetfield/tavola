package com.tavola.domain.model

/** A group of people who share a calendar and its recipes. Everyone in [memberIds] has the same permissions. */
data class Group(val id: String, val name: String, val memberIds: List<String>, val inviteCode: String)

/**
 * A person of a group, as the group shows them. [displayName] and [photoUrl] come from their Google account;
 * [colorIndex] is the position in the member palette (the screens wrap it around the colors they have).
 */
data class GroupMember(val userId: String, val displayName: String?, val photoUrl: String?, val colorIndex: Int)

/** The group of the signed-in person: they belong to at most one. */
sealed interface MyGroupState {
    data object None : MyGroupState

    data class Joined(val group: Group, val members: List<GroupMember>) : MyGroupState
}

/** Why a group action did not work, as far as the person can do something about it. */
enum class GroupFailure {
    /** There is no connection: trying again later may work. */
    Network,

    /** Nobody is signed in. */
    NotSignedIn,

    /** The person already belongs to a group and has to leave it first. */
    AlreadyInGroup,

    /** The name is empty once cleaned up. */
    InvalidName,

    /** Anything else; the details are not shown to the person. */
    Other,
}

sealed interface GroupActionResult {
    data object Done : GroupActionResult

    data class Failed(val reason: GroupFailure) : GroupActionResult
}

sealed interface JoinGroupResult {
    data object Joined : JoinGroupResult

    /** The text is not a code, or no group has it (it may have been renewed). */
    data object InvalidCode : JoinGroupResult

    data class Failed(val reason: GroupFailure) : JoinGroupResult
}
