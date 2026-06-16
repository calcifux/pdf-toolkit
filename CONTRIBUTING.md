# Contributing to pdf-toolkit

Thanks for your interest in improving pdf-toolkit! Contributions of all sizes are
welcome — bug reports, docs, tests, new provider adapters.

## Ground rules

- Be respectful — see the [Code of Conduct](CODE_OF_CONDUCT.md).
- Open an **issue** to discuss non-trivial changes before sending a PR.
- Keep the build green: `mvn verify` must pass (it runs the unit tests).
- Match the existing style: full words over abbreviations, focused classes, Javadoc on
  public types, and comments that explain the *why*.

## Development setup

```bash
git clone https://github.com/calcifux/pdf-toolkit.git
cd pdf-toolkit
mvn verify          # build + run tests (JDK 21 required)
```

The project is a Maven reactor:

- `pdf-toolkit-core` — framework-agnostic model (`PdfDocument`/`PdfSpec`), page-layout → `@page` CSS,
  the `TemplateRenderer` / `PdfEngine` / `PdfPostProcessor` SPIs, and the `PdfGenerator` orchestrator.
  Keep it free of Spring AND of any PDF library (only slf4j).
- `pdf-toolkit-pebble` / `-thymeleaf` / `-freemarker` — one `TemplateRenderer` each (templating is optional).
- `pdf-toolkit-openhtmltopdf` — the default `PdfEngine` (OpenHTMLtoPDF / PDFBox).
- `pdf-toolkit-spring` — Spring Boot starter (auto-config, `pdf-toolkit.*` properties, engine selection,
  the static `Pdf` facade).

## Design rules to preserve

- **Two orthogonal ports.** Templating (`TemplateRenderer`) and HTML-to-PDF (`PdfEngine`) are independent
  SPIs — a new templating engine or a new PDF engine is a new implementation, never an `if/else` inside
  an existing one. The core imports neither a templating library nor a PDF library.
- **Templating is optional.** A `PdfSpec` carrying final `html` skips the renderer entirely; the toolkit
  must work with no templating engine on the classpath.
- **Generation, not document processing.** The core turns HTML into a PDF. Encrypt / watermark / merge /
  sign are `PdfPostProcessor`s contributed by the app (or an adapter), never baked into the core.

## Adding a templating engine

1. New module `pdf-toolkit-<engine>` with a `TemplateRenderer` implementation (support template
   inheritance + an inline mode where the engine allows).
2. Light it up in `PdfToolkitAutoConfiguration` behind `@ConditionalOnClass(<engine>)` +
   `@ConditionalOnProperty(pdf-toolkit.template.engine=<id>, matchIfMissing=true)`.
3. Cover it with a renderer test (inheritance + HTML-escaping).

## Adding a PDF engine or post-processor

1. Implement `PdfEngine` (a new HTML-to-PDF backend) or `PdfPostProcessor` (encrypt/watermark/merge) in
   an adapter module or your app.
2. For an engine, register it in `PdfToolkitAutoConfiguration` behind `@ConditionalOnClass` +
   `@ConditionalOnMissingBean(PdfEngine.class)`; a post-processor is just a bean the generator collects.
3. Cover it with a test (a `PdfEngine` test should assert the `%PDF-` magic on real output).

## Pull requests

- Branch from `main`, keep PRs focused, and describe the change and motivation.
- Add/adjust tests for behavior changes.
- The CI workflow runs `mvn verify` on every PR.

By contributing you agree that your contributions are licensed under the [MIT License](LICENSE).
