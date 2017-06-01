package scala.scalanative
package runtime

import scala.scalanative.native.{
  CBool,
  CChar,
  CInt,
  CLong,
  CShort,
  CSize,
  CStruct0,
  CStruct1,
  CUnsignedChar,
  CUnsignedInt,
  CUnsignedLong,
  CUnsignedShort,
  Ptr,
  extern,
  stdlib
}

// see http://en.cppreference.com/w/cpp/atomic

@extern
object Atomic {

  // Init
  def init_byte(atm: CAtomicByte, initValue: Byte): Unit = extern

  def init_short(atm: CAtomicShort, initValue: CShort): Unit = extern

  def init_int(atm: CAtomicInt, initValue: CInt): Unit = extern

  def init_long(atm: CAtomicLong, initValue: CLong): Unit = extern

  def init_ubyte(atm: CAtomicUByte, initValue: Byte): Unit = extern

  def init_ushort(atm: CAtomicUShort, initValue: CUnsignedShort): Unit = extern

  def init_uint(atm: CAtomicUInt, initValue: CUnsignedInt): Unit = extern

  def init_ulong(atm: CAtomicULong, initValue: CUnsignedLong): Unit = extern

  def init_char(atm: CAtomicChar, initValue: CChar): Unit = extern

  def init_uchar(atm: CAtomicUChar, initValue: CUnsignedChar): Unit = extern

  def init_csize(atm: CAtomicCSize, initValue: CSize): Unit = extern

  // Memory
  def alloc(sz: CSize): Ptr[Byte] = extern

  def free(ptr: Ptr[Byte]): Unit = extern

  // Load
  def load_byte(ptr: CAtomicByte): Byte = extern

  def load_short(ptr: CAtomicShort): CShort = extern

  def load_int(ptr: CAtomicInt): CInt = extern

  def load_long(ptr: CAtomicLong): CLong = extern

  def load_ubyte(ptr: CAtomicUByte): Byte = extern

  def load_ushort(ptr: CAtomicUShort): CUnsignedShort = extern

  def load_uint(ptr: CAtomicUInt): CUnsignedInt = extern

  def load_ulong(ptr: CAtomicULong): CUnsignedLong = extern

  def load_char(ptr: CAtomicChar): CChar = extern

  def load_uchar(ptr: CAtomicUChar): CUnsignedChar = extern

  def load_csize(ptr: CAtomicCSize): CSize = extern

  // Compare and Swap
  def compare_and_swap_strong_int(value: Ptr[CInt],
                                  expected: Ptr[CInt],
                                  desired: CInt): CBool = extern

  def compare_and_swap_weak_int(value: Ptr[CInt],
                                expected: Ptr[CInt],
                                desired: CInt): CBool = extern

  def compare_and_swap_strong_short(value: Ptr[CShort],
                                    expected: Ptr[CShort],
                                    desired: CShort): CBool = extern

  def compare_and_swap_weak_short(value: Ptr[CShort],
                                  expected: Ptr[CShort],
                                  desired: CShort): CBool = extern

  def compare_and_swap_strong_long(value: Ptr[CLong],
                                   expected: Ptr[CLong],
                                   desired: CLong): CBool = extern

  def compare_and_swap_weak_long(value: Ptr[CLong],
                                 expected: Ptr[CLong],
                                 desired: CLong): CBool = extern

  def compare_and_swap_strong_char(value: Ptr[CChar],
                                   expected: Ptr[CChar],
                                   desired: CChar): CBool = extern

  def compare_and_swap_weak_char(value: Ptr[CChar],
                                 expected: Ptr[CChar],
                                 desired: CChar): CBool = extern

  def compare_and_swap_strong_byte(value: Ptr[Byte],
                                   expected: Ptr[Byte],
                                   desired: Byte): CBool = extern

  def compare_and_swap_weak_byte(value: Ptr[Byte],
                                 expected: Ptr[Byte],
                                 desired: Byte): CBool = extern

  def compare_and_swap_strong_ubyte(value: Ptr[Byte],
                                    expected: Ptr[Byte],
                                    desired: Byte): CBool = extern

  def compare_and_swap_weak_ubyte(value: Ptr[Byte],
                                  expected: Ptr[Byte],
                                  desired: Byte): CBool = extern

  def compare_and_swap_strong_uint(value: Ptr[CUnsignedInt],
                                   expected: Ptr[CUnsignedInt],
                                   desired: CUnsignedInt): CBool = extern

  def compare_and_swap_weak_uint(value: Ptr[CUnsignedInt],
                                 expected: Ptr[CUnsignedInt],
                                 desired: CUnsignedInt): CBool = extern

  def compare_and_swap_strong_ulong(value: Ptr[CUnsignedLong],
                                    expected: Ptr[CUnsignedLong],
                                    desired: CUnsignedLong): CBool = extern

  def compare_and_swap_weak_ulong(value: Ptr[CUnsignedLong],
                                  expected: Ptr[CUnsignedLong],
                                  desired: CUnsignedLong): CBool = extern

