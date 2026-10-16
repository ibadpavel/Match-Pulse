# Рефактор загрузки эмблем команд

Контекст: вся логика эмблем сосредоточена в `app/src/main/java/com/example/ui/components/TeamBadge.kt`.
В доменной модели (`MatchEntity`) нет `teamId` / `crestUrl` — только строки `homeTeam` / `awayTeam`.
UI: `TeamBadge(match.homeTeam)` / `TeamBadge(match.awayTeam)` в `MainScreen.kt`.

---

## Как работает сейчас

```
сырое имя клуба
    → TeamNameNormalizer.clean()          # HTML unescape, lowercase, strip FC/FK…
    → L1 in-memory ConcurrentHashMap
    → L1 SharedPreferences("team_badge_cache")   # без TTL / версии
    → L1 getFastStaticBadgeUrl()                 # when { contains() → id }
         → https://crests.football-data.org/{id}.png
    → L2 resolveDynamicBadge()                   # GitHub tree + fuzzy firstOrNull
         → https://cdn.jsdelivr.net/gh/luukhopman/football-logos@master/…
    → Coil SubcomposeAsyncImage
    → fallback: инициалы + цвета по contains()
```

Ключ кэша и матчинга — **нормализованное имя**, не стабильный ID клуба.

---

## Список проблем

### P0 — явные баги (логотипы путаются)

| # | Проблема | Доказательство | Эффект |
|---|----------|----------------|--------|
| **P0.1** | **Дублирующиеся football-data ID** для разных клубов | В `getFastStaticBadgeUrl` разные ветки `when` указывают на один ID | Два клуба показывают **один и тот же** герб |
| | ID `1890` | Celje **и** Maccabi | |
| | ID `1889` | Hapoel Beer-Sheva **и** Levski Sofia | |
| | ID `1894` | Omonia **и** RFS | |
| | ID `1888` | Ludogorets **и** Sabah | |
| | ID `1886` | Qarabag **и** Ararat-Armenia | |
| **P0.2** | **«Байерн» → Leverkusen** | Правило `contains("байер")` для Bayer срабатывает раньше/вместо Bayern; `"байерн".contains("байер") == true` | Бавария с кириллическим написанием «Байерн» получает герб Байера |
| **P0.3** | Тесты не ловят коллизии | `TeamBadgeProviderTest` проверяет только `url != null` и `.png` | Общие ID и ложные матчи проходят CI |

### P1 — архитектура матчинга (высокая вероятность путаницы)

| # | Проблема | Детали |
|---|----------|--------|
| **P1.1** | Матчинг через упорядоченный `String.contains` | Первое совпадение побеждает; нет word-boundary / exact alias / приоритета по длине |
| **P1.2** | Слишком широкие токены | `inter` → Inter Milan; `sporting` → Sporting CP; `porto` → FC Porto; `lech` → Lech Poznań; `arsenal` → Arsenal London |
| **P1.3** | Нет модели Team | В Room только строки имён; crest нельзя сохранить рядом с матчем из API |
| **P1.4** | Динамический resolver = `firstOrNull` fuzzy | Короткий токен (`united`, `city`, `dinamo`) может попасть в чужой файл каталога |
| **P1.5** | Неверный URL кэшируется навсегда | Prefs читаются **до** статического каталога → исправление ID в коде не помогает без очистки данных приложения |

### P2 — техника загрузки / кэш / UI

| # | Проблема | Детали |
|---|----------|--------|
| **P2.1** | God-object `TeamBadge.kt` (~580 строк) | Normalizer + каталог + HTTP + prefs + Compose UI + цвета в одном файле |
| **P2.2** | Нет версионирования кэша | Нет `CACHE_VERSION`, нет invalidation API |
| **P2.3** | Нет custom ImageLoader | Coil без memory/disk key привязанных к team identity (сейчас ключ = URL; при коллизии URL это усиливает баг) |
| **P2.4** | Race на загрузку GitHub tree | Два параллельных `resolveDynamicBadge` могут оба сходить в API при `cachedOpenCatalogPaths == null` |
| **P2.5** | Фильтр слов `length >= 3` | Отбрасывает полезные короткие имена (AZ, NEC) в dynamic path |
| **P2.6** | Источники имён разнородны | ICS / HTML scrape / seed (`Atleti`, `B. Dortmund`, `H. Beer-Sheva`) — алиасы только в contains-списке |

