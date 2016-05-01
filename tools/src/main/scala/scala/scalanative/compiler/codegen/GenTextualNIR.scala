package scala.scalanative
package compiler
package codegen

class GenTextualNIR(assembly: Seq[nir.Defn]) extends GenShow(assembly) {
  val showDefns = nir.Shows.showDefns
}
