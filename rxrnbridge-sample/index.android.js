/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
"use strict";

var React = require("react-native");
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
} = React;

var SampleModule = require("./SampleModule");

var RxRNBridgeSample = React.createClass({
  getInitialState: function() {
    return {
      foo: "wait",
      rnReleases: [],
    };
  },
  componentDidMount: function() {
    var self = this;
    SampleModule.foo().then(function(value) {
      self.setState(Object.assign({}, {foo: value}));
    });
    SampleModule.listRNReleases().then(function(rnReleases) {
      console.log('rnReleases', rnReleases)
      self.setState(Object.assign({}, {rnReleases: rnReleases}));
    });
  },
  render: function() {
    var rnReleaseView
    if (this.state.rnReleases.length > 0) {
      rnReleaseView = this.state.rnReleases.map(rnRelease =>
        <Text key={rnRelease.tagName}>
          {rnRelease.tagName}
        </Text>
      )
    } else {
      rnReleaseView = <Text>Fetching...</Text>
    }

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
        <Text>
          {this.state.foo}
        </Text>
        <Text>React Native Releses</Text>
        {rnReleaseView}

      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('RxRNBridgeSample', () => RxRNBridgeSample);
