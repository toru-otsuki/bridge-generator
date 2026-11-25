package com.generated.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.String;
import tools.jackson.databind.annotation.JsonDeserialize;

public class PingRpcRequest {
  @JsonDeserialize(
      using = JSONRPC.LenientVersion.class
  )
  public JSONRPC.Version jsonrpc;

  @JsonDeserialize(
      using = JSONRPC.LenientString.class
  )
  @Schema(
      allowableValues = "ping"
  )
  public String method;

  public int id;

  public PingRpcParams params;
}
