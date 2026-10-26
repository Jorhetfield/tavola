package com.tavola.data.calendar

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A quick task of a day. [position] keeps the order inside the day; gaps are fine, only the order matters. */
@Entity(tableName = "day_tasks", indices = [Index("date")])
internal data class DayTaskEntity(@PrimaryKey val id: String, val date: String, val position: Int, val text: String, val done: Boolean)
