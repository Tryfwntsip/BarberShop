package com.kourio.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void newUserIsActiveWithNoFailedLogins() {
        User u = new User();
        assertTrue(u.isActive());
        assertEquals(0, u.getFailedLogins());
        assertNull(u.getLockedUntil());
    }

    @Test
    void isLockedReturnsFalseWhenLockedUntilIsNull() {
        User u = new User();
        assertFalse(u.isLocked());
    }

    @Test
    void isLockedReturnsTrueWhenLockedUntilIsInTheFuture() {
        User u = new User();
        u.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        assertTrue(u.isLocked());
    }

    @Test
    void isLockedReturnsFalseWhenLockExpired() {
        User u = new User();
        u.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        assertFalse(u.isLocked());
    }

    @Test
    void fullNameJoinsFirstAndLastName() {
        User u = new User(1, "nikos", "hash", Role.BARBER, "Νίκος", "Γεωργίου", "6970000000", "n@kourio.gr");
        assertEquals("Νίκος Γεωργίου", u.getFullName());
    }

    @Test
    void toStringIncludesRoleWhenPresent() {
        User u = new User(1, "nikos", "hash", Role.BARBER, "Νίκος", "Γεωργίου", "6970000000", "n@kourio.gr");
        assertTrue(u.toString().contains("BARBER"));
    }
}
