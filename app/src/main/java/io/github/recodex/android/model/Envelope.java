package io.github.recodex.android.model;


public class Envelope<T> {
    private int code;
    private boolean success;
    private T payload;

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

    public int getCode() {
        return code;
    }
}
