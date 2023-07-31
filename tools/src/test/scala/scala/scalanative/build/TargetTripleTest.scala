package scala.scalanative.build

import org.junit.Test
import org.junit.Assert._

class TargetTripleTest {

  val cases = List(
    "aarch64-unknown-linux-gnu" ->
      TargetTriple("aarch64", "unknown", "linux", "gnu"),
    "arm64-apple-darwin22.4.0" ->
      TargetTriple("aarch64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin13.4.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin20.6.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin21.6.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-apple-darwin22.4.0" ->
      TargetTriple("x86_64", "apple", "darwin", "unknown"),
    "x86_64-pc-linux-gnu" ->
      TargetTriple("x86_64", "pc", "linux", "gnu"),
    "x86_64-pc-windows-msvc" ->
      TargetTriple("x86_64", "pc", "windows", "msvc"),
    "x86_64-portbld-freebsd13.1" ->
      TargetTriple("x86_64", "unknown", "freebsd", "unknown")
  )

  @Test
  def testParser(): Unit = cases.foreach {
    case (triple, expected) =>
      assertEquals(triple, expected, TargetTriple.parse(triple))
  }
}
