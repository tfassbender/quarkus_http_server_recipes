# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A small Quarkus HTTP server that lists `.txt` markdown files from a configurable directory and renders them as HTML. The static `index.html` (under `src/main/resources/META-INF/resources/`) is the frontend; it calls the REST API to browse and view recipes.

## Build & Run

- Dev mode (hot reload, port 4711): `./gradlew quarkusDev`
- Build runnable app: `./gradlew build` — produces `build/quarkus-app/quarkus-run.jar`
- Run built jar: `java -jar build/quarkus-app/quarkus-run.jar` (run from `build/quarkus-app/` so the external `config/application.properties` is picked up)
- Deployment helper scripts: `scripts/start-server.sh`, `scripts/stop-server.sh` (expect a `quarkus-app/` directory next to them)

No tests exist yet; if added, run with `./gradlew test` (single test: `./gradlew test --tests "FQCN.method"`).

## Configuration

`recipes.path` (read via `@ConfigProperty` in `RecipesService`) points to the directory containing recipe `.txt` files. It is set in `src/main/resources/application.properties` for dev, but the intended deployment pattern overrides it via an external `config/application.properties` placed next to `quarkus-run.jar` — see the comment in that file.

## Architecture

Three small layers, all under `net.tfassbender`:

- `rest/RecipesResource` — JAX-RS endpoints at `/recipes` (list) and `/recipes/{filename}` (HTML-rendered content). The single-file GET returns `text/plain` containing rendered HTML (consumed by the frontend as a string).
- `service/RecipesService` — `@ApplicationScoped` CDI bean; lists/reads files under `recipes.path`. Filenames are exposed/accepted without the `.txt` extension (stripped on list, appended on read).
- `markdown/RecipesMarkdownFormatter` — Flexmark wrapper with the Tables extension; instantiated per request in the resource.

Recipe files are expected to be `.txt` containing markdown.
