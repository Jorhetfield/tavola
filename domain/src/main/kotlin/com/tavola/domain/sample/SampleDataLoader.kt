package com.tavola.domain.sample

/**
 * Debug-only tool to fill the app with sample data and to wipe it. Only debug builds bind an implementation, so
 * release builds never contain fake data and nothing in them may depend on this interface.
 */
interface SampleDataLoader {
    /** Adds the sample recipes and a few weeks of sample meals and tasks around today, replacing what is on the same dates. */
    suspend fun loadSampleData()

    /** Deletes every recipe, meal and task. */
    suspend fun clearAllData()
}
