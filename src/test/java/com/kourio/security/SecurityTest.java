package com.kourio.security;

import com.kourio.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests για τους μηχανισμούς ασφάλειας:
 * BCrypt password hashing και κλείδωμα λογαριασμού (User.isLocked).
 */
@DisplayName("Ασφάλεια — BCrypt & account lockout")
class SecurityTest {

    @Test
    @DisplayName("Ο σωστός κωδικός επαληθεύεται έναντι του BCrypt hash")
    void correctPasswordVerifies() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        assertTrue(BCrypt.checkpw("password123", hash));
    }

    @Test
    @DisplayName("Λάθος κωδικός απορρίπτεται")
    void wrongPasswordRejected() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        assertFalse(BCrypt.checkpw("password124", hash));
    }

    @Test
    @DisplayName("Ο ίδιος κωδικός παράγει διαφορετικό hash κάθε φορά (τυχαίο salt)")
    void saltMakesHashesUnique() {
        String hash1 = BCrypt.hashpw("password123", BCrypt.gensalt());
        String hash2 = BCrypt.hashpw("password123", BCrypt.gensalt());
        assertNotEquals(hash1, hash2, "Το salt πρέπει να κάνει κάθε hash μοναδικό");
        // ...αλλά και τα δύο επαληθεύουν τον ίδιο κωδικό
        assertTrue(BCrypt.checkpw("password123", hash1));
        assertTrue(BCrypt.checkpw("password123", hash2));
    }

    @Test
    @DisplayName("Το hash δεν περιέχει τον κωδικό σε plaintext")
    void hashDoesNotContainPlaintext() {
        String hash = BCrypt.hashpw("password123", BCrypt.gensalt());
        assertFalse(hash.contains("password123"));
    }

    @Test
    @DisplayName("isLocked: μελλοντικό lockedUntil → κλειδωμένος")
    void lockedWhenLockedUntilInFuture() {
        User user = new User();
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        assertTrue(user.isLocked());
    }

    @Test
    @DisplayName("isLocked: παρελθοντικό lockedUntil → ξεκλείδωτος (αυτόματη λήξη)")
    void unlockedWhenLockExpired() {
        User user = new User();
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        assertFalse(user.isLocked());
    }

    @Test
    @DisplayName("isLocked: χωρίς lockedUntil → ξεκλείδωτος")
    void unlockedWhenNeverLocked() {
        User user = new User();
        user.setLockedUntil(null);
        assertFalse(user.isLocked());
    }
}
