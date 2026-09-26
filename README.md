# listing-watcher

Watches OLX "Oddam za darmo" (free items) listings, filters them by rules and (later) sends alerts to Telegram.

**Stack:** Java 21, Maven, `java.net.http.HttpClient`, Jackson, JDBC + SQLite, JUnit 5. No Spring (yet).

## How it works

```
Poller ──► Source.fetchLatest()
             └─ OlxSource: OlxClient.search() ──► JSON ──► OlxResponseParser.parse() ──► List<Listing>
       ──► for each listing: ListingRepository.saveIfNew()   true = new, false = already seen (dedup)
       ──► Stage 2: Filter.apply() ──► Decision (pass / drop / score), drop reason stored in DB
       ──► Later:   Notifier (Telegram)
```

## Project structure

```
src/
├── main/
│   ├── java/com/listingwatcher/
│   │   ├── Application.java                 wiring only: creates objects and starts the poller   (stage 1)
│   │   ├── model/
│   │   │   └── Listing.java                 record — our listing, knows nothing about OLX        (stage 1)
│   │   ├── source/
│   │   │   └── Source.java                  interface: "give me the latest listings"             (stage 1)
│   │   ├── olx/                             everything that knows about OLX lives here
│   │   │   ├── OlxResponseParser.java       OLX JSON → List<Listing>                             (stage 1)
│   │   │   ├── OlxClient.java               HTTP POST to the GraphQL endpoint → JSON string      (stage 1)
│   │   │   └── OlxSource.java               implements Source = client + parser                  (stage 1)
│   │   ├── storage/                         everything that knows about SQL lives here
│   │   │   ├── ListingRepository.java       interface (DAO)                                      (stage 1)
│   │   │   └── SqliteListingRepository.java JDBC implementation                                  (stage 1)
│   │   ├── poll/
│   │   │   ├── Poller.java                  polling loop, every 2–5 min                          (stage 1)
│   │   │   └── Backoff.java                 how long to wait after a failure                     (stage 1)
│   │   ├── filter/
│   │   │   ├── Filter.java                  interface: Listing → Decision                        (stage 2)
│   │   │   └── Decision.java                sealed: Pass / Drop / Score                          (stage 2)
│   │   └── notify/
│   │       └── Notifier.java                alerts                                               (later)
│   └── resources/
│       └── listing-search.graphql           our trimmed GraphQL query (no owner data requested)
└── test/
    ├── java/com/listingwatcher/             one test class per production class: <ClassName>Test
    └── resources/fixtures/                  saved real OLX responses — tests never hit the network
```

### Key contracts

| Type | Main method |
|---|---|
| `OlxResponseParser` | `List<Listing> parse(String json)` |
| `OlxClient` | `String search(int offset, int limit)` |
| `Source` | `List<Listing> fetchLatest()` |
| `ListingRepository` | `boolean saveIfNew(Listing listing)` |
| `Filter` | `Decision apply(Listing listing)` |

### Design rules
1. `model` imports nothing from other project packages — everything depends on `Listing`, `Listing` depends on nothing.
2. Only `olx/` knows the OLX format; only `storage/` knows SQL. If OLX changes, one package changes.
3. Classes depend on interfaces, not implementations (`Poller` gets a `Source`, not an `OlxSource`) — so they can be tested with mocks.
4. Objects are created only in `Application.main`; everything else receives its dependencies through the constructor.
5. A class is created when a task needs it — no empty placeholders.

## Roadmap
- [x] Stage 0 — project skeleton, CI, search endpoint + fixtures, OLX terms check
- [ ] Stage 1 — collector: polling with backoff, parsing, dedup by listing id, SQLite storage
- [ ] Stage 2 — rule filters (category lists + regex), sealed `Decision`, drop reason stored
- [ ] Later — AI scoring, Telegram alerts, Spring Boot dashboard, second profile (cars)

## Principles
- Dedup by listing **id**, never by refresh time (bumped listings come back as "new").
- No personal data of listing owners (name, phone) is requested or stored.
- Target server has 2 GB RAM — keep dependencies light.
