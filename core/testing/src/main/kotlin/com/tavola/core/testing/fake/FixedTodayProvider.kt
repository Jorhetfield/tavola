package com.tavola.core.testing.fake

import com.tavola.domain.time.TodayProvider
import kotlinx.datetime.LocalDate

/** "Hoy" fijo y modificable, para tests que cruzan medianoche o cambian de semana. */
class FixedTodayProvider(var date: LocalDate = LocalDate(2026, 10, 21)) : TodayProvider {
    override fun today(): LocalDate = date
}
