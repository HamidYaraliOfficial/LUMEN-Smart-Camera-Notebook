package com.lumen.app

import com.lumen.app.domain.model.BusinessHoursSchedule
import com.lumen.app.domain.model.DayHours
import com.lumen.app.domain.usecase.CalculateBusinessStatusUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZonedDateTime

class CalculateBusinessStatusUseCaseTest {

    private val useCase = CalculateBusinessStatusUseCase()

    private fun mondayToFridayNineToFive(): BusinessHoursSchedule {
        val days = (1..7).map { dow ->
            if (dow in 1..5) DayHours(dow, isOpen = true, openMinuteOfDay = 9 * 60, closeMinuteOfDay = 17 * 60)
            else DayHours(dow, isOpen = false, openMinuteOfDay = null, closeMinuteOfDay = null)
        }
        return BusinessHoursSchedule("s1", "Test Shop", "UTC", days, 0L)
    }

    @Test
    fun `open during business hours on a weekday`() {
        val schedule = mondayToFridayNineToFive()
        val now = ZonedDateTime.now(java.time.ZoneId.of("UTC"))
            .with(DayOfWeek.WEDNESDAY).withHour(11).withMinute(0)
        val status = useCase(schedule, now)
        assertTrue(status.isOpenNow)
        assertEquals("Closes at 17:00", status.nextChangeLabel)
    }

    @Test
    fun `closed outside business hours on a weekday`() {
        val schedule = mondayToFridayNineToFive()
        val now = ZonedDateTime.now(java.time.ZoneId.of("UTC"))
            .with(DayOfWeek.WEDNESDAY).withHour(20).withMinute(0)
        val status = useCase(schedule, now)
        assertTrue(!status.isOpenNow)
        assertEquals("Opens at 09:00", status.nextChangeLabel)
    }

    @Test
    fun `closed all day on the weekend`() {
        val schedule = mondayToFridayNineToFive()
        val now = ZonedDateTime.now(java.time.ZoneId.of("UTC"))
            .with(DayOfWeek.SATURDAY).withHour(10).withMinute(0)
        val status = useCase(schedule, now)
        assertTrue(!status.isOpenNow)
        assertEquals("Opens at 09:00", status.nextChangeLabel)
    }
}
