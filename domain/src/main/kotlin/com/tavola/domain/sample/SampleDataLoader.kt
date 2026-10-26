package com.tavola.domain.sample

/**
 * Debug-only tool to fill the app with sample data and to wipe it. Only debug builds bind an implementation, so
 * release builds never contain fake data and nothing in them may depend on this interface.
 */
interface SampleDataLoader {
    /**
     * Adds the sample recipes and a few weeks of sample meals and tasks around today. It only adds or overwrites:
     * recipes, meals and tasks that share an id or a slot with a sample one are replaced, and anything else already
     * in the app stays. Use [clearAllData] first for a clean sample.
     */
    suspend fun loadSampleData()

    /** Deletes every recipe, meal and task. */
    suspend fun clearAllData()
}