### Не главная причина

Compose `LazyColumn` + `key = { it.id }` + `remember(teamName)` — классический ViewHolder-recycling **маловероятен** как корень путаницы. Симптом «чужой герб» здесь почти всегда = **одинаковый/неверный URL**, а не гонка Coil.

---

## Целевая архитектура (предложение)

Разделить ответственность и опереться на **стабильный ключ клуба**, а не на substring.

```
┌─────────────────────────────────────────────────────────┐
│  UI: TeamBadge(teamKey / displayName)                   │
│       Coil + initials fallback                          │
└──────────────────────────▲──────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────┐
│  TeamCrestRepository                                    │
│   resolve(displayName) → CrestRef(url, source, teamKey) │
│   cache: memory → disk (versioned) → catalog → network  │
└──────────────────────────▲──────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────┴──────┐  ┌────────┴────────┐  ┌──────┴───────┐
│ AliasCatalog │  │ StaticCrestMap  │  │ DynamicSeek  │
│ exact/alias  │  │ teamKey → URL   │  │ scored match │
│ map (JSON)   │  │ (unique IDs)    │  │ + min score  │
└──────────────┘  └─────────────────┘  └──────────────┘
```

### Принципы

1. **Уникальность**: один `teamKey` → один URL; тест `assertDistinctUrls` / `assertNoSharedIds`.
2. **Exact-first**: сначала точные алиасы (`"atleti" → atletico_madrid`), потом longest-token, потом scored fuzzy; **запретить** голый `contains("байер")` без границ.
3. **Версия кэша**: `team_badge_cache_vN`; при bump — игнор старых prefs.
4. **Статика важнее prefs**: при hit в static catalog не отдавать устаревший prefs (или писать prefs только для dynamic).
5. **Fallback честный**: лучше инициалы, чем чужой герб (dynamic только при score ≥ порога).
6. **(Опционально позже)** тянуть `crest` из Football-Data API в `MatchEntity` — тогда резолвер по имени становится запасным путём.

---

## План рефактора (по этапам)

### Этап 0 — зафиксировать баги тестами (без UI)

- Тест: пары клубов с общими ID сейчас дают **одинаковый** URL → после фикса URL должны различаться.
- Тест: `"Байерн"` / `"Бавария"` → Bayern (`…/5.png`), `"Байер"` / `"Leverkusen"` → Bayer (`…/3.png`).
- Тест: uniqueness — все клубы из seed/турнирного списка имеют **разные** URL (кроме явных дублей одной команды под алиасами).
- Тест: cache version — после bump старый prefs не перекрывает новый static.

### Этап 1 — hotfix P0 (минимальный diff)

- Вынести ID в `Map<String, Int>` / data-класс с **проверкой уникальности** при инициализации (`require(ids.distinct().size == ids.size)` или inverse map).
- Подобрать корректные ID / прямые jsDelivr URL для Celje, Maccabi, Levski, Beer-Sheva, Omonia, RFS, Ludogorets, Sabah, Qarabag, Ararat.
- Исправить кириллицу Bayern: `"байерн"|"бавария"|"bayern"` **до** правила Bayer; Bayer только `"байер"` + `"leverkusen"` **без** матча на `"байерн"` (word boundary / exact / negative lookahead).
- Bump `CACHE_VERSION` (или очистка ключей при старте), чтобы сбросить отравленный prefs.

### Этап 2 — каталог алиасов вместо `when { contains }`

