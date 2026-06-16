# Changelog

All notable changes to this project are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0]

First release — HTML-to-PDF for Java, agnostic of both the templating engine and the PDF engine.

### Added

- **`pdf-toolkit-core`** — framework-agnostic, no PDF library, only slf4j:
  - `PdfDocument` (abstract) → `PdfSpec` model: an HTML source that is one of `template` (named, with
    inheritance), `inlineHtml` (a raw template string, e.g. from a DB) or `html` (final, no templating
    needed); plus model vars, locale, title, base URI and `PageOptions`.
  - `PageOptions` → compiled to an `@page` CSS rule (size, orientation, margins, running header/footer
    text, page numbers) injected before the engine, generalizing pdf-utils' page-CSS trick.
  - `TemplateRenderer` SPI — OPTIONAL, pluggable templating with inheritance + inline mode.
  - `PdfEngine` SPI — pluggable HTML-to-PDF.
  - `PdfPostProcessor` SPI — the seam for encrypt / watermark / merge / sign (kept OUT of the core).
  - `PdfGenerator` — resolves the HTML, injects page CSS + title, runs the engine, then the
    post-processor chain; `generate(...)` to bytes and `generateToFile(...)`, plus `previewHtml(...)`.
- **`pdf-toolkit-pebble`** (default), **`-thymeleaf`**, **`-freemarker`** — `TemplateRenderer` adapters,
  each with template inheritance + inline mode + HTML auto-escaping.
- **`pdf-toolkit-openhtmltopdf`** — the default `PdfEngine` (OpenHTMLtoPDF / PDFBox), the engine pdf-utils
  used; resolves relative assets against the spec's base URI, honors CSS paged-media.
- **`pdf-toolkit-spring`** — Spring Boot starter: `pdf-toolkit.*` properties, templating-engine selection
  (auto when one is present), the OpenHTMLtoPDF engine, `PdfPostProcessor` collection, the `PdfGenerator`,
  and the static **`Pdf`** facade (`Pdf.generate(...)`, `Pdf.toFile(...)`, `Pdf.previewHtml(...)`).

[Unreleased]: https://github.com/calcifux/pdf-toolkit/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/calcifux/pdf-toolkit/releases/tag/v0.1.0
