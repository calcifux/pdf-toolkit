package com.github.calcifux.pdftoolkit;

/**
 * Compiles {@link PageOptions} into an {@code @page} CSS rule and injects it (plus an optional
 * {@code <title>}) into the HTML {@code <head>} before the engine runs — the generalized version of
 * pdf-utils' page-CSS trick. Header/footer text and page numbers use CSS paged-media margin boxes
 * ({@code @top-center}, {@code @bottom-left}, {@code @bottom-right}), which OpenHTMLtoPDF honors.
 */
public final class PageCssInjector {

    private PageCssInjector() {
    }

    /** Inject the {@code @page} rule for {@code options} and a {@code <title>} for {@code title} (if any). */
    public static String inject(String html, PageOptions options, String title) {
        StringBuilder head = new StringBuilder();
        if (title != null && !title.isBlank() && !html.toLowerCase().contains("<title")) {
            head.append("<title>").append(escapeHtml(title)).append("</title>\n");
        }
        head.append(pageCss(options == null ? PageOptions.defaults() : options));
        return injectIntoHead(html, head.toString());
    }

    static String pageCss(PageOptions options) {
        String size = (options.getCustomSize() != null && !options.getCustomSize().isBlank())
                ? options.getCustomSize()
                : options.getSize().getCssValue() + " " + options.getOrientation().getCssValue();

        StringBuilder marginBoxes = new StringBuilder();
        if (options.getHeaderText() != null && !options.getHeaderText().isBlank()) {
            marginBoxes.append("    @top-center { content: \"")
                    .append(escapeCss(options.getHeaderText())).append("\"; font-size: 10px; color: #666; }\n");
        }
        if (options.getFooterText() != null && !options.getFooterText().isBlank()) {
            marginBoxes.append("    @bottom-left { content: \"")
                    .append(escapeCss(options.getFooterText())).append("\"; font-size: 10px; color: #666; }\n");
        }
        if (options.isPageNumbers()) {
            marginBoxes.append("    @bottom-right { content: counter(page) \" / \" counter(pages); font-size: 10px; color: #666; }\n");
        }

        return "<style>\n"
                + "  @page {\n"
                + "    size: " + size + ";\n"
                + "    margin: " + options.getMargin() + ";\n"
                + marginBoxes
                + "  }\n"
                + "</style>\n";
    }

    private static String injectIntoHead(String html, String snippet) {
        int headIndex = html.toLowerCase().indexOf("<head");
        if (headIndex != -1) {
            int closeHead = html.indexOf('>', headIndex);
            if (closeHead != -1) {
                return html.substring(0, closeHead + 1) + "\n" + snippet + html.substring(closeHead + 1);
            }
        }
        // No <head> — OpenHTMLtoPDF parses leniently; prepend the snippet (same as pdf-utils).
        return snippet + html;
    }

    private static String escapeCss(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
