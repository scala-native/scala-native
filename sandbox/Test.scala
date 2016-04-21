package test

import scala.scalanative.native.extern

@extern
object Foo {
  val foo: Int = extern
  var bar: Int = extern
  def baz(x: Int, y: Int): Int = extern
  def `llvm.powi.f32`(value: Float, power: Int): Float = extern
}

object Test {
  def main(args: Array[String]): Unit = ()
}
