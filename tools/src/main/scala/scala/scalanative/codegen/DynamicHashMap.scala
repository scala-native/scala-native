package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Method}

class DynamicHashMap(meta: Metadata, cls: Class, proxies: Seq[Defn]) {
  val methods: Seq[Global] = {
    val own  = proxies.collect { case p if p.name.top == cls.name => p.name }
    val sigs = own.map(_.id).toSet
    cls.parent
      .fold(Seq.empty[Global])(meta.dynmap(_).methods)
      .filterNot(m => sigs.contains(m.id)) ++ own
  }
  val ty: Type =
    Type.StructValue(Global.None, Seq(Type.Int, Type.Ptr, Type.Ptr, Type.Ptr))
  val value: Val =
    DynmethodPerfectHashMap(methods, meta.linked.dynsigs)
}
