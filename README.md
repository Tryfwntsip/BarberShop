# BarberShop

Σύστημα διαχείρισης ραντεβού για κουρείο — desktop εφαρμογή σε **Java 17 / Swing** με αρχιτεκτονική **MVC** και βάση **MySQL**.

## Χαρακτηριστικά

- **Τρεις ρόλοι χρηστών** με ξεχωριστά dashboards:
  - **Πελάτης** — κράτηση ραντεβού, έλεγχος διαθεσιμότητας, πρόταση κουρέα βάσει στυλ, ακύρωση (έως 2 ώρες πριν).
  - **Κουρέας** — ημερήσιο/μηνιαίο πρόγραμμα, ενημέρωση κατάστασης ραντεβού, αναζήτηση ιστορικού πελατών.
  - **Ιδιοκτήτης** — διαχείριση υπηρεσιών & προσωπικού, στατιστικά και εξαγωγή αναφορών σε CSV.
- **Ασφάλεια:** BCrypt password hashing, κλείδωμα λογαριασμού μετά από 5 αποτυχημένες προσπάθειες, audit log.
- **Ακεραιότητα δεδομένων:** κρατήσεις μέσα σε transaction με έλεγχο επικάλυψης, σταθερότητα τιμής/διάρκειας τη στιγμή της κράτησης.
- **Σύγχρονο UI** με FlatLaf dark theme.

## Τεχνολογίες

Java 17 · Swing · FlatLaf · MySQL 8 (JDBC) · jBCrypt · Maven · JUnit 5

## Εκκίνηση

### 1. Βάση δεδομένων
```bash
mysql -u root -p < schema.sql      # δημιουργεί τη βάση kourio_db με demo δεδομένα
```

### 2. Στοιχεία σύνδεσης (δεν αποθηκεύονται στον κώδικα)
Όρισε τον κωδικό της MySQL μέσω μεταβλητής περιβάλλοντος:
```bash
export KOURIO_DB_PASSWORD="ο_κωδικός_σου"
# προαιρετικά: KOURIO_DB_URL, KOURIO_DB_USER
```

### 3. Build & Run
```bash
mvn compile
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "target/classes:$(cat cp.txt)" com.kourio.views.MainFrame
```

## Demo λογαριασμοί

Όλοι έχουν κωδικό `password123` (seed data):

| Ρόλος       | Username    |
|-------------|-------------|
| Ιδιοκτήτης  | `admin`     |
| Κουρέας     | `barber1`   |
| Πελάτης     | `customer1` |

## Δομή

```
src/main/java/com/kourio/
├── models/       # Domain entities (User, Service, Appointment, ...)
├── daos/         # JDBC data-access layer
├── views/        # Swing UI (login + dashboards, FlatLaf theming)
└── controllers/  # MVC controllers ανά ρόλο
```
