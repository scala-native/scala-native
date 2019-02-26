package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Method}

class DynamicHashMap(meta: Metadata, cls: Class, proxies: Seq[Defn]) {
  val methods: Seq[Global.Member] = {
    val own = proxies.collect {
      case p if p.name.top == cls.name =>
        p.name.asInstanceOf[Global.Member]
    }
    val sigs = own.map(_.sig).toSet
    cls.parent
      .fold(Seq.empty[Global.Member])(meta.dynmap(_).methods)
      .filterNot(m => sigs.contains(m.sig)) ++ own
  }
  val ty: Type =
    Type.Ptr
  val value: Val =
    DynmethodPerfectHashMap(methods, meta.linked.dynsigs)
}
