"use strict";

var RNSampleModule = require("NativeModules").RNSampleModule;
console.log('RNSampleModule', RNSampleModule);

var SampleModule = {
  foo() {
    return RNSampleModule.foo();
  },
  listRNReleases() {
    return RNSampleModule.listGitHubReleases("facebook", "react-native");
  }
};

module.exports = SampleModule;
