# RxRNBridge

RxJava Observable bridge for React Native

## Introduction

React Native supports callbacks or promise from native java code.

RxRNBridge as a helper further supports RxJava observables and user don't need to do type convert handy.

RxRNBridge is good for Android legacy code and leverage original RxJava ecosystem's powerful and start new UI view by React Native.

## Usage

* `build.gradle`

```gradle
...
apply plugin: 'com.neenbedankt.android-apt'

repositories {
    jcenter()
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
    	...
    	classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

compile 'com.github.kudo:rxrnbridge:0.0.1'
apt 'com.github.kudo.rxrnbridge-compiler:0.0.1'
    
```

* `YourNativeModule.java`

You can now simply return Observable from methods and simply use `@ReactMethodObservable` annotation.

```java
@ReactMethodObservable
public Observable<String> foo() { ... }
```

* `YourNativePackage.java`

New a module instance by `RxRNBridge.newInstance(YourNativeModule.class, reactContext);`

E.g.

```java
List<NativeModule> modules = new ArrayList<>();

NativeModule module = RxRNBridge.newInstance(RNSampleModule.class, reactContext);
modules.add(module);
...
return modules;

```

* `YourModule.java`

Currently NativeModule methods will append two callbacks (e.g. `foo(errorCallback, successCallback)`)
You could encapsulate callback as Promise

```javascript
var SampleModule = {
  foo: function(): Promise {
    return new Promise((resolve, reject) => {
      RNSampleModule.foo(
        (error) => {
          reject(error);
        }, (val) => {
          resolve(val);
        });
    });
  }
};
```

## TODO
* Add natived supported Promise after RN release [this feature](https://github.com/facebook/react-native/commit/b86a6e3b44a63e92cf3a7976d2fa26c4bf412df1)

## Special thanks
* [@ChenNevin](https://twitter.com/ChenNevin) for share me with Android development and Java annotation processing
* [ButterKnife](https://github.com/JakeWharton/butterknife) for structure and sample for annotation processor

## License

	
	Copyright (c) 2015, Kudo Chien
	All rights reserved.
	
	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:
	
	* Redistributions of source code must retain the above copyright notice, this
	  list of conditions and the following disclaimer.
	
	* Redistributions in binary form must reproduce the above copyright notice,
	  this list of conditions and the following disclaimer in the documentation
	  and/or other materials provided with the distribution.
	
	* Neither the name of RxRNBridge nor the names of its
	  contributors may be used to endorse or promote products derived from
	  this software without specific prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
	DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
	CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
	OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
