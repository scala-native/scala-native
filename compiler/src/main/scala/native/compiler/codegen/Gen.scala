package native
package compiler
package codegen

import java.nio.ByteBuffer
import native.nir._

trait Gen extends ((Seq[Defn], ByteBuffer) => Unit)
