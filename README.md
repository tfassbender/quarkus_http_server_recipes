# Recipes

A small Quarkus HTTP server that serves a directory of Markdown recipes as a single-page web app. Drop `.txt` files into a folder, point the server at it, and browse the recipes from any device on the network.

## Features

- **Tag-based grouping** — recipes declare tags in a first-line HTML comment; the sidebar groups them into collapsible sections.
- **Search** — substring search across name, title and tags, plus exact tag lookup via `#tag`. Multiple terms are AND-combined; matching groups auto-expand.
- **Favorites** — star icon per recipe, stored in `localStorage` (never leaves the browser). Favorited recipes appear in a pinned group at the top.
- **Deep links** — the currently selected recipe is reflected in the URL (`#/recipe/<name>`), so individual recipes can be bookmarked and shared.
- **Mobile-friendly** — on narrow screens the sidebar turns into a slide-in drawer with a dim backdrop.
- **Help dialog** — `?` button in the sidebar header documents the file format.
- **Server-side render cache** — rendered HTML is cached per file and invalidated when the file's modification time changes.

## Recipe file format

Recipes are plain `.txt` files containing Markdown. The displayed name is the file's first `# Heading`; if there is none, the filename (without `.txt`) is used as a fallback.

Tags are optional and go on the **first line** as an HTML comment so they aren't rendered:

```
<!--tags: #pasta #vegetarisch-->
# Spaghetti Aglio e Olio

## Zutaten
| Menge | Zutat       |
|-------|-------------|
| 500 g | Spaghetti   |
| 6     | Knoblauchzehen |
...
```

Tables (for ingredient lists) and standard Markdown features are supported via Flexmark.

## Configuration

`recipes.path` (in `application.properties` or via Quarkus config) points to the directory containing the recipe files. For deployments the intended pattern is an external `config/application.properties` next to `quarkus-run.jar`:

```
build/quarkus-app/
  quarkus-run.jar
  config/
    application.properties   # overrides recipes.path
```

The server listens on port `4711` by default.

## Build & run

- Dev mode (hot reload): `./gradlew quarkusDev`
- Build a runnable app: `./gradlew build` (produces `build/quarkus-app/quarkus-run.jar`)
- Run the built jar: `java -jar build/quarkus-app/quarkus-run.jar` (run from `build/quarkus-app/` so the external `config/` is picked up)

Helper scripts in `scripts/` (`start-server.sh`, `stop-server.sh`) launch/stop the packaged jar on a Linux host.
