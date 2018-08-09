package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.sema._

class DynamicHashMap(meta: Metadata, cls: Class, dyns: Seq[String]) {
  val methods: Seq[Method] = {
    val own  = cls.methods.filter(_.attrs.isDyn)
    val sigs = own.map(m => m.name.id).toSet
    cls.parent
      .fold(Seq.empty[Method])(meta.dynmap(_).methods)
      .filterNot(m => sigs.contains(m.name.id)) ++ own
  }
  val ty: Type =
    Type.Struct(Global.None, Seq(Type.Int, Type.Ptr, Type.Ptr, Type.Ptr))
  val value: Val =
    DynmethodPerfectHashMap(methods, dyns)
}
