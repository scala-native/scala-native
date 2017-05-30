package scala.scalanative
package runtime

import scala.scalanative.native.{
  CBool,
  CChar,
  CInt,
  CLong,
  CShort,
  CSize,
  CUnsignedChar,
  CUnsignedInt,
  CUnsignedLong,
  CUnsignedShort,
  Ptr,
  extern
}

// see http://en.cppreference.com/w/cpp/atomic

@extern
object Atomic {

  // Compare and Swap
  def compare_and_swap_strong_int(value: Ptr[CInt],
                                  expected: Ptr[CInt],
                                  desired: CInt): CBool = extern

  def compare_and_swap_weak_int(value: Ptr[CInt],
                                expected: Ptr[CInt],
                                desired: CInt): CBool = extern

  def compare_and_swap_strong_bool(value: Ptr[CBool],
                                   expected: CBool,
                                   desired: CBool): CBool = extern

  def compare_and_swap_weak_bool(value: Ptr[CBool],
                                 expected: CBool,
                                 desired: CBool): CBool = extern

  def compare_and_swap_strong_short(value: Ptr[CShort],
                                    expected: CShort,
                                    desired: CShort): CBool = extern

  def compare_and_swap_weak_short(value: Ptr[CShort],
                                  expected: CShort,
                                  desired: CShort): CBool = extern

  def compare_and_swap_strong_long(value: Ptr[CLong],
                                   expected: CLong,
                                   desired: CLong): CBool = extern

  def compare_and_swap_weak_long(value: Ptr[CLong],
                                 expected: CLong,
                                 desired: CLong): CBool = extern

  def compare_and_swap_strong_char(value: Ptr[CChar],
                                   expected: CChar,
                                   desired: CChar): CBool = extern

  def compare_and_swap_weak_char(value: Ptr[CChar],
                                 expected: CChar,
                                 desired: CChar): CBool = extern

  def compare_and_swap_strong_byte(value: Ptr[Byte],
                                   expected: Byte,
                                   desired: Byte): CBool = extern

  def compare_and_swap_weak_byte(value: Ptr[Byte],
                                 expected: Byte,
                                 desired: Byte): CBool = extern

  def compare_and_swap_stronguint(value: Ptr[CUnsignedInt],
                                  expected: CUnsignedInt,
                                  desired: CUnsignedInt): CBool = extern

  def compare_and_swap_weak_uint(value: Ptr[CUnsignedInt],
                                 expected: CUnsignedInt,
                                 desired: CUnsignedInt): CBool = extern

  def compare_and_swap_strong_ulong(value: Ptr[CUnsignedLong],
                                    expected: CUnsignedLong,
                                    desired: CUnsignedLong): CBool = extern

  def compare_and_swap_weak_ulong(value: Ptr[CUnsignedLong],
                                  expected: CUnsignedLong,
                                  desired: CUnsignedLong): CBool = extern

  def compare_and_swap_strong_ushort(value: Ptr[CUnsignedShort],
                                     expected: CUnsignedShort,
                                     desired: CUnsignedShort): CBool = extern

  def compare_and_swap_weak_ushort(value: Ptr[CUnsignedShort],
                                   expected: CUnsignedShort,
                                   desired: CUnsignedShort): CBool = extern

  def compare_and_swap_strong_uchar(value: Ptr[CUnsignedChar],
                                    expected: CUnsignedChar,
                                    desired: CUnsignedChar): CBool = extern

  def compare_and_swap_weak_uchar(value: Ptr[CUnsignedChar],
                                  expected: CUnsignedChar,
                                  desired: CUnsignedChar): CBool = extern

  def compare_and_swap_strong_csize(value: Ptr[CSize],
                                    expected: CSize,
                                    desired: CSize): CBool = extern

  def compare_and_swap_weak_csize(value: Ptr[CSize],
                                  expected: CSize,
                                  desired: CSize): CBool = extern

  // Add and Sub
  def atomic_add_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_add_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_add_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_add_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_add_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_add_uint(ptr: Ptr[CUnsignedInt],
                      value: CUnsignedInt): CUnsignedInt = extern

  def atomic_add_ulong(ptr: Ptr[CUnsignedLong],
                       value: CUnsignedLong): CUnsignedLong = extern

  def atomic_add_ushort(ptr: Ptr[CUnsignedShort],
                        value: CUnsignedShort): CUnsignedShort = extern

  def atomic_add_uchar(ptr: Ptr[CUnsignedChar],
                       value: CUnsignedChar): CUnsignedChar = extern

  def atomic_add_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  def atomic_sub_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_sub_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_sub_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_sub_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_sub_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_sub_uint(ptr: Ptr[CUnsignedInt],
                      value: CUnsignedInt): CUnsignedInt = extern

  def atomic_sub_ulong(ptr: Ptr[CUnsignedLong],
                       value: CUnsignedLong): CUnsignedLong = extern

  def atomic_sub_ushort(ptr: Ptr[CUnsignedShort],
                        value: CUnsignedShort): CUnsignedShort = extern

  def atomic_sub_uchar(ptr: Ptr[CUnsignedChar],
                       value: CUnsignedChar): CUnsignedChar = extern

  def atomic_sub_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // Boolean operations
  def atomic_or(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_and(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_xor(ptr: Ptr[CInt], value: CInt): CInt = extern

}
