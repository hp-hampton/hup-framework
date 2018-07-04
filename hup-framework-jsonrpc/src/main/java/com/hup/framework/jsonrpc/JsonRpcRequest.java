package com.hup.framework.jsonrpc;

import lombok.Data;

/**
 * Json Rpc 请求
 *
 * @version 1.0
 */
@Data
public class JsonRpcRequest extends JsonRpc {

    private String method;

    private Object[] params;

}
