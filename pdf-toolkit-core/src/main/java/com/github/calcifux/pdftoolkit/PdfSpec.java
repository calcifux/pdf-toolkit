package com.github.calcifux.pdftoolkit;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Locale;
import java.util.Map;

/**
 * The payload describing one PDF: where the HTML comes from, the model, the page layout, the title and
 * the base URI for assets. The HTML source is ONE of three (checked in this order by {@link PdfGenerator}):
 *
 * <ul>
 *   <li>{@link #html} — final HTML, used as-is (no templating engine needed at all);</li>
 *   <li>{@link #inlineHtml} — a raw template source string (e.g. stored in a DB), rendered via the
 *       engine's inline mode;</li>
 *   <li>{@link #template} — a logical template name resolved by the engine (inheritance/layouts work).</li>
 * </ul>
 *
 * <pre>{@code
 * PdfSpec.builder()
 *     .template("invoice")                 // src/main/resources/templates/invoice.peb
 *     .with("total", total).with("folio", folio)
 *     .title("Invoice " + folio)
 *     .page(PageOptions.builder().size(PageSize.A4).pageNumbers(true).build())
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class PdfSpec {

    /** Logical template name (resolved by the engine; supports inheritance/layouts). */
    private final String template;

    /** Raw inline template source (needs an inline-capable engine). */
    private final String inlineHtml;

    /** Final HTML, used as-is — no templating engine required. */
    private final String html;

    @Singular("with")
    private final Map<String, Object> model;

    private final Locale locale;

    /** Document title (injected as {@code <title>} if the HTML has none). */
    private final String title;

    /** Page layout; {@link PageOptions#defaults()} when null. */
    private final PageOptions page;

    /** Base URI to resolve relative assets (images/fonts/css) against. */
    private final String baseUri;

    /** Engine fast mode (default true). */
    @Builder.Default
    private final boolean fastMode = true;
}
