module.exports = function(config) {
  config.set({
    frameworks: ['jasmine', 'sinon'],
    browsers: ['Chrome'],
    singleRun: true,

    files: [
      // only specify one entry point
      // and require all tests in there
      'js/spec/spec_index.js'
    ],

    preprocessors: {
      // add webpack as preprocessor
      'js/spec/spec_index.js': ['webpack', 'sourcemap']
    },

    webpack: {
      devtool: 'inline-source-map'

      // karma watches the test entry points
      // (you don't need to specify the entry option)
      // webpack watches dependencies

      // webpack configuration
    },

    webpackMiddleware: {
      // webpack-dev-middleware configuration
      // i. e.
      stats: 'errors-only'
    }
  });
};