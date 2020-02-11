package org.kestra.repository.elasticsearch.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import lombok.Getter;
import org.kestra.core.serializers.JacksonMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

@EachProperty("kestra.elasticsearch.indices")
@Getter
public class IndicesConfig {
    private final static ObjectMapper yamlMapper = JacksonMapper.ofYaml();
    private final static ObjectMapper jsonMapper = JacksonMapper.ofJson();
    private final static TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};

    String name;

    Class<?> cls;

    String index;

    String settings;

    String mappingFile;

    public IndicesConfig(@Parameter String name) {
        this.name = name;
    }

    public String getMappingContent() {
        URL url = Objects.requireNonNull(IndicesConfig.class.getClassLoader()
            .getResource("mappings/" + this.getMappingFile() + ".yml"));

        try {
            Map<String, Object> map = yamlMapper.readValue(url, typeReference);
            return jsonMapper.writeValueAsString(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

