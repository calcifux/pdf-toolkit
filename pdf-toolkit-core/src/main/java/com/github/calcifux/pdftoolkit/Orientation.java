package com.github.calcifux.pdftoolkit;

/** Page orientation, compiled into the {@code @page size} CSS keyword. */
public enum Orientation {
    PORTRAIT("portrait"),
    LANDSCAPE("landscape");

    private final String cssValue;

    Orientation(String cssValue) {
        this.cssValue = cssValue;
    }

    public String getCssValue() {
        return cssValue;
    }
}
