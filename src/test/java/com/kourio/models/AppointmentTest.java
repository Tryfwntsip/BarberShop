package com.kourio.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    @Test
    void newAppointmentDefaultsToConfirmed() {
        Appointment a = new Appointment();
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
    }

    @Test
    void constructorStoresBookingSnapshot() {
        Appointment a = new Appointment(1, 4, 2, 3,
                LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), LocalTime.of(10, 45),
                new BigDecimal("22.00"), 45, AppointmentStatus.CONFIRMED, "σημείωση");

        // Η τιμή και η διάρκεια «κλειδώνουν» τη στιγμή της κράτησης (price stability)
        assertEquals(new BigDecimal("22.00"), a.getPriceAtBooking());
        assertEquals(45, a.getDurationAtBooking());
        assertEquals(LocalTime.of(10, 45), a.getEndTime());
    }

    @Test
    void statusCanTransitionThroughLifecycle() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.IN_PROGRESS);
        assertEquals(AppointmentStatus.IN_PROGRESS, a.getStatus());
        a.setStatus(AppointmentStatus.COMPLETED);
        assertEquals(AppointmentStatus.COMPLETED, a.getStatus());
    }

    @Test
    void associatedEntitiesAreOptional() {
        Appointment a = new Appointment();
        assertNull(a.getCustomer());
        assertNull(a.getBarber());
        assertNull(a.getService());
    }
}
