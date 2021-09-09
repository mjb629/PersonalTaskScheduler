package org.naftalin.pdtj.m4;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WorkPeriodTest {

    private LocalDate startLocalDate;

    @Before
    public void setup() {
        startLocalDate = LocalDate.ofEpochDay(0);
    }

    @Test
    public void basicSplitTest() {
        LocalDateTime startTime = startLocalDate.atTime(23, 0);
        LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
        WorkPeriod p = WorkPeriod.of(startTime, endTime);
        LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();

        List<WorkPeriod> workPeriods = p.split(midnight, null);

        assertEquals(workPeriods,Arrays.asList(WorkPeriod.of(startTime, midnight), WorkPeriod.of(midnight, endTime)));
    }

    @Test
    public void testSplitTwoDayPeriod() {
        LocalDateTime startTime = startLocalDate.atTime(0, 0);
        LocalDateTime endTime = startLocalDate.plusDays(2).atTime(0, 0);
        WorkPeriod p = WorkPeriod.of(startTime, endTime);
        LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();

        List<WorkPeriod> split = p.split(midnight, null);

        assertEquals(split, Arrays.asList(WorkPeriod.of(startTime, midnight), WorkPeriod.of(midnight, endTime)));
    }

    @Test
    public void testSplitOnEndTime() {
        WorkPeriod p = WorkPeriod.of(startLocalDate.atTime(22, 0), startLocalDate.atTime(23, 0));

        List<WorkPeriod> splitPeriods = p.split(startLocalDate.atTime(23, 0), null);

        assertEquals(splitPeriods, Arrays.asList(p));
    }

    @Test
    public void testSplitOnStartTime() {
        WorkPeriod p = WorkPeriod.of(startLocalDate.atTime(22, 0), startLocalDate.atTime(23, 0));
        List<WorkPeriod> split = p.split(startLocalDate.atTime(22, 0), null);
        assertEquals(Arrays.asList(p), split);
    }

    @Test
    public void testSplitWithSingleLongTaskPart() {
        LocalDateTime startTime = startLocalDate.atTime(23, 0);
        LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
        WorkPeriod p = WorkPeriod.of(startTime, endTime);
        Task t = new Task(0, 120, "");
        p.addTaskPart(TaskPart.wholeOf(t));
        LocalDateTime midnight = startLocalDate.plusDays(1).atStartOfDay();

        List<WorkPeriod> splitPeriods = p.split(midnight, null);

        WorkPeriod expectedFirstHalf = WorkPeriod.of(startTime, midnight);
        expectedFirstHalf.addTaskPart(new TaskPart(t, Duration.ofMinutes(60), 1));
        WorkPeriod expectedSecondHalf = WorkPeriod.of(midnight, endTime);
        expectedSecondHalf.addTaskPart(new TaskPart(t, Duration.ofMinutes(60), 2));

        assertEquals(splitPeriods, Arrays.asList(expectedFirstHalf, expectedSecondHalf));
    }

    @Test
    public void testSplitWithTaskPartBoundaryOnSplit() {
        LocalDateTime startTime = startLocalDate.atTime(23, 0);
        LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
        LocalDateTime midnight = startTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        WorkPeriod p = WorkPeriod.of(startTime, endTime);
        Task t1 = new Task(0, 60, "");
        TaskPart tp1 = TaskPart.wholeOf(t1);
        Task t2 = new Task(0, 60, "");
        TaskPart tp2 = TaskPart.wholeOf(t2);
        p.addTaskPart(tp1);
        p.addTaskPart(tp2);
        List<WorkPeriod> split = p.split(midnight, null);

        WorkPeriod expectedFirstHalf = WorkPeriod.of(startTime, midnight);
        expectedFirstHalf.addTaskPart(tp1);
        assertEquals(expectedFirstHalf, split.get(0));
        WorkPeriod expectedSecondHalf = WorkPeriod.of(midnight, endTime);
        expectedSecondHalf.addTaskPart(tp2);
        assertEquals(expectedSecondHalf, split.get(1));
    }

	@Test
	public void testSplitWithTaskPartBoundaryNotOnSplit() {
		LocalDateTime startTime = startLocalDate.atTime(23, 0);
		LocalDateTime endTime = startLocalDate.plusDays(1).atTime(1, 0);
		WorkPeriod p = WorkPeriod.of(startTime, endTime);
		Task t1 = new Task(0, 40, "");
		TaskPart tp1 = TaskPart.wholeOf(t1);
		Task t2 = new Task(0, 40, "");
		TaskPart tp2 = TaskPart.wholeOf(t2);
		Task t3 = new Task(0, 20, "");
		TaskPart tp3 = TaskPart.wholeOf(t3);
		p.addTaskPart(tp1);
		p.addTaskPart(tp2);
		p.addTaskPart(tp3);

		List<WorkPeriod> split = p.split(startTime.plusDays(1).truncatedTo(ChronoUnit.DAYS), null);

        LocalDateTime midnight = endTime.withHour(0);
		WorkPeriod expectedFirstHalf = WorkPeriod.of(startTime, midnight);
		expectedFirstHalf.addTaskPart(tp1);
		expectedFirstHalf.addTaskPart(new TaskPart(t2, Duration.ofMinutes(20), 1));
		assertEquals(expectedFirstHalf, split.get(0));

		WorkPeriod expectedSecondHalf = WorkPeriod.of(midnight, endTime);
		expectedSecondHalf.addTaskPart(new TaskPart(t2, Duration.ofMinutes(20), 2));
		expectedSecondHalf.addTaskPart(tp3);
		assertEquals(expectedSecondHalf, split.get(1));
	}
}
