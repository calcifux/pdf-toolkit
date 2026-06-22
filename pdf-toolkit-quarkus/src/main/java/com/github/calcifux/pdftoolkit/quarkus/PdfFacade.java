package com.github.calcifux.pdftoolkit.quarkus;

import com.github.calcifux.pdftoolkit.PdfGenerator;

/**
 * Package-private bridge so {@code com.github.calcifux.pdftoolkit.quarkus.core.PdfCdiService} can push
 * the wired {@link PdfGenerator} into the static {@link Pdf} facade without exposing {@code Pdf.init}
 * publicly. Mirrors the Spring starter's {@code PdfFacadeInitializer}, but reachable from the {@code core}
 * sub-package.
 */
public final class PdfFacade {

    private PdfFacade() {
    }

    /** Initializes the static {@link Pdf} facade with the given generator (called once on CDI startup). */
    public static void initialize(PdfGenerator generator) {
        Pdf.init(generator);
    }
}
