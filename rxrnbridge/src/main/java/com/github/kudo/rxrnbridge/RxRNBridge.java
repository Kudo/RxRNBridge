package com.github.kudo.rxrnbridge;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.lang.reflect.InvocationTargetException;

public class RxRNBridge {
    private static final String TAG = "RxRNBridge";
    private static final String GEN_CLASS_SUFFIX = "$$RxBridge";

    @NonNull
    public static NativeModule newInstance(Class<? extends ReactContextBaseJavaModule> reactModuleClass, ReactApplicationContext reactContext)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String clsName = reactModuleClass.getName();
        String injectClsName = clsName + GEN_CLASS_SUFFIX;
        Class <? extends ReactContextBaseJavaModule> injectCls = null;
        try {
            injectCls = Class.forName(injectClsName).asSubclass(ReactContextBaseJavaModule.class);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Unable to find injectClass - " + injectClsName);
        }

        return (NativeModule) (injectCls != null
                ? injectCls.getDeclaredConstructor(ReactApplicationContext.class).newInstance(reactContext)
                : reactModuleClass.getDeclaredConstructor(ReactApplicationContext.class).newInstance(reactContext));
    }
}
