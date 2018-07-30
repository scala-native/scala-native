package scala.scalanative
package optimizer

import scala.collection.mutable
import nir._

trait Pass {

  def onInsts(insts: Seq[Inst]): Seq[Inst]
}
