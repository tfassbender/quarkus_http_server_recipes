# Improvement Plan

Ordered roughly by dependency / value. Each item is small enough to ship on its own.

## 1. Tags (first, drives later UI work) — DONE

- **File format:** first line of each `.txt` is an HTML comment so markdown ignores it if anything slips through:
  `<!--tags: #tag1 #tag2-->`
- **Parser:** new `RecipeMetadata` record `(List<String> tags, String body)`. Strip the first line if it matches the tag-comment pattern; otherwise treat the whole file as body with empty tags.
- **Service:** `listRecipeFiles()` returns `List<RecipeSummary(name, tags)>` instead of `List<String>`. `readRecipeFile` returns only the markdown body (tags stripped before Flexmark sees it).
- **REST:** `/recipes` returns the new structure. `/recipes/{filename}` unchanged in shape.
- **UI:** group sidebar list by tag (collapsible sections). Untagged recipes go under "Other".

## 2. Filename path sanitization (bundle with #1) — DONE

- In `RecipesService.readRecipeFile`, reject `filename` containing `/`, `\`, or `..`, or resolve and verify the path stays under `recipes.path`. Cheap, prevents traversal.

## 3. Server-side caching of rendered HTML — DONE

- Single shared `RecipesMarkdownFormatter` (move to `@ApplicationScoped` or static), reuse parser/renderer.
- Cache rendered HTML keyed by filename; invalidate when `Files.getLastModifiedTime` changes. `ConcurrentHashMap<String, CacheEntry(mtime, html)>` is enough — no eviction needed for this scale.

## 4. Deep-linkable URLs — DONE

- Use `location.hash` (`#/recipe/<name>`) so no server routing changes are needed.
- On load: read hash, select+fetch that recipe. On selection: update hash. Listen for `hashchange` for back/forward.

## 5. Favorites in localStorage — DONE

- Star icon next to each recipe in the sidebar; toggling persists `recipes:favorites` (JSON array of names) in `localStorage`.
- Render a "Favorites" section at the top of the sidebar.
- **Explicitly client-only** — server never sees this.

## 6. Search improvement

- Replace per-character substring filter with: split query on whitespace, each token must match either the name (substring, case-insensitive) or a tag (exact, with or without leading `#`).
- Keep the input responsive but debounce to ~100ms once tag-grouping is in place.

## 7. Help dialog

- Small `?` button in the sidebar header opens a modal explaining the recipe file format: where to put the tag line, the exact `<!--tags: #tag1 #tag2-->` syntax, and which markdown features are supported (tables in particular).
- Pure static content in `index.html`; no backend.

## 8. Mobile + print CSS (do together)

- Mobile: collapse sidebar into a hamburger/drawer below ~640px so content gets full width.
- Print: `@media print` hides sidebar, switches to light background, enlarges body font, `page-break-inside: avoid` on ingredient tables.

## Out of scope (for now)

- Servings scaler
- Light/dark toggle
- Image/asset support
