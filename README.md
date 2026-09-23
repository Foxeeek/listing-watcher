# listing-watcher

Watches OLX "Oddam za darmo" (free items) listings, filters them by rules and (later) sends alerts to Telegram.

**Stack:** Java 21, Maven, `java.net.http.HttpClient`, Jackson, JDBC + SQLite, JUnit 5. No Spring (yet).

## Roadmap
- [ ] Stage 0 — project skeleton, CI, search endpoint + fixtures, OLX terms check
- [ ] Stage 1 — collector: polling with backoff, parsing, dedup by listing id, SQLite storage
- [ ] Stage 2 — rule filters (category lists + regex), sealed `Decision`, drop reason stored
- [ ] Later — AI scoring, Telegram alerts, Spring Boot dashboard, second profile (cars)

## Principles
- Dedup by listing **id**, never by refresh time (bumped listings come back as "new").
- No personal data of listing owners (name, phone) is stored.
- Target server has 2 GB RAM — keep dependencies light.
