package com.elasticbeanstalk.mini_elastic_beanstalk.util;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class YamlUtils {

    private final Yaml yaml = new Yaml();

    public Map<String, Object> loadYaml(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return yaml.load(inputStream);
        }
    }

    public void saveYaml(Map<String, Object> data, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            yaml.dump(data, writer);
        }
    }

    public String toYamlString(Object object) {
        return yaml.dump(object);
    }

    public <T> T fromYamlString(String yamlString, Class<T> clazz) {
        return yaml.loadAs(yamlString, clazz);
    }
}