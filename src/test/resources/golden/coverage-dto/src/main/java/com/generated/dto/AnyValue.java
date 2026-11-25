package com.generated.dto;

@io.swagger.v3.oas.annotations.media.Schema(name = "CORBA.Any")
public class AnyValue {
  public String type;
  public Object value;
  public AnyValue() {}
  public AnyValue(String type, Object value) {
    this.type = type;
    this.value = value;
  }
}
