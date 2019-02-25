package scala.scalanative
package interflow

import scalanative.nir._

final case class MergePhi(param: Val.Local, incoming: Seq[(Local, Val)])
