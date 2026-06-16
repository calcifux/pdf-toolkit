# 0006. Spring starter and static facade

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
The core is framework-agnostic and wireable by hand (`new PdfGenerator(renderer, engine)`), but the common consumer is a Spring Boot app that wants the toolkit to assemble itself from properties and expose a one-line call. It also needs every piece to be overridable, and it should mirror the sibling navajas so the ergonomics are familiar (auth-toolkit's `Auth`, mailable-toolkit's `Mail`).

## Decision
`pdf-toolkit-spring` is a Spring Boot starter centered on `PdfToolkitAutoConfiguration` (`@AutoConfiguration`, `@EnableConfigurationProperties(PdfToolkitProperties.class)`, driven by `pdf-toolkit.*`):

- **Conditional templating selection.** Three nested configs (`PebbleRendererConfig` `@Order(1)`, `ThymeleafRendererConfig` `@Order(2)`, `FreemarkerRendererConfig` `@Order(3)`), each `@ConditionalOnClass` its adapter and `@ConditionalOnProperty(... name = "engine", havingValue = ..., matchIfMissing = true)`. Pebble is the default; `pdf-toolkit.template.engine` picks when several adapters are present.
- **Default engine.** `openHtmlPdfEngine()` is `@ConditionalOnClass(OpenHtmlPdfEngine.class)` + `@ConditionalOnMissingBean(PdfEngine.class)`.
- **Orchestrator.** `pdfGenerator(...)` is `@ConditionalOnBean(PdfEngine.class)`, taking the `TemplateRenderer` via `ObjectProvider.getIfAvailable()` (templating stays optional) and all `PdfPostProcessor` beans via `orderedStream()`.
- **Static facade.** `PdfFacadeInitializer` pushes the wired `PdfGenerator` into the static `Pdf` facade (`Pdf.init(...)`), enabling `Pdf.generate(document)` / `Pdf.toFile(...)`.
- Every bean is `@ConditionalOnMissingBean`, so an app can override any piece — its own `TemplateRenderer`, `PdfEngine`, `PdfPostProcessor` or `PdfGenerator` wins and autoconfig steps aside.

## Consequences
- Zero-config for the common case: add the starter + an engine adapter (+ optionally one templating adapter) and call `Pdf.generate(...)`.
- Any component is replaceable without touching the toolkit, via `@ConditionalOnMissingBean`.
- Mirrors auth-toolkit (`Auth`) and mailable-toolkit (`Mail`), so the API feels consistent across the navajas.
- Trade-off: the static `Pdf` facade is process-global mutable state initialized at startup; it is convenient but means tests/multi-context setups should prefer injecting `PdfGenerator` directly.
- Trade-off: when multiple templating adapters are on the classpath, `matchIfMissing = true` with `@Order` makes Pebble the implicit winner — relying on that is fragile, so `pdf-toolkit.template.engine` should be set explicitly in that case.
