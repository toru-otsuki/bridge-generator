package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Integer;

public class FindItemsRpcResult {
  @JsonProperty("return")
  public Integer returnValue;

  public TagDto[] tags;

  public String message;

  @JsonProperty("system_exception")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public CORBA.SystemException systemException;
}
