// format: off
package scala.scalanative.libc

import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.language.implicitConversions


@extern object atomic {
  type memory_order = Int // enum
  @extern object memory_order{
    @name("scalanative_atomic_memory_order_relaxed")
    final def memory_order_relaxed: memory_order = extern    
    @name("scalanative_atomic_memory_order_consume")
    final def memory_order_consume: memory_order = extern    
    @name("scalanative_atomic_memory_order_acquire")
    final def memory_order_acquire: memory_order = extern    
    @name("scalanative_atomic_memory_order_release")
    final def memory_order_release: memory_order = extern    
    @name("scalanative_atomic_memory_order_acq_rel")
    final def memory_order_acq_rel: memory_order = extern    
    @name("scalanative_atomic_memory_order_seq_cst")
    final def memory_order_seq_cst: memory_order = extern    
  }

  @name("scalanative_atomic_thread_fence")
  final def atomic_thread_fence(order: memory_order): Unit = extern
  
  @name("scalanative_atomic_signal_fence")
  final def atomic_signal_fence(order: memory_order): Unit = extern
 
  type atomic_char = Byte
  @name("scalanative_atomic_init_byte") 
  def atomic_init(atm: Ptr[atomic_char], initValue: Byte): Unit = extern
  
  @name("scalanative_atomic_load_byte")
  def atomic_load(ptr: Ptr[atomic_char]): Byte = extern
  @name("scalanative_atomic_load_explicit_byte")
  def atomic_load_explicit(ptr: Ptr[atomic_char], memoryOrder: memory_order): Byte = extern
  
