package scala.scalanative
package native

@extern
object Atomic {

  // Compare and Swap
  def compare_and_swap_int(value: Ptr[CInt], expected: CInt, desired: CInt): CBool = extern

  def compare_and_swap_bool(value: Ptr[CBool], expected: CBool, desired: CBool): CBool = extern

  def compare_and_swap_long(value: Ptr[CLong], expected: CLong, desired: CLong): CBool = extern

  // Add and Sub
  def add_and_fetch_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def add_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def add_and_fetch_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def sub_and_fetch_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def sub_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def sub_and_fetch_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  // Boolean operations
  def or_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def and_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def xor_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

}
