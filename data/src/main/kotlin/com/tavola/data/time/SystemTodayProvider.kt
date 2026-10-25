package com.tavola.data.time

import com.tavola.domain.time.TodayProvider
import java.time.LocalDate as JavaLocalDate
import javax.inject.Inject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

/** Hoy según el reloj y la zona horaria del dispositivo. */
internal class SystemTodayProvider @Inject constructor() : TodayProvider {
    override fun today(): LocalDate = JavaLocalDate.now().toKotlinLocalDate()
}
