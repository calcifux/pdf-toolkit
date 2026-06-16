package com.github.calcifux.pdftoolkit;

/**
 * A typed, reusable PDF definition — the jr's unit of work (the PDF analogue of mailable-toolkit's
 * {@code Mailable}). Subclass it and return a {@link PdfSpec} from {@link #build()}; constructor
 * arguments carry the data.
 *
 * <pre>{@code
 * public class InvoicePdf extends PdfDocument {
 *     private final Invoice invoice;
 *     public InvoicePdf(Invoice invoice) { this.invoice = invoice; }
 *
 *     @Override public String filename() { return "invoice-" + invoice.folio() + ".pdf"; }
 *
 *     @Override public PdfSpec build() {
 *         return PdfSpec.builder()
 *             .template("invoice")
 *             .with("invoice", invoice)
 *             .title("Invoice " + invoice.folio())
 *             .page(PageOptions.builder().pageNumbers(true).build())
 *             .build();
 *     }
 * }
 * }</pre>
 */
public abstract class PdfDocument {

    /** Build the payload (HTML source, model, page layout, title). */
    public abstract PdfSpec build();

    /** Suggested filename when saving to disk; null lets the caller decide. */
    public String filename() {
        return null;
    }
}
