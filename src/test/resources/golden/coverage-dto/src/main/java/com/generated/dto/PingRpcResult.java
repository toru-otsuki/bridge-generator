package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Boolean;

public class PingRpcResult {
  @JsonProperty("return")
  public Boolean returnValue;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
