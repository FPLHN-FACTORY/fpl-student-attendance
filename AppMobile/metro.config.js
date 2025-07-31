const { getDefaultConfig } = require('expo/metro-config')
const path = require('path')

const config = getDefaultConfig(__dirname)

config.transformer.babelTransformerPath = require.resolve('react-native-svg-transformer')

config.resolver.assetExts = config.resolver.assetExts.filter((ext) => ext !== 'svg')
config.resolver.sourceExts.push('svg')

config.resolver.assetExts = [...config.resolver.assetExts, 'bin', 'txt', 'json']

config.resolver.extraNodeModules = {
  ...config.resolver.extraNodeModules,
  'ramda/src/omit': path.resolve(__dirname, 'node_modules/ramda/es/omit.js'),
}
module.exports = config
