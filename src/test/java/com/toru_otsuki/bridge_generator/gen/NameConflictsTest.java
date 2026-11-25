package com.toru_otsuki.bridge_generator.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.toru_otsuki.bridge_generator.model.CorbaType;
import com.toru_otsuki.bridge_generator.model.IdlEnum;
import com.toru_otsuki.bridge_generator.model.Operation;
import com.toru_otsuki.bridge_generator.model.Param;
import com.toru_otsuki.bridge_generator.model.Service;
import com.toru_otsuki.bridge_generator.model.Struct;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NameConflictsTest {
  private static final Naming EMPTY = naming("", "");

  private static final Service SERVICE =
      new Service(
          "ItemService",
          "corba.m",
          List.of(
              new Operation(
                  "findItems",
                  Optional.empty(),
                  List.of(
                      new Param(
                          "message",
                          CorbaType.STRING,
                          "org.omg.CORBA.StringHolder",
                          Param.Direction.OUT)),
                  List.of())));

  @Test
  void emptyAffixesWithoutConflict() {
    GenSettings settings = settings(List.of(struct("corba.m", "Item")), EMPTY);
    assertEquals(List.of(), NameConflicts.find(List.of(SERVICE), settings));
  }

  @Test
  void idlPackageSameAsGenerated() {
    GenSettings settings = settings(List.of(struct("b.dto", "Item")), Naming.DEFAULT);
    assertMentions(NameConflicts.find(List.of(SERVICE), settings), "b.dto");
  }

  @Test
  void dtoNameConflicts() {
    GenSettings rest = settings(List.of(struct("corba.m", "Item")), naming("Find", "sRpcRequest"));
    assertMentions(NameConflicts.find(List.of(SERVICE), rest), "FindItemsRpcRequest");

    GenSettings duplicate =
        settings(List.of(struct("corba.a", "Item"), struct("corba.b", "Item")), EMPTY);
    assertMentions(NameConflicts.find(List.of(), duplicate), "corba.a.Item");

    GenSettings javaLang = settings(List.of(struct("corba.m", "Object")), EMPTY);
    assertMentions(NameConflicts.find(List.of(), javaLang), "java.lang");
  }

  @Test
  void rpcNameConflicts() {
    Naming sameName =
        rpcRequestAndResponse(new Naming.Affix("", "Rpc"), new Naming.Affix("", "Rpc"));
    assertMentions(
        NameConflicts.find(List.of(SERVICE), settings(List.of(), sameName)), "FindItemsRpc");

    Naming bare = rpcRequestAndResponse(new Naming.Affix("", ""), Naming.DEFAULT.rpcResponse());
    assertMentions(
        NameConflicts.find(List.of(operationNamed("string")), settings(List.of(), bare)),
        "java.lang");
    assertMentions(
        NameConflicts.find(List.of(operationNamed("anyValue")), settings(List.of(), bare)),
        "AnyValue");

    // 別のサービスの同じ名前の操作は、同じクラスなので重なりとしない
    Service other = new Service("Other", "corba.m", SERVICE.operations());
    assertEquals(
        List.of(),
        NameConflicts.find(List.of(SERVICE, other), settings(List.of(), Naming.DEFAULT)));
  }

  private static Service operationNamed(String name) {
    return new Service(
        "S", "corba.m", List.of(new Operation(name, Optional.empty(), List.of(), List.of())));
  }

  private static Naming rpcRequestAndResponse(Naming.Affix request, Naming.Affix response) {
    return new Naming(
        Naming.DEFAULT.dto(),
        Naming.DEFAULT.rpcParams(),
        request,
        Naming.DEFAULT.rpcResult(),
        response);
  }

  private static Naming naming(String dtoPrefix, String dtoSuffix) {
    return new Naming(
        new Naming.Affix(dtoPrefix, dtoSuffix),
        Naming.DEFAULT.rpcParams(),
        Naming.DEFAULT.rpcRequest(),
        Naming.DEFAULT.rpcResult(),
        Naming.DEFAULT.rpcResponse());
  }

  private static void assertMentions(List<String> problems, String text) {
    assertEquals(1, problems.size(), problems.toString());
    assertTrue(problems.get(0).contains(text), problems.get(0));
  }

  private static Struct struct(String pkg, String name) {
    return new Struct(pkg, name, "IDL:" + name + ":1.0", name, List.of(), false);
  }

  private static GenSettings settings(List<Struct> structs, Naming naming) {
    return new GenSettings("b", "SJIS", Path.of("stubs"), structs, List.<IdlEnum>of(), naming);
  }
}
