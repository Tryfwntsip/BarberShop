package com.kourio.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void newServiceIsActiveByDefault() {
        Service s = new Service();
        assertTrue(s.isActive());
    }

    @Test
    void toStringShowsDurationAndPriceForComboBoxDisplay() {
        Service s = new Service(1, "Κούρεμα (Ανδρικό)", new BigDecimal("15.00"), 30, "Μαλλιά", true);
        String text = s.toString();
        assertTrue(text.contains("Κούρεμα (Ανδρικό)"));
        assertTrue(text.contains("30 min"));
        assertTrue(text.contains("15.00"));
    }

    @Test
    void toStringFallsBackToNameWhenPriceMissing() {
        Service s = new Service();
        s.setName("Ξύρισμα");
        assertEquals("Ξύρισμα", s.toString());
    }
}
