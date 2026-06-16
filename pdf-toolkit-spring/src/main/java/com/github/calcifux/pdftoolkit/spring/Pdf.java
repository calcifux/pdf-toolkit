package com.github.calcifux.pdftoolkit.spring;

import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfSpec;
import com.github.calcifux.pdftoolkit.PdfToolkitException;

import java.nio.file.Path;

/**
 * Static facade for generating PDFs — the jr's entry point (like mailable-toolkit's {@code Mail} or
 * auth-toolkit's {@code Auth}). The autoconfig wires the {@link PdfGenerator} into it.
 *
 * <pre>{@code
 * byte[] pdf = Pdf.generate(new InvoicePdf(invoice));
 * Pdf.toFile(new InvoicePdf(invoice), Path.of("/tmp/invoice.pdf"));
 * byte[] adhoc = Pdf.generate(PdfSpec.builder().html(finalHtml).build());   // no template needed
 * String html = Pdf.previewHtml(spec);                                       // resolved HTML, no PDF
 * }</pre>
 */
public final class Pdf {

    private static volatile PdfGenerator generator;

    private Pdf() {
    }

    /** Wired once by the autoconfig. */
    static void init(PdfGenerator generator) {
        Pdf.generator = generator;
    }

    public static byte[] generate(PdfDocument document) {
        return require().generate(document);
    }

    public static byte[] generate(PdfSpec spec) {
        return require().generate(spec);
    }

    public static Path toFile(PdfDocument document, Path output) {
        return require().generateToFile(document, output);
    }

    public static Path toFile(PdfSpec spec, Path output) {
        return require().generateToFile(spec, output);
    }

    /** The resolved HTML (template rendered, page CSS + title injected) without producing a PDF. */
    public static String previewHtml(PdfSpec spec) {
        return require().previewHtml(spec);
    }

    private static PdfGenerator require() {
        if (generator == null) {
            throw new PdfToolkitException("Pdf facade not initialized — is pdf-toolkit-spring on the "
                    + "classpath with a PdfEngine (e.g. pdf-toolkit-openhtmltopdf)?");
        }
        return generator;
    }
}
