#[no_mangle]
pub extern "C" fn isEven(x: i32) -> bool {
  x % 2 == 0
}