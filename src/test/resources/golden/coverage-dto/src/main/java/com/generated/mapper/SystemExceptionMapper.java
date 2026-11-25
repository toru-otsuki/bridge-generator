package com.generated.mapper;

import com.generated.dto.CORBA;

public final class SystemExceptionMapper {
  private SystemExceptionMapper() {}

  public static CORBA.SystemException toDto(org.omg.CORBA.SystemException e) {
    Class<?> standard = e.getClass();
    while (!standard.getPackageName().equals("org.omg.CORBA")) {
      standard = standard.getSuperclass();
    }
    CORBA.SystemException dto = new CORBA.SystemException();
    dto.id = "IDL:omg.org/CORBA/" + standard.getSimpleName() + ":1.0";
    dto.minor = Integer.toUnsignedLong(e.minor);
    dto.completionStatus = CORBA.CompletionStatus.values()[e.completed.value()];
    return dto;
  }
}
