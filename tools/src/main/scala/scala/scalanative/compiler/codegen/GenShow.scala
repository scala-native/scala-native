package scala.scalanative
package compiler
package codegen

abstract class GenShow(assembly: Seq[nir.Defn]) extends Gen(assembly) {
  def gen(buffer: java.nio.ByteBuffer) =
    buffer.put(showDefns(assembly).toString.getBytes)

  def showDefns: util.Show[Seq[nir.Defn]]
}
