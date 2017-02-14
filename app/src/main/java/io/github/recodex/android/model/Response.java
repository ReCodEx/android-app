package io.github.recodex.android.model;


public class Response<T> {
    private int code;
    private boolean status;
    private T payload;

    public T getPayload() {
        return payload;
    }

    public int getCode() {
        return code;
    }

    public boolean isStatus() {
        return status;
    }
}
