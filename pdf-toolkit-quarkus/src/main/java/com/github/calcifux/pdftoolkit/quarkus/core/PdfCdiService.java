package com.github.calcifux.pdftoolkit.quarkus.core;

import com.github.calcifux.pdftoolkit.PdfDocument;
import com.github.calcifux.pdftoolkit.PdfEngine;
import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfSpec;
import com.github.calcifux.pdftoolkit.PdfToolkitException;
import com.github.calcifux.pdftoolkit.TemplateRenderer;
import com.github.calcifux.pdftoolkit.openhtmltopdf.OpenHtmlPdfEngine;
import com.github.calcifux.pdftoolkit.pebble.PebbleTemplateRenderer;
import com.github.calcifux.pdftoolkit.quarkus.Pdf;
import com.github.calcifux.pdftoolkit.quarkus.PdfFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;

/**
 * CDI-managed twin of the static {@link Pdf} facade — the primary entry point on Quarkus / any CDI +
 * MicroProfile Config runtime (Helidon, OpenLiberty, ...). Mirrors auth-toolkit's {@code AuthCdiService}
 * and remote-upload's {@code RemoteUploadCdiService}: an injectable {@code @ApplicationScoped} bean that
 * wraps the framework-agnostic {@link PdfGenerator}.
 *
 * <p>Builds the generator from a Pebble {@link TemplateRenderer} + the OpenHTMLtoPDF {@link PdfEngine},
 * configured by {@code pdf-toolkit.template.*} (engine/prefix/suffix). On startup it also pushes the
 * generator into the static {@link Pdf} facade via standard CDI lifecycle
 * ({@code @Observes @Initialized(ApplicationScoped.class)}) so drop-in {@code Pdf.generate(...)} calls
 * work too — no Quarkus-specific {@code StartupEvent}.</p>
 *
 * <pre>{@code
 * @Inject PdfCdiService pdf;
 * byte[] bytes = pdf.generate(new InvoicePdf(invoice));
 * }</pre>
 *
 * <p>Configuration:</p>
 * <pre>{@code
 * pdf-toolkit.template.engine=pebble   # only pebble is bundled by this adapter
 * pdf-toolkit.template.prefix=         # classpath prefix for template refs (e.g. "templates/")
 * pdf-toolkit.template.suffix=.peb     # template file suffix
 * }</pre>
 */
@Slf4j
@ApplicationScoped
public class PdfCdiService {

    private final String engine;
    private final String prefix;
    private final String suffix;

    private volatile PdfGenerator generator;

    /**
     * @param engine templating engine id; only {@code pebble} is bundled by the -quarkus adapter
     *               (default {@code pebble}). Injected from {@code pdf-toolkit.template.engine}.
     * @param prefix classpath prefix for template refs (default {@code ""}).
     *               Injected from {@code pdf-toolkit.template.prefix}.
     * @param suffix template file suffix (default {@code .peb}).
     *               Injected from {@code pdf-toolkit.template.suffix}.
     */
    @Inject
    public PdfCdiService(
            @ConfigProperty(name = "pdf-toolkit.template.engine", defaultValue = "pebble") String engine,
            // Optional, NO defaultValue="": SmallRye trata un default String vacío como null y truena el
            // arranque (SRCFG00040). Optional<String> + orElse("") expresa "sin prefijo" de forma robusta.
            @ConfigProperty(name = "pdf-toolkit.template.prefix") Optional<String> prefix,
            @ConfigProperty(name = "pdf-toolkit.template.suffix", defaultValue = ".peb") String suffix) {
        this.engine = engine;
        this.prefix = prefix.orElse("");
        this.suffix = suffix;
    }

    /** Builds the PdfGenerator from config (lazy, idempotent — first call on startup or first use). */
    synchronized void build() {
        if (generator != null) {
            return;
        }
        TemplateRenderer renderer = buildRenderer();
        PdfEngine pdfEngine = new OpenHtmlPdfEngine();
        this.generator = new PdfGenerator(renderer, pdfEngine);
        log.info("[PdfCdiService] wired PdfGenerator: renderer='{}' (prefix='{}', suffix='{}'), engine='{}'",
                renderer.engineId(), prefix, suffix, pdfEngine.engineId());
    }

    /**
     * Pushes the wired generator into the static {@link Pdf} facade on application startup, using the
     * portable CDI lifecycle event (not Quarkus' {@code StartupEvent}). Keeps drop-in {@code Pdf.*}
     * calls working alongside injection.
     */
    void onStart(@Observes @Initialized(ApplicationScoped.class) Object ignored) {
        PdfFacade.initialize(require());
        log.debug("[PdfCdiService] static Pdf facade initialized on ApplicationScoped startup");
    }

    // --- Backend parity surface (same signatures as the spring Pdf facade) ---

    public byte[] generate(PdfDocument document) {
        return require().generate(document);
    }

    public byte[] generate(PdfSpec spec) {
        return require().generate(spec);
    }

    public Path toFile(PdfDocument document, Path output) {
        return require().generateToFile(document, output);
    }

    public Path toFile(PdfSpec spec, Path output) {
        return require().generateToFile(spec, output);
    }

    /** The resolved HTML (template rendered, page CSS + title injected) without producing a PDF. */
    public String previewHtml(PdfSpec spec) {
        return require().previewHtml(spec);
    }

    /** The wired generator, for callers that want the framework-agnostic orchestrator directly. */
    public PdfGenerator generator() {
        return require();
    }

    private TemplateRenderer buildRenderer() {
        if (engine == null || engine.isBlank() || "pebble".equalsIgnoreCase(engine)) {
            return new PebbleTemplateRenderer(prefix, suffixOr(".peb"));
        }
        throw new PdfToolkitException("Unsupported pdf-toolkit.template.engine='" + engine
                + "' for pdf-toolkit-quarkus (only 'pebble' is bundled)");
    }

    private String suffixOr(String engineDefault) {
        return (suffix != null && !suffix.isBlank()) ? suffix : engineDefault;
    }

    private PdfGenerator require() {
        if (generator == null) {
            // @PostConstruct not yet run (e.g. direct test instantiation): build lazily.
            build();
        }
        return generator;
    }
}
