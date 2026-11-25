package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MovePointRpcResult {
  @JsonProperty("return")
  public PointDto returnValue;

  public PointDto previous;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
