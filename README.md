# Recipes

A small Quarkus HTTP server that serves a directory of Markdown recipes as a single-page web app. Drop `.txt` files into a folder, point the server at it, and browse the recipes from any device on the network.

## Features

- **Tag-based grouping** — recipes declare tags in a first-line HTML comment; the sidebar groups them into collapsible sections.
- **Search** — substring search across name, title and tags, plus exact tag lookup via `#tag`. Multiple terms are AND-combined; matching groups auto-expand.
- **Ingredient search** — a dialog (🥕🔍) ranks recipes by how many of the entered ingredients appear in their `## Zutaten` section. Matching ignores German endings (`tomate` finds "Tomaten") and finds compound words ending with the term (`zwiebel` finds "Lauchzwiebeln", but `tomate` not "Tomatenmark"); `hack*` / `*öl` / `*kartoffel*` are explicit wildcards. `#tag` terms list recipes with all these tags first and the others separately. The matched ingredients can be shown as chips (👁); the query is kept in `localStorage`.
- **Recipe images** — images from an `images` directory next to the recipes, referenced by metadata comments and shown in one row below the title. A click opens an image in an almost-fullscreen viewer that refits when a phone is rotated.
- **All recipes** — a toned-down group at the bottom of the sidebar lists every recipe alphabetically, independent of tags.
- **Random recipe** — a dialog (🎲) suggests a random recipe, optionally restricted to recipes having all selected tags and ingredients (same matching as the ingredient search). A click on the suggestion opens it.
- **Favorites** — star icon per recipe, stored in `localStorage` (never leaves the browser). Favorited recipes appear in a pinned group at the top.
- **Deep links** — the currently selected recipe is reflected in the URL (`#/recipe/<name>`), so individual recipes can be bookmarked and shared.
- **Mobile-friendly** — on narrow screens the sidebar turns into a slide-in drawer with a dim backdrop.
- **Help dialog** — `?` button in the sidebar header documents the file format.
- **Git sync** — optionally clones the recipes from a git repository and pulls periodically. A ⟳ button forces a pull (at most once per minute across all users); the sidebar shows when the recipes were last updated.
- **In-memory index** — all recipes are parsed and rendered once per sync, so requests never touch the file system.
- **Safe rendering** — raw HTML in recipe files is escaped, so recipes can't inject scripts.

## Recipe file format

Recipes are plain `.txt` files containing Markdown. The displayed name is the file's first `# Heading`; if there is none, the filename (without `.txt`) is used as a fallback.

Metadata goes into HTML comments at the **top of the file** (before any other line), so it isn't rendered. Both kinds are optional and can appear in any order:

- `<!--tags: #a #b-->` — tags separated by spaces; recipes without tags land in the group *Andere*.
- `<!--image: <path>-->` or `<!--image: <path> | <caption>-->` — one image per line; the optional text after the first `|` is shown below the image. The path is relative to the `images` directory inside `recipes.path` and may contain subdirectories (`pasta/teller.jpg`). Supported types: `jpg`, `jpeg`, `png`, `gif`, `webp`, `avif` (no SVG, since it can contain scripts).


```
<!--tags: #pasta #vegetarisch-->
<!--image: spaghetti.jpg | Mit frischer Petersilie-->
# Spaghetti Aglio e Olio

## Zutaten
| Menge | Zutat       |
|-------|-------------|
| 500 g | Spaghetti   |
| 6     | Knoblauchzehen |
...
```

Tables (for ingredient lists) and standard Markdown features are supported via Flexmark.

The images are served by `GET /images/<path>`. A recipes directory with images looks like this:

```
recipes/
  spaghetti.txt
  images/
    spaghetti.jpg
    pasta/
      teller.jpg
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `recipes.path` | – | Directory with the recipe files. With a git remote, the repository is cloned into it (must be empty or missing). |
| `recipes.git.remote` | – | Optional git URL. Without it, `recipes.path` is used as a plain directory. |
| `recipes.git.branch` | remote default | Branch to follow. |
| `recipes.git.username` / `recipes.git.token` | – | Optional HTTPS credentials (e.g. a personal access token) for private repositories. |
| `recipes.sync.interval` | `5m` | How often to pull and re-read the recipe files (also applies without git). |
| `recipes.sync.manual-cooldown` | `60s` | Minimum time between two manual pulls, shared across all users. |

The local checkout is treated as a read-only mirror: every sync fetches and hard-resets to the remote branch, discarding local changes. If a sync fails, the previously loaded recipes stay available.

For deployments the intended pattern is an external `config/application.properties` next to `quarkus-run.jar`:

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
- Run tests: `./gradlew test`
- Run the built jar: `java -jar build/quarkus-app/quarkus-run.jar` (run from `build/quarkus-app/` so the external `config/` is picked up)

Helper scripts in `scripts/` (`start-server.sh`, `stop-server.sh`) launch/stop the packaged jar on a Linux host.
