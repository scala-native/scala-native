package scala.scalanative
package runtime

import native._

@extern
object Intrinsics {
  def `llvm.sqrt.f32`(value: Float): Float                        = extern
  def `llvm.sqrt.f64`(value: Double): Double                      = extern
  def `llvm.powi.f32`(value: Float, power: Int): Float            = extern
  def `llvm.powi.f64`(value: Double, power: Int): Double          = extern
  def `llvm.sin.f32`(value: Float): Float                         = extern
  def `llvm.sin.f64`(value: Double): Double                       = extern
  def `llvm.cos.f32`(value: Float): Float                         = extern
  def `llvm.cos.f64`(value: Double): Double                       = extern
  def `llvm.pow.f32`(value: Float, power: Float): Float           = extern
  def `llvm.pow.f64`(value: Double, power: Double): Double        = extern
  def `llvm.exp.f32`(value: Float): Float                         = extern
  def `llvm.exp.f64`(value: Double): Double                       = extern
  def `llvm.exp2.f32`(value: Float): Float                        = extern
  def `llvm.exp2.f64`(value: Double): Double                      = extern
  def `llvm.log.f32`(value: Float): Float                         = extern
  def `llvm.log.f64`(value: Double): Double                       = extern
  def `llvm.log10.f32`(value: Float): Float                       = extern
  def `llvm.log10.f64`(value: Double): Double                     = extern
  def `llvm.log2.f32`(value: Float): Float                        = extern
  def `llvm.log2.f64`(value: Double): Double                      = extern
  def `llvm.fma.f32`(a: Float, b: Float, c: Float): Float         = extern
  def `llvm.fma.f64`(a: Double, b: Double, c: Double): Double     = extern
  def `llvm.fabs.f32`(value: Float): Float                        = extern
  def `llvm.fabs.f64`(value: Double): Double                      = extern
  def `llvm.minnum.f32`(left: Float, right: Float): Float         = extern
  def `llvm.minnum.f64`(left: Double, right: Double): Double      = extern
  def `llvm.maxnum.f32`(left: Float, right: Float): Float         = extern
  def `llvm.maxnum.f64`(left: Double, right: Double): Double      = extern
  def `llvm.copysign.f32`(magn: Float, sgn: Float): Float         = extern
  def `llvm.copysign.f64`(magn: Double, sgn: Double): Double      = extern
  def `llvm.floor.f32`(value: Float): Float                       = extern
  def `llvm.floor.f64`(value: Double): Double                     = extern
  def `llvm.ceil.f32`(value: Float): Float                        = extern
  def `llvm.ceil.f64`(value: Double): Double                      = extern
  def `llvm.trunc.f32`(value: Float): Float                       = extern
  def `llvm.trunc.f64`(value: Double): Double                     = extern
  def `llvm.rint.f32`(value: Float): Float                        = extern
  def `llvm.rint.f64`(value: Double): Double                      = extern
  def `llvm.nearbyint.f32`(value: Float): Float                   = extern
  def `llvm.nearbyint.f64`(value: Double): Double                 = extern
  def `llvm.round.f32`(value: Float): Float                       = extern
  def `llvm.round.f64`(value: Double): Double                     = extern
  def `llvm.bitreverse.i16`(value: Short): Short                  = extern
  def `llvm.bitreverse.i32`(value: Int): Int                      = extern
  def `llvm.bitreverse.i64`(value: Long): Long                    = extern
  def `llvm.bswap.i16`(value: Short): Short                       = extern
  def `llvm.bswap.i32`(value: Int): Int                           = extern
  def `llvm.bswap.i64`(value: Long): Long                         = extern
  def `llvm.ctpop.i16`(value: Short): Short                       = extern
  def `llvm.ctpop.i32`(value: Int): Int                           = extern
  def `llvm.ctpop.i64`(value: Long): Long                         = extern
  def `llvm.ctlz.i8`(source: Byte, iszeroundef: Boolean): Byte    = extern
  def `llvm.ctlz.i16`(source: Short, iszeroundef: Boolean): Short = extern
  def `llvm.ctlz.i32`(source: Int, iszeroundef: Boolean): Int     = extern
  def `llvm.ctlz.i64`(source: Long, iszeroundef: Boolean): Long   = extern
  def `llvm.cttz.i8`(source: Byte, iszeroundef: Boolean): Byte    = extern
  def `llvm.cttz.i16`(source: Short, iszeroundef: Boolean): Short = extern
  def `llvm.cttz.i32`(source: Int, iszeroundef: Boolean): Int     = extern
  def `llvm.cttz.i64`(source: Long, iszeroundef: Boolean): Long   = extern
  def `llvm.memset.p0i8.i32`(dest: Ptr[Byte],
                             value: Byte,
                             len: Int,
                             align: Int,
                             isvolatile: Boolean): Unit = extern
  def `llvm.memset.p0i8.i64`(dest: Ptr[Byte],
                             value: Byte,
                             len: Long,
                             align: Int,
                             isvolatile: Boolean): Unit = extern
  def `llvm.memcpy.p0i8.p0i8.i32`(dest: Ptr[Byte],
                                  src: Ptr[Byte],
                                  len: Int,
                                  align: Int,
                                  isvolatile: Boolean): Unit = extern
  def `llvm.memcpy.p0i8.p0i8.i64`(dest: Ptr[Byte],
                                  src: Ptr[Byte],
                                  len: Long,
                                  align: Int,
                                  isvolatile: Boolean): Unit = extern
}
