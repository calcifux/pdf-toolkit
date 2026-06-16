package com.github.calcifux.pdftoolkit.spring;

import com.github.calcifux.pdftoolkit.PdfEngine;
import com.github.calcifux.pdftoolkit.PdfGenerator;
import com.github.calcifux.pdftoolkit.PdfPostProcessor;
import com.github.calcifux.pdftoolkit.TemplateRenderer;
import com.github.calcifux.pdftoolkit.freemarker.FreemarkerTemplateRenderer;
import com.github.calcifux.pdftoolkit.openhtmltopdf.OpenHtmlPdfEngine;
import com.github.calcifux.pdftoolkit.pebble.PebbleTemplateRenderer;
import com.github.calcifux.pdftoolkit.thymeleaf.ThymeleafTemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Wires the toolkit from {@code pdf-toolkit.*}: optionally picks a templating engine (Pebble default;
 * any adapter on the classpath, {@code template.engine} chooses when several), provides the OpenHTMLtoPDF
 * {@link PdfEngine}, collects any {@link PdfPostProcessor} beans, builds the {@link PdfGenerator} and
 * initializes the static {@link Pdf} facade. Everything is {@code @ConditionalOnMissingBean} so an app
 * can override any piece — and templating is optional (feed final HTML and no engine is needed).
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(PdfToolkitProperties.class)
public class PdfToolkitAutoConfiguration {

    // --- Templating engine (optional; explicit pdf-toolkit.template.engine wins, else first on classpath) ---

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(PebbleTemplateRenderer.class)
    @ConditionalOnProperty(prefix = "pdf-toolkit.template", name = "engine", havingValue = "pebble", matchIfMissing = true)
    @Order(1)
    static class PebbleRendererConfig {
        @Bean
        @ConditionalOnMissingBean(TemplateRenderer.class)
        TemplateRenderer pebbleTemplateRenderer(PdfToolkitProperties props) {
            return new PebbleTemplateRenderer(props.getTemplate().getPrefix(), suffixOr(props, ".peb"));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ThymeleafTemplateRenderer.class)
    @ConditionalOnProperty(prefix = "pdf-toolkit.template", name = "engine", havingValue = "thymeleaf", matchIfMissing = true)
    @Order(2)
    static class ThymeleafRendererConfig {
        @Bean
        @ConditionalOnMissingBean(TemplateRenderer.class)
        TemplateRenderer thymeleafTemplateRenderer(PdfToolkitProperties props) {
            return new ThymeleafTemplateRenderer(props.getTemplate().getPrefix(), suffixOr(props, ".html"));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(FreemarkerTemplateRenderer.class)
    @ConditionalOnProperty(prefix = "pdf-toolkit.template", name = "engine", havingValue = "freemarker", matchIfMissing = true)
    @Order(3)
    static class FreemarkerRendererConfig {
        @Bean
        @ConditionalOnMissingBean(TemplateRenderer.class)
        TemplateRenderer freemarkerTemplateRenderer(PdfToolkitProperties props) {
            return new FreemarkerTemplateRenderer(props.getTemplate().getPrefix(), suffixOr(props, ".ftlh"));
        }
    }

    // --- PDF engine (default: OpenHTMLtoPDF) ---

    @Bean
    @ConditionalOnClass(OpenHtmlPdfEngine.class)
    @ConditionalOnMissingBean(PdfEngine.class)
    PdfEngine openHtmlPdfEngine() {
        return new OpenHtmlPdfEngine();
    }

    // --- Orchestrator + facade ---

    @Bean
    @ConditionalOnBean(PdfEngine.class)
    @ConditionalOnMissingBean(PdfGenerator.class)
    PdfGenerator pdfGenerator(ObjectProvider<TemplateRenderer> renderer, PdfEngine engine,
                              ObjectProvider<PdfPostProcessor> postProcessors) {
        return new PdfGenerator(renderer.getIfAvailable(), engine, postProcessors.orderedStream().toList());
    }

    @Bean
    @ConditionalOnBean(PdfGenerator.class)
    PdfFacadeInitializer pdfFacadeInitializer(PdfGenerator generator) {
        return new PdfFacadeInitializer(generator);
    }

    /** Pushes the wired generator into the static {@link Pdf} facade on startup. */
    static final class PdfFacadeInitializer {
        PdfFacadeInitializer(PdfGenerator generator) {
            Pdf.init(generator);
        }
    }

    private static String suffixOr(PdfToolkitProperties props, String engineDefault) {
        String configured = props.getTemplate().getSuffix();
        return (configured != null && !configured.isBlank()) ? configured : engineDefault;
    }
}
