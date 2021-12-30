package org.scalanative

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.scalanative.api._

class NativeCompilerTest extends AnyFlatSpec:

  def nativeCompilation(source: String): Unit = {
    try scalanative.NIRCompiler(_.compile(source))
    catch {
      case ex: CompilationFailedException =>
        fail(s"Failed to compile source: ${ex.getMessage}", ex)
    }
  }

  "The Scala Native compiler plugin" should "compile t8612" in nativeCompilation("""
    |object Foo1:
    |  def assert1(x: Boolean) = if !x then ???
    |  inline def assert2(x: Boolean) = if !x then ???
    |  inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |
    |object Foo2:
    |  def assert1(x: Boolean) = if !x then ???
    |  transparent inline def assert2(x: Boolean) = if !x then ???
    |  transparent inline def assert3(inline x: Boolean) = if !x then ???
    |
    |  assert1(???)
    |  assert2(???)
    |  assert3(???)
    |""".stripMargin)

  it should "compile i505" in nativeCompilation("""
  |object Test {
  |  def main(args: Array[String]): Unit = {
  |    val a: Int = synchronized(1)
  |    val b: Long = synchronized(1L)
  |    val c: Boolean = synchronized(true)
  |    val d: Float = synchronized(1f)
  |    val e: Double = synchronized(1.0)
  |    val f: Byte = synchronized(1.toByte)
  |    val g: Char = synchronized('1')
  |    val h: Short = synchronized(1.toShort)
  |    val i: String = synchronized("Hello")
  |    val j: List[Int] = synchronized(List(1))
  |    synchronized(())
  |  }
  |}
  """.stripMargin)
