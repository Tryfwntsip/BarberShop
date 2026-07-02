package com.kourio.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Καθαρή (pure) κλάση με τους επιχειρησιακούς κανόνες κρατήσεων.
 *
 * Δεν έχει καμία εξάρτηση από βάση δεδομένων ή Swing — μόνο στατικές
 * συναρτήσεις πάνω σε απλές τιμές. Αυτό επιτρέπει:
 *   1. Unit testing χωρίς MySQL (βλ. src/test/java/.../BookingRulesTest).
 *   2. Επαναχρησιμοποίηση των ίδιων κανόνων από DAO και Controllers,
 *      ώστε η λογική να ορίζεται σε ΕΝΑ σημείο.
 */
public final class BookingRules {

    /** Ελάχιστος χρόνος (λεπτά) πριν το ραντεβού για επιτρεπτή ακύρωση (SRS §3.3.2). */
    public static final int CANCELLATION_WINDOW_MINUTES = 120;

    /** Μέγιστες αποτυχημένες προσπάθειες login πριν το κλείδωμα λογαριασμού. */
    public static final int MAX_FAILED_LOGINS = 5;

    private BookingRules() {
        // Utility class — no instances
    }

    /**
     * Έλεγχος επικάλυψης δύο χρονικών διαστημάτων [start1, end1) και [start2, end2).
     * Κλασική συνθήκη: start1 < end2 AND start2 < end1.
     * Τα διαστήματα είναι ημι-ανοιχτά: ραντεβού που τελειώνει 10:00
     * ΔΕΝ συγκρούεται με ραντεβού που ξεκινά 10:00.
     */
    public static boolean overlaps(LocalTime start1, LocalTime end1,
                                   LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Έλεγχος αν το διάστημα [start, start+duration) χωράει μέσα στο ωράριο
     * εργασίας [workStart, workEnd] του κουρέα.
     */
    public static boolean withinWorkingHours(LocalTime start, int durationMinutes,
                                             LocalTime workStart, LocalTime workEnd) {
        LocalTime end = start.plusMinutes(durationMinutes);
        return !start.isBefore(workStart) && !end.isAfter(workEnd);
    }

    /**
     * Κανόνας ακύρωσης: επιτρέπεται μόνο αν το ραντεβού είναι στο μέλλον
     * και απέχει τουλάχιστον {@link #CANCELLATION_WINDOW_MINUTES} λεπτά.
     */
    public static boolean canCancel(LocalDateTime now, LocalDateTime appointmentStart) {
        if (appointmentStart.isBefore(now)) {
            return false; // Το ραντεβού έχει ήδη παρέλθει
        }
        long minutesRemaining = Duration.between(now, appointmentStart).toMinutes();
        return minutesRemaining >= CANCELLATION_WINDOW_MINUTES;
    }

    /**
     * Πρέπει ο λογαριασμός να κλειδωθεί μετά από αυτή την αποτυχημένη προσπάθεια;
     */
    public static boolean shouldLockAccount(int failedLogins) {
        return failedLogins >= MAX_FAILED_LOGINS;
    }

    /**
     * Μέσο έσοδο ανά ολοκληρωμένο ραντεβού (UC-10).
     * Επιστρέφει 0 όταν δεν υπάρχουν ολοκληρωμένα, αποφεύγοντας διαίρεση με το μηδέν.
     */
    public static BigDecimal averageRevenue(BigDecimal totalRevenue, int completedCount) {
        if (completedCount <= 0 || totalRevenue == null) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * Ποσοστό no-show επί του συνόλου των κρατήσεων (UC-10), σε κλίμακα 0–100.
     */
    public static double noShowRate(int noShowCount, int totalBookings) {
        if (totalBookings <= 0) {
            return 0.0;
        }
        return ((double) noShowCount / totalBookings) * 100.0;
    }
}
