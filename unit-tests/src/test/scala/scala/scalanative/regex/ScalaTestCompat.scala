package scala.scalanative
package regex

// Provide simple implementations for methods commonly used by
// tests originally writted for Scalatest FunSuite.

object ScalaTestCompat {

  def fail() = assert(false, "Who knows why? No message")

  def fail(msg: String) = assert(false, msg)

  def ignore(msg: String)(block: => Unit): Unit = {}

  def pending(): Unit = {}

}
