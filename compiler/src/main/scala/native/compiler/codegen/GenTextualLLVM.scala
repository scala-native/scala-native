package native
package compiler
package codegen

import native.nir._
import native.util.{sh, Show}

object GenTextualLLVM extends GenShow {
  implicit def showDefns: Show[Seq[Defn]] = ???
}
