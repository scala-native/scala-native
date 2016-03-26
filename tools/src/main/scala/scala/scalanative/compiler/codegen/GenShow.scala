package scala.scalanative
package compiler
package codegen

trait GenShow extends Gen {
  def apply(defns: Seq[nir.Defn], buffer: java.nio.ByteBuffer) =
    buffer.put(showDefns(defns).toString.getBytes)

  def showDefns: util.Show[Seq[nir.Defn]]
}
