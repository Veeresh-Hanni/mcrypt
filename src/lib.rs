use pyo3::prelude::*;
use sha2::{Sha256, Digest};
use rand::Rng;
use std::env;
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

// Constant-time comparison ಫಂಕ್ಷನ್ (Timing Attack ತಡೆಯಲು)
fn constant_time_compare(a: &str, b: &str) -> bool {
    if a.len() != b.len() {
        return false;
    }
    let mut result = 0;
    for (byte_a, byte_b) in a.as_bytes().iter().zip(b.as_bytes().iter()) {
        result |= byte_a ^ byte_b;
    }
    result == 0
}

#[pyfunction]
#[pyo3(signature = (password, rounds=12, salt_length=16, custom_pepper=None))]
fn hash(
    password: &str, 
    rounds: u32, 
    salt_length: usize, 
    custom_pepper: Option<&str>
) -> PyResult<String> {
    // SECURITY GUARD: ಇನ್‌ಪುಟ್ ವ್ಯಾಲಿಡೇಶನ್
    if !(4..=31).contains(&rounds) {
        return Err(pyo3::exceptions::PyValueError::new_err("Rounds must be between 4 and 31"));
    }
    if password.is_empty() {
        return Err(pyo3::exceptions::PyValueError::new_err("Password cannot be empty"));
    }

    // 1. CSPRNG ಸಾಲ್ಟ್ ಜನರೇಷನ್
    let salt: String = rand::thread_rng()
        .sample_iter(&rand::distributions::Alphanumeric)
        .take(salt_length)
        .map(char::from)
        .collect();

    // 2. Strict Pepper ಸೆಟಪ್
    let pepper = custom_pepper
        .map(|s| s.to_string())
        .unwrap_or_else(|| env::var("MCRYPT_SECRET_PEPPER").unwrap_or_else(|_| "McryptSecretPepper2026!#".to_string()));

    let salted_data = format!("{}{}{}", password, salt, pepper);
    
    // 3. Blazing Fast Key Stretching Loop (2^rounds)
    let iterations = 1u64 << rounds; 
    let mut hasher = Sha256::new();
    hasher.update(salted_data.as_bytes());
    let mut hashed_bytes = hasher.finalize();

    for _ in 0..iterations {
        let mut h = Sha256::new();
        h.update(&hashed_bytes);
        hashed_bytes = h.finalize();
    }

    let final_hash = format!("{:x}", hashed_bytes);
    // ಅಸಲಿ ಬ್ರ್ಯಾಂಡ್ ಸಿಗ್ನೇಚರ್ $mcrypt$
    Ok(format!("$mcrypt$v3$r{:02}$sl{:02}${}${}", rounds, salt_length, salt, final_hash))
}

#[pyfunction]
#[pyo3(signature = (password_to_check, stored_hash, custom_pepper=None))]
fn verify(
    password_to_check: &str, 
    stored_hash: &str, 
    custom_pepper: Option<&str>
) -> PyResult<bool> {
    let parts: Vec<&str> = stored_hash.split('$').collect();
    if parts.len() < 7 || parts[1] != "mcrypt" {
        return Ok(false);
    }

    let rounds: u32 = parts[3].replace("r", "").parse().map_err(|_| pyo3::exceptions::PyValueError::new_err("Invalid rounds"))?;
    let extracted_salt = parts[5];

    let pepper = custom_pepper
        .map(|s| s.to_string())
        .unwrap_or_else(|| env::var("MCRYPT_SECRET_PEPPER").unwrap_or_else(|_| "McryptSecretPepper2026!#".to_string()));

    let test_salted_data = format!("{}{}{}", password_to_check, extracted_salt, pepper);
    let iterations = 1u64 << rounds;

    let mut hasher = Sha256::new();
    hasher.update(test_salted_data.as_bytes());
    let mut hashed_bytes = hasher.finalize();

    for _ in 0..iterations {
        let mut h = Sha256::new();
        h.update(&hashed_bytes);
        hashed_bytes = h.finalize();
    }

    let test_hash = format!("{:x}", hashed_bytes);
    let original_crypto_hash = parts[6];

    Ok(constant_time_compare(&test_hash, original_crypto_hash))
}


// extern "C" ಮತ್ತು no_mangle ಎಂದರೆ ಈ ಫಂಕ್ಷನ್ ಹೆಸರನ್ನು C-Compiler ಗೆ ತಿಳಿಯುವ ಹಾಗೆ ಕಂಪೈಲ್ ಮಾಡು ಎಂದರ್ಥ
#[no_mangle]
pub extern "C" fn mcrypt_hash(
    c_password: *const c_char, 
    rounds: u32
) -> *const c_char {
    if c_password.is_null() { return std::ptr::null(); }
    
    // C string ಅನ್ನು Rust string ಆಗಿ ಕನ್ವರ್ಟ್ ಮಾಡುವುದು
    let c_str = unsafe { CStr::from_ptr(c_password) };
    let password = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return std::ptr::null(),
    };

    // ನಾವು ಮುಂಚೆ ಬರೆದ ಹ್ಯಾಶ್ ಲಾಜಿಕ್ ಅನ್ನು ಇಲ್ಲಿ ಕರೆಯುತ್ತೇವೆ (ಸರಳತೆಗಾಗಿ ಡೈರೆಕ್ಟ್ ರಿಟರ್ನ್ ಮಾಡ್ತಿದ್ದೇನೆ)
    let hashed_string = match hash(password, rounds, 16, None) {
        Ok(h) => h,
        Err(_) => return std::ptr::null(),
    };

    // Rust string ಅನ್ನು ಮರಳಿ C-Compatible Pointer ಆಗಿ ಬದಲಾಯಿಸಿ ಕಳುಹಿಸುವುದು
    let c_hashed = CString::new(hashed_string).unwrap();
    c_hashed.into_raw() // ಮೆಮೊರಿ ಬೌಂಡರಿ ಕ್ರಾಸ್ ಮಾಡಲು ಇದು ಬೇಕು
}

#[no_mangle]
pub extern "C" fn mcrypt_verify(
    c_password: *const c_char, 
    c_hash: *const c_char
) -> bool {
    if c_password.is_null() || c_hash.is_null() { return false; }
    
    let p_str = unsafe { CStr::from_ptr(c_password) }.to_str().unwrap_or("");
    let h_str = unsafe { CStr::from_ptr(c_hash) }.to_str().unwrap_or("");
    
    verify(p_str, h_str, None).unwrap_or(false)
}
// Python Module ಇಂಟರ್ಫೇಸ್
#[pymodule]
fn mcrypt(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_function(wrap_pyfunction!(hash, m)?)?;
    m.add_function(wrap_pyfunction!(verify, m)?)?;
    Ok(())
}