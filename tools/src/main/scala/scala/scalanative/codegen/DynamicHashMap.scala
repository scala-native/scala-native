package scala.scalanative
package codegen

import scalanative.linker.{Class, Method}

private[codegen] object DynamicHashMap {
  final val ty: nir.Type = nir.Type.Ptr
}

private[codegen] class DynamicHashMap(cls: Class, proxies: Seq[nir.Defn])(implicit
    meta: Metadata
) {

  val methods: Seq[nir.Global.Member] = {
    val own = proxies.collect {
      case p if p.name.top == cls.name =>
        p.name.asInstanceOf[nir.Global.Member]
    }
    val sigs = own.map(_.sig).toSet
    cls.parent
      .fold(Seq.empty[nir.Global.Member])(meta.dynmap(_).methods)
      .filterNot(m => sigs.contains(m.sig)) ++ own
  }

  val value: nir.Val = DynmethodPerfectHashMap(methods, meta.analysis.dynsigs)

}
