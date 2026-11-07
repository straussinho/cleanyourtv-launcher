package io.github.justinnewman.gameday;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/** Personnalisation : tous les matchs du jour, le filtre "Équipe - Compétition" et les titres "A - B". */
public class PersoScheduleTest {
    private static final Instant NOW = Instant.parse("2026-11-07T11:00:00Z");
    private static final ZoneId UTC = ZoneOffset.UTC;

    private static Calendar cal(String name) {
        String link = "https://h.example/" + name.replace(' ', '-');
        return new Calendar(Links.id(link), name, link, Sport.byLabel("Soccer"), 14);
    }

    private static Ics.Event ev(String title, String start) {
        Instant s = Instant.parse(start);
        return new Ics.Event(title, s, s.plus(Duration.ofMinutes(105)), false);
    }

    private static List<String> tops(Calendar c, Ics.Event... events) {
        Map<String, List<Ics.Event>> m = new HashMap<>();
        m.put(c.id, Arrays.asList(events));
        List<String> out = new ArrayList<>();
        for (Schedule.Turn t : Schedule.turns(Arrays.asList(c), m, NOW, UTC)) out.add(t.topLine);
        return out;
    }

    @Test
    public void everyGameOfTodayTakesATurn() {
        assertEquals(Arrays.asList("Leeds - Spurs", "Arsenal - Hull", "Fulham - Newcastle"),
                tops(cal("Premier League"),
                        ev("Leeds - Spurs", "2026-11-07T12:30:00Z"),
                        ev("Arsenal - Hull", "2026-11-07T15:00:00Z"),
                        ev("Fulham - Newcastle", "2026-11-07T15:00:00Z"),
                        ev("Everton - Wolves", "2026-11-08T14:00:00Z")));
    }

    @Test
    public void aGameStillOnStaysWithTheRestOfTheDay() {
        assertEquals(Arrays.asList("Leeds - Spurs", "Arsenal - Hull"),
                tops(cal("Premier League"),
                        ev("Leeds - Spurs", "2026-11-07T10:30:00Z"),
                        ev("Arsenal - Hull", "2026-11-07T15:00:00Z")));
    }

    @Test
    public void aLaterMatchdayShowsOnlyItsFirstGame() {
        assertEquals(Arrays.asList("Armenia - Cyprus"),
                tops(cal("Ligue des nations"),
                        ev("Armenia - Cyprus", "2026-11-12T17:00:00Z"),
                        ev("Italy - France", "2026-11-12T19:45:00Z")));
    }

    @Test
    public void teamAndCompetitionKeepsOnlyThatTeam() {
        assertEquals(Arrays.asList("Italy - France"),
                tops(cal("France - Ligue des nations"),
                        ev("Armenia - Cyprus", "2026-11-12T17:00:00Z"),
                        ev("Italy - France", "2026-11-12T19:45:00Z"),
                        ev("France - Belgium", "2026-11-15T19:45:00Z")));
    }

    @Test
    public void aDashBetweenTwoTeamsIsAGame() {
        assertEquals("Kickoff", Titles.word(Sport.byLabel("Soccer"), "Leeds United - Tottenham Hotspur"));
    }
}
