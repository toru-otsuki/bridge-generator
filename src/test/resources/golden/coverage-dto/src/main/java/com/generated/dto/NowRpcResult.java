package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Long;

public class NowRpcResult {
  @JsonProperty("return")
  public Long returnValue;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
