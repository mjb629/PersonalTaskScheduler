package org.naftalin.pdtj.m4;

import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.*;
import java.util.List;

import static org.junit.Assert.*;

public class CalendarTest {

	private SchedulerCalendar calendar;

	private Task t60mins, t30mins, t20mins;
	private WorkPeriod p20mins, p30mins, p60mins;
	private LocalDateTime localSchedStart;
	private Clock clock;

	@Before
	public void setup () {
		t20mins = new Task(20, "");
		t30mins = new Task(30, "");
		t60mins = new Task(60, "");

		calendar = new SchedulerCalendar();

		LocalDate startDate = LocalDate.ofEpochDay(0);
		ZoneId theZone = ZoneOffset.UTC;
		clock = Clock.fixed(Instant.from(startDate.atStartOfDay(theZone)), theZone);
		localSchedStart = LocalDateTime.now(clock);

		p20mins = WorkPeriod.of(localSchedStart, localSchedStart.plusMinutes(20));
		p30mins = WorkPeriod.of(localSchedStart.plusMinutes(60), localSchedStart.plusMinutes(90));
		p60mins = WorkPeriod.of(localSchedStart.plusMinutes(60), localSchedStart.plusMinutes(120));
	}

	@Test
	public void testAllocateOneTaskSuccess() {
		calendar.addTask(t20mins);
		calendar.addWorkPeriod(p30mins);

		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		List<WorkPeriod> scheduledPeriods = schedule.getScheduledPeriods();
		assertEquals(1, scheduledPeriods.size());
		List<TaskPart> taskParts = scheduledPeriods.get(0).getTaskParts();
		assertEquals(1, taskParts.size());
		assertEquals(t20mins, taskParts.get(0).getOwner());
		assertEquals(Duration.ofMinutes(20), taskParts.get(0).getDuration());
	}

	@Test
	public void testAllocateOneTaskFailure() {
		calendar.addTask(t30mins);
		calendar.addWorkPeriod(p20mins);

		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testAllocationAllowsForDst() {

		// this test now passes, because the scheduler now takes account
		// of DST changes when calculating the duration of a WorkPeriod
		LocalDate startDate = LocalDate.of(2018, 3, 25);
		ZoneId theZone = ZoneId.of("Europe/London");
		clock = Clock.fixed(Instant.from(startDate.atStartOfDay(theZone)), theZone);
		localSchedStart = LocalDateTime.now(clock);

		calendar.addTask(t60mins);
		p60mins = WorkPeriod.of(localSchedStart.plusMinutes(60), localSchedStart.plusMinutes(120));
		calendar.addWorkPeriod(p60mins);

		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testAllocateExactFit() {
		calendar.addTask(t60mins);
		p60mins = WorkPeriod.of(localSchedStart.plusMinutes(60), localSchedStart.plusMinutes(120));
		calendar.addWorkPeriod(p60mins);

		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		assertTrue(schedule.isSuccessful());
	}

	@Test
	public void testAllocateNoWorkPeriods() {
		calendar.addTask(t60mins);
		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());
		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testAllocateWorkPeriodsInPast() {
		calendar.addTask(t60mins);
		calendar.addWorkPeriod((WorkPeriod.of(localSchedStart.minusMinutes(60), localSchedStart.minusMinutes(30))));
		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());
		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testNoTasksNoPeriods() {
		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());
		assertTrue(schedule.getScheduledPeriods().isEmpty());
		assertTrue(schedule.isSuccessful());
	}

	@Test
	public void testNowNearEndOfPeriodFails() {
		calendar.addTask(t30mins);
		calendar.addWorkPeriod(WorkPeriod.of(localSchedStart.minusMinutes(30), localSchedStart.plusSeconds(30)));

		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testVeryShortAvailabilityNotUsed() {
		calendar.addTask(t60mins);
		calendar.addWorkPeriod(WorkPeriod.of(localSchedStart.minusMinutes(30), localSchedStart.plusSeconds(30)));
		calendar.addWorkPeriod(WorkPeriod.of(localSchedStart.plusMinutes(30), localSchedStart.plusMinutes(90)));

		List<WorkPeriod> schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone()).getScheduledPeriods();

		assertEquals(schedule.get(1).getTaskParts().get(0).getDuration(), t60mins.getDuration());
	}

	@Test
	public void testAllocateTwoPeriodsSuccess() {
		calendar.addTask(t30mins);
		calendar.addWorkPeriod(p20mins);
		calendar.addWorkPeriod(p30mins);
		List<WorkPeriod> schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone()).getScheduledPeriods();

		assertEquals(2, schedule.size());
		List<TaskPart> taskParts1 = schedule.get(0).getTaskParts();
		assertEquals(1, taskParts1.size());
		assertEquals(t30mins, taskParts1.get(0).getOwner());
		assertEquals(Duration.ofMinutes(20), taskParts1.get(0).getDuration());
		List<TaskPart> taskParts2 = schedule.get(1).getTaskParts();
		assertEquals(1, taskParts2.size());
		assertEquals(t30mins, taskParts2.get(0).getOwner());
		assertEquals(Duration.ofMinutes(10), taskParts2.get(0).getDuration());
	}

	@Test
	public void testAllocateTwoPeriodsFailure() {
		calendar.addTask(t60mins);
		calendar.addWorkPeriod(p20mins);
		calendar.addWorkPeriod(p30mins);
		Schedule schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone());

		assertFalse(schedule.isSuccessful());
	}

	@Test
	public void testAllocateTwoTasksSuccess() {
		calendar.addTask(t20mins);
		calendar.addTask(t30mins);
		calendar.addWorkPeriod(p60mins);
		List<WorkPeriod> schedule = calendar.createSchedule(LocalDate.now(clock), clock.getZone()).getScheduledPeriods();

		assertEquals(1, schedule.size());
		List<TaskPart> taskParts = schedule.get(0).getTaskParts();
		assertEquals(2, taskParts.size());
		assertEquals(t20mins, taskParts.get(0).getOwner());
		assertEquals(Duration.ofMinutes(20), taskParts.get(0).getDuration());
		assertEquals(t30mins, taskParts.get(1).getOwner());
		assertEquals(Duration.ofMinutes(30), taskParts.get(1).getDuration());
	}

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testOverlappingPeriodsRejected() {
		calendar.addWorkPeriod(p20mins);
		WorkPeriod newPeriod = WorkPeriod.of(localSchedStart.plusMinutes(15), localSchedStart.plusMinutes(25));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(StringStartsWith.startsWith("Work Periods cannot overlap:"));
		exception.expectMessage(p20mins.toString());
		exception.expectMessage(StringEndsWith.endsWith(newPeriod.toString()));
		calendar.addWorkPeriod(newPeriod);
	}

}
