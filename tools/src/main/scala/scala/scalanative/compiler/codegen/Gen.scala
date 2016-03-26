package scala.scalanative
package compiler
package codegen

trait Gen extends ((Seq[nir.Defn], java.nio.ByteBuffer) => Unit)
