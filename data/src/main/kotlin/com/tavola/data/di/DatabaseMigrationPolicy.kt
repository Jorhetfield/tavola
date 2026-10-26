package com.tavola.data.di

/**
 * What the database does when its schema version changes and there is no migration for it. Debug builds wipe the data
 * (so development never gets stuck on a schema mismatch); release builds never do and need real migrations.
 */
internal data class DatabaseMigrationPolicy(val destructive: Boolean)
