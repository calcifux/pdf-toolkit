package com.github.calcifux.pdftoolkit.spring;

import com.github.calcifux.pdftoolkit.PdfEngine;
import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfSpec;
import com.github.calcifux.pdftoolkit.TemplateRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wires the autoconfig with pebble + openhtmltopdf on the test classpath: proves the engine selection,
 * the default OpenHTMLtoPDF PdfEngine, and an end-to-end real PDF from both a template and final HTML.
 */
class PdfToolkitAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PdfToolkitAutoConfiguration.class));

    @Test
    void picks_pebble_and_openhtmltopdf_by_default() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(PdfGenerator.class);
            assertThat(context.getBean(TemplateRenderer.class).engineId()).isEqualTo("pebble");
            assertThat(context.getBean(PdfEngine.class).engineId()).isEqualTo("openhtmltopdf");
        });
    }

    @Test
    void generates_a_real_pdf_from_a_pebble_template() {
        runner.run(context -> {
            byte[] pdf = context.getBean(PdfGenerator.class).generate(PdfSpec.builder()
                    .template("pdf/invoice")
                    .with("folio", "A-1")
                    .with("total", "100.00")
                    .title("Invoice A-1")
                    .build());

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
        });
    }

    @Test
    void generates_a_real_pdf_from_final_html_without_templating() {
        runner.run(context -> {
            byte[] pdf = context.getBean(PdfGenerator.class).generate(PdfSpec.builder()
                    .html("<html><head></head><body><h1>Hi Calcifux</h1></body></html>")
                    .build());

            assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
        });
    }
}
