/* eslint-env node */
const { configure } = require('quasar/wrappers')

module.exports = configure(function (/* ctx */) {
  return {
    boot: [],

    css: ['app.scss'],

    extras: ['roboto-font', 'material-icons'],

    build: {
      target: {
        browser: ['es2019', 'edge88', 'firefox78', 'chrome87', 'safari13.1'],
        node: 'node20',
      },
      vueRouterMode: 'hash',
      typescript: {
        strict: true,
        vueShim: true,
      },
    },

    devServer: {
      open: false,
      port: 9000,
      proxy: {
        '/api': 'http://localhost:8080',
      },
    },

    framework: {
      config: {},
      iconSet: 'material-icons',
      plugins: ['Notify', 'Loading', 'Dialog'],
    },

    animations: [],
  }
})
