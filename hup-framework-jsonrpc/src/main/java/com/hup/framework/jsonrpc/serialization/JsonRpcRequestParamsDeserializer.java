package com.hup.framework.jsonrpc.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.hup.framework.jsonrpc.JsonRpcContext;
import org.springframework.core.ResolvableType;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Json Rpc 请求参数反序列化
 *
 * @version 1.0
 */
public class JsonRpcRequestParamsDeserializer extends JsonDeserializer<Object[]> implements ContextualDeserializer {

    @Override
    public Object[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if(p.currentToken()==JsonToken.START_ARRAY) {
            // start process array
            p.nextToken();
        }
        //
        Method method = JsonRpcContext.get().getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for(int parameterIndex=0; parameterIndex<parameterTypes.length; parameterIndex++) {
            ResolvableType resolvableType  = ResolvableType.forMethodParameter(method, parameterIndex);
            JavaType parameterJavaType = ctxt.constructType(resolvableType.getType());
            //
            Object argument = p.getCodec().readValue(p, parameterJavaType);
            arguments[parameterIndex] = argument;
        }
        //
        if(p.currentToken()==null && p.nextToken()==JsonToken.END_ARRAY) {
            // end process array
        }
        return arguments;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        if("params".equals(property.getName())) {
            return new JsonRpcRequestParamsDeserializer();
        }
        //
        throw new UnsupportedOperationException();
    }

}
