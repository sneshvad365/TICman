/* eslint-env node */
const { resolve } = require('path')

module.exports = {
  parserOptions: {
    extraFileExtensions: ['.vue'],
    parser: require.resolve('@typescript-eslint/parser'),
    project: resolve(__dirname, './tsconfig.json'),
    tsconfigRootDir: __dirname,
  },

  env: {
    browser: true,
    es2021: true,
    node: true,
    'vue/setup-compiler-macros': true,
  },

  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
  ],

  plugins: ['vue'],

  globals: {
    ga: 'readonly',
    cordova: 'readonly',
    __statics: 'readonly',
    __QUASAR_SSR__: 'readonly',
    __QUASAR_SSR_SERVER__: 'readonly',
    __QUASAR_SSR_CLIENT__: 'readonly',
    __QUASAR_SSR_PWA__: 'readonly',
    process: 'readonly',
    Capacitor: 'readonly',
    chrome: 'readonly',
  },

  rules: {
    'prefer-promise-reject-errors': 'off',
    quotes: ['warn', 'single', { avoidEscape: true }],
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/no-var-requires': 'off',
  },
}
