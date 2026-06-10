import mcrypt
import os


print("--- mcrypt (Mass Crypto) Rust Native Test ---")


salt = mcrypt
# 1. ಹ್ಯಾಶ್ ಟೆಸ್ಟ್
password = "Gadag@2026"
hashed = mcrypt.hash(password, rounds=12)
print(f"Generated Hash:\n{hashed}\n")

# 2. ವೆರಿಫಿಕೇಶನ್ ಟೆಸ್ಟ್
is_correct = mcrypt.verify(password, hashed)
print(f"Is Password Valid?: {is_correct}") # True ಬರಬೇಕು

# 3. ತಪ್ಪು ಪಾಸ್‌ವರ್ಡ್ ಟೆಸ್ಟ್
is_wrong = mcrypt.verify("WrongPass123", hashed)
print(f"Is Wrong Password Valid?: {is_wrong}") # False ಬರಬೇಕು