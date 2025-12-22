package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 兼容前端把对象字段传成空字符串的情况（例如 subtitleTemplate: ""）。
 * - 空字符串 / 空白字符串 => null
 * - JSON 对象 => 正常反序列化为 SubtitleTemplate
 */
public class SubtitleTemplateLenientDeserializer extends JsonDeserializer<SubtitleTemplate> {
    @Override
    public SubtitleTemplate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t == JsonToken.VALUE_STRING) {
            String s = p.getValueAsString();
            if (s == null || s.trim().isEmpty()) {
                return null;
            }
            // 字符串但非空：尝试按 JSON 解析（兼容某些端把对象 JSON 字符串化）
            try {
                ObjectMapper mapper = (ObjectMapper) p.getCodec();
                JsonNode node = mapper.readTree(s);
                return mapper.treeToValue(node, SubtitleTemplate.class);
            } catch (Exception ignore) {
                return null;
            }
        }
        // object/array 等：交给 Jackson 走常规映射
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        return mapper.treeToValue(node, SubtitleTemplate.class);
    }
}


