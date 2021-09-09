package org.naftalin.pdtj.m4;

import org.junit.Before;
import org.junit.Test;

import java.time.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Module5DemoTest {

    private Clock clock;
    @Before
    public void setup() {
        LocalDate date = LocalDate.of(2021, 3, 28);
        ZoneId zone = ZoneId.of("Europe/London");
        ZonedDateTime zdt = date.atStartOfDay(zone);
        clock = Clock.fixed(Instant.from(zdt), zone);
//        clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    }
    @Test
    public void testAllocateOneTaskSuccess() {
        SchedulerCalendar calendar = new SchedulerCalendar();
        LocalDateTime start = LocalDateTime.now(clock);
        calendar.addWorkPeriod(WorkPeriod.of(start, start.plusHours(2)));
        Task task = new Task(120, "");
        calendar.addTask(task);
        Schedule schedule = calendar.createSchedule(start, clock.getZone());
        ZonedDateTime absolutePeriodStart = ZonedDateTime.of(start, clock.getZone());
        ZonedDateTime absolutePeriodEnd = ZonedDateTime.of(start.plusHours(2), clock.getZone());
        Duration effectiveWpDuration = Duration.between(absolutePeriodStart, absolutePeriodEnd);
        boolean periodCanHoldTask = effectiveWpDuration.compareTo(task.getDuration()) >= 0;
        assertEquals(periodCanHoldTask, schedule.isSuccessful());
//        assertTrue(schedule.isSuccessful());
    }
}
 