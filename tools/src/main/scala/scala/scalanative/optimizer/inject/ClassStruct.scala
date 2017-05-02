package scala.scalanative
package optimizer
package inject

import scala.collection.mutable.Buffer
import analysis.ClassHierarchy.Top
import nir._

/** Injects `main` trait dispatch tables.
 */
class ClassStruct(top: Top) extends Inject {
  def apply(buf: Buffer[Defn]): Unit = {

    top.classes.foreach { cls =>
      val struct = cls.layout.struct
      val defn   = Defn.Struct(Attrs.None, struct.name, struct.tys)
      buf += defn
    }
  }
}

object ClassStruct extends InjectCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ClassStruct(top)
}
