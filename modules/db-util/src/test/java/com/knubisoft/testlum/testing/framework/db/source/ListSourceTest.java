package com.knubisoft.testlum.testing.framework.db.source;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ListSource} verifying query delimiter splitting,
 * newline replacement, and blank query filtering.
 */
class ListSourceTest {

    @Nested
    class BasicSplitting {
        @Test
        void singleQueryNoDelimiter() {
            final ListSource source = new ListSource(List.of("SELECT 1"));
            assertEquals(1, source.getQueries().size());
            assertEquals("SELECT 1", source.getQueries().get(0));
        }

        @Test
        void multipleQueriesWithDelimiter() {
            final ListSource source = new ListSource(List.of("SELECT 1;;SELECT 2"));
            assertEquals(2, source.getQueries().size());
            assertEquals("SELECT 1", source.getQueries().get(0));
            assertEquals("SELECT 2", source.getQueries().get(1));
        }

        @Test
        void multipleEntriesInList() {
            final ListSource source = new ListSource(List.of("SELECT 1", "SELECT 2"));
            assertEquals(2, source.getQueries().size());
        }
    }

    @Nested
    class NewlineHandling {
        @Test
        void newlinesReplacedWithSpaces() {
            final ListSource source = new ListSource(List.of("SELECT\n1"));
            assertEquals("SELECT 1", source.getQueries().get(0));
        }
    }

    @Nested
    class BlankFiltering {
        @Test
        void blankQueriesFiltered() {
            final ListSource source = new ListSource(List.of("SELECT 1;;  ;;SELECT 2"));
            assertEquals(2, source.getQueries().size());
        }

        @Test
        void emptyListProducesNoQueries() {
            final ListSource source = new ListSource(List.of());
            assertTrue(source.getQueries().isEmpty());
        }

        @Test
        void onlyDelimitersProduceNoQueries() {
            final ListSource source = new ListSource(List.of(";;;;"));
            assertTrue(source.getQueries().isEmpty());
        }
    }

    @Nested
    class CombinedBehavior {
        @Test
        void delimiterAndNewlinesAndBlanks() {
            final ListSource source = new ListSource(
                    List.of("INSERT INTO t\nVALUES(1);;  ;;DELETE FROM t"));
            assertEquals(2, source.getQueries().size());
            assertEquals("INSERT INTO t VALUES(1)", source.getQueries().get(0));
            assertEquals("DELETE FROM t", source.getQueries().get(1));
        }
    }
}
