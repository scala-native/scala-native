use std::ffi::CStr;

#[no_mangle]
pub extern "C" fn check_test_string(cstring:  *const i8) -> bool {
  unsafe {
  let c_str: &CStr = CStr::from_ptr(cstring);
  let str: &str = c_str.to_str().unwrap();
  return str == "Hello Rust";
  }
}