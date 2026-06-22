package com.github.calcifux.pdftoolkit.quarkus;

import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfSpec;
import com.github.calcifux.pdftoolkit.quarkus.core.PdfCdiService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the -quarkus wiring without a CDI container: instantiate {@link PdfCdiService} with the same
 * defaults CDI would inject ({@code pebble} / {@code ""} / {@code .peb}), then drive a real OpenHTMLtoPDF
 * render from both a Pebble template and final HTML — mirrors the spring starter's smoke test.
 */
class PdfCdiServiceTest {

    /** Same defaults as {@code @ConfigProperty(... defaultValue = ...)} on the CDI constructor. */
    private final PdfCdiService service = new PdfCdiService("pebble", Optional.of(""), ".peb");

    @Test
    void wiresAPebbleRendererAndOpenHtmlEngine() {
        PdfGenerator generator = service.generator();
        assertThat(generator).isNotNull();
    }

    @Test
    void rendersARealPdfFromAPebbleTemplate() {
        byte[] pdf = service.generate(PdfSpec.builder()
                .template("pdf/invoice")
                .with("folio", "A-1")
                .with("total", "100.00")
                .title("Invoice A-1")
                .build());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
    }

    @Test
    void rendersARealPdfFromFinalHtmlWithoutTemplating() {
        byte[] pdf = service.generate(PdfSpec.builder()
                .html("<html><head></head><body><h1>Hi Calcifux</h1></body></html>")
                .build());

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
    }

    @Test
    void previewHtmlInjectsTitleWithoutProducingPdf() {
        String html = service.previewHtml(PdfSpec.builder()
                .template("pdf/invoice")
                .with("folio", "B-2")
                .with("total", "50.00")
                .title("Invoice B-2")
                .build());

        assertThat(html).contains("Invoice B-2").contains("B-2");
    }
}
