extern crate is_even;
use is_even::IsEven;

#[no_mangle]
pub extern "C" fn isEven(x: i32) -> bool {
  x.is_even()
}