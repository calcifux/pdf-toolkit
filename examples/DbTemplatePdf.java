// Example — the "users edit the template, it lives in the DB" case (the pdf-utils real-world scenario:
// billing statements whose HTML changes over time). The app loads the edited string and hands it over;
// the toolkit renders it inline (Pebble/Thymeleaf/FreeMarker all support inline source).
package com.example.pdf;

import com.github.calcifux.pdftoolkit.PageOptions;
import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfSpec;

import java.util.Map;

public class DbTemplatePdf extends PdfDocument {

    private final String subject;
    private final String templateHtml;            // the string your users edited, loaded from the DB
    private final Map<String, Object> variables;

    public DbTemplatePdf(String subject, String templateHtml, Map<String, Object> variables) {
        this.subject = subject;
        this.templateHtml = templateHtml;
        this.variables = variables;
    }

    @Override
    public PdfSpec build() {
        return PdfSpec.builder()
                .inlineHtml(templateHtml)         // rendered as-is — no classpath lookup
                .model(variables)
                .title(subject)
                .page(PageOptions.builder().pageNumbers(true).build())
                .build();
    }

    // Wiring:
    //   String html = templateRepo.activeFor(accountType);   // from Oracle / Postgres / ...
    //   byte[] pdf = Pdf.generate(new DbTemplatePdf("Statement", html, vars));
    //
    // SECURITY: user-edited templates are a template-injection surface. Fine for trusted admins; if
    // less-trusted users edit them, sandbox the engine and document which ${variables} are available.
}
