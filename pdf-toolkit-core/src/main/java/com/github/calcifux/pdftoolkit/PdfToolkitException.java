package com.github.calcifux.pdftoolkit;

/**
 * Unchecked failure from anywhere in the toolkit — a template that won't render, an engine that can't
 * produce a PDF, a post-processor that fails, or a spec with no HTML source. One exception type keeps
 * the seams simple; the message says which stage failed.
 */
public class PdfToolkitException extends RuntimeException {

    public PdfToolkitException(String message) {
        super(message);
    }

    public PdfToolkitException(String message, Throwable cause) {
        super(message, cause);
    }
}
