package com.github.kudo.rxrnbridge.internal;

import com.facebook.react.bridge.Callback;
// import com.facebook.react.bridge.Promise;

import rx.Observable;
import rx.functions.Action1;

public class RxRNBridgeInternal {
    /*
    @SuppressWarnings("unchecked")
    public static void rxRNBridgePromise(final Observable observable, final Promise promise) {
        observable.subscribe(
                new Action1<Object>() {
                    @Override
                    public void call(Object object) {
                        promise.resolve(object);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        promise.reject(throwable.getMessage());
                    }
                });
    }
    */

    @SuppressWarnings("unchecked")
    public static void rxRNBridgeCallbacks(final Observable observable, final Callback errorCallback, final Callback successCallback) {
        observable.subscribe(
                new Action1<Object>() {
                    @Override
                    public void call(Object object) {
                        successCallback.invoke(object);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorCallback.invoke(throwable.getMessage());
                    }
                });
    }
}
