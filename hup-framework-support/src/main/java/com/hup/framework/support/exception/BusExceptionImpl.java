package com.hup.framework.support.exception;

public class BusExceptionImpl extends DefinitionBusinessException {

    public BusExceptionImpl(String code) {
        super(code);
    }

    public BusExceptionImpl(String code, String message) {
        super(code, message);
    }

    public BusExceptionImpl(String code, Throwable throwable) {
        super(code, throwable);
    }

    public BusExceptionImpl(String code, String message, Throwable throwable) {
        super(code, message, throwable);
    }
}
