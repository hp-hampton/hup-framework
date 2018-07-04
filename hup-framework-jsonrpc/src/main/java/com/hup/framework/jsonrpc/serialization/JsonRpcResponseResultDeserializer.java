package com.hup.framework.jsonrpc.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.hup.framework.jsonrpc.JsonRpcContext;
import org.springframework.core.ResolvableType;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Json Rpc 响应结果反序列化
 *
 * @author kongbei
 * @version 1.0
 */
public class JsonRpcResponseResultDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Method method = JsonRpcContext.get().getMethod();
        //
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method);
        JavaType resultJavaType = ctxt.constructType(resolvableType.getType());
        //
        return p.getCodec().readValue(p, resultJavaType);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        if ("result".equals(property.getName())) {
            return new JsonRpcResponseResultDeserializer();
        }
        //
        throw new UnsupportedOperationException();
    }

}
