use pyo3::prelude::*;
use sha2::{Sha256, Digest};
use rand::RngCore;
use rand::rngs::OsRng;

// CSPRNG Salt Generator
pub fn core_gensalt(rounds: u32, salt_length: usize) -> String {
    let mut bytes = vec![0u8; salt_length];
    OsRng.fill_bytes(&mut bytes);
    
    let secure_salt_str: String = bytes
        .iter()
        .map(|b| {
            const CHARSET: &[u8] = b"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            CHARSET[(*b % CHARSET.len() as u8) as usize] as char
        })
        .collect();

    format!("$mcrypt$v3$r{:02}$sl{:02}${}", rounds, salt_length, secure_salt_str)
}

// Hashing with explicit Salt Prefix
pub fn core_hash_with_salt(password: &str, salt_prefix: &str, custom_pepper: Option<&str>) -> Result<String, String> {
    let parts: Vec<&str> = salt_prefix.split('$').collect();
    if parts.len() < 6 || parts[1] != "mcrypt" {
        return Err("Invalid mcrypt salt format".to_string());
    }

    let rounds: u32 = parts[3].replace("r", "").parse().map_err(|_| "Invalid rounds")?;
    let extracted_salt = parts[5];
    let pepper = custom_pepper.unwrap_or("McryptSecretPepper2026!#");
    
    let salted_data = format!("{}{}{}", password, extracted_salt, pepper);
    let iterations = 1u64 << rounds;
    
    let mut hasher = Sha256::new();
    hasher.update(salted_data.as_bytes());
    let mut hashed_bytes = hasher.finalize();

    for _ in 0..iterations {
        let mut h = Sha256::new();
        h.update(&hashed_bytes);
        hashed_bytes = h.finalize();
    }

    Ok(format!("{}${}", salt_prefix, format!("{:x}", hashed_bytes)))
}