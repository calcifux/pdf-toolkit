# Examples

Illustrative snippets (not compiled as part of the build) showing the common shapes. See the root
[README](../README.md) for the `application.yml` config and the templating setup.

| File | Shows |
| --- | --- |
| [`InvoicePdf.java`](InvoicePdf.java) | The simplest document — a named template + vars + title, with page numbers. |
| [`ReportPdf.java`](ReportPdf.java) | Landscape, running header + footer text, page numbers, a base URI for relative assets, and an explicit locale. |
| [`DbTemplatePdf.java`](DbTemplatePdf.java) | A template string edited by users + stored in the DB, rendered inline — the billing-statement case, with the user-edited-template security note. |
| [`EncryptPostProcessor.java`](EncryptPostProcessor.java) | Password-protect every PDF with a `PdfPostProcessor` (PDFBox, in the app) — the generate-vs-process boundary. |

### Final HTML, no templating engine

If you already have the HTML, you need no templating adapter at all:

```java
byte[] pdf = Pdf.generate(PdfSpec.builder()
        .html("<html><head></head><body><h1>Hi Calcifux</h1></body></html>")
        .build());
```

### A Pebble template these expect

`src/main/resources/templates/layout.peb`

```html
<html><head><style>body { font-family: sans-serif; }</style></head>
<body>{% block content %}{% endblock %}</body></html>
```

`src/main/resources/templates/invoice.peb`

```html
{% extends "templates/layout" %}
{% block content %}<h1>Invoice {{ folio }}</h1><p>Total: {{ total }}</p>{% endblock %}
```
