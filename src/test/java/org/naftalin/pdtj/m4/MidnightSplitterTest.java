package org.naftalin.pdtj.m4;

import org.junit.Before;
import org.junit.Test;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class MidnightSplitterTest {

    private Clock clock;
    private Schedule.MidnightSplitter midnightSplitter;

    @Before
    public void setup() {
        clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        midnightSplitter = new Schedule.MidnightSplitter();
    }

    @Test
    public void testNoSplit() {
        ZonedDateTime eventStartTime = ZonedDateTime.now(clock);
        Event testEvent = Event.of(eventStartTime, eventStartTime.plusHours(6), "");

        List<Event> splitEvents = midnightSplitter.splitAtAllMidnights(testEvent, clock.getZone()).collect(toList());

        assertEquals(splitEvents, Arrays.asList(testEvent));
    }

    @Test
    public void testOneSplit() {
        ZonedDateTime eventStartTime = ZonedDateTime.now(clock);
        Event testEvent = Event.of(eventStartTime, eventStartTime.plusHours(25), "");

        List<Event> splitEvents = midnightSplitter.splitAtAllMidnights(testEvent, clock.getZone()).collect(toList());

        ZonedDateTime splitTime = eventStartTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        assertEquals(splitEvents, Arrays.asList(testEvent.withEndTime(splitTime), testEvent.withStartTime(splitTime)));
    }

    @Test
    public void testTwoSplits() {
        ZonedDateTime eventStartTime = ZonedDateTime.now(clock);
        Event testEvent = Event.of(eventStartTime, eventStartTime.plusHours(49), "");

        List<Event> splitEvents = midnightSplitter.splitAtAllMidnights(testEvent, clock.getZone()).collect(toList());

        ZonedDateTime firstSplit = eventStartTime.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime secondSplit = firstSplit.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        Event firstPart = testEvent.withEndTime(firstSplit);
        Event secondPart = testEvent.withStartTime(firstSplit).withEndTime(secondSplit);
        Event thirdPart = testEvent.withStartTime(secondSplit);
        assertEquals(splitEvents, Arrays.asList(firstPart, secondPart, thirdPart));
    }

    @Test
    public void testOverDstTransition() {
        ZonedDateTime eventStartTime = ZonedDateTime.of(LocalDateTime.of(2021, 3, 27, 12, 0), ZoneId.of("Europe/London"));
        Event testEvent = Event.of(eventStartTime, eventStartTime.plusHours(24), "");

        List<Event> splitEvents = midnightSplitter.splitAtAllMidnights(testEvent, ZoneId.of("Europe/Paris")).collect(toList());

        ZonedDateTime split = eventStartTime.withZoneSameInstant(ZoneId.of("Europe/Paris")).plusDays(1).truncatedTo(ChronoUnit.DAYS);
        assertEquals(splitEvents, Arrays.asList(testEvent.withEndTime(split), testEvent.withStartTime(split)));
    }
}

