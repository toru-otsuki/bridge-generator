package com.generated.corba;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.corba.Coverage.ItemService;
import com.corba.Coverage.ItemServiceHelper;

@Component
public class ItemServiceCorbaClient {
  private final CorbaOrb corbaOrb;
  private final String objectRef;
  private volatile ItemService service;

  public ItemServiceCorbaClient(CorbaOrb corbaOrb, @Value("${corba.ItemService.object-ref}") String objectRef) {
    if (objectRef == null || objectRef.isBlank()) {
      throw new IllegalStateException("corba.ItemService.object-ref is not set");
    }
    this.corbaOrb = corbaOrb;
    this.objectRef = objectRef.strip();
  }

  private <T> T invoke(java.util.function.Function<ItemService, T> call) {
    ItemService current = this.service;
    if (current == null) {
      current = ItemServiceHelper.narrow(this.corbaOrb.orb().string_to_object(this.objectRef));
      this.service = current;
    }
    try {
      return call.apply(current);
    } catch (org.omg.CORBA.COMM_FAILURE | org.omg.CORBA.TRANSIENT | org.omg.CORBA.OBJECT_NOT_EXIST e) {
      this.service = null;
      throw e;
    }
  }

  private void invokeVoid(java.util.function.Consumer<ItemService> call) {
    this.invoke(
        __stub -> {
          call.accept(__stub);
          return null;
        });
  }

  public com.corba.Coverage.Item getItem(int id) {
    return this.invoke(__stub -> __stub.getItem(id));
  }

  public void putItem(com.corba.Coverage.Item item) {
    this.invokeVoid(__stub -> __stub.putItem(item));
  }

  public com.corba.Coverage.Tag[] listTags(String prefix) {
    return this.invoke(__stub -> __stub.listTags(prefix));
  }

  public String[] echoNames(String[] names) {
    return this.invoke(__stub -> __stub.echoNames(names));
  }

  public String greet(String name) {
    return this.invoke(__stub -> __stub.greet(name));
  }

  public org.omg.CORBA.Any echoAny(org.omg.CORBA.Any value) {
    return this.invoke(__stub -> __stub.echoAny(value));
  }

  public int findItems(String keyword, com.corba.Coverage.TagSeqHolder tags, org.omg.CORBA.StringHolder message) {
    return this.invoke(__stub -> __stub.findItems(keyword, tags, message));
  }

  public void touch(org.omg.CORBA.IntHolder counter) {
    this.invokeVoid(__stub -> __stub.touch(counter));
  }

  public void relabel(org.omg.CORBA.StringHolder label, com.corba.Coverage.Inner.PointHolder p) {
    this.invokeVoid(__stub -> __stub.relabel(label, p));
  }

  public boolean ping() {
    return this.invoke(__stub -> __stub.ping());
  }

  public String[] globalNames(String[] names) {
    return this.invoke(__stub -> __stub.globalNames(names));
  }

  public com.corba.Coverage.Inner.Point movePoint(com.corba.Coverage.Inner.Point p, com.corba.Coverage.Inner.PointHolder previous) {
    return this.invoke(__stub -> __stub.movePoint(p, previous));
  }

  public void outAny(org.omg.CORBA.AnyHolder value) {
    this.invokeVoid(__stub -> __stub.outAny(value));
  }

  public com.corba.Coverage.Status setStatus(int id, com.corba.Coverage.Status status, com.corba.Coverage.StatusSeqHolder history) {
    return this.invoke(__stub -> __stub.setStatus(id, status, history));
  }

  public void cycleStatus(com.corba.Coverage.StatusHolder status) {
    this.invokeVoid(__stub -> __stub.cycleStatus(status));
  }

}
