package scala.scalanative
package optimizer
package inject

import scala.collection.mutable.Buffer
import analysis.ClassHierarchy.Top
import nir._

/** Injects `main` trait dispatch tables.
 */
class TraitDispatchTables(top: Top) extends Inject {

  def apply(buf: Buffer[Defn]): Unit = {
    buf += top.tables.dispatchDefn
    buf += top.tables.classHasTraitDefn
    buf += top.tables.traitHasTraitDefn
  }
}

object TraitDispatchTables extends InjectCompanion {
  override def apply(config: tools.Config, top: Top) =
    new TraitDispatchTables(top)
}
