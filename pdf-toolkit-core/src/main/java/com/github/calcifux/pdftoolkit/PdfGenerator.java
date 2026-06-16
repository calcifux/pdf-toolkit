package com.github.calcifux.pdftoolkit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates one PDF: resolve the HTML (final / inline / named template — templating is optional),
 * inject the {@code @page} CSS + title, hand it to the {@link PdfEngine}, then run any
 * {@link PdfPostProcessor}s. Framework-agnostic; the Spring starter wraps it behind the static
 * {@code Pdf} facade, but a CLI or job can use it directly.
 */
public class PdfGenerator {

    private final TemplateRenderer renderer;   // optional: null when you only feed final HTML
    private final PdfEngine engine;
    private final List<PdfPostProcessor> postProcessors;

    /** Engine only — no templating (use {@code PdfSpec.html(...)} with already-rendered HTML). */
    public PdfGenerator(PdfEngine engine) {
        this(null, engine, List.of());
    }

    public PdfGenerator(TemplateRenderer renderer, PdfEngine engine) {
        this(renderer, engine, List.of());
    }

    public PdfGenerator(TemplateRenderer renderer, PdfEngine engine, List<PdfPostProcessor> postProcessors) {
        if (engine == null) {
            throw new PdfToolkitException("A PdfEngine is required (add a pdf-toolkit engine adapter, e.g. openhtmltopdf)");
        }
        this.renderer = renderer;
        this.engine = engine;
        this.postProcessors = postProcessors == null ? List.of() : List.copyOf(postProcessors);
    }

    public byte[] generate(PdfDocument document) {
        return generate(document.build());
    }

    public byte[] generate(PdfSpec spec) {
        String html = PageCssInjector.inject(renderHtml(spec), spec.getPage(), spec.getTitle());
        byte[] pdf = engine.render(html, new PdfRenderOptions(spec.getBaseUri(), spec.isFastMode()));
        for (PdfPostProcessor processor : postProcessors) {
            pdf = processor.process(pdf);
            if (pdf == null) {
                throw new PdfToolkitException("PdfPostProcessor returned null: " + processor.getClass().getName());
            }
        }
        return pdf;
    }

    public Path generateToFile(PdfDocument document, Path output) {
        return write(generate(document), output);
    }

    public Path generateToFile(PdfSpec spec, Path output) {
        return write(generate(spec), output);
    }

    /** The resolved HTML (template/inline rendered, page CSS + title injected) — for preview / debugging. */
    public String previewHtml(PdfSpec spec) {
        return PageCssInjector.inject(renderHtml(spec), spec.getPage(), spec.getTitle());
    }

    // Resolve the HTML source: final html > inline template > named template.
    private String renderHtml(PdfSpec spec) {
        if (notBlank(spec.getHtml())) {
            return spec.getHtml();
        }
        if (notBlank(spec.getInlineHtml())) {
            TemplateRenderer engineRenderer = requireRenderer();
            if (!engineRenderer.supportsInline()) {
                throw new PdfToolkitException("Engine '" + engineRenderer.engineId()
                        + "' does not support inline templates; use a named template or final html");
            }
            return engineRenderer.renderInline(spec.getInlineHtml(), spec.getModel(), spec.getLocale());
        }
        if (notBlank(spec.getTemplate())) {
            return requireRenderer().render(spec.getTemplate(), spec.getModel(), spec.getLocale());
        }
        throw new PdfToolkitException("PdfSpec has no source: set html(...), inlineHtml(...) or template(...)");
    }

    private TemplateRenderer requireRenderer() {
        if (renderer == null) {
            throw new PdfToolkitException("No TemplateRenderer configured — add a pdf-toolkit templating "
                    + "adapter (pebble/thymeleaf/freemarker), or pass already-rendered HTML via PdfSpec.html(...)");
        }
        return renderer;
    }

    private Path write(byte[] pdf, Path output) {
        try {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Files.write(output, pdf);
            return output;
        } catch (Exception e) {
            throw new PdfToolkitException("Failed writing PDF to " + output, e);
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
