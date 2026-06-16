// Example — a landscape report: running header + footer text, page numbers, a base URI so the template
// can reference relative assets (logo, fonts), and an explicit locale.
package com.example.pdf;

import com.github.calcifux.pdftoolkit.Orientation;
import com.github.calcifux.pdftoolkit.PageOptions;
import com.github.calcifux.pdftoolkit.PageSize;
import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfSpec;

import java.util.List;
import java.util.Locale;

public class ReportPdf extends PdfDocument {

    private final String period;
    private final List<?> rows;

    public ReportPdf(String period, List<?> rows) {
        this.period = period;
        this.rows = rows;
    }

    @Override
    public PdfSpec build() {
        return PdfSpec.builder()
                .template("report")
                .with("period", period)
                .with("rows", rows)
                .title("Report " + period)
                .locale(Locale.forLanguageTag("es-MX"))
                .baseUri("classpath:/static/")     // relative <img src="logo.png"> resolves here
                .page(PageOptions.builder()
                        .size(PageSize.A4)
                        .orientation(Orientation.LANDSCAPE)
                        .margin("12mm")
                        .headerText("Acme Corp — " + period)
                        .footerText("Confidential")
                        .pageNumbers(true)
                        .build())
                .build();
    }
}
