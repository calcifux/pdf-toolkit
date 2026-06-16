package com.github.calcifux.pdftoolkit;

/**
 * Engine-level knobs passed to a {@link PdfEngine}: the {@code baseUri} used to resolve relative assets
 * (images, fonts, stylesheets) in the HTML — null/blank means everything must be absolute or embedded
 * (e.g. {@code data:} URIs) — and whether to use the engine's fast mode.
 */
public record PdfRenderOptions(String baseUri, boolean fastMode) {

    public static PdfRenderOptions defaults() {
        return new PdfRenderOptions(null, true);
    }

    public static PdfRenderOptions withBaseUri(String baseUri) {
        return new PdfRenderOptions(baseUri, true);
    }
}
