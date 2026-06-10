const fs = require('fs');
const path = require('path');

// Create lib directory if it doesn't exist
const libDir = path.join(__dirname, '../lib');
if (!fs.existsSync(libDir)) {
    fs.mkdirSync(libDir, { recursive: true });
}

// Copy platform-specific binaries from target/release
const sourceDir = path.join(__dirname, '../../../target/release');
const platformBinaries = {
    'win32': 'mcrypt_native.dll',
    'darwin': 'libmcrypt_native.dylib',
    'linux': 'libmcrypt_native.so'
};

// Copy all platform binaries if they exist (for CI/CD cross-platform builds)
Object.values(platformBinaries).forEach(binary => {
    const source = path.join(sourceDir, binary);
    const dest = path.join(libDir, binary);
    
    if (fs.existsSync(source)) {
        fs.copyFileSync(source, dest);
        console.log(`✓ Copied ${binary}`);
    }
});

console.log('Build complete: Binaries packaged for distribution');
