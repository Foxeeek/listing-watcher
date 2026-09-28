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

## Вікно 2 — 2026-09-28

### Стан
- **Тікет #2 ✅** — `OlxClient` + `Source`/`OlxSource` + `Application` злито в `main` через PR #2, CI зелений. Тести 6/6 (`OlxClientTest` 2, `OlxResponseParserTest` 4).
- `Application` наживо друкує кількість свіжих оголошень і перші 3 назви.

### Що додалось у коді
- `olx/OlxClient`:
  - `HttpClient` приходить через конструктор (один на програму, налаштовується в `Application`);
  - текст GraphQL-запиту читається один раз з classpath (`OlxClient.class.getResourceAsStream("/listing-search.graphql")`);
  - `buildRequestBody(offset, limit)` — тіло через Jackson (`searchParameters` = масив `{key, value}`, значення рядками);
  - `search(offset, limit)` — POST, таймаут 10 с, статус ≠ 200 → `IOException` зі статусом і початком тіла.
- `source/Source` — `List<Listing> fetchLatest() throws IOException, InterruptedException`.
- `olx/OlxSource` — `search(0, 40)` → `OlxResponseParser.parse`.
- `Application` — один `HttpClient` з `connectTimeout(5 с)` → `OlxClient` → `OlxSource` (змінна типу `Source`).

### Нові факти про OLX API
- Java `HttpClient` проходить без cookies і спецзаголовків.
- Кривий JSON у тілі → **HTTP 400** + пояснення в тілі (`FST_ERR_CTP_INVALID_JSON_BODY`).
- Невірна форма `searchParameters` → **HTTP 200 + `errors[]`** (без `data`) — третя форма помилки.
- ~40% відповіді — промо (`promotion.top_ad`: 22 з 52), розкидані по списку; сортування фактично за часом оновлення ⇒ позиція в списку нічого не гарантує, лише дедуп по `id`.

### Борг
- Парсер: коли у відповіді `errors[]` — додавати їхній `message` у текст винятку.
- Додати в запит `promotion { top_ad }`; на етапі 2 вирішити, чи відфільтровувати промо.
- Тест перевірки статусу в `OlxClient` з моком `HttpClient` (Mockito), без мережі.
- Дрібне з вікна 1 (назви `toListings`/`parsePhoto`, `List.copyOf` для фото, назви тестів).

### Наступне (вікно 3)
1. **Тікет 2b — пошук у своєму місті + радіус** на боці OLX: спершу знайти в DevTools, які ключі з'являються в `searchParameters` при виборі міста й радіуса; потім зробити місто/радіус налаштовуваними (приходять ззовні, не зашиті в `OlxClient`) + тест на тіло запиту.
2. **#3** SQLite + дедуп: `storage/ListingRepository.saveIfNew(Listing)`.
3. **#4** `poll/Poller` + `Backoff`, jar на сервер.
