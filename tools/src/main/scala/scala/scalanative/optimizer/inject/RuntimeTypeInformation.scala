package scala.scalanative
package optimizer
package inject

import scala.collection.mutable.Buffer
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules, traits and structs. */
class RuntimeTypeInformation(implicit top: Top, fresh: Fresh) extends Inject {

  def injectClassType(buf: Buffer[Defn], cls: Class) {
    val typeDefn =
      Defn.Const(Attrs.None, cls.typeName, cls.typeStruct, cls.typeValue)

    buf += typeDefn
  }

  def injectType(buf: Buffer[Defn], node: Scope): Unit = {
    val typeId   = Val.Int(node.id)
    val typeStr  = Val.String(node.name.id)
    val typeSize = Val.Long(-1)
    val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeStr, typeSize))
    val typeDefn = Defn.Const(Attrs.None, node.typeName, Rt.Type, typeVal)

    buf += typeDefn
  }

  override def apply(buf: Buffer[Defn]) = {
    top.classes.foreach(injectClassType(buf, _))
    top.traits.foreach(injectType(buf, _))
    top.structs.foreach(injectType(buf, _))
  }
}

object RuntimeTypeInformation extends InjectCompanion {
  override val depends =
    Seq(Rt.Type.name)

  override def apply(config: tools.Config, top: Top) =
    new RuntimeTypeInformation()(top, top.fresh)
}
