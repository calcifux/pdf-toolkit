package com.github.calcifux.pdftoolkit;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Drives PdfGenerator with a capturing fake engine (returns the received HTML as its "PDF" bytes, so we
 * can assert on the HTML the engine was handed) and a fake renderer — covering the three HTML sources,
 * @page CSS injection, and the post-processor chain. No real PDF library here; that is the engine
 * adapter module's job.
 */
class PdfGeneratorTest {

    private final CapturingEngine engine = new CapturingEngine();

    @Test
    void final_html_needs_no_renderer_and_gets_page_css_injected() {
        PdfGenerator generator = new PdfGenerator(engine);

        byte[] pdf = generator.generate(PdfSpec.builder()
                .html("<html><head></head><body>Hi Calcifux</body></html>")
                .build());

        String seen = new String(pdf, StandardCharsets.UTF_8);
        assertThat(seen).contains("Hi Calcifux");
        assertThat(seen).contains("@page");
        assertThat(seen).contains("size: A4 portrait");
        assertThat(seen).contains("margin: 20mm 15mm");
    }

    @Test
    void page_options_compile_to_css_margin_boxes() {
        PdfGenerator generator = new PdfGenerator(engine);

        generator.generate(PdfSpec.builder()
                .html("<html><head></head><body>x</body></html>")
                .title("Statement 42")
                .page(PageOptions.builder()
                        .size(PageSize.LETTER)
                        .orientation(Orientation.LANDSCAPE)
                        .margin("10mm")
                        .headerText("Acme Corp")
                        .pageNumbers(true)
                        .build())
                .build());

        assertThat(engine.lastHtml).contains("size: letter landscape");
        assertThat(engine.lastHtml).contains("margin: 10mm");
        assertThat(engine.lastHtml).contains("@top-center { content: \"Acme Corp\"");
        assertThat(engine.lastHtml).contains("counter(page) \" / \" counter(pages)");
        assertThat(engine.lastHtml).contains("<title>Statement 42</title>");
    }

    @Test
    void inline_template_is_rendered_through_the_engine() {
        PdfGenerator generator = new PdfGenerator(new FakeRenderer(), engine);

        generator.generate(PdfSpec.builder()
                .inlineHtml("<p>{{name}}</p>")
                .with("name", "Calcifux")
                .build());

        assertThat(engine.lastHtml).contains("INLINE:<p>{{name}}</p>");
        assertThat(engine.lastHtml).contains("name=Calcifux");
    }

    @Test
    void named_template_is_resolved_through_the_renderer() {
        PdfGenerator generator = new PdfGenerator(new FakeRenderer(), engine);

        generator.generate(PdfSpec.builder().template("invoice").with("folio", 7).build());

        assertThat(engine.lastHtml).contains("RENDERED:invoice");
        assertThat(engine.lastHtml).contains("folio=7");
    }

    @Test
    void post_processors_run_as_a_chain_in_order() {
        PdfPostProcessor wrap = pdf -> ("[" + new String(pdf, StandardCharsets.UTF_8) + "]").getBytes(StandardCharsets.UTF_8);
        PdfPostProcessor exclaim = pdf -> (new String(pdf, StandardCharsets.UTF_8) + "!").getBytes(StandardCharsets.UTF_8);
        PdfGenerator generator = new PdfGenerator(null, engine, java.util.List.of(wrap, exclaim));

        byte[] pdf = generator.generate(PdfSpec.builder().html("<html><body>x</body></html>").build());

        String result = new String(pdf, StandardCharsets.UTF_8);
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]!");
    }

    @Test
    void a_spec_with_no_source_fails_clearly() {
        PdfGenerator generator = new PdfGenerator(engine);
        assertThatThrownBy(() -> generator.generate(PdfSpec.builder().build()))
                .isInstanceOf(PdfToolkitException.class)
                .hasMessageContaining("no source");
    }

    @Test
    void a_template_without_a_renderer_fails_clearly() {
        PdfGenerator generator = new PdfGenerator(engine);   // no renderer
        assertThatThrownBy(() -> generator.generate(PdfSpec.builder().template("invoice").build()))
                .isInstanceOf(PdfToolkitException.class)
                .hasMessageContaining("No TemplateRenderer");
    }

    // --- fakes ---

    static class CapturingEngine implements PdfEngine {
        String lastHtml;
        PdfRenderOptions lastOptions;

        @Override
        public byte[] render(String html, PdfRenderOptions options) {
            this.lastHtml = html;
            this.lastOptions = options;
            return html.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String engineId() {
            return "fake";
        }
    }

    static class FakeRenderer implements TemplateRenderer {
        @Override
        public String render(String templateRef, Map<String, Object> model, Locale locale) {
            return "<html><body>RENDERED:" + templateRef + ":" + model + "</body></html>";
        }

        @Override
        public String renderInline(String templateSource, Map<String, Object> model, Locale locale) {
            return "<html><body>INLINE:" + templateSource + ":" + model + "</body></html>";
        }

        @Override
        public boolean supportsInline() {
            return true;
        }

        @Override
        public String engineId() {
            return "fake-renderer";
        }
    }
}
