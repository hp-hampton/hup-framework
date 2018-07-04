package com.hup.framework.support.exception;

public class SystemExceptionImpl extends DefinitionBusinessException {
    public SystemExceptionImpl(String code) {
        super(code);
    }

    public SystemExceptionImpl(String code, String message) {
        super(code, message);
    }

    public SystemExceptionImpl(String code, Throwable throwable) {
        super(code, throwable);
    }

    public SystemExceptionImpl(String code, String message, Throwable throwable) {
        super(code, message, throwable);
    }
}
