package org.naftalin.pdtj;

import org.naftalin.pdtj.m4.WorkPeriod;
import org.naftalin.pdtj.m4.WorkPeriods;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class OffsetDateTimeExample {
    public static void main(String[] args) {

        Clock testClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        List<WorkPeriod> wps = WorkPeriods.generateWorkPeriods(LocalDate.now(testClock), 2);
        ZoneOffset origZone = ZoneOffset.of("+0");
        ZoneOffset destZone = ZoneOffset.of("+8");
        LocalDateTime landingLocalTime = LocalDateTime.of(LocalDate.now(testClock).plusDays(1), LocalTime.of(17, 55));

        OffsetDateTime landingWithDestinationOffset = OffsetDateTime.of(landingLocalTime, destZone);
        OffsetDateTime landingWithOriginOffset = landingWithDestinationOffset.withOffsetSameInstant(origZone);
        LocalDateTime originLocalLandingTime = landingWithOriginOffset.toLocalDateTime();

        int zoneDifferenceSeconds = destZone.getTotalSeconds() - origZone.getTotalSeconds();
        originLocalLandingTime = landingLocalTime.minusSeconds(zoneDifferenceSeconds);

        List<WorkPeriod> usableWorkPeriods = new ArrayList<>();
        for (WorkPeriod wp : wps) {
            System.out.print(wp + "\t" + (!wp.contains(originLocalLandingTime)));
        }
    }
}






