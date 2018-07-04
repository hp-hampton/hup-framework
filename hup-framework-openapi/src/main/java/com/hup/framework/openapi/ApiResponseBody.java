package com.hup.framework.openapi;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ApiResponseBody<T> implements Serializable {
    private Boolean success;
    private String code;
    private String message;
    private T data;
}
