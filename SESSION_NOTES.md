# Session notes

Робочий журнал між вікнами роботи: що зроблено, що далі, щоб наступна сесія стартувала без переказу.

## Вікно 1 — 2026-09-23 → 2026-09-27

### Стан
- **Етап 0 ✅** — каркас (Maven, Java 21, JUnit 5, Mockito), GitHub Actions CI, README зі структурою й правилами дизайну, фікстури.
- **Тікет #1 ✅** — `OlxResponseParser` (Jackson Tree Model) злито в `main` через PR #1, CI зелений, 4 тести.

### Що є в коді
- `model/` — record-и `Listing`, `Category`, `Location`, `Photos` (про OLX нічого не знають).
- `olx/OlxResponseParser` — `List<Listing> parse(String json)`:
  - `__typename != ListingSuccess` → `IllegalArgumentException` зі значенням typename;
  - немає масиву `data` → виняток (не тихий порожній список);
  - обов'язкові поля (`id`, `title`, `url`, `created_time`, `last_refresh_time`, `category`, `location`, `photos`) → `requireField`, відсутнє/`null` → виняток з назвою поля;
  - `description` — необов'язкове (`path(...).asText("")`).
- `src/test/resources/fixtures/` — `search-page1.json`, `search-page2.json` (живі відповіді 23.09, по 26 оголошень), `search-error-bad-limit.json` (`ListingError`), `listing-without-id.json`.
- `src/main/resources/listing-search.graphql` — наш урізаний запит (без `user`/`contact` → жодних персональних даних).

### Факти про OLX API (перевірено)
- `POST https://www.olx.pl/apigateway/graphql`, тіло `{query, variables.searchParameters}`:
  `offset`, `limit`, `category_id=1151`, `sort_by=created_at:desc`, `filter_enum_price=free`. Cookies не потрібні.
- Помилка приходить як **HTTP 200** + `__typename: ListingError` → перевіряти тіло, не лише статус.
- `limit=20` → приходить 26 (домішуються промо); сторінки перетинаються → дедуп по `id` навіть в одному проході.
- «Найновіші» = за часом підняття: піднята стара річ стоїть першою ⇒ **новизна тільки по `id`**, не по `last_refresh_time`.
- «Oddam za darmo» — фільтр ціни поверх звичайних категорій (id категорій — у фасетах відповіді).

### Відомий дрібний борг (не блокує)
- `toListings` → `toListing` (повертає одне), `parsePhoto` → `parsePhotos`, параметр `photoNode` → `photosNode`.
- `Photos`: `List.copyOf(...)`, щоб список не змінювався ззовні.
- Назви тестів — за поведінкою (`parse_listingWithoutId_throws...`), осмислені `@DisplayName`.
- `photos` зараз обов'язкове — вирішити, чи так і лишити.

### Наступне (вікно 2)
1. **Розминка HttpClient ✅ (27.09)** (`Main.java`, локальний, у git не йде). Висновки для `OlxClient`:
   - `403` → **винятку немає**, просто `statusCode() == 403` → статус перевіряти самому, не-200 → власний виняток зі статусом;
   - таймаут → `HttpTimeoutException` (це `IOException`);
   - `Duration.ofSeconds(0)` у `.timeout()` → `IllegalArgumentException` (таймаут має бути > 0);
   - **розподіл відповідальності:** `OlxClient` падає гучно (не ковтає помилки, інакше — «тихий нуль»), а `Poller` (#4) ловить, логує, чекає (backoff) і пробує наступного циклу.
2. **Тікет #2** (гілка `ticket-2-client`):
   - `olx/OlxClient.search(offset, limit)` — запит з classpath, тіло через Jackson (окремий тестований метод), POST, таймаут, статус ≠ 200 → виняток;
   - `source/Source.fetchLatest()` + `olx/OlxSource`;
   - `Application` друкує живі оголошення;
   - `OlxClientTest` — тіло запиту без мережі.
3. **#3** SQLite + дедуп: `storage/ListingRepository.saveIfNew(Listing)`.
4. **#4** `poll/Poller` кожні 2–5 хв + `Backoff`, jar на сервер (2 GB RAM).

### Робоче оточення
- Remote називається **`Foxeek`**, не `origin`: `git push -u Foxeek <branch>`.
- `mvn` локально не в PATH — тести через IntelliJ; перевірка «по-справжньому» = зелений CI.
- `src/main/java/Main.java` і `fixtures/test.json` — чернетки розминок, виключені локально через `.git/info/exclude`.
