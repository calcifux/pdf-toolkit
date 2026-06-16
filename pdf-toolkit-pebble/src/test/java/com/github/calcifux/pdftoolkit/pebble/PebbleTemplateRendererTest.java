package com.github.calcifux.pdftoolkit.pebble;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PebbleTemplateRendererTest {

    private final PebbleTemplateRenderer renderer = new PebbleTemplateRenderer();

    @Test
    void resolves_template_inheritance() {
        // pdf/welcome.peb does {% extends "pdf/base" %} and fills the content block.
        String html = renderer.render("pdf/welcome", Map.of("name", "Calcifux"), null);

        assertThat(html).contains("id=\"content\"");   // from the base layout
        assertThat(html).contains("Hi Calcifux");      // from the child block
    }

    @Test
    void renders_inline_source() {
        String html = renderer.renderInline("Hi {{ name }}", Map.of("name", "Calcifux"), null);
        assertThat(html).isEqualTo("Hi Calcifux");
        assertThat(renderer.supportsInline()).isTrue();
    }

    @Test
    void escapes_html_by_default() {
        String html = renderer.renderInline("{{ value }}", Map.of("value", "<b>Calcifux & co</b>"), null);
        assertThat(html).contains("&lt;b&gt;").contains("&amp;");
        assertThat(html).doesNotContain("<b>");
    }
}
