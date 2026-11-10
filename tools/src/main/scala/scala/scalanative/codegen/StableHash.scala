package scala.scalanative
package codegen

import java.nio.charset.StandardCharsets

/** Hashes that depend only on their input, never on the rest of the program,
 *  the JVM or the Scala version, so code emitted for one class stays the same
 *  when unrelated classes change.
 */
private[codegen] object StableHash {

  /** 32-bit FNV-1a over the UTF-8 encoding of `value`. */
  def fnv1a32(value: String): Int = {
    var hash = 0x811c9dc5
    val bytes = value.getBytes(StandardCharsets.UTF_8)
    var i = 0
    while (i < bytes.length) {
      hash ^= bytes(i) & 0xff
      hash *= 0x01000193
      i += 1
    }
    hash
  }

  /** Key of a trait in itables: decides where lookups for the trait start. */
  def traitKey(trt: linker.Trait): Int = fnv1a32(trt.name.mangle)

  /** Key of a reflective proxy signature, see ReflectiveProxyTable. */
  def dynsigKey(sig: nir.Sig): Int = fnv1a32(sig.mangle)

  /** Low 16 bits of a hash of the class name, stored in class RTTI flags to
   *  start lookups in the reflective proxy table.
   */
  def classKey(cls: linker.Class): Int = fnv1a32(cls.name.mangle) & 0xffff
}
