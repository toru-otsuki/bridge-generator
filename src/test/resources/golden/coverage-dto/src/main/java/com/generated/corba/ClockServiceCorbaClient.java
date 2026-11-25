package com.generated.corba;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.corba.Other.ClockService;
import com.corba.Other.ClockServiceHelper;

@Component
public class ClockServiceCorbaClient {
  private final CorbaOrb corbaOrb;
  private final String objectRef;
  private volatile ClockService service;

  public ClockServiceCorbaClient(CorbaOrb corbaOrb, @Value("${corba.ClockService.object-ref}") String objectRef) {
    if (objectRef == null || objectRef.isBlank()) {
      throw new IllegalStateException("corba.ClockService.object-ref is not set");
    }
    this.corbaOrb = corbaOrb;
    this.objectRef = objectRef.strip();
  }

  private <T> T invoke(java.util.function.Function<ClockService, T> call) {
    ClockService current = this.service;
    if (current == null) {
      current = ClockServiceHelper.narrow(this.corbaOrb.orb().string_to_object(this.objectRef));
      this.service = current;
    }
    try {
      return call.apply(current);
    } catch (org.omg.CORBA.COMM_FAILURE | org.omg.CORBA.TRANSIENT | org.omg.CORBA.OBJECT_NOT_EXIST e) {
      this.service = null;
      throw e;
    }
  }

  private void invokeVoid(java.util.function.Consumer<ClockService> call) {
    this.invoke(
        __stub -> {
          call.accept(__stub);
          return null;
        });
  }

  public long now() {
    return this.invoke(__stub -> __stub.now());
  }

  public String zone(com.corba.Coverage.Tag hint) {
    return this.invoke(__stub -> __stub.zone(hint));
  }

}
