package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;

public class OutAnyRpcResult {
  @JsonProperty("return")
  public Object returnValue;

  public AnyValue value;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
