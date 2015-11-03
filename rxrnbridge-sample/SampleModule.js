"use strict";

var RNSampleModule = require("NativeModules").RNSampleModule;

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
  },
  listRNReleases: function(): Promise {
    return new Promise((resolve, reject) => {
      RNSampleModule.listGitHubReleases("facebook", "react-native",
        (error) => {
          reject(error);
        }, (releases) => {
          resolve(releases);
        });
    });
  }
};

module.exports = SampleModule;
