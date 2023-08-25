package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Method}

object DynamicHashMap {
  final val ty: Type = Type.Ptr
}

class DynamicHashMap(cls: Class, proxies: Seq[Defn])(implicit meta: Metadata) {
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
  val value: Val = DynmethodPerfectHashMap(methods, meta.analysis.dynsigs)
}
