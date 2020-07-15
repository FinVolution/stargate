package com.ppdai.stargate.job;

public interface IHandler<T> {

    String getName();

    void execute(T t) throws Exception;

    void onSuccess(T t);

    void onFail(T t);

    void onExpire(T t);

    void onInterrupt(T t);
}
