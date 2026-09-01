package com.pumpkings.pkshopkeepers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class MetadataConsistencyTest {

    @Test
    void gradleAndPaperDescriptorUseTheSameVersion() throws IOException {
        String gradle = Files.readString(Path.of("build.gradle"));
        String descriptor = Files.readString(Path.of("src", "main", "resources", "paper-plugin.yml"));
        assertEquals(capture(gradle, "version\\s*=\\s*'([^']+)'"),
                capture(descriptor, "(?m)^version:\\s*['\"]?([^'\"\\s]+)"));
    }

    @Test
    void descriptorDeclaresFoliaSupport() throws IOException {
        String descriptor = Files.readString(Path.of("src", "main", "resources", "paper-plugin.yml"));
        assertTrue(descriptor.contains("folia-supported: true"));
    }

    private String capture(String text, String expression) {
        Matcher matcher = Pattern.compile(expression).matcher(text);
        assertTrue(matcher.find(), "Missing metadata matching " + expression);
        return matcher.group(1);
    }
}
