package com.github.calcifux.pdftoolkit.openhtmltopdf;

import com.github.calcifux.pdftoolkit.PdfEngine;
import com.github.calcifux.pdftoolkit.PdfRenderOptions;
import com.github.calcifux.pdftoolkit.PdfToolkitException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;

/**
 * Default {@link PdfEngine}: OpenHTMLtoPDF with the PDFBox backend (the engine pdf-utils used). Takes
 * complete XHTML (page CSS + title already injected by the orchestrator) and rasterizes it to PDF bytes.
 *
 * <p>Relative assets (images, fonts, stylesheets) resolve against {@code options.baseUri()}; with no base
 * URI everything must be absolute or embedded ({@code data:} URIs) — which is the common, portable case.
 * The input must be well-formed XHTML (OpenHTMLtoPDF's parser is strict).</p>
 */
public class OpenHtmlPdfEngine implements PdfEngine {

    @Override
    public byte[] render(String html, PdfRenderOptions options) {
        PdfRenderOptions effective = options == null ? PdfRenderOptions.defaults() : options;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            if (effective.fastMode()) {
                builder.useFastMode();
            }
            builder.withHtmlContent(html, effective.baseUri());
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new PdfToolkitException("OpenHTMLtoPDF render failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String engineId() {
        return "openhtmltopdf";
    }
}
