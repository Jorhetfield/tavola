package com.tavola.data.recipe

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.tavola.data.group.GroupPaths
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The recipe book of the group in Firestore: every member reads and writes the same recipes, and a change shows up on every
 * phone through the listeners. The group of the person comes from [GroupRepository]; with no group there are no recipes.
 *
 * Writes are kept by Firestore when there is no connection and sent later, so they only wait a short while for the server's
 * answer (a refusal comes within it) instead of hanging while offline.
 */
@Singleton
internal class FirestoreRecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
) : RecipeRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecipes(): Flow<List<Recipe>> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId == null) {
            flowOf(emptyList())
        } else {
            recipes(groupId).snapshots(MetadataChanges.INCLUDE).map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.data?.toRecipe(document.id) }
                    .sortedBy { recipe -> recipe.title.lowercase() }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecipe(id: String): Flow<Recipe?> = groupRepository.groupId.flatMapLatest { groupId ->
        if (groupId ==
            null
        ) {
            flowOf(null)
        } else {
            recipes(groupId).document(id).snapshots().map { snapshot -> snapshot.data?.toRecipe(snapshot.id) }
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): String {
        val collection = recipes(currentGroupId())
        val reference = if (recipe.id.isBlank()) collection.document() else collection.document(recipe.id)
        // Only a new recipe is signed with whoever makes it; an edit keeps its author (the write merges, so the field is not touched).
        val createdBy = if (recipe.id.isBlank()) authRepository.currentUser.first()?.id else recipe.createdBy
        withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { reference.set(recipe.toDocument(createdBy), SetOptions.merge()).await() }
        return reference.id
    }

    override suspend fun deleteRecipe(id: String) {
        val groupId = groupRepository.groupId.first() ?: return
        withTimeoutOrNull(ACKNOWLEDGEMENT_WAIT_MILLIS) { recipes(groupId).document(id).delete().await() }
    }

    /** The group of the person; right after a group is created it can take a moment to arrive, so this waits a little for it. */
    private suspend fun currentGroupId(): String = withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() }
        ?: error("There is no group to save the recipe in")

    private fun recipes(groupId: String): CollectionReference =
        firestore.collection(GroupPaths.GROUPS).document(groupId).collection(RECIPES_COLLECTION)

    private companion object {
        const val ACKNOWLEDGEMENT_WAIT_MILLIS = 4_000L
        const val GROUP_WAIT_MILLIS = 10_000L
    }
}
