package com.github.calcifux.pdftoolkit;

import lombok.Builder;
import lombok.Getter;

/**
 * Page layout, compiled into an {@code @page} CSS rule the toolkit injects before the engine runs
 * (the same trick pdf-utils used, generalized). Size + orientation + margins, optional running header /
 * footer text (printed in the top/bottom margin boxes) and optional page numbers ("1 / 10").
 *
 * <pre>{@code
 * PageOptions.builder()
 *     .size(PageSize.A4).orientation(Orientation.PORTRAIT)
 *     .margin("20mm 15mm")
 *     .headerText("Acme Corp").pageNumbers(true)
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class PageOptions {

    /** Standard size keyword (ignored when {@link #customSize} is set). */
    @Builder.Default
    private final PageSize size = PageSize.A4;

    @Builder.Default
    private final Orientation orientation = Orientation.PORTRAIT;

    /** CSS margin shorthand, e.g. {@code "20mm 15mm"} or {@code "top right bottom left"}. */
    @Builder.Default
    private final String margin = "20mm 15mm";

    /** Non-standard size as raw CSS ({@code "210mm 297mm"}); overrides {@link #size} when set. */
    private final String customSize;

    /** Print "{page} / {pages}" centered in the bottom margin. */
    private final boolean pageNumbers;

    /** Optional running header text (centered in the top margin box). */
    private final String headerText;

    /** Optional running footer text (centered in the bottom margin box; combines with page numbers). */
    private final String footerText;

    /** The defaults: A4, portrait, 20mm/15mm margins, no header/footer/page-numbers. */
    public static PageOptions defaults() {
        return PageOptions.builder().build();
    }
}
