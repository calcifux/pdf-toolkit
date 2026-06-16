# 0004. Page layout via injected @page CSS

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
Documents need page size, orientation, margins, running headers/footers and page numbers. Each PDF engine exposes its own layout API for these, so calling engine-specific layout code would tie page configuration to one renderer and break the pluggable-engine seam (ADR-0003). pdf-utils solved this for one engine by injecting an `@page` rule; the toolkit needs that portable across any engine.

## Decision
Page layout is expressed as the typed `PageOptions` (size / orientation / `margin` shorthand / `customSize` / `headerText` / `footerText` / `pageNumbers`, with `PageOptions.defaults()` = A4, portrait, `20mm 15mm`). `PageCssInjector.inject(html, page, title)` compiles those options into a CSS `@page` rule (plus margin-box content for header/footer and the `counter`-based "page / pages") and injects it — together with the `<title>` — into the HTML `<head>` **before** the HTML reaches the engine.

`PdfGenerator` always runs this step (`previewHtml(...)` exposes the injected HTML for debugging), so by the time `PdfEngine.render(...)` is called the layout is already standard CSS, not an engine API call. This generalizes the trick pdf-utils used.

## Consequences
- Page layout is portable: it works identically on any `PdfEngine` that honors `@page`/margin-box CSS.
- Layout stays declarative and inspectable — `previewHtml(...)` shows exactly what the engine receives.
- No engine-specific layout code in the core; the engine seam stays clean.
- Trade-off: relies on the engine's CSS paged-media support; an engine with weak `@page` handling would render layout imperfectly (OpenHTMLtoPDF, the default, supports it).
- Trade-off: header/footer/page-number styling is what the injected CSS produces; richer running content means authoring it in the template/CSS directly.
