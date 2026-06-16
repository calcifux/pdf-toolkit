# pdf-toolkit

[![CI](https://github.com/calcifux/pdf-toolkit/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/calcifux/pdf-toolkit/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/calcifux/pdf-toolkit.svg)](https://jitpack.io/#calcifux/pdf-toolkit)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)](https://spring.io/projects/spring-boot)

**HTML-to-PDF for Java.** Write a typed `PdfDocument` — a template + vars, page size/margins, headers,
footers, page numbers — and get bytes:

```java
byte[] pdf = Pdf.generate(new InvoicePdf(invoice));
```

Templating is **optional and pluggable** behind one SPI (Pebble, Thymeleaf, FreeMarker — or skip it and
feed final HTML), and the **HTML-to-PDF engine is pluggable** behind another SPI (OpenHTMLtoPDF by
default). Encrypt / watermark / merge are a `PdfPostProcessor` seam in your app, not baked in. It mounts
as a Spring Boot starter, but the core has no Spring and no PDF library on it.

## Modules

| Module | What it is |
| --- | --- |
| `pdf-toolkit-core` | Pure Java (slf4j only). The `PdfDocument`/`PdfSpec` model, page-layout → `@page` CSS, the `TemplateRenderer` / `PdfEngine` / `PdfPostProcessor` SPIs, and the `PdfGenerator`. No Spring, no PDF library. |
| `pdf-toolkit-pebble` | `TemplateRenderer` over Pebble (default engine — `{% extends %}`/`{% block %}` inheritance). |
| `pdf-toolkit-thymeleaf` | `TemplateRenderer` over Thymeleaf (what pdf-utils used; fragment inheritance). |
| `pdf-toolkit-freemarker` | `TemplateRenderer` over FreeMarker (macro/include inheritance, HTML auto-escape). |
| `pdf-toolkit-openhtmltopdf` | The default `PdfEngine` (OpenHTMLtoPDF / PDFBox). |
| `pdf-toolkit-spring` | Spring Boot starter: auto-config, `pdf-toolkit.*` properties, engine selection, the static `Pdf` facade. |

## Install (JitPack)

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<!-- the starter -->
<dependency>
  <groupId>com.github.calcifux.pdf-toolkit</groupId>
  <artifactId>pdf-toolkit-spring</artifactId>
  <version>v0.1.0</version>
</dependency>

<!-- the default PDF engine -->
<dependency>
  <groupId>com.github.calcifux.pdf-toolkit</groupId>
  <artifactId>pdf-toolkit-openhtmltopdf</artifactId>
  <version>v0.1.0</version>
</dependency>

<!-- optional: pick ONE templating engine (skip if you feed final HTML) -->
<dependency>
  <groupId>com.github.calcifux.pdf-toolkit</groupId>
  <artifactId>pdf-toolkit-pebble</artifactId>
  <version>v0.1.0</version>
</dependency>
```

---

# For the jr — write a document and generate it

### 1. Configure once (`application.yml`)

```yaml
pdf-toolkit:
  template:
    engine: pebble          # pebble | thymeleaf | freemarker (auto-picked if exactly one on the classpath)
    prefix: "templates/"    # templates live in src/main/resources/templates/
```

(If you only ever feed final HTML, you don't even need a templating engine or this config.)

### 2. Write a `PdfDocument`

```java
public class InvoicePdf extends PdfDocument {

    private final Invoice invoice;

    public InvoicePdf(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    public String filename() {
        return "invoice-" + invoice.folio() + ".pdf";
    }

    @Override
    public PdfSpec build() {
        return PdfSpec.builder()
                .template("invoice")                  // templates/invoice.peb
                .with("invoice", invoice)
                .title("Invoice " + invoice.folio())
                .page(PageOptions.builder()
                        .size(PageSize.A4)
                        .margin("20mm 15mm")
                        .pageNumbers(true)
                        .build())
                .build();
    }
}
```

### 3. A template (Pebble, with inheritance)

`src/main/resources/templates/layout.peb` — the shared shell:

```html
<html>
  <head><style>body { font-family: sans-serif; }</style></head>
  <body>{% block content %}{% endblock %}</body>
</html>
```

`src/main/resources/templates/invoice.peb`:

```html
{% extends "templates/layout" %}
{% block content %}
  <h1>Invoice {{ invoice.folio }}</h1>
  <p>Total: {{ invoice.total }}</p>
{% endblock %}
```

> The HTML must be **well-formed XHTML** (OpenHTMLtoPDF's parser is strict). Use absolute or `data:` URIs
> for images, or set a base URI (below).

### 4. Generate it

```java
byte[] pdf = Pdf.generate(new InvoicePdf(invoice));            // bytes
Pdf.toFile(new InvoicePdf(invoice), Path.of("/tmp/inv.pdf"));  // straight to disk
```

### Page layout

```java
PageOptions.builder()
    .size(PageSize.LETTER)                 // A3 / A4 / A5 / LETTER / LEGAL
    .orientation(Orientation.LANDSCAPE)    // PORTRAIT | LANDSCAPE
    .margin("15mm")
    .headerText("Acme Corp")               // centered in the top margin
    .footerText("Confidential")            // bottom-left
    .pageNumbers(true)                     // "1 / 10" bottom-right
    .customSize("210mm 297mm")             // overrides size, for anything non-standard
    .build();
```

These compile to an `@page` CSS rule the toolkit injects into your HTML's `<head>` before the engine runs.

### A template string stored in a DB

Users edit a template in an admin, it lives in your DB, you render it as-is:

```java
String source = templateRepo.activeFor(accountType);    // the edited string
PdfSpec.builder().inlineHtml(source).with("total", total).title("Statement").build();
```

### No templating at all

Already have final HTML? Skip the engine entirely (you don't even need a templating adapter):

```java
byte[] pdf = Pdf.generate(PdfSpec.builder().html(finalHtml).build());
```

---

# For the architect — how it's wired

Two **orthogonal** SPIs, the core importing neither a templating library nor a PDF library:

```
 PdfDocument.build()
        │
        ▼                ┌──────────── TemplateRenderer (SPI, OPTIONAL) ───────────┐
   PdfSpec ──────────────│  pebble · thymeleaf · freemarker   (inheritance/inline) │
   (template|inline|html)└──────────────────────────────────────────────────────────┘
        │ html (or used as-is)
        ▼
   PageCssInjector  ── injects @page CSS (size/margin/header/footer/page-#) + <title>
        │
        ▼                ┌──────────────── PdfEngine (SPI) ─────────────┐
   complete HTML ───────►│ OpenHtmlPdfEngine (OpenHTMLtoPDF / PDFBox)   │ ──► PDF bytes
        │                └───────────────────────────────────────────────┘
        ▼
   PdfPostProcessor chain ── encrypt · watermark · merge · sign  (your app's beans)
        │
        ▼
     byte[]
```

- **Templating is optional.** `PdfSpec.html(finalHtml)` bypasses the renderer; the toolkit runs with no
  templating engine on the classpath.
- **Generation, not document processing.** The toolkit turns HTML into a PDF. Password-protection,
  watermarking, merging, signing are `PdfPostProcessor` beans you contribute — the core never pulls a
  heavy PDF library to do them. (Same boundary as mailable-toolkit: a toolkit carries bytes; processing
  documents is the app's job.)
- **Everything is `@ConditionalOnMissingBean`.** Define your own `TemplateRenderer`, `PdfEngine`,
  `PdfPostProcessor` or `PdfGenerator` and the autoconfig steps aside.

### A post-processor (e.g. password-protect with PDFBox)

```java
@Bean
PdfPostProcessor encrypt(/* your config */) {
    return pdf -> {
        try (PDDocument doc = PDDocument.load(pdf)) {                 // PDFBox, in YOUR app
            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy policy = new StandardProtectionPolicy(ownerPwd, userPwd, ap);
            doc.protect(policy);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) { throw new RuntimeException(e); }
    };
}
```

The `PdfGenerator` runs registered post-processors in order, after the engine.

### Without Spring

The core is standalone — wire it by hand in a CLI or a job:

```java
PdfEngine engine = new OpenHtmlPdfEngine();
TemplateRenderer renderer = new PebbleTemplateRenderer("templates/", ".peb");
PdfGenerator generator = new PdfGenerator(renderer, engine);

byte[] pdf = generator.generate(new InvoicePdf(invoice));
// or, with no templating at all:
byte[] adhoc = new PdfGenerator(engine).generate(PdfSpec.builder().html(finalHtml).build());
```

### A different PDF engine

Implement `PdfEngine` (e.g. over a different library) and either register it as a bean
(`@ConditionalOnMissingBean(PdfEngine.class)` lets yours win) or pass it to `PdfGenerator` directly. Your
`PdfDocument` code never changes.

## Examples

See [`examples/`](examples/) for `InvoicePdf` (templated), `ReportPdf` (landscape + headers + page
numbers), a DB-stored-template document, a final-HTML document (no engine), and an encryption
`PdfPostProcessor`.

## Build

```bash
mvn verify              # build + tests
mvn -DskipTests install
```

Requires JDK 21.

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md) and the
[Code of Conduct](CODE_OF_CONDUCT.md). In short: open an issue to discuss non-trivial changes, keep
`mvn verify` green, and follow the existing style. Release notes live in [CHANGELOG.md](CHANGELOG.md).

## License

[MIT](LICENSE) © Carlos Guillermo Reyes Ramiro
