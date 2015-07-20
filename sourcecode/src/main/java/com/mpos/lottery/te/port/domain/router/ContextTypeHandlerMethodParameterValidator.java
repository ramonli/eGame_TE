package com.mpos.lottery.te.port.domain.router;

import com.mpos.lottery.common.router.HandlerMethodParameterValidator;
import com.mpos.lottery.te.port.Context;

import java.lang.reflect.Method;

/**
 * A routine method validator. a controler method must satisfy:
 * <ol>
 * <li>Supports only 2 parameters of type <code>Context</code>.</li>
 * <li>No return value allowed, only supports <code>void</code>.</li>
 * </ol>
 */
public class ContextTypeHandlerMethodParameterValidator implements HandlerMethodParameterValidator {

    @Override
    public void verify(Method handlerMethod) {
        Class<?>[] params = handlerMethod.getParameterTypes();
        if (params.length != 2) {
            throw new IllegalArgumentException("Controller method can only accept 2 parameters(" + Context.class + ","
                    + Context.class + "), while method(" + handlerMethod + ") required " + params.length
                    + " parameters.");
        }
        for (Class<?> param : params) {
            if (!Context.class.isAssignableFrom(param)) {
                throw new IllegalArgumentException("Controller method can only accept 2 parameters(" + Context.class
                        + "," + Context.class + "), while method(" + handlerMethod + ") required a parameter of type("
                        + param + ").");
            }
        }

        // only supports void
        if (!void.class.equals(handlerMethod.getReturnType())) {
            throw new IllegalStateException("No return value allowed to Controller method ");
        }
    }

}
