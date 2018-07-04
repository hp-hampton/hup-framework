package com.hup.framework.jsonrpc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Json Rpc 响应
 *
 * @version 1.0
 */
@Data
public class JsonRpcResponse extends JsonRpc {

    private Object result;

    private Error error;

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    public static class Error {
        private boolean ppsException;
        @NonNull
        private String code;
        @NonNull
        private String message;

    }

}
