package test

import scala.scalanative.native.extern

@extern
object powi {
  def `llvm.powi.f32`(value: Float, power: Int): Float = extern
}

object Test {
  def main(args: Array[String]): Unit = {
    powi.`llvm.powi.f32`(42.0f, 2)
  }
}
