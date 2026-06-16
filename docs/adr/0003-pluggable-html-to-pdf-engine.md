# 0003. Pluggable HTML-to-PDF engine

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
The HTML-to-PDF step is the heaviest dependency in the stack (OpenHTMLtoPDF pulls in PDFBox). Renderers differ in CSS support, font handling, licensing and strictness, and a project may need to swap one for another (e.g. for an alternative library) without rewriting its documents. Baking a specific renderer into the core would force that library on every consumer and freeze the choice.

## Decision
The HTML-to-PDF step is a **pluggable SPI**: `PdfEngine` in `pdf-toolkit-core`, with `render(html, PdfRenderOptions)` returning PDF bytes and a stable `engineId()`. The HTML handed to the engine is already complete — page CSS and title are injected upstream by the orchestrator (see ADR-0004) — so the engine just rasterizes.

- `pdf-toolkit-openhtmltopdf` provides the default adapter, `OpenHtmlPdfEngine` (OpenHTMLtoPDF / PDFBox backend).
- The core imports **no PDF library**; `openhtmltopdf-*` lives only in the adapter module.
- Swapping engines is a dependency swap (drop in a different `PdfEngine` adapter) plus a bean/property — `PdfGenerator` requires a non-null `PdfEngine` and throws a `PdfToolkitException` directing the user to add an engine adapter if none is present. `PdfDocument` code never changes.

## Consequences
- The core stays light and free of any PDF library; consumers choose the renderer (and its transitive weight) by which adapter they add.
- A new engine is a single SPI implementation; documents are unaffected.
- Trade-off: `PdfRenderOptions` (base URI, fast mode) is the common contract — engine-specific tuning beyond it must be configured inside the adapter.
- Trade-off: OpenHTMLtoPDF's strict XHTML parser is exposed to authors (well-formed markup, absolute/`data:` asset URIs); that constraint follows the chosen default engine.
