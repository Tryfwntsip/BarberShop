# Contributing

Thank you for helping improve BarberShop.

## Development

Requirements:

- JDK 17 or newer
- Maven
- MySQL 8 for database-backed flows

Run the automated checks before opening a pull request:

```sh
mvn verify
```

Never commit real customer data, database dumps, credentials, `.env` files, generated reports, or academic assessment material. Demo accounts and seed data must be clearly fictional.

Pull requests should be focused, include tests for changed behavior, and update the README when setup or user-visible behavior changes.

## Security

Do not open a public issue for a suspected vulnerability or exposed credential. Follow [SECURITY.md](SECURITY.md).
