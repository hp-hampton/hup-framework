package com.hup.framework.support.exception;

public abstract class DefinitionBusinessException extends RuntimeException {

    private String code;

    public String getCode() {
        return code;
    }

    public DefinitionBusinessException(String code) {
        this(code, null, null);
    }

    public DefinitionBusinessException(String code, String message) {
        this(code, message, null);
    }

    public DefinitionBusinessException(String code, Throwable throwable) {
        this(code, null, throwable);
    }

    public DefinitionBusinessException(String code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public static DefinitionBusinessExceptionBuild biz(String code) {
        return new DefinitionBusinessExceptionBuild("bus", code);
    }

    public static DefinitionBusinessExceptionBuild sys(String code) {
        return new DefinitionBusinessExceptionBuild("sys", code);
    }

    public static DefinitionBusinessExceptionBuild biz(ExceptionDefinition definition) {
        return new DefinitionBusinessExceptionBuild("bus", definition);
    }

    public static DefinitionBusinessExceptionBuild sys(ExceptionDefinition definition) {
        return new DefinitionBusinessExceptionBuild("sys", definition);
    }


    public static class DefinitionBusinessExceptionBuild {

        private String type;

        private String code;

        private String message;

        private Throwable cause;

        public DefinitionBusinessExceptionBuild(String type, String code) {
            this.type = type;
            this.code = code;
        }

        public DefinitionBusinessExceptionBuild(String type, ExceptionDefinition exceptionDefinition) {
            this.type = type;
            this.code = exceptionDefinition.getCode();
            this.message = exceptionDefinition.getMessage();
        }

        public DefinitionBusinessExceptionBuild message(String message, Object... args) {
            this.message = String.format(message, args);
            return this;
        }

        public DefinitionBusinessExceptionBuild cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public void make() {
            throw build();
        }


        public DefinitionBusinessException build() {

            if ("sys".equalsIgnoreCase(type)) {
                return new SystemExceptionImpl(code, message, cause);
            }
            if ("bus".equalsIgnoreCase(type)) {
                return new BusExceptionImpl(code, message, cause);
            }
            throw new IllegalArgumentException(String.format("Exception type %s not supported yet!", type));
        }

    }
}
