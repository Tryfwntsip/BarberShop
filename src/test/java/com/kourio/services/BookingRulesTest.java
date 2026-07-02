package com.kourio.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests για τους επιχειρησιακούς κανόνες κρατήσεων (BookingRules).
 * Τρέχουν χωρίς βάση δεδομένων: mvn test
 */
@DisplayName("BookingRules — επιχειρησιακοί κανόνες κρατήσεων")
class BookingRulesTest {

    @Nested
    @DisplayName("Επικάλυψη ραντεβού (overlaps)")
    class OverlapTests {

        @Test
        @DisplayName("Πλήρης επικάλυψη: ίδιο ακριβώς slot → σύγκρουση")
        void sameSlotOverlaps() {
            assertTrue(BookingRules.overlaps(
                    LocalTime.of(10, 0), LocalTime.of(10, 30),
                    LocalTime.of(10, 0), LocalTime.of(10, 30)));
        }

        @Test
        @DisplayName("Μερική επικάλυψη: 10:00–10:30 vs 10:15–10:45 → σύγκρουση")
        void partialOverlapDetected() {
            assertTrue(BookingRules.overlaps(
                    LocalTime.of(10, 0), LocalTime.of(10, 30),
                    LocalTime.of(10, 15), LocalTime.of(10, 45)));
        }

        @Test
        @DisplayName("Το ένα περιέχει το άλλο: 10:00–11:00 vs 10:15–10:30 → σύγκρουση")
        void containedIntervalOverlaps() {
            assertTrue(BookingRules.overlaps(
                    LocalTime.of(10, 0), LocalTime.of(11, 0),
                    LocalTime.of(10, 15), LocalTime.of(10, 30)));
        }

        @Test
        @DisplayName("Back-to-back ραντεβού: 09:00–10:00 και 10:00–10:30 → ΔΕΝ συγκρούονται")
        void backToBackDoesNotOverlap() {
            assertFalse(BookingRules.overlaps(
                    LocalTime.of(9, 0), LocalTime.of(10, 0),
                    LocalTime.of(10, 0), LocalTime.of(10, 30)));
        }

        @Test
        @DisplayName("Εντελώς ξένα διαστήματα → ΔΕΝ συγκρούονται")
        void disjointIntervalsDoNotOverlap() {
            assertFalse(BookingRules.overlaps(
                    LocalTime.of(9, 0), LocalTime.of(9, 30),
                    LocalTime.of(12, 0), LocalTime.of(12, 30)));
        }
    }

    @Nested
    @DisplayName("Ωράριο εργασίας κουρέα (withinWorkingHours)")
    class WorkingHoursTests {

        private final LocalTime workStart = LocalTime.of(9, 0);
        private final LocalTime workEnd = LocalTime.of(17, 0);

        @Test
        @DisplayName("Slot εντός ωραρίου → επιτρέπεται")
        void insideWorkingHours() {
            assertTrue(BookingRules.withinWorkingHours(LocalTime.of(10, 0), 30, workStart, workEnd));
        }

        @Test
        @DisplayName("Οριακό: 16:30 + 30' τελειώνει ακριβώς 17:00 → επιτρέπεται")
        void endsExactlyAtClosing() {
            assertTrue(BookingRules.withinWorkingHours(LocalTime.of(16, 30), 30, workStart, workEnd));
        }

        @Test
        @DisplayName("16:45 + 30' ξεπερνά το κλείσιμο → απορρίπτεται")
        void spillsPastClosing() {
            assertFalse(BookingRules.withinWorkingHours(LocalTime.of(16, 45), 30, workStart, workEnd));
        }

        @Test
        @DisplayName("Έναρξη πριν το άνοιγμα → απορρίπτεται")
        void startsBeforeOpening() {
            assertFalse(BookingRules.withinWorkingHours(LocalTime.of(8, 30), 30, workStart, workEnd));
        }
    }

    @Nested
    @DisplayName("Κανόνας ακύρωσης 2 ωρών (canCancel — SRS §3.3.2)")
    class CancellationTests {

        private final LocalDateTime now = LocalDateTime.of(2026, 7, 2, 12, 0);

        @Test
        @DisplayName("Ραντεβού σε 3 ώρες → ακύρωση επιτρέπεται")
        void cancellationAllowedWellInAdvance() {
            assertTrue(BookingRules.canCancel(now, now.plusHours(3)));
        }

        @Test
        @DisplayName("Οριακό: ακριβώς 2 ώρες πριν → επιτρέπεται (>= 120 λεπτά)")
        void exactlyTwoHoursAllowed() {
            assertTrue(BookingRules.canCancel(now, now.plusMinutes(120)));
        }

        @Test
        @DisplayName("119 λεπτά πριν → απορρίπτεται")
        void justUnderTwoHoursRejected() {
            assertFalse(BookingRules.canCancel(now, now.plusMinutes(119)));
        }

        @Test
        @DisplayName("Ραντεβού που έχει ήδη παρέλθει → απορρίπτεται")
        void pastAppointmentRejected() {
            assertFalse(BookingRules.canCancel(now, now.minusHours(1)));
        }
    }

    @Nested
    @DisplayName("Κλείδωμα λογαριασμού (shouldLockAccount)")
    class LockoutTests {

        @Test
        @DisplayName("4 αποτυχίες → όχι κλείδωμα")
        void fourFailuresNoLock() {
            assertFalse(BookingRules.shouldLockAccount(4));
        }

        @Test
        @DisplayName("5 αποτυχίες → κλείδωμα")
        void fiveFailuresLock() {
            assertTrue(BookingRules.shouldLockAccount(5));
        }
    }

    @Nested
    @DisplayName("Στατιστικά UC-10 (averageRevenue / noShowRate)")
    class StatisticsTests {

        @Test
        @DisplayName("Μέσο έσοδο: 100€ / 3 ολοκληρωμένα = 33.33 (HALF_UP)")
        void averageRevenueRounding() {
            assertEquals(new BigDecimal("33.33"),
                    BookingRules.averageRevenue(new BigDecimal("100.00"), 3));
        }

        @Test
        @DisplayName("Κανένα ολοκληρωμένο → 0 (όχι διαίρεση με το μηδέν)")
        void averageRevenueZeroCompleted() {
            assertEquals(BigDecimal.ZERO,
                    BookingRules.averageRevenue(new BigDecimal("100.00"), 0));
        }

        @Test
        @DisplayName("No-show rate: 2 από 8 κρατήσεις = 25%")
        void noShowRateComputed() {
            assertEquals(25.0, BookingRules.noShowRate(2, 8), 0.0001);
        }

        @Test
        @DisplayName("Καμία κράτηση → 0% (όχι διαίρεση με το μηδέν)")
        void noShowRateZeroBookings() {
            assertEquals(0.0, BookingRules.noShowRate(3, 0), 0.0001);
        }
    }
}
