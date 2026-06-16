# 0002. Optional, pluggable templating

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
Apps disagree on templating: some standardize on Pebble, some already use Thymeleaf (pdf-utils did), some FreeMarker, and some have final HTML they just want rendered to PDF — for example a template string a user edited and stored in a DB. Hard-wiring one engine into the core would force a templating dependency on every consumer and pin them to one syntax.

## Decision
Templating is an **optional, engine-agnostic SPI**: `TemplateRenderer` in `pdf-toolkit-core`, with `render(templateRef, model, locale)` for named templates (inheritance/layouts resolved by the engine) plus an opt-in `renderInline(...)` / `supportsInline()` pair for raw source strings, and a stable `engineId()`.

- Each engine is its own adapter module — `pdf-toolkit-pebble` (the default), `pdf-toolkit-thymeleaf`, `pdf-toolkit-freemarker`. The core depends on none of them.
- `PdfSpec` carries the HTML source as **one of three** options, checked in order by `PdfGenerator.renderHtml(...)`: `html` (final HTML, used as-is), `inlineHtml` (raw source via the engine's inline mode), or `template` (logical name). A spec built with `html(...)` skips the renderer entirely, so the toolkit works with **no templating engine on the classpath** — `PdfGenerator` can be constructed with a `null` renderer.
- The `PdfDocument`/`PdfSpec`/`PdfGenerator` code never imports a templating engine; switching engines is a dependency swap plus the `pdf-toolkit.template.engine` property — document code never changes.

## Consequences
- Pure HTML-to-PDF use needs zero templating dependencies.
- Apps keep their preferred engine and its inheritance idioms (`{% extends %}`, Thymeleaf fragments, FreeMarker macros).
- DB-stored / inline templates are supported only where the engine reports `supportsInline()`; `PdfGenerator` throws a clear `PdfToolkitException` otherwise.
- Trade-off: template features beyond name-resolution + inline rendering vary per engine; the SPI intentionally exposes only the common surface, so engine-specific tricks live in the adapter.