- Файл `assets/team_crest_catalog.json` (или Kotlin `object TeamCrestCatalog`):
  ```json
  {
    "bayern_munich": {
      "crestId": 5,
      "aliases": ["bayern", "fc bayern", "бавария", "байерн", "bayern munchen"]
    }
  }
  ```
- Резолв: normalize → exact alias hit → optional token score → null.
- Цвета/инициалы читать из того же каталога (убрать второй `contains`-when).

### Этап 3 — пересмотреть L2 (dynamic)

- Scoring: Jaccard / число совпавших токенов / штраф за лишние слова в filename.
- Порог: ниже порога → `null` (fallback initials), **не** кэшировать промах.
- Кэшировать только URL с `source=DYNAMIC` и `confidence`.
- Mutex на загрузку GitHub tree; User-Agent уже есть.
- Рассмотреть отказ от GitHub API в рантайме (rate limit): бандл локального index JSON из репо логотипов при сборке.

### Этап 4 — разнести код по слоям

| Файл | Ответственность |
|------|-----------------|
| `TeamNameNormalizer.kt` | unescape + clean |
| `TeamCrestCatalog.kt` | aliases + IDs + colors |
| `TeamCrestRepository.kt` | cache + resolve + dynamic |
| `TeamBadge.kt` | только Compose UI |

### Этап 5 — (опционально) crest из API в данные матча

- При sync с Football-Data.org сохранять `homeCrestUrl` / `awayCrestUrl` (или team ids) в Room.
- `TeamBadge` сначала использует URL из матча; catalog — для ICS/seed без crest.

---

## Рекомендуемый порядок внедрения

1. **Этап 0 + 1** — сразу убирает видимую путаницу логотипов (основной pain).
2. **Этап 2** — убирает класс P1 substring-багов и делает каталог поддерживаемым.
3. **Этап 3–4** — устойчивость и читаемость.
4. **Этап 5** — если/когда live API уже отдаёт crest.

---

## Статус реализации (этапы 0–4)

| Этап | Статус | Что сделано |
|------|--------|-------------|
| 0 | ✅ | Тесты uniqueness, Bayern/Bayer, бывшие shared-ID пары, scoring |
| 1 | ✅ | Уникальные URL; jsDelivr для Maccabi/Beer-Sheva/Levski/Ludogorets/Lech; Sabah `10233`; cache `v2` |
| 2 | ✅ | `TeamCrestCatalog` — exact + longest alias с word-boundary |
| 3 | ✅ | Scored dynamic (`MIN_DYNAMIC_SCORE=0.72`), mutex на GitHub tree, prefs только для dynamic |
| 4 | ✅ | Слои: `TeamNameNormalizer` / `TeamCrestCatalog` / `TeamCrestRepository` / `TeamBadge` UI |
| 5 | ⏳ | Не в этом PR (crest из API в `MatchEntity`) |

## Критерии готовности

- [x] Нет двух разных клубов с одним crest URL в статическом каталоге.
- [x] `"Байерн"` ≠ Leverkusen; `"Байер"` = Leverkusen.
- [x] После обновления каталога старый SharedPreferences не отдаёт чужой герб (`team_badge_cache_v2`).
- [x] Unit-тесты на uniqueness, aliases, Bayern/Bayer, дубликаты бывших пар.
- [x] Dynamic path не кэширует слабые совпадения.
- [ ] UI без регрессий: карточка матча по-прежнему показывает бейдж / initials (проверяется сборкой APK + unit tests).

---

## Файлы для изменений

| Путь | Роль |
|------|------|
| `app/.../ui/components/TeamBadge.kt` | текущий монолит — разнести / починить |
| `app/src/test/.../TeamBadgeProviderTest.kt` | усилить assertions |
| `app/.../data/local/MatchEntity.kt` | только на этапе 5 |
| `app/.../ui/MainScreen.kt` | вызов `TeamBadge` — минимально |

---

## Вне скоупа этого рефактора

- Смена CDN / дизайн бейджа (форма, размер).
- Локальные drawable всех клубов в APK.
- Полный отказ от name-based resolve без API crest (этап 5).
