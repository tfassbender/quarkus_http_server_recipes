# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A small Quarkus HTTP server that lists `.txt` markdown files from a configurable directory and renders them as HTML. The static `index.html` (under `src/main/resources/META-INF/resources/`) is the frontend; it calls the REST API to browse and view recipes.

## Build & Run

- Dev mode (hot reload, port 4711): `./gradlew quarkusDev`
- Build runnable app: `./gradlew build` — produces `build/quarkus-app/quarkus-run.jar`
- Run built jar: `java -jar build/quarkus-app/quarkus-run.jar` (run from `build/quarkus-app/` so the external `config/application.properties` is picked up)
- Deployment helper scripts: `scripts/start-server.sh`, `scripts/stop-server.sh` (expect a `quarkus-app/` directory next to them)

Tests: `./gradlew test` (single test: `./gradlew test --tests "FQCN.method"`). `@QuarkusTest`s use the `%test` profile, which reads fixtures from `src/test/resources/recipes` with git disabled.

## Configuration

All keys live under the `recipes.*` prefix and are mapped by `config/RecipesConfig` (`@ConfigMapping`): `recipes.path`, optional `recipes.git.{remote,branch,username,token}`, and `recipes.sync.{interval,manual-cooldown}`. See the README for details. The dev value is in `src/main/resources/application.properties`; the deployment pattern overrides it via an external `config/application.properties` placed next to `quarkus-run.jar`.

## Architecture

Layers under `net.tfassbender`:

- `rest/RecipesResource` — `/recipes` (list) and `/recipes/{name}` (rendered HTML, returned as `text/plain` because the frontend inserts it as a string).
- `rest/SyncResource` — `GET /sync` (status: `gitEnabled`, `lastSuccess`, `lastError`), `POST /sync` (manual pull, globally rate limited → `429` with `Retry-After`).
- `rest/ExceptionMappers` — maps domain exceptions to status codes (`RecipeNotFoundException` → 404, `PullCooldownException` → 429, `SyncFailedException` → 500).
- `sync/SyncService` — syncs on startup, via `@Scheduled`, and on manual request; serialized by a lock. A sync = `GitRepositoryClient.update()` (clone-if-missing, else fetch + hard reset + clean) followed by `RecipesService.rebuildIndex()`. On failure the old index is kept and the error recorded in `SyncStatus`.
- `sync/PullCooldown` — lock-free global cooldown (`AtomicReference<Instant>`, injectable `Clock` for tests).
- `service/RecipesService` — holds an immutable `RecipeIndex` (summaries + pre-rendered HTML) behind a `volatile` field, swapped atomically on rebuild. Lookups go by name through the index only, so there is no file-system access per request. Filenames are exposed without the `.txt` extension.
- `service/RecipeParser` — tag line (`<!-- tags: #a #b -->` on line 1) and title (first `# ` heading) extraction.
- `markdown/RecipesMarkdownFormatter` — Flexmark with the Tables extension and `ESCAPE_HTML`, so raw HTML in recipes is rendered as text.

Recipe files are expected to be `.txt` containing markdown.
