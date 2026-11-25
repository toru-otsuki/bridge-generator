package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetItemRpcResult {
  @JsonProperty("return")
  public ItemDto returnValue;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
