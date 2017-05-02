package scala.scalanative
package optimizer
package inject

import scala.collection.mutable.Buffer
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules, traits and structs. */
class RuntimeTypeInformation(implicit top: Top, fresh: Fresh) extends Inject {
  override def apply(buf: Buffer[Defn]) = {
    def inject(node: Scope) = {
      buf += Defn.Const(Attrs.None,
                        node.rtti.name,
                        node.rtti.struct,
                        node.rtti.value)
    }
    top.classes.foreach(inject)
    top.traits.foreach(inject)
    top.structs.foreach(inject)
  }
}

object RuntimeTypeInformation extends InjectCompanion {
  override def apply(config: tools.Config, top: Top) =
    new RuntimeTypeInformation()(top, top.fresh)
}
