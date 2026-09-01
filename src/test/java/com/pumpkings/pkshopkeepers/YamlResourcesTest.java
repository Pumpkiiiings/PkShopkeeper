package com.pumpkings.pkshopkeepers;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

class YamlResourcesTest {

    @ParameterizedTest
    @ValueSource(strings = {"config.yml", "guis.yml", "paper-plugin.yml"})
    void bundledYamlIsValid(String fileName) throws IOException {
        Path path = Path.of("src", "main", "resources", fileName);
        try (InputStream input = Files.newInputStream(path)) {
            Object document = new Yaml().load(input);
            assertNotNull(document, fileName + " is empty");
            assertInstanceOf(Map.class, document, fileName + " must have a mapping at its root");
        }
    }
}
