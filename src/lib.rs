pub mod hashers;

use pyo3::prelude::*;
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

#[pyfunction]
#[pyo3(signature = (rounds=12, salt_length=16))]
fn gensalt(rounds: u32, salt_length: usize) -> String {
    hashers::core_gensalt(rounds, salt_length)
}

#[pyfunction]
#[pyo3(signature = (password, salt_prefix, custom_pepper=None))]
fn hash_with_salt(password: &str, salt_prefix: &str, custom_pepper: Option<&str>) -> PyResult<String> {
    match hashers::core_hash_with_salt(password, salt_prefix, custom_pepper) {
        Ok(h) => Ok(h),
        Err(e) => Err(pyo3::exceptions::PyValueError::new_err(e)),
    }
}

// ಇದು ಹ್ಯಾಶ್‌ನಿಂದಲೇ ಸಾಲ್ಟ್ ತಗೊಂಡು ಆಟೋಮ್ಯಾಟಿಕ್ ವೆರಿಫೈ ಮಾಡುತ್ತೆ
#[pyfunction]
#[pyo3(signature = (password_to_check, stored_hash, custom_pepper=None))]
fn verify(password_to_check: &str, stored_hash: &str, custom_pepper: Option<&str>) -> bool {
    // ಸ್ಟೋರ್ಡ್ ಹ್ಯಾಶ್‌ನಿಂದ ಸಾಲ್ಟ್ ಪ್ರಿಫಿಕ್ಸ್ ಕಟ್ ಮಾಡುವುದು
    let parts: Vec<&str> = stored_hash.split('$').collect();
    if parts.len() < 7 { return false; }
    
    let salt_prefix = format!("$mcrypt$v3$r{}$sl{}${}", parts[3], parts[4], parts[5]);
    
    match hashers::core_hash_with_salt(password_to_check, &salt_prefix, custom_pepper) {
        Ok(new_hash) => {
            // Constant-time check
            let mut result = 0;
            if new_hash.len() != stored_hash.len() { return false; }
            for (a, b) in new_hash.as_bytes().iter().zip(stored_hash.as_bytes().iter()) {
                result |= a ^ b;
            }
            result == 0
        },
        Err(_) => false,
    }
}

#[pymodule]
fn mcrypt(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_function(wrap_pyfunction!(gensalt, m)?)?;
    m.add_function(wrap_pyfunction!(hash_with_salt, m)?)?;
    m.add_function(wrap_pyfunction!(verify, m)?)?;
    Ok(())
}


// ---------------- GLOBAL C-FFI BINDINGS ----------------
// no_mangle ಮತ್ತು extern "C" ಎಂದರೆ ಈ ಫಂಕ್ಷನ್‌ಗಳನ್ನು ಯಾವುದೇ ಭಾಷೆ ಲಿಂಕ್ ಮಾಡಬಹುದು

#[no_mangle]
pub extern "C" fn mcrypt_gensalt(rounds: u32, salt_length: usize) -> *const c_char {
    let salt = hashers::core_gensalt(rounds, salt_length);
    CString::new(salt).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn mcrypt_hash_with_salt(c_password: *const c_char, c_salt_prefix: *const c_char) -> *const c_char {
    if c_password.is_null() || c_salt_prefix.is_null() { return std::ptr::null(); }
    
    let password = unsafe { CStr::from_ptr(c_password) }.to_str().unwrap_or("");
    let salt_prefix = unsafe { CStr::from_ptr(c_salt_prefix) }.to_str().unwrap_or("");

    match hashers::core_hash_with_salt(password, salt_prefix, None) {
        Ok(h) => CString::new(h).unwrap().into_raw(),
        Err(_) => std::ptr::null(),
    }
}

#[no_mangle]
pub extern "C" fn mcrypt_verify(c_password: *const c_char, c_stored_hash: *const c_char) -> bool {
    if c_password.is_null() || c_stored_hash.is_null() { return false; }
    
    let password = unsafe { CStr::from_ptr(c_password) }.to_str().unwrap_or("");
    let stored_hash = unsafe { CStr::from_ptr(c_stored_hash) }.to_str().unwrap_or("");

    let parts: Vec<&str> = stored_hash.split('$').collect();
    if parts.len() < 7 { return false; }
    
    let salt_prefix = format!("$mcrypt$v3$r{}$sl{}${}", parts[3], parts[4], parts[5]);
    
    match hashers::core_hash_with_salt(password, &salt_prefix, None) {
        Ok(new_hash) => {
            let mut result = 0;
            if new_hash.len() != stored_hash.len() { return false; }
            for (a, b) in new_hash.as_bytes().iter().zip(stored_hash.as_bytes().iter()) {
                result |= a ^ b;
            }
            result == 0
        },
        Err(_) => false,
    }
}

// ಈ ಫಂಕ್ಷನ್ ಮೆಮೊರಿ ಲೀಕ್ ತಡೆಯಲು ಅತ್ಯಗತ್ಯ (C/C++ ನಲ್ಲಿ ಸ್ಟ್ರಿಂಗ್ ಫ್ರೀ ಮಾಡಲು)
#[no_mangle]
pub extern "C" fn mcrypt_free_string(s: *mut c_char) {
    if !s.is_null() {
        unsafe { drop(CString::from_raw(s)); }
    }
}