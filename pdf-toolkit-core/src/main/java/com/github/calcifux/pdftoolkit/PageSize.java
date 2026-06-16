package com.github.calcifux.pdftoolkit;

/**
 * Standard page sizes, as the CSS {@code @page size} keyword. For a non-standard size use
 * {@link PageOptions}{@code .customSize("210mm 297mm")} instead, which overrides this.
 */
public enum PageSize {
    A3("A3"),
    A4("A4"),
    A5("A5"),
    LETTER("letter"),
    LEGAL("legal");

    private final String cssValue;

    PageSize(String cssValue) {
        this.cssValue = cssValue;
    }

    public String getCssValue() {
        return cssValue;
    }
}
