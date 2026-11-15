package scala.scalanative
package codegen

import scalanative.linker.Class

/** Reflective proxies of the program, one entry per class and proxy signature
 *  it responds to, inherited proxies included.
 *
 *  An open-addressing table of `{class rtti, signature key, proxy}` entries, at
 *  most half full. A lookup for class C and signature key K starts at
 *  `hash(classKey(C), K) & mask` and probes linearly until an entry with C's
 *  rtti and K, or an empty one. The program registers the table at startup, so
 *  class RTTI and code calling reflectively do not depend on it.
 */
private[codegen] object ReflectiveProxyTable {

  val name: nir.Global.Member =
    nir.Global
      .Top("__scalanative_metadata")
      .member(nir.Sig.Generated("reflective_proxies"))

  // Keep in sync with dyndispatch.c
  private val entryType = nir.Type.StructValue(
    nir.Type.Ptr :: nir.Type.Int :: nir.Type.Ptr :: Nil
  )
  private val emptyEntry =
    nir.Val.StructValue(nir.Val.Null :: nir.Val.Int(0) :: nir.Val.Null :: Nil)

  /** Start of the lookup, keep in sync with dyndispatch.c */
  def hash(classKey: Int, signatureKey: Int): Int =
    (signatureKey * 0x9e3779b1) ^ classKey

  def defn(proxies: Seq[nir.Defn])(implicit meta: Metadata): nir.Defn.Const = {
    val byClass: Map[nir.Global.Top, Seq[nir.Global.Member]] =
      proxies
        .map(_.name)
        .collect { case name: nir.Global.Member => name }
        .groupBy(_.top)

    // Own proxies, then inherited ones that are not overridden
    val responds =
      scala.collection.mutable.Map.empty[Class, Seq[nir.Global.Member]]
    def proxiesOf(cls: Class): Seq[nir.Global.Member] =
      responds.getOrElseUpdate(
        cls, {
          val own = byClass.getOrElse(cls.name, Nil)
          val sigs = own.map(_.sig).toSet
          cls.parent
            .fold(Seq.empty[nir.Global.Member])(proxiesOf)
            .filterNot(m => sigs.contains(m.sig)) ++ own
        }
      )

    val entries = for {
      cls <- meta.classes
      proxy <- proxiesOf(cls)
    } yield (cls, StableHash.dynsigKey(proxy.sig), proxy)

    val size = OpenAddressing.nextPowerOfTwo(entries.size * 2)
    val mask = size - 1
    val (slots, _) = OpenAddressing.place(size, emptyEntry)(entries.map {
      case (cls, key, proxy) =>
        (hash(StableHash.classKey(cls), key) & mask) -> nir.Val.StructValue(
          meta.rtti(cls).const :: nir.Val.Int(key) ::
            nir.Val.Global(proxy, nir.Type.Ptr) :: Nil
        )
    })

    val table = nir.Val.StructValue(
      nir.Val.Int(mask) ::
        nir.Val.Const(nir.Val.ArrayValue(entryType, slots)) :: Nil
    )
    nir.Defn.Const(nir.Attrs.None, name, table.ty, table)(
      nir.SourcePosition.NoPosition
    )
  }
}
