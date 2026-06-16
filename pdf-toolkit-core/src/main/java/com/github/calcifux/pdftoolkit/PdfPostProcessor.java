package com.github.calcifux.pdftoolkit;

/**
 * Optional SEAM applied to the finished PDF bytes, in order, after the engine renders them — the place
 * for things that are PDF document concerns but NOT generation: password-protection / encryption,
 * watermarking, stamping, signing, merging a cover page. Register one (or several) and the
 * {@link PdfGenerator} runs them as a chain.
 *
 * <p>Keeping this a seam means the core stays free of heavy PDF libraries: a PDFBox/iText-based
 * processor lives in your app (or an adapter module), and the toolkit just carries the bytes through.</p>
 */
@FunctionalInterface
public interface PdfPostProcessor {

    /** Transform the PDF bytes and return the result (never null). */
    byte[] process(byte[] pdf);
}
