package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public final class JSONRPC {
  private JSONRPC() {}

  @Schema(name = "JSONRPC.Version", enumAsRef = true)
  public enum Version {
    @JsonProperty("2.0")
    V2_0
  }

  public static final class LenientVersion extends ValueDeserializer<Version> {
    @Override
    public Version deserialize(JsonParser p, DeserializationContext ctxt) {
      JsonNode node = ctxt.readTree(p);
      return node.isString() && node.stringValue().equals("2.0") ? Version.V2_0 : null;
    }
  }

  public static final class LenientString extends ValueDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
      JsonNode node = ctxt.readTree(p);
      return node.isString() ? node.stringValue() : null;
    }
  }
}
