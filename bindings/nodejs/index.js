const ffi = require('ffi-napi');
const path = require('path');

// ಲೋಕಲ್ ಅಥವಾ ಪ್ರೊಡಕ್ಷನ್ ಬೈನರಿ ಪಾತ್ ಸೆಟ್ ಮಾಡುವುದು
const libPath = path.join(__dirname, '../../target/release/mcrypt.dll');

const lib = ffi.Library(libPath, {
    'mcrypt_gensalt': ['string', ['uint32', 'size_t']],
    'mcrypt_hash_with_salt': ['string', ['string', 'string']],
    'mcrypt_verify': ['bool', ['string', 'string']]
});

module.exports = {
    gensalt: (rounds, len) => lib.mcrypt_gensalt(rounds, len),
    hashWithSalt: (pass, salt) => lib.mcrypt_hash_with_salt(pass, salt),
    verify: (pass, hash) => lib.mcrypt_verify(pass, hash)
};