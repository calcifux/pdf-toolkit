package com.github.calcifux.pdftoolkit.openhtmltopdf;

import com.github.calcifux.pdftoolkit.PdfRenderOptions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OpenHtmlPdfEngineTest {

    private final OpenHtmlPdfEngine engine = new OpenHtmlPdfEngine();

    @Test
    void renders_well_formed_xhtml_into_a_real_pdf() {
        String html = "<html><head><style>@page { size: A4; }</style></head>"
                + "<body><h1>Hi Calcifux</h1><p>pdf-toolkit</p></body></html>";

        byte[] pdf = engine.render(html, PdfRenderOptions.defaults());

        assertThat(pdf).isNotEmpty();
        // PDF files start with the "%PDF" magic and end with the "%%EOF" trailer.
        String head = new String(pdf, 0, 5, StandardCharsets.ISO_8859_1);
        assertThat(head).startsWith("%PDF-");
        assertThat(new String(pdf, StandardCharsets.ISO_8859_1)).contains("%%EOF");
    }

    @Test
    void engine_id_is_stable() {
        assertThat(engine.engineId()).isEqualTo("openhtmltopdf");
    }
}
