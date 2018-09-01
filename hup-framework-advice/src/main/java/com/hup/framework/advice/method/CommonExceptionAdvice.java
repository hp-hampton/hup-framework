package com.hup.framework.advice.method;

import com.hup.framework.advice.ApiResponseBody;
import com.hup.framework.advice.Interceptor.AdviceInterceptor;
import com.hup.framework.support.exception.DefinitionBusinessException;
import com.hup.framework.support.exception.SystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class CommonExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public ApiResponseBody allExceptionHandler(Exception exception) throws Exception {
        if (!AdviceInterceptor.isOpenApi()) {
            throw exception;
        }
        DefinitionBusinessException definitionBusinessException = findDefinitionBusinessException(exception);
        if (definitionBusinessException == null) {
            if (exception instanceof AccessDeniedException) {
                definitionBusinessException =
                        DefinitionBusinessException.sys(SystemException.AUTHORITY_EXCEPTION).cause(exception).build();
            } else {
                definitionBusinessException =
                        DefinitionBusinessException.sys(SystemException.SYSTEM_EXCEPTION).cause(exception).build();
            }
        }
        // log(definitionBusinessException.toString(), definitionBusinessException);
        return ApiResponseBody.builder().success(false).
                code(definitionBusinessException.getCode()).message(definitionBusinessException.getMessage()).build();
    }

    public DefinitionBusinessException findDefinitionBusinessException(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof DefinitionBusinessException) {
                return (DefinitionBusinessException) throwable;
            }
            throwable = throwable.getCause();
        }
        return null;
    }
}