  @name("scalanative_atomic_store_byte")
  def atomic_store(ptr: Ptr[atomic_char], v: Byte): Unit = extern
  @name("scalanative_atomic_store_explicit_byte")
  def atomic_store_explicit(ptr: Ptr[atomic_char], v: Byte, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_byte")
  def atomic_exchange(ptr: Ptr[atomic_char], v: Byte): Byte = extern
  @name("scalanative_atomic_exchange_explicit_byte")
  def atomic_exchange_explicit(ptr: Ptr[atomic_char], v: Byte, memoryOrder: memory_order): Byte = extern
  
  @name("scalanative_atomic_compare_exchange_strong_byte")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_char], expected: Ptr[Byte], desired: Byte): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_byte")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_char], expected: Ptr[Byte], desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_byte")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_char],expected: Ptr[Byte], desired: Byte): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_byte")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_char], expected: Ptr[Byte], desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_byte")          
  def atomic_fetch_add(ptr: Ptr[atomic_char], value: Byte): Byte = extern
  @name("scalanative_atomic_fetch_add_explicit_byte") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_char], value: Byte, memoryOrder: memory_order): Byte = extern
  @name("scalanative_atomic_fetch_sub_byte")          
  def atomic_fetch_sub(ptr: Ptr[atomic_char], value: Byte): Byte = extern
  @name("scalanative_atomic_fetch_sub_explicit_byte") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_char], value: Byte, memoryOrder: memory_order): Byte = extern
  @name("scalanative_atomic_fetch_or_byte")          
  def atomic_fetch_or(ptr: Ptr[atomic_char], value: Byte): Byte = extern
  @name("scalanative_atomic_fetch_or_explicit_byte") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_char], value: Byte, memoryOrder: memory_order): Byte = extern
  @name("scalanative_atomic_fetch_and_byte")          
  def atomic_fetch_and(ptr: Ptr[atomic_char], value: Byte): Byte = extern
  @name("scalanative_atomic_fetch_and_explicit_byte") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_char], value: Byte, memoryOrder: memory_order): Byte = extern
  @name("scalanative_atomic_fetch_xor_byte")          
  def atomic_fetch_xor(ptr: Ptr[atomic_char], value: Byte): Byte = extern
  @name("scalanative_atomic_fetch_xor_explicit_byte") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_char], value: Byte, memoryOrder: memory_order): Byte = extern

  type atomic_uchar = UByte
  @name("scalanative_atomic_init_ubyte") 
  def atomic_init(atm: Ptr[atomic_uchar], initValue: UByte): Unit = extern
  
  @name("scalanative_atomic_load_ubyte")
  def atomic_load(ptr: Ptr[atomic_uchar]): UByte = extern
  @name("scalanative_atomic_load_explicit_ubyte")
  def atomic_load_explicit(ptr: Ptr[atomic_uchar], memoryOrder: memory_order): UByte = extern
  
  @name("scalanative_atomic_store_ubyte")
  def atomic_store(ptr: Ptr[atomic_uchar], v: UByte): Unit = extern
  @name("scalanative_atomic_store_explicit_ubyte")
  def atomic_store_explicit(ptr: Ptr[atomic_uchar], v: UByte, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_ubyte")
  def atomic_exchange(ptr: Ptr[atomic_uchar], v: UByte): UByte = extern
  @name("scalanative_atomic_exchange_explicit_ubyte")
  def atomic_exchange_explicit(ptr: Ptr[atomic_uchar], v: UByte, memoryOrder: memory_order): UByte = extern
  
  @name("scalanative_atomic_compare_exchange_strong_ubyte")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_uchar], expected: Ptr[UByte], desired: UByte): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_ubyte")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_uchar], expected: Ptr[UByte], desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_ubyte")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_uchar],expected: Ptr[UByte], desired: UByte): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_ubyte")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_uchar], expected: Ptr[UByte], desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_ubyte")          
  def atomic_fetch_add(ptr: Ptr[atomic_uchar], value: UByte): UByte = extern
  @name("scalanative_atomic_fetch_add_explicit_ubyte") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_uchar], value: UByte, memoryOrder: memory_order): UByte = extern
  @name("scalanative_atomic_fetch_sub_ubyte")          
  def atomic_fetch_sub(ptr: Ptr[atomic_uchar], value: UByte): UByte = extern
  @name("scalanative_atomic_fetch_sub_explicit_ubyte") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_uchar], value: UByte, memoryOrder: memory_order): UByte = extern
  @name("scalanative_atomic_fetch_or_ubyte")          
  def atomic_fetch_or(ptr: Ptr[atomic_uchar], value: UByte): UByte = extern
  @name("scalanative_atomic_fetch_or_explicit_ubyte") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_uchar], value: UByte, memoryOrder: memory_order): UByte = extern
  @name("scalanative_atomic_fetch_and_ubyte")          
  def atomic_fetch_and(ptr: Ptr[atomic_uchar], value: UByte): UByte = extern
  @name("scalanative_atomic_fetch_and_explicit_ubyte") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_uchar], value: UByte, memoryOrder: memory_order): UByte = extern
  @name("scalanative_atomic_fetch_xor_ubyte")          
  def atomic_fetch_xor(ptr: Ptr[atomic_uchar], value: UByte): UByte = extern
  @name("scalanative_atomic_fetch_xor_explicit_ubyte") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_uchar], value: UByte, memoryOrder: memory_order): UByte = extern

  type atomic_short = CShort
  @name("scalanative_atomic_init_short") 
  def atomic_init(atm: Ptr[atomic_short], initValue: CShort): Unit = extern
  
  @name("scalanative_atomic_load_short")
  def atomic_load(ptr: Ptr[atomic_short]): CShort = extern
  @name("scalanative_atomic_load_explicit_short")
  def atomic_load_explicit(ptr: Ptr[atomic_short], memoryOrder: memory_order): CShort = extern
  
  @name("scalanative_atomic_store_short")
  def atomic_store(ptr: Ptr[atomic_short], v: CShort): Unit = extern
  @name("scalanative_atomic_store_explicit_short")
  def atomic_store_explicit(ptr: Ptr[atomic_short], v: CShort, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_short")
  def atomic_exchange(ptr: Ptr[atomic_short], v: CShort): CShort = extern
  @name("scalanative_atomic_exchange_explicit_short")
  def atomic_exchange_explicit(ptr: Ptr[atomic_short], v: CShort, memoryOrder: memory_order): CShort = extern
  
  @name("scalanative_atomic_compare_exchange_strong_short")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_short], expected: Ptr[CShort], desired: CShort): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_short")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_short], expected: Ptr[CShort], desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_short")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_short],expected: Ptr[CShort], desired: CShort): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_short")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_short], expected: Ptr[CShort], desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_short")          
  def atomic_fetch_add(ptr: Ptr[atomic_short], value: CShort): CShort = extern
  @name("scalanative_atomic_fetch_add_explicit_short") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_short], value: CShort, memoryOrder: memory_order): CShort = extern
  @name("scalanative_atomic_fetch_sub_short")          
  def atomic_fetch_sub(ptr: Ptr[atomic_short], value: CShort): CShort = extern
  @name("scalanative_atomic_fetch_sub_explicit_short") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_short], value: CShort, memoryOrder: memory_order): CShort = extern
  @name("scalanative_atomic_fetch_or_short")          
  def atomic_fetch_or(ptr: Ptr[atomic_short], value: CShort): CShort = extern
  @name("scalanative_atomic_fetch_or_explicit_short") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_short], value: CShort, memoryOrder: memory_order): CShort = extern
  @name("scalanative_atomic_fetch_and_short")          
  def atomic_fetch_and(ptr: Ptr[atomic_short], value: CShort): CShort = extern
  @name("scalanative_atomic_fetch_and_explicit_short") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_short], value: CShort, memoryOrder: memory_order): CShort = extern
  @name("scalanative_atomic_fetch_xor_short")          
  def atomic_fetch_xor(ptr: Ptr[atomic_short], value: CShort): CShort = extern
  @name("scalanative_atomic_fetch_xor_explicit_short") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_short], value: CShort, memoryOrder: memory_order): CShort = extern

  type atomic_ushort = CUnsignedShort
  @name("scalanative_atomic_init_ushort") 
  def atomic_init(atm: Ptr[atomic_ushort], initValue: CUnsignedShort): Unit = extern
  
  @name("scalanative_atomic_load_ushort")
  def atomic_load(ptr: Ptr[atomic_ushort]): CUnsignedShort = extern
  @name("scalanative_atomic_load_explicit_ushort")
  def atomic_load_explicit(ptr: Ptr[atomic_ushort], memoryOrder: memory_order): CUnsignedShort = extern
  
  @name("scalanative_atomic_store_ushort")
  def atomic_store(ptr: Ptr[atomic_ushort], v: CUnsignedShort): Unit = extern
  @name("scalanative_atomic_store_explicit_ushort")
  def atomic_store_explicit(ptr: Ptr[atomic_ushort], v: CUnsignedShort, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_ushort")
  def atomic_exchange(ptr: Ptr[atomic_ushort], v: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_exchange_explicit_ushort")
  def atomic_exchange_explicit(ptr: Ptr[atomic_ushort], v: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern
  
  @name("scalanative_atomic_compare_exchange_strong_ushort")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_ushort], expected: Ptr[CUnsignedShort], desired: CUnsignedShort): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_ushort")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_ushort], expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_ushort")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_ushort],expected: Ptr[CUnsignedShort], desired: CUnsignedShort): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_ushort")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_ushort], expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_ushort")          
  def atomic_fetch_add(ptr: Ptr[atomic_ushort], value: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_add_explicit_ushort") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_ushort], value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_sub_ushort")          
  def atomic_fetch_sub(ptr: Ptr[atomic_ushort], value: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_sub_explicit_ushort") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_ushort], value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_or_ushort")          
  def atomic_fetch_or(ptr: Ptr[atomic_ushort], value: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_or_explicit_ushort") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_ushort], value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_and_ushort")          
  def atomic_fetch_and(ptr: Ptr[atomic_ushort], value: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_and_explicit_ushort") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_ushort], value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_xor_ushort")          
  def atomic_fetch_xor(ptr: Ptr[atomic_ushort], value: CUnsignedShort): CUnsignedShort = extern
  @name("scalanative_atomic_fetch_xor_explicit_ushort") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_ushort], value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = extern

  type atomic_int = CInt
  @name("scalanative_atomic_init_int") 
  def atomic_init(atm: Ptr[atomic_int], initValue: CInt): Unit = extern
  
  @name("scalanative_atomic_load_int")
  def atomic_load(ptr: Ptr[atomic_int]): CInt = extern
  @name("scalanative_atomic_load_explicit_int")
  def atomic_load_explicit(ptr: Ptr[atomic_int], memoryOrder: memory_order): CInt = extern
  
  @name("scalanative_atomic_store_int")
  def atomic_store(ptr: Ptr[atomic_int], v: CInt): Unit = extern
  @name("scalanative_atomic_store_explicit_int")
  def atomic_store_explicit(ptr: Ptr[atomic_int], v: CInt, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_int")
  def atomic_exchange(ptr: Ptr[atomic_int], v: CInt): CInt = extern
  @name("scalanative_atomic_exchange_explicit_int")
  def atomic_exchange_explicit(ptr: Ptr[atomic_int], v: CInt, memoryOrder: memory_order): CInt = extern
  
  @name("scalanative_atomic_compare_exchange_strong_int")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_int], expected: Ptr[CInt], desired: CInt): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_int")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_int], expected: Ptr[CInt], desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_int")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_int],expected: Ptr[CInt], desired: CInt): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_int")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_int], expected: Ptr[CInt], desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_int")          
  def atomic_fetch_add(ptr: Ptr[atomic_int], value: CInt): CInt = extern
  @name("scalanative_atomic_fetch_add_explicit_int") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_int], value: CInt, memoryOrder: memory_order): CInt = extern
  @name("scalanative_atomic_fetch_sub_int")          
  def atomic_fetch_sub(ptr: Ptr[atomic_int], value: CInt): CInt = extern
  @name("scalanative_atomic_fetch_sub_explicit_int") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_int], value: CInt, memoryOrder: memory_order): CInt = extern
  @name("scalanative_atomic_fetch_or_int")          
  def atomic_fetch_or(ptr: Ptr[atomic_int], value: CInt): CInt = extern
  @name("scalanative_atomic_fetch_or_explicit_int") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_int], value: CInt, memoryOrder: memory_order): CInt = extern
  @name("scalanative_atomic_fetch_and_int")          
  def atomic_fetch_and(ptr: Ptr[atomic_int], value: CInt): CInt = extern
  @name("scalanative_atomic_fetch_and_explicit_int") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_int], value: CInt, memoryOrder: memory_order): CInt = extern
  @name("scalanative_atomic_fetch_xor_int")          
  def atomic_fetch_xor(ptr: Ptr[atomic_int], value: CInt): CInt = extern
  @name("scalanative_atomic_fetch_xor_explicit_int") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_int], value: CInt, memoryOrder: memory_order): CInt = extern

  type atomic_uint = CUnsignedInt
  @name("scalanative_atomic_init_uint") 
  def atomic_init(atm: Ptr[atomic_uint], initValue: CUnsignedInt): Unit = extern
  
  @name("scalanative_atomic_load_uint")
  def atomic_load(ptr: Ptr[atomic_uint]): CUnsignedInt = extern
  @name("scalanative_atomic_load_explicit_uint")
  def atomic_load_explicit(ptr: Ptr[atomic_uint], memoryOrder: memory_order): CUnsignedInt = extern
  
  @name("scalanative_atomic_store_uint")
  def atomic_store(ptr: Ptr[atomic_uint], v: CUnsignedInt): Unit = extern
  @name("scalanative_atomic_store_explicit_uint")
  def atomic_store_explicit(ptr: Ptr[atomic_uint], v: CUnsignedInt, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_uint")
  def atomic_exchange(ptr: Ptr[atomic_uint], v: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_exchange_explicit_uint")
  def atomic_exchange_explicit(ptr: Ptr[atomic_uint], v: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern
  
  @name("scalanative_atomic_compare_exchange_strong_uint")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_uint], expected: Ptr[CUnsignedInt], desired: CUnsignedInt): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_uint")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_uint], expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_uint")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_uint],expected: Ptr[CUnsignedInt], desired: CUnsignedInt): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_uint")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_uint], expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_uint")          
  def atomic_fetch_add(ptr: Ptr[atomic_uint], value: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_add_explicit_uint") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_uint], value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_sub_uint")          
  def atomic_fetch_sub(ptr: Ptr[atomic_uint], value: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_sub_explicit_uint") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_uint], value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_or_uint")          
  def atomic_fetch_or(ptr: Ptr[atomic_uint], value: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_or_explicit_uint") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_uint], value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_and_uint")          
  def atomic_fetch_and(ptr: Ptr[atomic_uint], value: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_and_explicit_uint") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_uint], value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_xor_uint")          
  def atomic_fetch_xor(ptr: Ptr[atomic_uint], value: CUnsignedInt): CUnsignedInt = extern
  @name("scalanative_atomic_fetch_xor_explicit_uint") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_uint], value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = extern

  type atomic_long = CLong
  @name("scalanative_atomic_init_long") 
  def atomic_init(atm: Ptr[atomic_long], initValue: CLong): Unit = extern
  
  @name("scalanative_atomic_load_long")
  def atomic_load(ptr: Ptr[atomic_long]): CLong = extern
  @name("scalanative_atomic_load_explicit_long")
  def atomic_load_explicit(ptr: Ptr[atomic_long], memoryOrder: memory_order): CLong = extern
  
  @name("scalanative_atomic_store_long")
  def atomic_store(ptr: Ptr[atomic_long], v: CLong): Unit = extern
  @name("scalanative_atomic_store_explicit_long")
  def atomic_store_explicit(ptr: Ptr[atomic_long], v: CLong, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_long")
  def atomic_exchange(ptr: Ptr[atomic_long], v: CLong): CLong = extern
  @name("scalanative_atomic_exchange_explicit_long")
  def atomic_exchange_explicit(ptr: Ptr[atomic_long], v: CLong, memoryOrder: memory_order): CLong = extern
  
  @name("scalanative_atomic_compare_exchange_strong_long")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_long], expected: Ptr[CLong], desired: CLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_long")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_long], expected: Ptr[CLong], desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_long")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_long],expected: Ptr[CLong], desired: CLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_long")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_long], expected: Ptr[CLong], desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_long")          
  def atomic_fetch_add(ptr: Ptr[atomic_long], value: CLong): CLong = extern
  @name("scalanative_atomic_fetch_add_explicit_long") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_long], value: CLong, memoryOrder: memory_order): CLong = extern
  @name("scalanative_atomic_fetch_sub_long")          
  def atomic_fetch_sub(ptr: Ptr[atomic_long], value: CLong): CLong = extern
  @name("scalanative_atomic_fetch_sub_explicit_long") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_long], value: CLong, memoryOrder: memory_order): CLong = extern
  @name("scalanative_atomic_fetch_or_long")          
  def atomic_fetch_or(ptr: Ptr[atomic_long], value: CLong): CLong = extern
  @name("scalanative_atomic_fetch_or_explicit_long") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_long], value: CLong, memoryOrder: memory_order): CLong = extern
  @name("scalanative_atomic_fetch_and_long")          
  def atomic_fetch_and(ptr: Ptr[atomic_long], value: CLong): CLong = extern
  @name("scalanative_atomic_fetch_and_explicit_long") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_long], value: CLong, memoryOrder: memory_order): CLong = extern
  @name("scalanative_atomic_fetch_xor_long")          
  def atomic_fetch_xor(ptr: Ptr[atomic_long], value: CLong): CLong = extern
  @name("scalanative_atomic_fetch_xor_explicit_long") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_long], value: CLong, memoryOrder: memory_order): CLong = extern

  type atomic_ulong = CUnsignedLong
  @name("scalanative_atomic_init_ulong") 
  def atomic_init(atm: Ptr[atomic_ulong], initValue: CUnsignedLong): Unit = extern
  
  @name("scalanative_atomic_load_ulong")
  def atomic_load(ptr: Ptr[atomic_ulong]): CUnsignedLong = extern
  @name("scalanative_atomic_load_explicit_ulong")
  def atomic_load_explicit(ptr: Ptr[atomic_ulong], memoryOrder: memory_order): CUnsignedLong = extern
  
  @name("scalanative_atomic_store_ulong")
  def atomic_store(ptr: Ptr[atomic_ulong], v: CUnsignedLong): Unit = extern
  @name("scalanative_atomic_store_explicit_ulong")
  def atomic_store_explicit(ptr: Ptr[atomic_ulong], v: CUnsignedLong, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_ulong")
  def atomic_exchange(ptr: Ptr[atomic_ulong], v: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_exchange_explicit_ulong")
  def atomic_exchange_explicit(ptr: Ptr[atomic_ulong], v: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern
  
  @name("scalanative_atomic_compare_exchange_strong_ulong")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_ulong], expected: Ptr[CUnsignedLong], desired: CUnsignedLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_ulong")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_ulong], expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_ulong")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_ulong],expected: Ptr[CUnsignedLong], desired: CUnsignedLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_ulong")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_ulong], expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_ulong")          
  def atomic_fetch_add(ptr: Ptr[atomic_ulong], value: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_add_explicit_ulong") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_ulong], value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_sub_ulong")          
  def atomic_fetch_sub(ptr: Ptr[atomic_ulong], value: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_sub_explicit_ulong") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_ulong], value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_or_ulong")          
  def atomic_fetch_or(ptr: Ptr[atomic_ulong], value: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_or_explicit_ulong") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_ulong], value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_and_ulong")          
  def atomic_fetch_and(ptr: Ptr[atomic_ulong], value: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_and_explicit_ulong") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_ulong], value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_xor_ulong")          
  def atomic_fetch_xor(ptr: Ptr[atomic_ulong], value: CUnsignedLong): CUnsignedLong = extern
  @name("scalanative_atomic_fetch_xor_explicit_ulong") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_ulong], value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = extern

  type atomic_llong = CLongLong
  @name("scalanative_atomic_init_llong") 
  def atomic_init(atm: Ptr[atomic_llong], initValue: CLongLong): Unit = extern
  
  @name("scalanative_atomic_load_llong")
  def atomic_load(ptr: Ptr[atomic_llong]): CLongLong = extern
  @name("scalanative_atomic_load_explicit_llong")
  def atomic_load_explicit(ptr: Ptr[atomic_llong], memoryOrder: memory_order): CLongLong = extern
  
  @name("scalanative_atomic_store_llong")
  def atomic_store(ptr: Ptr[atomic_llong], v: CLongLong): Unit = extern
  @name("scalanative_atomic_store_explicit_llong")
  def atomic_store_explicit(ptr: Ptr[atomic_llong], v: CLongLong, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_llong")
  def atomic_exchange(ptr: Ptr[atomic_llong], v: CLongLong): CLongLong = extern
  @name("scalanative_atomic_exchange_explicit_llong")
  def atomic_exchange_explicit(ptr: Ptr[atomic_llong], v: CLongLong, memoryOrder: memory_order): CLongLong = extern
  
  @name("scalanative_atomic_compare_exchange_strong_llong")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_llong], expected: Ptr[CLongLong], desired: CLongLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_llong")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_llong], expected: Ptr[CLongLong], desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_llong")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_llong],expected: Ptr[CLongLong], desired: CLongLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_llong")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_llong], expected: Ptr[CLongLong], desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_llong")          
  def atomic_fetch_add(ptr: Ptr[atomic_llong], value: CLongLong): CLongLong = extern
  @name("scalanative_atomic_fetch_add_explicit_llong") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_llong], value: CLongLong, memoryOrder: memory_order): CLongLong = extern
  @name("scalanative_atomic_fetch_sub_llong")          
  def atomic_fetch_sub(ptr: Ptr[atomic_llong], value: CLongLong): CLongLong = extern
  @name("scalanative_atomic_fetch_sub_explicit_llong") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_llong], value: CLongLong, memoryOrder: memory_order): CLongLong = extern
  @name("scalanative_atomic_fetch_or_llong")          
  def atomic_fetch_or(ptr: Ptr[atomic_llong], value: CLongLong): CLongLong = extern
  @name("scalanative_atomic_fetch_or_explicit_llong") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_llong], value: CLongLong, memoryOrder: memory_order): CLongLong = extern
  @name("scalanative_atomic_fetch_and_llong")          
  def atomic_fetch_and(ptr: Ptr[atomic_llong], value: CLongLong): CLongLong = extern
  @name("scalanative_atomic_fetch_and_explicit_llong") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_llong], value: CLongLong, memoryOrder: memory_order): CLongLong = extern
  @name("scalanative_atomic_fetch_xor_llong")          
  def atomic_fetch_xor(ptr: Ptr[atomic_llong], value: CLongLong): CLongLong = extern
  @name("scalanative_atomic_fetch_xor_explicit_llong") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_llong], value: CLongLong, memoryOrder: memory_order): CLongLong = extern

  type atomic_ullong = CUnsignedLongLong
  @name("scalanative_atomic_init_ullong") 
  def atomic_init(atm: Ptr[atomic_ullong], initValue: CUnsignedLongLong): Unit = extern
  
  @name("scalanative_atomic_load_ullong")
  def atomic_load(ptr: Ptr[atomic_ullong]): CUnsignedLongLong = extern
  @name("scalanative_atomic_load_explicit_ullong")
  def atomic_load_explicit(ptr: Ptr[atomic_ullong], memoryOrder: memory_order): CUnsignedLongLong = extern
  
  @name("scalanative_atomic_store_ullong")
  def atomic_store(ptr: Ptr[atomic_ullong], v: CUnsignedLongLong): Unit = extern
  @name("scalanative_atomic_store_explicit_ullong")
  def atomic_store_explicit(ptr: Ptr[atomic_ullong], v: CUnsignedLongLong, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_ullong")
  def atomic_exchange(ptr: Ptr[atomic_ullong], v: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_exchange_explicit_ullong")
  def atomic_exchange_explicit(ptr: Ptr[atomic_ullong], v: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern
  
  @name("scalanative_atomic_compare_exchange_strong_ullong")
  def atomic_compare_exchange_strong(ptr: Ptr[atomic_ullong], expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_ullong")
  def atomic_compare_exchange_strong_explicit(ptr: Ptr[atomic_ullong], expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_ullong")
  def atomic_compare_exchange_weak(ptr: Ptr[atomic_ullong],expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_ullong")
  def atomic_compare_exchange_weak_explicit(ptr: Ptr[atomic_ullong], expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_ullong")          
  def atomic_fetch_add(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_add_explicit_ullong") 
  def atomic_fetch_add_explicit(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_sub_ullong")          
  def atomic_fetch_sub(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_sub_explicit_ullong") 
  def atomic_fetch_sub_explicit(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_or_ullong")          
  def atomic_fetch_or(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_or_explicit_ullong") 
  def atomic_fetch_or_explicit(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_and_ullong")          
  def atomic_fetch_and(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_and_explicit_ullong") 
  def atomic_fetch_and_explicit(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_xor_ullong")          
  def atomic_fetch_xor(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong): CUnsignedLongLong = extern
  @name("scalanative_atomic_fetch_xor_explicit_ullong") 
  def atomic_fetch_xor_explicit(ptr: Ptr[atomic_ullong], value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = extern


  type atomic_ptr[T] = Ptr[T]
  @name("scalanative_atomic_init_intptr") 
  def atomic_init[T](atm: atomic_ptr[T], initValue: T): Unit = extern

  @name("scalanative_atomic_load_intptr")
  def atomic_load[T](ptr: atomic_ptr[T]): T = extern
  @name("scalanative_atomic_load_explicit_intptr")
  def atomic_load_explicit[T](ptr: atomic_ptr[T], memoryOrder: memory_order): T = extern
  
  @name("scalanative_atomic_store_intptr")
  def atomic_store[T](ptr: atomic_ptr[T], v: T): Unit = extern
  @name("scalanative_atomic_store_explicit_intptr")
  def atomic_store_explicit[T](ptr: atomic_ptr[T], v: T, memoryOrder: memory_order): Unit = extern
  
  @name("scalanative_atomic_exchange_intptr")
  def atomic_exchange[T](ptr: atomic_ptr[T], v: T): T = extern
  @name("scalanative_atomic_exchange_explicit_intptr")
  def atomic_exchange_explicit[T](ptr: atomic_ptr[T], v: T, memoryOrder: memory_order): T = extern
  
  @name("scalanative_atomic_compare_exchange_strong_intptr")
  def atomic_compare_exchange_strong[T](ptr: atomic_ptr[T], expected: Ptr[T], desired: T): CBool = extern
  @name("scalanative_atomic_compare_exchange_strong_explicit_intptr")
  def atomic_compare_exchange_strong_explicit[T](ptr: atomic_ptr[T],expected: Ptr[T],desired: T,memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_compare_exchange_weak_intptr")
  def atomic_compare_exchange_weak[T](ptr: atomic_ptr[T], expected: Ptr[T], desired: T): CBool = extern
  @name("scalanative_atomic_compare_exchange_weak_explicit_intptr")
  def atomic_compare_exchange_weak_explicit[T](ptr: atomic_ptr[T], expected: Ptr[T], desired: T, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): CBool = extern
  
  @name("scalanative_atomic_fetch_add_intptr")
  def atomic_fetch_add[T](ptr: atomic_ptr[T], value: T): T = extern
  @name("scalanative_atomic_fetch_add_explicit_intptr")
  def atomic_fetch_add_explicit[T](ptr: atomic_ptr[T], value: T, memoryOrder: memory_order): T = extern
  @name("scalanative_atomic_fetch_sub_intptr")
  def atomic_fetch_sub[T](ptr: atomic_ptr[T], value: T): T = extern
  @name("scalanative_atomic_fetch_sub_explicit_intptr")
  def atomic_fetch_sub_explicit[T](ptr: atomic_ptr[T], value: T, memoryOrder: memory_order): T = extern
  @name("scalanative_atomic_fetch_or_intptr")
  def atomic_fetch_or[T](ptr: atomic_ptr[T], value: T): T = extern
  @name("scalanative_atomic_fetch_or_explicit_intptr")
  def atomic_fetch_or_explicit[T](ptr: atomic_ptr[T], value: T, memoryOrder: memory_order): T = extern
  @name("scalanative_atomic_fetch_and_intptr")
  def atomic_fetch_and[T](ptr: atomic_ptr[T], value: T): T = extern
  @name("scalanative_atomic_fetch_and_explicit_intptr")
  def atomic_fetch_and_explicit[T](ptr: atomic_ptr[T], value: T, memoryOrder: memory_order): T = extern
  @name("scalanative_atomic_fetch_xor_intptr")
  def atomic_fetch_xor[T](ptr: atomic_ptr[T], value: T): T = extern
  @name("scalanative_atomic_fetch_xor_explicit_intptr")
  def atomic_fetch_xor_explicit[T](ptr: atomic_ptr[T], value: T, memoryOrder: memory_order): T = extern


  // Helper wrappers

  object CAtomicByte{
    def apply(initialValue: Byte)(implicit zone: Zone): CAtomicByte = {
      val ref = new CAtomicByte(zone.alloc(sizeof[Byte]).asInstanceOf[Ptr[atomic.atomic_char]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicByte(private val underlying: Ptr[atomic.atomic_char]) extends AnyVal {
    def init(value: Byte): Unit = atomic_init(underlying, value)

    def load(): Byte = atomic_load(underlying)
    def load(memoryOrder: memory_order): Byte =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: Byte): Unit = atomic_store(underlying, value)
    def store(value: Byte, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: Byte): Byte = atomic_exchange(underlying, value)
    def exchange(value: Byte, memoryOrder: memory_order): Byte = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[Byte], desired: Byte): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[Byte], desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[Byte], desired: Byte, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[Byte], desired: Byte): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[Byte], desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[Byte], desired: Byte, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: Byte): Byte = atomic_fetch_add(underlying, value)
    def fetchAdd(value: Byte, memoryOrder: memory_order): Byte = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: Byte): Byte = atomic_fetch_sub(underlying, value)
    def fetchSub(value: Byte, memoryOrder: memory_order): Byte = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: Byte): Byte = atomic_fetch_and(underlying, value)
    def fetchAnd(value: Byte, memoryOrder: memory_order): Byte = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: Byte): Byte = atomic_fetch_or(underlying, value)
    def fetchOr(value: Byte, memoryOrder: memory_order): Byte = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: Byte): Byte = atomic_fetch_xor(underlying, value)
    def fetchXor(value: Byte, memoryOrder: memory_order): Byte = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: Byte, desired: Byte): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: Byte, desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: Byte, desired: Byte, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: Byte, desired: Byte): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: Byte, desired: Byte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: Byte, desired: Byte, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[Byte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicUnsignedByte{
    def apply(initialValue: UByte)(implicit zone: Zone): CAtomicUnsignedByte = {
      val ref = new CAtomicUnsignedByte(zone.alloc(sizeof[UByte]).asInstanceOf[Ptr[atomic.atomic_uchar]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicUnsignedByte(private val underlying: Ptr[atomic.atomic_uchar]) extends AnyVal {
    def init(value: UByte): Unit = atomic_init(underlying, value)

    def load(): UByte = atomic_load(underlying)
    def load(memoryOrder: memory_order): UByte =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: UByte): Unit = atomic_store(underlying, value)
    def store(value: UByte, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: UByte): UByte = atomic_exchange(underlying, value)
    def exchange(value: UByte, memoryOrder: memory_order): UByte = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[UByte], desired: UByte): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[UByte], desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[UByte], desired: UByte, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[UByte], desired: UByte): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[UByte], desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[UByte], desired: UByte, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: UByte): UByte = atomic_fetch_add(underlying, value)
    def fetchAdd(value: UByte, memoryOrder: memory_order): UByte = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: UByte): UByte = atomic_fetch_sub(underlying, value)
    def fetchSub(value: UByte, memoryOrder: memory_order): UByte = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: UByte): UByte = atomic_fetch_and(underlying, value)
    def fetchAnd(value: UByte, memoryOrder: memory_order): UByte = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: UByte): UByte = atomic_fetch_or(underlying, value)
    def fetchOr(value: UByte, memoryOrder: memory_order): UByte = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: UByte): UByte = atomic_fetch_xor(underlying, value)
    def fetchXor(value: UByte, memoryOrder: memory_order): UByte = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: UByte, desired: UByte): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: UByte, desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: UByte, desired: UByte, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: UByte, desired: UByte): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: UByte, desired: UByte, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: UByte, desired: UByte, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[UByte]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicShort{
    def apply(initialValue: CShort)(implicit zone: Zone): CAtomicShort = {
      val ref = new CAtomicShort(zone.alloc(sizeof[CShort]).asInstanceOf[Ptr[atomic.atomic_short]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicShort(private val underlying: Ptr[atomic.atomic_short]) extends AnyVal {
    def init(value: CShort): Unit = atomic_init(underlying, value)

    def load(): CShort = atomic_load(underlying)
    def load(memoryOrder: memory_order): CShort =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CShort): Unit = atomic_store(underlying, value)
    def store(value: CShort, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CShort): CShort = atomic_exchange(underlying, value)
    def exchange(value: CShort, memoryOrder: memory_order): CShort = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CShort], desired: CShort): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CShort], desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CShort], desired: CShort, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CShort], desired: CShort): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CShort], desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CShort], desired: CShort, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CShort): CShort = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CShort, memoryOrder: memory_order): CShort = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CShort): CShort = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CShort, memoryOrder: memory_order): CShort = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CShort): CShort = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CShort, memoryOrder: memory_order): CShort = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CShort): CShort = atomic_fetch_or(underlying, value)
    def fetchOr(value: CShort, memoryOrder: memory_order): CShort = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CShort): CShort = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CShort, memoryOrder: memory_order): CShort = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CShort, desired: CShort): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CShort, desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CShort, desired: CShort, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CShort, desired: CShort): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CShort, desired: CShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CShort, desired: CShort, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicUnsignedShort{
    def apply(initialValue: CUnsignedShort)(implicit zone: Zone): CAtomicUnsignedShort = {
      val ref = new CAtomicUnsignedShort(zone.alloc(sizeof[CUnsignedShort]).asInstanceOf[Ptr[atomic.atomic_ushort]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicUnsignedShort(private val underlying: Ptr[atomic.atomic_ushort]) extends AnyVal {
    def init(value: CUnsignedShort): Unit = atomic_init(underlying, value)

    def load(): CUnsignedShort = atomic_load(underlying)
    def load(memoryOrder: memory_order): CUnsignedShort =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CUnsignedShort): Unit = atomic_store(underlying, value)
    def store(value: CUnsignedShort, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CUnsignedShort): CUnsignedShort = atomic_exchange(underlying, value)
    def exchange(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CUnsignedShort], desired: CUnsignedShort): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CUnsignedShort], desired: CUnsignedShort): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CUnsignedShort], desired: CUnsignedShort, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CUnsignedShort): CUnsignedShort = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CUnsignedShort): CUnsignedShort = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CUnsignedShort): CUnsignedShort = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CUnsignedShort): CUnsignedShort = atomic_fetch_or(underlying, value)
    def fetchOr(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CUnsignedShort): CUnsignedShort = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CUnsignedShort, memoryOrder: memory_order): CUnsignedShort = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CUnsignedShort, desired: CUnsignedShort): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CUnsignedShort, desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CUnsignedShort, desired: CUnsignedShort, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CUnsignedShort, desired: CUnsignedShort): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CUnsignedShort, desired: CUnsignedShort, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CUnsignedShort, desired: CUnsignedShort, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedShort]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicInt{
    def apply(initialValue: CInt)(implicit zone: Zone): CAtomicInt = {
      val ref = new CAtomicInt(zone.alloc(sizeof[CInt]).asInstanceOf[Ptr[atomic.atomic_int]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicInt(private val underlying: Ptr[atomic.atomic_int]) extends AnyVal {
    def init(value: CInt): Unit = atomic_init(underlying, value)

    def load(): CInt = atomic_load(underlying)
    def load(memoryOrder: memory_order): CInt =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CInt): Unit = atomic_store(underlying, value)
    def store(value: CInt, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CInt): CInt = atomic_exchange(underlying, value)
    def exchange(value: CInt, memoryOrder: memory_order): CInt = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CInt], desired: CInt): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CInt], desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CInt], desired: CInt, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CInt], desired: CInt): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CInt], desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CInt], desired: CInt, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CInt): CInt = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CInt, memoryOrder: memory_order): CInt = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CInt): CInt = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CInt, memoryOrder: memory_order): CInt = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CInt): CInt = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CInt, memoryOrder: memory_order): CInt = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CInt): CInt = atomic_fetch_or(underlying, value)
    def fetchOr(value: CInt, memoryOrder: memory_order): CInt = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CInt): CInt = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CInt, memoryOrder: memory_order): CInt = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CInt, desired: CInt): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CInt, desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CInt, desired: CInt, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CInt, desired: CInt): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CInt, desired: CInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CInt, desired: CInt, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicUnsignedInt{
    def apply(initialValue: CUnsignedInt)(implicit zone: Zone): CAtomicUnsignedInt = {
      val ref = new CAtomicUnsignedInt(zone.alloc(sizeof[CUnsignedInt]).asInstanceOf[Ptr[atomic.atomic_uint]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicUnsignedInt(private val underlying: Ptr[atomic.atomic_uint]) extends AnyVal {
    def init(value: CUnsignedInt): Unit = atomic_init(underlying, value)

    def load(): CUnsignedInt = atomic_load(underlying)
    def load(memoryOrder: memory_order): CUnsignedInt =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CUnsignedInt): Unit = atomic_store(underlying, value)
    def store(value: CUnsignedInt, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CUnsignedInt): CUnsignedInt = atomic_exchange(underlying, value)
    def exchange(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CUnsignedInt], desired: CUnsignedInt): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CUnsignedInt], desired: CUnsignedInt): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CUnsignedInt], desired: CUnsignedInt, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CUnsignedInt): CUnsignedInt = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CUnsignedInt): CUnsignedInt = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CUnsignedInt): CUnsignedInt = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CUnsignedInt): CUnsignedInt = atomic_fetch_or(underlying, value)
    def fetchOr(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CUnsignedInt): CUnsignedInt = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CUnsignedInt, memoryOrder: memory_order): CUnsignedInt = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CUnsignedInt, desired: CUnsignedInt): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CUnsignedInt, desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CUnsignedInt, desired: CUnsignedInt, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CUnsignedInt, desired: CUnsignedInt): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CUnsignedInt, desired: CUnsignedInt, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CUnsignedInt, desired: CUnsignedInt, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedInt]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicLong{
    def apply(initialValue: CLong)(implicit zone: Zone): CAtomicLong = {
      val ref = new CAtomicLong(zone.alloc(sizeof[CLong]).asInstanceOf[Ptr[atomic.atomic_long]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicLong(private val underlying: Ptr[atomic.atomic_long]) extends AnyVal {
    def init(value: CLong): Unit = atomic_init(underlying, value)

    def load(): CLong = atomic_load(underlying)
    def load(memoryOrder: memory_order): CLong =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CLong): Unit = atomic_store(underlying, value)
    def store(value: CLong, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CLong): CLong = atomic_exchange(underlying, value)
    def exchange(value: CLong, memoryOrder: memory_order): CLong = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CLong], desired: CLong): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CLong], desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CLong], desired: CLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CLong], desired: CLong): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CLong], desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CLong], desired: CLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CLong): CLong = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CLong, memoryOrder: memory_order): CLong = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CLong): CLong = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CLong, memoryOrder: memory_order): CLong = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CLong): CLong = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CLong, memoryOrder: memory_order): CLong = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CLong): CLong = atomic_fetch_or(underlying, value)
    def fetchOr(value: CLong, memoryOrder: memory_order): CLong = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CLong): CLong = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CLong, memoryOrder: memory_order): CLong = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CLong, desired: CLong): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CLong, desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CLong, desired: CLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CLong, desired: CLong): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CLong, desired: CLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CLong, desired: CLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicUnsignedLong{
    def apply(initialValue: CUnsignedLong)(implicit zone: Zone): CAtomicUnsignedLong = {
      val ref = new CAtomicUnsignedLong(zone.alloc(sizeof[CUnsignedLong]).asInstanceOf[Ptr[atomic.atomic_ulong]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicUnsignedLong(private val underlying: Ptr[atomic.atomic_ulong]) extends AnyVal {
    def init(value: CUnsignedLong): Unit = atomic_init(underlying, value)

    def load(): CUnsignedLong = atomic_load(underlying)
    def load(memoryOrder: memory_order): CUnsignedLong =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CUnsignedLong): Unit = atomic_store(underlying, value)
    def store(value: CUnsignedLong, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CUnsignedLong): CUnsignedLong = atomic_exchange(underlying, value)
    def exchange(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CUnsignedLong], desired: CUnsignedLong): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CUnsignedLong], desired: CUnsignedLong): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CUnsignedLong], desired: CUnsignedLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CUnsignedLong): CUnsignedLong = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CUnsignedLong): CUnsignedLong = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CUnsignedLong): CUnsignedLong = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CUnsignedLong): CUnsignedLong = atomic_fetch_or(underlying, value)
    def fetchOr(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CUnsignedLong): CUnsignedLong = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CUnsignedLong, memoryOrder: memory_order): CUnsignedLong = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CUnsignedLong, desired: CUnsignedLong): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CUnsignedLong, desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CUnsignedLong, desired: CUnsignedLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CUnsignedLong, desired: CUnsignedLong): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CUnsignedLong, desired: CUnsignedLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CUnsignedLong, desired: CUnsignedLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicLongLong{
    def apply(initialValue: CLongLong)(implicit zone: Zone): CAtomicLongLong = {
      val ref = new CAtomicLongLong(zone.alloc(sizeof[CLongLong]).asInstanceOf[Ptr[atomic.atomic_llong]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicLongLong(private val underlying: Ptr[atomic.atomic_llong]) extends AnyVal {
    def init(value: CLongLong): Unit = atomic_init(underlying, value)

    def load(): CLongLong = atomic_load(underlying)
    def load(memoryOrder: memory_order): CLongLong =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CLongLong): Unit = atomic_store(underlying, value)
    def store(value: CLongLong, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CLongLong): CLongLong = atomic_exchange(underlying, value)
    def exchange(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CLongLong], desired: CLongLong): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CLongLong], desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CLongLong], desired: CLongLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CLongLong], desired: CLongLong): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CLongLong], desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CLongLong], desired: CLongLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CLongLong): CLongLong = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CLongLong): CLongLong = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CLongLong): CLongLong = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CLongLong): CLongLong = atomic_fetch_or(underlying, value)
    def fetchOr(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CLongLong): CLongLong = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CLongLong, memoryOrder: memory_order): CLongLong = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CLongLong, desired: CLongLong): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CLongLong, desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CLongLong, desired: CLongLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CLongLong, desired: CLongLong): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CLongLong, desired: CLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CLongLong, desired: CLongLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }
  object CAtomicUnsignedLongLong{
    def apply(initialValue: CUnsignedLongLong)(implicit zone: Zone): CAtomicUnsignedLongLong = {
      val ref = new CAtomicUnsignedLongLong(zone.alloc(sizeof[CUnsignedLongLong]).asInstanceOf[Ptr[atomic.atomic_ullong]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicUnsignedLongLong(private val underlying: Ptr[atomic.atomic_ullong]) extends AnyVal {
    def init(value: CUnsignedLongLong): Unit = atomic_init(underlying, value)

    def load(): CUnsignedLongLong = atomic_load(underlying)
    def load(memoryOrder: memory_order): CUnsignedLongLong =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: CUnsignedLongLong): Unit = atomic_store(underlying, value)
    def store(value: CUnsignedLongLong, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: CUnsignedLongLong): CUnsignedLongLong = atomic_exchange(underlying, value)
    def exchange(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[CUnsignedLongLong], desired: CUnsignedLongLong, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: CUnsignedLongLong): CUnsignedLongLong = atomic_fetch_add(underlying, value)
    def fetchAdd(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: CUnsignedLongLong): CUnsignedLongLong = atomic_fetch_sub(underlying, value)
    def fetchSub(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: CUnsignedLongLong): CUnsignedLongLong = atomic_fetch_and(underlying, value)
    def fetchAnd(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: CUnsignedLongLong): CUnsignedLongLong = atomic_fetch_or(underlying, value)
    def fetchOr(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: CUnsignedLongLong): CUnsignedLongLong = atomic_fetch_xor(underlying, value)
    def fetchXor(value: CUnsignedLongLong, memoryOrder: memory_order): CUnsignedLongLong = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: CUnsignedLongLong, desired: CUnsignedLongLong, memoryOrder: memory_order): Boolean = {
      val expectedPtr = stackalloc[CUnsignedLongLong]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }

  object CAtomicPtr{
    def apply[T](initialValue: Ptr[T])(implicit zone: Zone): CAtomicPtr[T] = {
      val ref = new CAtomicPtr(zone.alloc(sizeof[Ptr[T]]).asInstanceOf[Ptr[atomic.atomic_ptr[T]]])
      ref.init(initialValue)
      ref
    }
  }
  final class CAtomicPtr[T](private val underlying: Ptr[atomic.atomic_ptr[T]]) extends AnyVal {
    def init(value: Ptr[T]): Unit = atomic_init(underlying, value)

    def load(): Ptr[T] = atomic_load(underlying)
    def load(memoryOrder: memory_order): Ptr[T] =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: Ptr[T]): Unit = atomic_store(underlying, value)
    def store(value: Ptr[T], memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: Ptr[T]): Ptr[T] = atomic_exchange(underlying, value)
    def exchange(value: Ptr[T], memoryOrder: memory_order): Ptr[T] = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[Ptr[T]], desired: Ptr[T]): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[Ptr[T]], desired: Ptr[T], memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[Ptr[T]], desired: Ptr[T], memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[Ptr[T]], desired: Ptr[T]): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[Ptr[T]], desired: Ptr[T], memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[Ptr[T]], desired: Ptr[T], memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: Ptr[T]): Ptr[T] = atomic_fetch_add(underlying, value)
    def fetchAdd(value: Ptr[T], memoryOrder: memory_order): Ptr[T] = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: Ptr[T]): Ptr[T] = atomic_fetch_sub(underlying, value)
    def fetchSub(value: Ptr[T], memoryOrder: memory_order): Ptr[T] = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: Ptr[T]): Ptr[T] = atomic_fetch_and(underlying, value)
    def fetchAnd(value: Ptr[T], memoryOrder: memory_order): Ptr[T] = atomic_fetch_and_explicit(underlying, value, memoryOrder)
    def fetchXor(value: Ptr[T]): Ptr[T] = atomic_fetch_xor(underlying, value)
    def fetchXor(value: Ptr[T], memoryOrder: memory_order): Ptr[T] = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: Ptr[T], desired: Ptr[T])(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr, desired)
    }
    def compareExchangeStrong(expectedValue: Ptr[T], desired: Ptr[T], memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: Ptr[T], desired: Ptr[T], memoryOrder: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: Ptr[T], desired: Ptr[T])(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr, desired)
    }
    def compareExchangeWeak(expectedValue: Ptr[T], desired: Ptr[T], memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: Ptr[T], desired: Ptr[T], memoryOrder: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[Ptr[T]]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr, desired, memoryOrder, memoryOrder)
    }
  }

  final class CAtomicRef[T <: AnyRef](private val underlying: Ptr[T]) extends AnyVal {
    def init(value: T): Unit = atomic_init(underlying, value)

    def load(): T = atomic_load(underlying)
    def load(memoryOrder: memory_order): T =  atomic_load_explicit(underlying, memoryOrder)

    def store(value: T): Unit = atomic_store(underlying, value)
    def store(value: T, memoryOrder: memory_order): Unit = atomic_store_explicit(underlying, value, memoryOrder)

    def exchange(value: T): T = atomic_exchange(underlying, value)
    def exchange(value: T, memoryOrder: memory_order): T = atomic_exchange_explicit(underlying, value, memoryOrder)
    
    def compareExchangeStrong(expected: Ptr[T], desired: T): Boolean = atomic_compare_exchange_strong(underlying, expected, desired)
    def compareExchangeStrong(expected: Ptr[T], desired: T, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeStrong(expected: Ptr[T], desired: T, memoryOrder: memory_order): Boolean = atomic_compare_exchange_strong_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def compareExchangeWeak(expected: Ptr[T], desired: T): Boolean = atomic_compare_exchange_weak(underlying, expected, desired)
    def compareExchangeWeak(expected: Ptr[T], desired: T, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    def compareExchangeWeak(expected: Ptr[T], desired: T, memoryOrder: memory_order): Boolean = atomic_compare_exchange_weak_explicit(underlying, expected, desired, memoryOrder, memoryOrder)

    def fetchAdd(value: T): T = atomic_fetch_add(underlying, value)
    def fetchAdd(value: T, memoryOrder: memory_order): T = atomic_fetch_add_explicit(underlying, value, memoryOrder)

    def fetchSub(value: T): T = atomic_fetch_sub(underlying, value)
    def fetchSub(value: T, memoryOrder: memory_order): T = atomic_fetch_sub_explicit(underlying, value, memoryOrder)

    def fetchAnd(value: T): T = atomic_fetch_and(underlying, value)
    def fetchAnd(value: T, memoryOrder: memory_order): T = atomic_fetch_and_explicit(underlying, value, memoryOrder)

    def fetchOr(value: T): T = atomic_fetch_or(underlying, value)
    def fetchOr(value: T, memoryOrder: memory_order): T = atomic_fetch_or_explicit(underlying, value, memoryOrder)

    def fetchXor(value: T): T= atomic_fetch_xor(underlying, value)
    def fetchXor(value: T, memoryOrder: memory_order): T = atomic_fetch_xor_explicit(underlying, value, memoryOrder)

    def compareExchangeStrong(expectedValue: T, desired: T)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired)
    }
    def compareExchangeStrong(expectedValue: T, desired: T, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeStrong(expectedValue: T, desired: T, memoryOrder: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_strong_explicit(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired, memoryOrder, memoryOrder)
    }

    def compareExchangeWeak(expectedValue: T, desired: T): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired)
    }
    def compareExchangeWeak(expectedValue: T, desired: T, memoryOrderOnSuccess: memory_order, memoryOrderOnFailure: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired, memoryOrderOnSuccess, memoryOrderOnFailure)
    }
    def compareExchangeWeak(expectedValue: T, desired: T, memoryOrder: memory_order)(implicit dummy: DummyImplicit): Boolean = {
      val expectedPtr = stackalloc[AnyRef]()
      !expectedPtr = expectedValue
      atomic_compare_exchange_weak_explicit(underlying, expectedPtr.asInstanceOf[Ptr[T]], desired, memoryOrder, memoryOrder)
    }
  }

}

