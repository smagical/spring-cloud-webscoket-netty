package com.ssk.zsaltedfish.netty.webscoket.pojo;

import com.ssk.zsaltedfish.netty.webscoket.support.methodparamreslove.WebSocketMethodParamReslove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.HashMap;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AnnatationMethodParam {

    private Method method;
    private HashMap<MethodParameter, WebSocketMethodParamReslove> methodParameterAndReslove;

}
