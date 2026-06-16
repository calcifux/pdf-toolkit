package com.github.calcifux.pdftoolkit;

import java.util.Locale;
import java.util.Map;

/**
 * The multi-templating SEAM that turns a template + model into HTML. One port; each engine (Pebble,
 * Thymeleaf, FreeMarker, …) is an adapter module. The core and your PdfDocument never import an engine —
 * switching engines is a dependency swap + one property, your document code never changes.
 *
 * <p>Templating is OPTIONAL: a {@link PdfSpec} that carries final {@code html} skips this seam entirely,
 * so you can use pdf-toolkit with no templating engine at all (pure HTML-to-PDF). Template INHERITANCE
 * ({@code extends}/layout/blocks) is the engine's job — it resolves {@code template} by name from the
 * classpath so parents/layouts are found.</p>
 */
public interface TemplateRenderer {

    /** Render a template referenced by logical name (engine resolves it, inheritance included). */
    String render(String templateRef, Map<String, Object> model, Locale locale);

    /** Render a raw inline template source. Only engines where {@link #supportsInline()} is true. */
    default String renderInline(String templateSource, Map<String, Object> model, Locale locale) {
        throw new UnsupportedOperationException(engineId() + " does not support inline templates");
    }

    /** Whether this engine can render a raw source string (for DB-stored / inline templates). */
    default boolean supportsInline() {
        return false;
    }

    /** Stable id of the engine ({@code "pebble"}, {@code "thymeleaf"}, {@code "freemarker"}, …). */
    String engineId();
}
