// Example (architect) — password-protect every generated PDF with a PdfPostProcessor. This is the
// boundary: the toolkit GENERATES the PDF; encrypting / watermarking / merging / signing are document
// processing that lives in YOUR app (with PDFBox/iText here), contributed as a bean the generator runs
// after the engine. The core never pulls a heavy PDF library to do this.
package com.example.pdf;

import com.github.calcifux.pdftoolkit.PdfPostProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;

@Configuration
public class EncryptPostProcessor {

    /** Any PdfPostProcessor bean is picked up by the autoconfig and run, in order, after the engine. */
    @Bean
    PdfPostProcessor encryptPdfs() {
        return pdf -> {
            try (PDDocument document = PDDocument.load(pdf)) {
                AccessPermission permissions = new AccessPermission();
                permissions.setCanPrint(true);
                permissions.setCanModify(false);
                // owner password unlocks everything; user password is what the recipient types to open
                StandardProtectionPolicy policy =
                        new StandardProtectionPolicy("owner-secret", "user-secret", permissions);
                policy.setEncryptionKeyLength(128);
                document.protect(policy);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                return out.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Failed to encrypt PDF", e);
            }
        };
    }
}
