package com.ujiuye.entity;

import java.io.Serializable;

public class Result implements Serializable {
    private boolean falg;
    private String msg;

    public Result() {
    }

    public Result(boolean falg, String msg) {
        this.falg = falg;
        this.msg = msg;
    }

    public boolean isFalg() {
        return falg;
    }

    public void setFalg(boolean falg) {
        this.falg = falg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
