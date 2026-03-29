package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link DateFormat} enum verifying format detection, temporal parsing, and date comparison. */
class DateFormatTest {

    @Test
    void findFormatDateTimeWithDash() {
        final DateFormat fmt = DateFormat.findFormat("2024-01-15 10:30:00");
        assertEquals(DateFormat.DATE_TIME_FORMAT_WITH_DASH, fmt);
    }

    @Test
    void findFormatDateTimeWithSlash() {
        final DateFormat fmt = DateFormat.findFormat("2024/01/15 10:30:00");
        assertEquals(DateFormat.DATE_TIME_FORMAT_WITH_SLASH, fmt);
    }

    @Test
    void findFormatDateWithDash() {
        final DateFormat fmt = DateFormat.findFormat("2024-01-15");
        assertEquals(DateFormat.DATE_FORMAT_WITH_DASH, fmt);
    }

    @Test
    void findFormatDateWithSlash() {
        final DateFormat fmt = DateFormat.findFormat("2024/01/15");
        assertEquals(DateFormat.DATE_FORMAT_WITH_SLASH, fmt);
    }

    @Test
    void findFormatTime() {
        final DateFormat fmt = DateFormat.findFormat("10:30:00");
        assertEquals(DateFormat.TIME_FORMAT, fmt);
    }

    @Test
    void findFormatThrowsForUnsupported() {
        assertThrows(MatchException.class, () -> DateFormat.findFormat("not-a-date"));
    }

    @Test
    void parseDateTimeWithDash() {
        final Comparable<?> result = DateFormat.DATE_TIME_FORMAT_WITH_DASH.parse("2024-01-15 10:30:00");
        assertInstanceOf(LocalDateTime.class, result);
    }

    @Test
    void parseDateWithDash() {
        final Comparable<?> result = DateFormat.DATE_FORMAT_WITH_DASH.parse("2024-01-15");
        assertInstanceOf(LocalDate.class, result);
    }

    @Test
    void parseTime() {
        final Comparable<?> result = DateFormat.TIME_FORMAT.parse("10:30:00");
        assertInstanceOf(LocalTime.class, result);
    }

    @Test
    void parseDateTimeWithSlash() {
        final Comparable<?> result = DateFormat.DATE_TIME_FORMAT_WITH_SLASH.parse("2024/01/15 10:30:00");
        assertInstanceOf(LocalDateTime.class, result);
    }

    @Test
    void parseDateWithSlash() {
        final Comparable<?> result = DateFormat.DATE_FORMAT_WITH_SLASH.parse("2024/01/15");
        assertInstanceOf(LocalDate.class, result);
    }

    @Test
    void parseInvalidThrows() {
        assertThrows(MatchException.class, () -> DateFormat.DATE_FORMAT_WITH_DASH.parse("not-a-date"));
    }

    @Test
    void nowReturnsCorrectType() {
        assertInstanceOf(LocalDateTime.class, DateFormat.DATE_TIME_FORMAT_WITH_DASH.now());
        assertInstanceOf(LocalDate.class, DateFormat.DATE_FORMAT_WITH_DASH.now());
        assertInstanceOf(LocalTime.class, DateFormat.TIME_FORMAT.now());
    }

    @Test
    void compareWithDates() {
        assertTrue(DateFormat.DATE_FORMAT_WITH_DASH
                .compareWith("2024-06-15", "2024-01-15", Operator.MORE_THEN));
        assertTrue(DateFormat.DATE_FORMAT_WITH_DASH
                .compareWith("2024-01-15", "2024-06-15", Operator.LESS_THEN));
    }

    @Test
    void compareWithNowReturnsResult() {
        assertTrue(DateFormat.DATE_FORMAT_WITH_DASH
                .compareWithNow("2000-01-01", Operator.LESS_THEN));
    }

    @Test
    void nowReturnsCorrectTypeForSlashFormats() {
        assertInstanceOf(LocalDateTime.class, DateFormat.DATE_TIME_FORMAT_WITH_SLASH.now());
        assertInstanceOf(LocalDate.class, DateFormat.DATE_FORMAT_WITH_SLASH.now());
    }

    @Test
    void compareWithSlashDates() {
        assertTrue(DateFormat.DATE_FORMAT_WITH_SLASH
                .compareWith("2024/06/15", "2024/01/15", Operator.MORE_THEN));
        assertTrue(DateFormat.DATE_FORMAT_WITH_SLASH
                .compareWith("2024/01/15", "2024/06/15", Operator.LESS_THEN));
    }
}
