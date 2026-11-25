package com.generated.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/")
public class ItemServiceController {
  private final com.generated.corba.ItemServiceCorbaClient client;

  public ItemServiceController(com.generated.corba.ItemServiceCorbaClient client) {
    this.client = client;
  }

  @PostMapping("/getItem")
  public com.generated.dto.GetItemRpcResponse getItem(@RequestBody com.generated.dto.GetItemRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.GetItemRpcParams req = rpc.params;
    try {
      int arg0 = req.id;
      com.corba.Coverage.Item callResult = client.getItem(arg0);
      com.generated.dto.GetItemRpcResult __result = new com.generated.dto.GetItemRpcResult();
      __result.returnValue = com.generated.mapper.ItemMapper.toDto(callResult);
      com.generated.dto.GetItemRpcResponse __response = new com.generated.dto.GetItemRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.GetItemRpcResult __result = new com.generated.dto.GetItemRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.GetItemRpcResponse __response = new com.generated.dto.GetItemRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/putItem")
  public com.generated.dto.PutItemRpcResponse putItem(@RequestBody com.generated.dto.PutItemRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.PutItemRpcParams req = rpc.params;
    try {
      com.corba.Coverage.Item arg0 = com.generated.mapper.ItemMapper.fromDto(req.item);
      client.putItem(arg0);
      com.generated.dto.PutItemRpcResult __result = new com.generated.dto.PutItemRpcResult();
      com.generated.dto.PutItemRpcResponse __response = new com.generated.dto.PutItemRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.PutItemRpcResult __result = new com.generated.dto.PutItemRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.PutItemRpcResponse __response = new com.generated.dto.PutItemRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/listTags")
  public com.generated.dto.ListTagsRpcResponse listTags(@RequestBody com.generated.dto.ListTagsRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.ListTagsRpcParams req = rpc.params;
    try {
      String arg0 = com.generated.mapper.StringMapper.decode(req.prefix);
      com.corba.Coverage.Tag[] callResult = client.listTags(arg0);
      com.generated.dto.ListTagsRpcResult __result = new com.generated.dto.ListTagsRpcResult();
      __result.returnValue = callResult == null ? null : java.util.Arrays.stream(callResult).map(com.generated.mapper.TagMapper::toDto).toArray(com.generated.dto.TagDto[]::new);
      com.generated.dto.ListTagsRpcResponse __response = new com.generated.dto.ListTagsRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.ListTagsRpcResult __result = new com.generated.dto.ListTagsRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.ListTagsRpcResponse __response = new com.generated.dto.ListTagsRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/echoNames")
  public com.generated.dto.EchoNamesRpcResponse echoNames(@RequestBody com.generated.dto.EchoNamesRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.EchoNamesRpcParams req = rpc.params;
    try {
      String[] arg0 = req.names == null ? null : java.util.Arrays.stream(req.names).map(com.generated.mapper.StringMapper::decode).toArray(String[]::new);
      String[] callResult = client.echoNames(arg0);
      com.generated.dto.EchoNamesRpcResult __result = new com.generated.dto.EchoNamesRpcResult();
      __result.returnValue = callResult == null ? null : java.util.Arrays.stream(callResult).map(com.generated.mapper.StringMapper::encode).toArray(String[]::new);
      com.generated.dto.EchoNamesRpcResponse __response = new com.generated.dto.EchoNamesRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.EchoNamesRpcResult __result = new com.generated.dto.EchoNamesRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.EchoNamesRpcResponse __response = new com.generated.dto.EchoNamesRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/greet")
  public com.generated.dto.GreetRpcResponse greet(@RequestBody com.generated.dto.GreetRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.GreetRpcParams req = rpc.params;
    try {
      String arg0 = com.generated.mapper.StringMapper.decode(req.name);
      String callResult = client.greet(arg0);
      com.generated.dto.GreetRpcResult __result = new com.generated.dto.GreetRpcResult();
      __result.returnValue = com.generated.mapper.StringMapper.encode(callResult);
      com.generated.dto.GreetRpcResponse __response = new com.generated.dto.GreetRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.GreetRpcResult __result = new com.generated.dto.GreetRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.GreetRpcResponse __response = new com.generated.dto.GreetRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/echoAny")
  public com.generated.dto.EchoAnyRpcResponse echoAny(@RequestBody com.generated.dto.EchoAnyRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.EchoAnyRpcParams req = rpc.params;
    try {
      org.omg.CORBA.Any arg0 = com.generated.mapper.AnyMapper.toAny(req.value);
      org.omg.CORBA.Any callResult = client.echoAny(arg0);
      com.generated.dto.EchoAnyRpcResult __result = new com.generated.dto.EchoAnyRpcResult();
      __result.returnValue = com.generated.mapper.AnyMapper.toAnyValue(callResult);
      com.generated.dto.EchoAnyRpcResponse __response = new com.generated.dto.EchoAnyRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.EchoAnyRpcResult __result = new com.generated.dto.EchoAnyRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.EchoAnyRpcResponse __response = new com.generated.dto.EchoAnyRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/findItems")
  public com.generated.dto.FindItemsRpcResponse findItems(@RequestBody com.generated.dto.FindItemsRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.FindItemsRpcParams req = rpc.params;
    try {
      String arg0 = com.generated.mapper.StringMapper.decode(req.keyword);
      com.corba.Coverage.TagSeqHolder arg1 = new com.corba.Coverage.TagSeqHolder();
      org.omg.CORBA.StringHolder arg2 = new org.omg.CORBA.StringHolder();
      int callResult = client.findItems(arg0, arg1, arg2);
      com.generated.dto.FindItemsRpcResult __result = new com.generated.dto.FindItemsRpcResult();
      __result.returnValue = callResult;
      __result.tags = arg1.value == null ? null : java.util.Arrays.stream(arg1.value).map(com.generated.mapper.TagMapper::toDto).toArray(com.generated.dto.TagDto[]::new);
      __result.message = com.generated.mapper.StringMapper.encode(arg2.value);
      com.generated.dto.FindItemsRpcResponse __response = new com.generated.dto.FindItemsRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.FindItemsRpcResult __result = new com.generated.dto.FindItemsRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.FindItemsRpcResponse __response = new com.generated.dto.FindItemsRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/touch")
  public com.generated.dto.TouchRpcResponse touch(@RequestBody com.generated.dto.TouchRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.TouchRpcParams req = rpc.params;
    try {
      org.omg.CORBA.IntHolder arg0 = new org.omg.CORBA.IntHolder(req.counter);
      client.touch(arg0);
      com.generated.dto.TouchRpcResult __result = new com.generated.dto.TouchRpcResult();
      __result.counter = arg0.value;
      com.generated.dto.TouchRpcResponse __response = new com.generated.dto.TouchRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.TouchRpcResult __result = new com.generated.dto.TouchRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.TouchRpcResponse __response = new com.generated.dto.TouchRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/relabel")
  public com.generated.dto.RelabelRpcResponse relabel(@RequestBody com.generated.dto.RelabelRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.RelabelRpcParams req = rpc.params;
    try {
      if (req.label == null) {
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.BAD_REQUEST, "params.label is required (inout parameter)");
      }
      org.omg.CORBA.StringHolder arg0 = new org.omg.CORBA.StringHolder(com.generated.mapper.StringMapper.decode(req.label));
      if (req.p == null) {
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.BAD_REQUEST, "params.p is required (inout parameter)");
      }
      com.corba.Coverage.Inner.PointHolder arg1 = new com.corba.Coverage.Inner.PointHolder(com.generated.mapper.PointMapper.fromDto(req.p));
      client.relabel(arg0, arg1);
      com.generated.dto.RelabelRpcResult __result = new com.generated.dto.RelabelRpcResult();
      __result.label = com.generated.mapper.StringMapper.encode(arg0.value);
      __result.p = com.generated.mapper.PointMapper.toDto(arg1.value);
      com.generated.dto.RelabelRpcResponse __response = new com.generated.dto.RelabelRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.RelabelRpcResult __result = new com.generated.dto.RelabelRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.RelabelRpcResponse __response = new com.generated.dto.RelabelRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/ping")
  public com.generated.dto.PingRpcResponse ping(@RequestBody com.generated.dto.PingRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.PingRpcParams req = rpc.params;
    try {
      boolean callResult = client.ping();
      com.generated.dto.PingRpcResult __result = new com.generated.dto.PingRpcResult();
      __result.returnValue = callResult;
      com.generated.dto.PingRpcResponse __response = new com.generated.dto.PingRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.PingRpcResult __result = new com.generated.dto.PingRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.PingRpcResponse __response = new com.generated.dto.PingRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/globalNames")
  public com.generated.dto.GlobalNamesRpcResponse globalNames(@RequestBody com.generated.dto.GlobalNamesRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.GlobalNamesRpcParams req = rpc.params;
    try {
      String[] arg0 = req.names == null ? null : java.util.Arrays.stream(req.names).map(com.generated.mapper.StringMapper::decode).toArray(String[]::new);
      String[] callResult = client.globalNames(arg0);
      com.generated.dto.GlobalNamesRpcResult __result = new com.generated.dto.GlobalNamesRpcResult();
      __result.returnValue = callResult == null ? null : java.util.Arrays.stream(callResult).map(com.generated.mapper.StringMapper::encode).toArray(String[]::new);
      com.generated.dto.GlobalNamesRpcResponse __response = new com.generated.dto.GlobalNamesRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.GlobalNamesRpcResult __result = new com.generated.dto.GlobalNamesRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.GlobalNamesRpcResponse __response = new com.generated.dto.GlobalNamesRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/movePoint")
  public com.generated.dto.MovePointRpcResponse movePoint(@RequestBody com.generated.dto.MovePointRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.MovePointRpcParams req = rpc.params;
    try {
      com.corba.Coverage.Inner.Point arg0 = com.generated.mapper.PointMapper.fromDto(req.p);
      com.corba.Coverage.Inner.PointHolder arg1 = new com.corba.Coverage.Inner.PointHolder();
      com.corba.Coverage.Inner.Point callResult = client.movePoint(arg0, arg1);
      com.generated.dto.MovePointRpcResult __result = new com.generated.dto.MovePointRpcResult();
      __result.returnValue = com.generated.mapper.PointMapper.toDto(callResult);
      __result.previous = com.generated.mapper.PointMapper.toDto(arg1.value);
      com.generated.dto.MovePointRpcResponse __response = new com.generated.dto.MovePointRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.MovePointRpcResult __result = new com.generated.dto.MovePointRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.MovePointRpcResponse __response = new com.generated.dto.MovePointRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/outAny")
  public com.generated.dto.OutAnyRpcResponse outAny(@RequestBody com.generated.dto.OutAnyRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.OutAnyRpcParams req = rpc.params;
    try {
      org.omg.CORBA.AnyHolder arg0 = new org.omg.CORBA.AnyHolder();
      client.outAny(arg0);
      com.generated.dto.OutAnyRpcResult __result = new com.generated.dto.OutAnyRpcResult();
      __result.value = com.generated.mapper.AnyMapper.toAnyValue(arg0.value);
      com.generated.dto.OutAnyRpcResponse __response = new com.generated.dto.OutAnyRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.OutAnyRpcResult __result = new com.generated.dto.OutAnyRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.OutAnyRpcResponse __response = new com.generated.dto.OutAnyRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/setStatus")
  public com.generated.dto.SetStatusRpcResponse setStatus(@RequestBody com.generated.dto.SetStatusRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.SetStatusRpcParams req = rpc.params;
    try {
      int arg0 = req.id;
      com.corba.Coverage.Status arg1 = com.generated.mapper.StatusMapper.fromDto(req.status);
      com.corba.Coverage.StatusSeqHolder arg2 = new com.corba.Coverage.StatusSeqHolder();
      com.corba.Coverage.Status callResult = client.setStatus(arg0, arg1, arg2);
      com.generated.dto.SetStatusRpcResult __result = new com.generated.dto.SetStatusRpcResult();
      __result.returnValue = com.generated.mapper.StatusMapper.toDto(callResult);
      __result.history = arg2.value == null ? null : java.util.Arrays.stream(arg2.value).map(com.generated.mapper.StatusMapper::toDto).toArray(com.generated.dto.StatusDto[]::new);
      com.generated.dto.SetStatusRpcResponse __response = new com.generated.dto.SetStatusRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.SetStatusRpcResult __result = new com.generated.dto.SetStatusRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.SetStatusRpcResponse __response = new com.generated.dto.SetStatusRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/cycleStatus")
  public com.generated.dto.CycleStatusRpcResponse cycleStatus(@RequestBody com.generated.dto.CycleStatusRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.CycleStatusRpcParams req = rpc.params;
    try {
      if (req.status == null) {
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.BAD_REQUEST, "params.status is required (inout parameter)");
      }
      com.corba.Coverage.StatusHolder arg0 = new com.corba.Coverage.StatusHolder(com.generated.mapper.StatusMapper.fromDto(req.status));
      client.cycleStatus(arg0);
      com.generated.dto.CycleStatusRpcResult __result = new com.generated.dto.CycleStatusRpcResult();
      __result.status = com.generated.mapper.StatusMapper.toDto(arg0.value);
      com.generated.dto.CycleStatusRpcResponse __response = new com.generated.dto.CycleStatusRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.CycleStatusRpcResult __result = new com.generated.dto.CycleStatusRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.CycleStatusRpcResponse __response = new com.generated.dto.CycleStatusRpcResponse();
      __response.result = __result;
      return __response;
    }
  }


}
