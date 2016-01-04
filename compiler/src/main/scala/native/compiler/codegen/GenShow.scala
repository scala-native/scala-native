package native
package compiler
package codegen

import java.nio.ByteBuffer
import native.nir._
import native.util.Show

trait GenShow extends Gen {
  def apply(defns: Seq[Defn], buffer: ByteBuffer) =
    buffer.put(showDefns(defns).toString.getBytes)

  def showDefns: Show[Seq[Defn]]
}
