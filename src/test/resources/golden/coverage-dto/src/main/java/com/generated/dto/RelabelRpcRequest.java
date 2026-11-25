package com.generated.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.String;
import tools.jackson.databind.annotation.JsonDeserialize;

public class RelabelRpcRequest {
  @JsonDeserialize(
      using = JSONRPC.LenientVersion.class
  )
  public JSONRPC.Version jsonrpc;

  @JsonDeserialize(
      using = JSONRPC.LenientString.class
  )
  @Schema(
      allowableValues = "relabel"
  )
  public String method;

  public int id;

  public RelabelRpcParams params;
}
