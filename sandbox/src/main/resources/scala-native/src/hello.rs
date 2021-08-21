#[no_mangle]
pub extern "C" fn rust_hello() {
  // Statements here are executed when the compiled binary is called
  
  // Print text to the console
  println!("Hello World from Rust!");
}