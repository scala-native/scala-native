package scala.scalanative
package compiler
package codegen

abstract class Gen(assembly: Seq[nir.Defn]) {
  def gen(to: java.nio.ByteBuffer)
}
