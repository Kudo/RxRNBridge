
package com.github.kudo.rxrnbridge.sample;

import android.annotation.SuppressLint;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.github.kudo.rxrnbridge.annotations.ReactMethodObservable;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.functions.Func1;


public class RNSampleModule extends ReactContextBaseJavaModule {
    public static final String TAG = "RNSampleModule";
    private GitHubService mGitHubService;

    public class Release {
        public String tagName;
        public String htmlUrl;
    }

    public interface GitHubService {
        @GET("/repos/{owner}/{repo}/releases")
        Observable<List<Release>> listReleases(
                @Path("owner") String owner,
                @Path("repo") String repo);
    }

    public RNSampleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setConverter(new GsonConverter(gson))
                .build();
        mGitHubService = restAdapter.create(GitHubService.class);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethodObservable
    public Observable<WritableArray> listGitHubReleases(final String owner, final String repo) {
        return mGitHubService.listReleases(owner, repo)
                .flatMap(new Func1<List<Release>, Observable<WritableArray>>() {
                    @Override
                    public Observable<WritableArray> call(List<Release> releases) {
                        WritableArray array = Arguments.createArray();
                        for (Release release : releases) {
                            WritableMap map = Arguments.createMap();
                            map.putString("tagName", release.tagName);
                            map.putString("htmlUrl", release.htmlUrl);
                            array.pushMap(map);
                        }
                        return Observable.just(array);
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    @ReactMethodObservable
    public Observable<String> foo() {
        final int timerSeconds = 3;
        return Observable.timer(timerSeconds, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<String>>() {
                    @Override
                    public Observable<String> call(Long aLong) {
                        return Observable.just(String.format("timer DONE for %d seconds", timerSeconds));
                    }
                });
    }
}
