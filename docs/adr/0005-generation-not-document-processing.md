# 0005. Generation, not document processing

- **Status:** Accepted
- **Date:** 2026-06-16

## Context
Beyond turning HTML into a PDF, apps often want to encrypt/password-protect, watermark, stamp, sign, or merge a cover page. These are real needs, but they are PDF *document* concerns rather than *generation*, and the libraries that do them (PDFBox, iText) are heavy and licensing-sensitive. Pulling them into the core would bloat every consumer and force one library/version on everyone.

## Decision
The toolkit's job ends at **generating** PDF bytes. Post-generation steps are an optional SPI, `PdfPostProcessor` — a `@FunctionalInterface` `byte[] process(byte[] pdf)`. The app (or an adapter module) contributes one or more processors; `PdfGenerator` runs them as an **ordered chain after the engine** (`for (PdfPostProcessor p : postProcessors) pdf = p.process(pdf)`), guarding against a `null` return with a `PdfToolkitException`.

So encrypt/watermark/merge/sign live in the consumer's code (e.g. a PDFBox `StandardProtectionPolicy` bean), and the core never pulls a heavy PDF library to do them. This is the **same boundary as mailable-toolkit**: a toolkit carries the bytes; processing documents is the app's job.

## Consequences
- The core stays free of PDFBox/iText for document manipulation — only the chosen `PdfEngine` adapter brings a PDF library, and only for rendering.
- Apps choose their processing library, version and licensing terms, and compose multiple processors in a defined order.
- Post-processing is fully optional: with no `PdfPostProcessor` beans the chain is empty and generation returns the engine's bytes directly.
- Trade-off: there is no built-in encrypt/watermark/merge — common needs require the app to write (or copy from `examples/`) a small processor.
- Trade-off: ordering is the registration order (Spring bean order); processors that must run in a specific sequence depend on that being set deliberately.
