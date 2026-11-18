package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class RaceWeekendTest {

    // The 2026 United States GP, times as Jolpica gives them (UTC).
    private static final RaceWeekend.Race AUSTIN = new RaceWeekend.Race(18, "United States Grand Prix", "Austin",
            Arrays.asList(
                    // Out of order on purpose: the calendar is sorted by start time.
                    new RaceWeekend.Session("race", Instant.parse("2026-10-25T20:00:00Z")),
                    new RaceWeekend.Session("FP1", Instant.parse("2026-10-23T17:30:00Z")),
                    new RaceWeekend.Session("FP2", Instant.parse("2026-10-23T21:00:00Z")),
                    new RaceWeekend.Session("FP3", Instant.parse("2026-10-24T17:30:00Z")),
                    new RaceWeekend.Session("Qualifying", Instant.parse("2026-10-24T21:00:00Z"))));
    private static final List<RaceWeekend.Race> CALENDAR = Collections.singletonList(AUSTIN);

    @Test
    public void nothingBeforeTheWeekendOpens() {
        assertNull(RaceWeekend.at(CALENDAR, Instant.parse("2026-10-16T17:29:00Z")));
    }

    @Test
    public void opensSevenDaysBeforeFirstPractice() {
        RaceWeekend.State s = RaceWeekend.at(CALENDAR, Instant.parse("2026-10-16T17:30:00Z"));
        assertSame(AUSTIN, s.race);
        assertEquals("FP1", s.next.name);
        assertEquals(Duration.ofDays(7), s.untilNext);
        assertEquals(RaceWeekend.Status.NEXT, s.statuses.get(0));
        assertEquals(RaceWeekend.Status.LATER, s.statuses.get(4));
    }

    @Test
    public void aSessionOnTrackIsLiveAndTheNextOneIsCountedDown() {
        RaceWeekend.State s = RaceWeekend.at(CALENDAR, Instant.parse("2026-10-24T21:30:00Z"));
        assertEquals("QUALIFYING", s.live.name);
        assertTrue(s.next.isRace());
        assertEquals(Arrays.asList(RaceWeekend.Status.DONE, RaceWeekend.Status.DONE, RaceWeekend.Status.DONE,
                RaceWeekend.Status.LIVE, RaceWeekend.Status.NEXT), s.statuses);
    }

    @Test
    public void theRaceIsLiveForTwoHoursThenFinished() {
        RaceWeekend.State live = RaceWeekend.at(CALENDAR, Instant.parse("2026-10-25T21:59:00Z"));
        assertTrue(live.live.isRace());
        assertNull(live.next);
        RaceWeekend.State done = RaceWeekend.at(CALENDAR, Instant.parse("2026-10-25T22:00:00Z"));
        assertTrue(done.finished());
    }

    @Test
    public void closesFourHoursAfterTheRaceStarts() {
        assertTrue(RaceWeekend.at(CALENDAR, Instant.parse("2026-10-25T23:59:00Z")).finished());
        assertNull(RaceWeekend.at(CALENDAR, Instant.parse("2026-10-26T00:00:00Z")));
    }

    // Las Vegas puis Qatar, cinq jours d'écart : les deux fenêtres de 7 jours se chevauchent.
    private static final RaceWeekend.Race VEGAS = new RaceWeekend.Race(21, "Las Vegas Grand Prix", "Las Vegas",
            Arrays.asList(
                    new RaceWeekend.Session("FP1", Instant.parse("2026-11-20T00:30:00Z")),
                    new RaceWeekend.Session("RACE", Instant.parse("2026-11-22T04:00:00Z"))));
    private static final RaceWeekend.Race QATAR = new RaceWeekend.Race(22, "Qatar Grand Prix", "Lusail",
            Arrays.asList(
                    new RaceWeekend.Session("FP1", Instant.parse("2026-11-27T13:30:00Z")),
                    new RaceWeekend.Session("RACE", Instant.parse("2026-11-29T16:00:00Z"))));

    @Test
    public void backToBackWeekendsShowTheFirstUntilItEnds() {
        List<RaceWeekend.Race> both = Arrays.asList(VEGAS, QATAR);
        assertSame(VEGAS, RaceWeekend.at(both, Instant.parse("2026-11-21T12:00:00Z")).race);
        assertSame(VEGAS, RaceWeekend.at(both, Instant.parse("2026-11-22T07:59:00Z")).race);
        assertSame(QATAR, RaceWeekend.at(both, Instant.parse("2026-11-22T08:00:00Z")).race);
    }

}
