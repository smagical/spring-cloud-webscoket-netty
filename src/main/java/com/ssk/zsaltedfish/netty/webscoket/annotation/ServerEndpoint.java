package com.ssk.zsaltedfish.netty.webscoket.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerEndpoint {

    /**
     * 路径采用antmatch 参考{@link org.springframework.util.AntPathMatcher}
     *
     * @return
     */
    public String[] path() default {"/"};
}
