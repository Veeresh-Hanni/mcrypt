pub mod hashers;

use std::env;
use std::process;

fn main() {
    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        println!("mcrypt Mass Crypto CLI");
        println!("Usage:\n  mcrypt gensalt <rounds>\n  mcrypt hash <password> <salt_prefix>\n  mcrypt verify <password> <stored_hash>");
        process::exit(1);
    }

    match args[1].as_str() {
        "gensalt" => {
            let rounds = args.get(2).unwrap_or(&"12".to_string()).parse().unwrap_or(12);
            let salt = hashers::core_gensalt(rounds, 16);
            println!("{}", salt);
        }
        "hash" => {
            if args.len() < 4 {
                eprintln!("Error: Missing password or salt_prefix");
                process::exit(1);
            }
            let password = &args[2];
            let salt_prefix = &args[3];
            match hashers::core_hash_with_salt(password, salt_prefix, None) {
                Ok(h) => println!("{}", h),
                Err(e) => eprintln!("Error: {}", e),
            }
        }
        "verify" => {
            if args.len() < 4 {
                eprintln!("Error: Missing password or stored_hash");
                process::exit(1);
            }
            let password_to_check = &args[2];
            let stored_hash = &args[3];
            
            let parts: Vec<&str> = stored_hash.split('$').collect();
            if parts.len() < 7 {
                println!("false");
                process::exit(0);
            }
            
            // ಫಿಕ್ಸ್: ಇಲ್ಲಿ ಸಿಂಗಲ್ ಡಾಲರ್ ಸೈನ್ ಮಾತ್ರ ಇರಬೇಕು ($)
            let salt_prefix = format!("$mcrypt$v3${}${}${}", parts[3], parts[4], parts[5]);
            
            match hashers::core_hash_with_salt(password_to_check, &salt_prefix, None) {
                Ok(new_hash) => {
                    // Timing Attack ಪ್ರೊಟೆಕ್ಷನ್ ಗಾಗಿ Constant-time Comparison
                    let mut result = 0;
                    if new_hash.len() != stored_hash.len() {
                        println!("false");
                    } else {
                        for (a, b) in new_hash.as_bytes().iter().zip(stored_hash.as_bytes().iter()) {
                            result |= a ^ b;
                        }
                        if result == 0 {
                            println!("true");
                        } else {
                            println!("false");
                        }
                    }
                },
                Err(_) => println!("false"),
            }
        }
        _ => println!("Unknown command. Use gensalt, hash, or verify."),
    }
}