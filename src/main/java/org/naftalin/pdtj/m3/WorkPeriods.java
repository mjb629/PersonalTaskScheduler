package org.naftalin.pdtj.m3;

import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.time.DayOfWeek.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

public class WorkPeriods {

    public static final LocalTime AM_START_TIME = LocalTime.of(9, 0);
    public static final LocalTime PM_START_TIME = LocalTime.of(13, 30);
    public static final Duration WORK_PERIOD_LENGTH = Duration.ofHours(3).plusMinutes(30);

    public static WorkPeriod createMorningWorkPeriod(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, AM_START_TIME);
        LocalDateTime endDateTime = startDateTime.plus(WORK_PERIOD_LENGTH);
        return WorkPeriod.of(startDateTime, endDateTime);
    }
    public static WorkPeriod createAfternoonWorkPeriod(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, PM_START_TIME);
        LocalDateTime endDateTime = startDateTime.plus(WORK_PERIOD_LENGTH);
        return WorkPeriod.of(startDateTime, endDateTime);
    }

    public static List<WorkPeriod> generateWorkPeriods(LocalDate startDate, int dayCount) {
        List<LocalDate> workingDays = generateWorkingDays(startDate, dayCount);
        return generateWorkPeriods(workingDays);
    }

    private static List<WorkPeriod> generateWorkPeriods(List<LocalDate> workingDays) {
        return workingDays.stream()
                .flatMap(d -> Stream.of(createMorningWorkPeriod(d),createAfternoonWorkPeriod(d)))
                .collect(toList());
    }

    private static List<WorkPeriod> generateWorkPeriods_loopVersion(List<LocalDate> workingDays) {
        List<WorkPeriod> workPeriods = new ArrayList<>();
        for (LocalDate d : workingDays) {
            workPeriods.add(createMorningWorkPeriod(d));
            workPeriods.add(createAfternoonWorkPeriod(d));
        }
        return workPeriods;
    }

    private static List<LocalDate> generateWorkingDays(LocalDate startDate, int dayCount) {
        return Stream.iterate(startDate, d -> d.with(nextWorkingDayAdjuster))
                .limit(dayCount)
                .collect(toList());
    }

    private static List<LocalDate> generateWorkingDays_loopVersion(LocalDate startDate, int dayCount) {
        int i = 0;
        List<LocalDate> workingDays = new ArrayList<>();
        for (LocalDate date = startDate; i < dayCount; date = date.plusDays(1)) {
            if (isWorkingDay(date)) {
                workingDays.add(date);
                i = i + 1;
            }
        }
        return workingDays;
    }

    private final static TemporalAdjuster nextWorkingDayAdjuster =
            d -> DayOfWeek.from(d) != FRIDAY
                    ? d.plus(1, DAYS)
                    : d.with(TemporalAdjusters.next(MONDAY));

    private static boolean isWorkingDay(LocalDate d) {
        DayOfWeek dayOfWeek = d.getDayOfWeek();
        return ! (dayOfWeek == SATURDAY || dayOfWeek == SUNDAY);
    }

    public static void main(String[] args) {
        List<WorkPeriod> workPeriods = generateWorkPeriods(LocalDate.ofEpochDay(0), 3);
        workPeriods.forEach(System.out::println);
    }
}
















