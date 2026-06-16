package com.github.calcifux.pdftoolkit.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config for the toolkit, bound from {@code pdf-toolkit.*}.
 *
 * <pre>{@code
 * pdf-toolkit:
 *   template:
 *     engine: pebble          # pebble | thymeleaf | freemarker (auto-picked if exactly one on the classpath)
 *     prefix: "templates/"    # classpath prefix for template refs
 *     suffix: ".peb"          # engine default if unset (.peb / .html / .ftlh)
 * }</pre>
 *
 * <p>Page layout (size, margins, headers, page numbers) is per-document on the {@code PdfSpec}, not here.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "pdf-toolkit")
public class PdfToolkitProperties {

    private Template template = new Template();

    /** Which templating engine + where templates live (only relevant if you template at all). */
    @Getter
    @Setter
    public static class Template {
        /** Engine id when several adapters are on the classpath ({@code pebble}/{@code thymeleaf}/{@code freemarker}); auto-picked if exactly one. */
        private String engine;
        /** Classpath prefix for template refs (e.g. {@code "templates/"}). */
        private String prefix = "";
        /** Suffix for template files (engine default if null: .peb / .html / .ftlh). */
        private String suffix;
    }
}
