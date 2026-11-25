package com.generated.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public final class CORBA {
  private CORBA() {}

  @Schema(name = "CORBA.SystemException")
  public static class SystemException {
    public String id;

    public long minor;

    @JsonProperty("completion_status")
    public CompletionStatus completionStatus;
  }

  @Schema(name = "CORBA.CompletionStatus", enumAsRef = true)
  public enum CompletionStatus {
    COMPLETED_YES,
    COMPLETED_NO,
    COMPLETED_MAYBE
  }
}
