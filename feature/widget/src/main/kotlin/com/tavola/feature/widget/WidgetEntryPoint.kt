package com.tavola.feature.widget

import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.time.TodayProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** What the widget takes from the app's Hilt graph: a widget is built by the system, not by Hilt, so it asks for it here. */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun authRepository(): AuthRepository

    fun groupRepository(): GroupRepository

    fun calendarRepository(): CalendarRepository

    fun todayProvider(): TodayProvider

    fun widgetLauncher(): WidgetLauncher
}
