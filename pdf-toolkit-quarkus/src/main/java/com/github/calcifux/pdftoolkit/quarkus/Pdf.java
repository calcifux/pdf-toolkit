package com.github.calcifux.pdftoolkit.quarkus;

import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfSpec;
import com.github.calcifux.pdftoolkit.PdfToolkitException;

import java.nio.file.Path;

/**
 * Static, drop-in facade for generating PDFs on Quarkus / any CDI runtime — the twin of
 * pdf-toolkit-spring's {@code Pdf} (same method surface), so backend code that calls
 * {@code Pdf.generate(document)} ports across with only an import change. The
 * {@link com.github.calcifux.pdftoolkit.quarkus.core.PdfCdiService} wires the
 * {@link PdfGenerator} into it on application startup (standard CDI lifecycle).
 *
 * <p>Mirrors auth-toolkit's static {@code Auth} facade: a thin, statically-reachable veneer over a
 * CDI-managed singleton. Prefer injecting {@code PdfCdiService} in new code; this facade exists for
 * drop-in parity with the Spring starter.</p>
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

    /** Wired once by {@code PdfCdiService} on startup. */
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
            throw new PdfToolkitException("Pdf facade not initialized — is pdf-toolkit-quarkus on the "
                    + "classpath and CDI started (PdfCdiService wires it via @Observes @Initialized)?");
        }
        return generator;
    }
}
