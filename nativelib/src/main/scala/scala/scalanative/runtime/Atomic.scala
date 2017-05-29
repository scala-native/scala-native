package scala.scalanative
package runtime

import scala.scalanative.native.{CBool, CChar, CDouble, CFloat, CInt, CLong, CShort, CSize, CUnsignedChar, CUnsignedInt, CUnsignedLong, CUnsignedShort, Ptr, extern}

@extern
object Atomic {

  // Compare and Swap
  def compare_and_swap_int(value: Ptr[CInt], expected: CInt, desired: CInt): CBool = extern

  def compare_and_swap_bool(value: Ptr[CBool], expected: CBool, desired: CBool): CBool = extern

  def compare_and_swap_short(value: Ptr[CShort], expected: CShort, desired: CShort): CBool = extern

  def compare_and_swap_long(value: Ptr[CLong], expected: CLong, desired: CLong): CBool = extern

  def compare_and_swap_char(value: Ptr[CChar], expected: CChar, desired: CChar): CBool = extern

  def compare_and_swap_byte(value: Ptr[Byte], expected: Byte, desired: Byte): CBool = extern

  def compare_and_swap_uint(value: Ptr[CUnsignedInt], expected: CUnsignedInt, desired: CUnsignedInt): CBool = extern

  def compare_and_swap_ulong(value: Ptr[CUnsignedLong], expected: CUnsignedLong, desired: CUnsignedLong): CBool = extern

  def compare_and_swap_ushort(value: Ptr[CUnsignedShort], expected: CUnsignedShort, desired: CUnsignedShort): CBool = extern

  def compare_and_swap_uchar(value: Ptr[CUnsignedChar], expected: CUnsignedChar, desired: CUnsignedChar): CBool = extern

  def compare_and_swap_csize(value: Ptr[CSize], expected: CSize, desired: CSize): CBool = extern

  // Add and Sub
  def add_and_fetch_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def add_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def add_and_fetch_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def add_and_fetch_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def add_and_fetch_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def add_and_fetch_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def add_and_fetch_uint(ptr: Ptr[CUnsignedInt], value: CUnsignedInt): CUnsignedInt = extern

  def add_and_fetch_ulong(ptr: Ptr[CUnsignedLong], value: CUnsignedLong): CUnsignedLong = extern

  def add_and_fetch_ushort(ptr: Ptr[CUnsignedShort], value: CUnsignedShort): CUnsignedShort = extern

  def add_and_fetch_uchar(ptr: Ptr[CUnsignedChar], value: CUnsignedChar): CUnsignedChar = extern

  def add_and_fetch_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  def sub_and_fetch_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def sub_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def sub_and_fetch_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def sub_and_fetch_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def sub_and_fetch_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def sub_and_fetch_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def sub_and_fetch_uint(ptr: Ptr[CUnsignedInt], value: CUnsignedInt): CUnsignedInt = extern

  def sub_and_fetch_ulong(ptr: Ptr[CUnsignedLong], value: CUnsignedLong): CUnsignedLong = extern

  def sub_and_fetch_ushort(ptr: Ptr[CUnsignedShort], value: CUnsignedShort): CUnsignedShort = extern

  def sub_and_fetch_uchar(ptr: Ptr[CUnsignedChar], value: CUnsignedChar): CUnsignedChar = extern

  def sub_and_fetch_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // Boolean operations
  def or_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def and_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def xor_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

  def nand_and_fetch_bool(ptr: Ptr[CBool], value: CBool): CBool = extern

}
