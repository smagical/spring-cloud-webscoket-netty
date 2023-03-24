package com.ssk.zsaltedfish.netty.webscoket.exception;

import lombok.Builder;
import lombok.Data;

@Builder
@Data

public class WebScoketExcpetion extends Exception {
    private Integer code;
    private String msg;

    public WebScoketExcpetion(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
