/**
 * Patch react-native-reanimated CMakeLists.txt to remove -Werror
 * which causes build failures with NDK 27+ due to stricter warnings.
 */
const fs = require('fs');
const path = require('path');

const cmakePath = path.join(
  __dirname,
  '..',
  'node_modules',
  'react-native-reanimated',
  'android',
  'CMakeLists.txt',
);

if (fs.existsSync(cmakePath)) {
  let content = fs.readFileSync(cmakePath, 'utf8');
  if (content.includes('-Wall -Werror')) {
    content = content.replace('-Wall -Werror', '-Wall -Wno-error');
    fs.writeFileSync(cmakePath, content, 'utf8');
    console.log('[patch] Removed -Werror from react-native-reanimated CMakeLists.txt');
  }
}
