package com.generated.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/")
public class ClockServiceController {
  private final com.generated.corba.ClockServiceCorbaClient client;

  public ClockServiceController(com.generated.corba.ClockServiceCorbaClient client) {
    this.client = client;
  }

  @PostMapping("/now")
  public com.generated.dto.NowRpcResponse now(@RequestBody com.generated.dto.NowRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.NowRpcParams req = rpc.params;
    try {
      long callResult = client.now();
      com.generated.dto.NowRpcResult __result = new com.generated.dto.NowRpcResult();
      __result.returnValue = callResult;
      com.generated.dto.NowRpcResponse __response = new com.generated.dto.NowRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.NowRpcResult __result = new com.generated.dto.NowRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.NowRpcResponse __response = new com.generated.dto.NowRpcResponse();
      __response.result = __result;
      return __response;
    }
  }

  @PostMapping("/zone")
  public com.generated.dto.ZoneRpcResponse zone(@RequestBody com.generated.dto.ZoneRpcRequest rpc) {
    if (rpc.params == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, "params is required");
    }
    com.generated.dto.ZoneRpcParams req = rpc.params;
    try {
      com.corba.Coverage.Tag arg0 = com.generated.mapper.TagMapper.fromDto(req.hint);
      String callResult = client.zone(arg0);
      com.generated.dto.ZoneRpcResult __result = new com.generated.dto.ZoneRpcResult();
      __result.returnValue = com.generated.mapper.StringMapper.encode(callResult);
      com.generated.dto.ZoneRpcResponse __response = new com.generated.dto.ZoneRpcResponse();
      __response.result = __result;
      return __response;
    } catch (org.omg.CORBA.SystemException __e) {
      com.generated.dto.ZoneRpcResult __result = new com.generated.dto.ZoneRpcResult();
      __result.systemException = com.generated.mapper.SystemExceptionMapper.toDto(__e);
      com.generated.dto.ZoneRpcResponse __response = new com.generated.dto.ZoneRpcResponse();
      __response.result = __result;
      return __response;
    }
  }


}
