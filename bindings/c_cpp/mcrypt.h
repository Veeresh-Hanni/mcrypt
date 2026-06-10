#ifndef MCRYPT_H
#define MCRYPT_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// Rust ನಿಂದ ಬರುವ ನೇರ ಫಂಕ್ಷನ್ಸ್
const char* mcrypt_gensalt(unsigned int rounds, unsigned long salt_length);
const char* mcrypt_hash_with_salt(const char* password, const char* salt_prefix);
bool mcrypt_verify(const char* password, const char* stored_hash);
void mcrypt_free_string(char* s);

#ifdef __cplusplus
}
#endif

#endif