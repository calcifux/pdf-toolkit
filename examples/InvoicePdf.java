// Example — the simplest document: a named template + vars + a title, with page numbers.
// Template: src/main/resources/templates/invoice.peb (which {% extends "templates/layout" %}).
package com.example.pdf;

import com.github.calcifux.pdftoolkit.PageOptions;
import com.github.calcifux.pdftoolkit.PageSize;
import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfSpec;

public class InvoicePdf extends PdfDocument {

    private final String folio;
    private final String total;

    public InvoicePdf(String folio, String total) {
        this.folio = folio;
        this.total = total;
    }

    @Override
    public String filename() {
        return "invoice-" + folio + ".pdf";
    }

    @Override
    public PdfSpec build() {
        return PdfSpec.builder()
                .template("invoice")               // templates/invoice.peb
                .with("folio", folio)
                .with("total", total)
                .title("Invoice " + folio)
                .page(PageOptions.builder()
                        .size(PageSize.A4)
                        .margin("20mm 15mm")
                        .pageNumbers(true)
                        .build())
                .build();
    }

    // Usage:
    //   byte[] pdf = Pdf.generate(new InvoicePdf("A-1024", "1,250.00"));
    //   Pdf.toFile(new InvoicePdf("A-1024", "1,250.00"), Path.of("/tmp/invoice.pdf"));
}