  def compare_and_swap_strong_ushort(value: Ptr[CUnsignedShort],
                                     expected: Ptr[CUnsignedShort],
                                     desired: CUnsignedShort): CBool = extern

  def compare_and_swap_weak_ushort(value: Ptr[CUnsignedShort],
                                   expected: Ptr[CUnsignedShort],
                                   desired: CUnsignedShort): CBool = extern

  def compare_and_swap_strong_uchar(value: Ptr[CUnsignedChar],
                                    expected: Ptr[CUnsignedChar],
                                    desired: CUnsignedChar): CBool = extern

  def compare_and_swap_weak_uchar(value: Ptr[CUnsignedChar],
                                  expected: Ptr[CUnsignedChar],
                                  desired: CUnsignedChar): CBool = extern

  def compare_and_swap_strong_csize(value: Ptr[CSize],
                                    expected: Ptr[CSize],
                                    desired: CSize): CBool = extern

  def compare_and_swap_weak_csize(value: Ptr[CSize],
                                  expected: Ptr[CSize],
                                  desired: CSize): CBool = extern

  // Add and Sub
  def atomic_add_int(ptr: CAtomicInt, value: CInt): CInt = extern

  def atomic_add_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_add_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_add_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_add_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_add_ubyte(ptr: Ptr[Byte], value: Byte): Byte = extern

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

  def atomic_sub_ubyte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_sub_uint(ptr: Ptr[CUnsignedInt],
                      value: CUnsignedInt): CUnsignedInt = extern

  def atomic_sub_ulong(ptr: Ptr[CUnsignedLong],
                       value: CUnsignedLong): CUnsignedLong = extern

  def atomic_sub_ushort(ptr: Ptr[CUnsignedShort],
                        value: CUnsignedShort): CUnsignedShort = extern

  def atomic_sub_uchar(ptr: Ptr[CUnsignedChar],
                       value: CUnsignedChar): CUnsignedChar = extern

  def atomic_sub_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // Or
  def atomic_or_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_or_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_or_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_or_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_or_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_or_ubyte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_or_uint(ptr: Ptr[CUnsignedInt],
                     value: CUnsignedInt): CUnsignedInt = extern

  def atomic_or_ulong(ptr: Ptr[CUnsignedLong],
                      value: CUnsignedLong): CUnsignedLong = extern

  def atomic_or_ushort(ptr: Ptr[CUnsignedShort],
                       value: CUnsignedShort): CUnsignedShort = extern

  def atomic_or_uchar(ptr: Ptr[CUnsignedChar],
                      value: CUnsignedChar): CUnsignedChar = extern

  def atomic_or_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // And
  def atomic_and_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_and_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_and_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_and_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_and_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_and_ubyte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_and_uint(ptr: Ptr[CUnsignedInt],
                      value: CUnsignedInt): CUnsignedInt = extern

  def atomic_and_ulong(ptr: Ptr[CUnsignedLong],
                       value: CUnsignedLong): CUnsignedLong = extern

  def atomic_and_ushort(ptr: Ptr[CUnsignedShort],
                        value: CUnsignedShort): CUnsignedShort = extern

  def atomic_and_uchar(ptr: Ptr[CUnsignedChar],
                       value: CUnsignedChar): CUnsignedChar = extern

  def atomic_and_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // Xor
  def atomic_xor_int(ptr: Ptr[CInt], value: CInt): CInt = extern

  def atomic_xor_long(ptr: Ptr[CLong], value: CLong): CLong = extern

  def atomic_xor_short(ptr: Ptr[CShort], value: CShort): CShort = extern

  def atomic_xor_char(ptr: Ptr[CChar], value: CChar): CChar = extern

  def atomic_xor_byte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_xor_ubyte(ptr: Ptr[Byte], value: Byte): Byte = extern

  def atomic_xor_uint(ptr: Ptr[CUnsignedInt],
                      value: CUnsignedInt): CUnsignedInt = extern

  def atomic_xor_ulong(ptr: Ptr[CUnsignedLong],
                       value: CUnsignedLong): CUnsignedLong = extern

  def atomic_xor_ushort(ptr: Ptr[CUnsignedShort],
                        value: CUnsignedShort): CUnsignedShort = extern

  def atomic_xor_uchar(ptr: Ptr[CUnsignedChar],
                       value: CUnsignedChar): CUnsignedChar = extern

  def atomic_xor_csize(ptr: Ptr[CSize], value: CSize): CSize = extern

  // Types
  type CAtomicByte   = Ptr[Byte]
  type CAtomicShort  = Ptr[CShort]
  type CAtomicInt    = Ptr[CInt]
  type CAtomicLong   = Ptr[CLong]
  type CAtomicUByte  = Ptr[Byte]
  type CAtomicUShort = Ptr[CUnsignedShort]
  type CAtomicUInt   = Ptr[CUnsignedInt]
  type CAtomicULong  = Ptr[CUnsignedLong]
  type CAtomicChar   = Ptr[CChar]
  type CAtomicUChar  = Ptr[CUnsignedChar]
  type CAtomicBool   = Ptr[CBool]
  type CAtomicCSize  = Ptr[CSize]

}
