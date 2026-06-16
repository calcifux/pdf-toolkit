package com.github.calcifux.pdftoolkit;

/**
 * The HTML-to-PDF SEAM. One port; each engine (OpenHTMLtoPDF by default, and any other you adapt) is an
 * adapter module. The core never imports a PDF library — swapping engines is a dependency swap + one
 * property, your document code never changes. The HTML handed in is already complete (page CSS + title
 * injected by the orchestrator); the engine just rasterizes it to PDF bytes.
 */
public interface PdfEngine {

    /** Render complete HTML into PDF bytes. */
    byte[] render(String html, PdfRenderOptions options);

    /** Stable id of the engine ({@code "openhtmltopdf"}, …). */
    String engineId();
}
