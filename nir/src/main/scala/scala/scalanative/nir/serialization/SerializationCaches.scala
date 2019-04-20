package scala.scalanative
package nir
package serialization

import java.{util => ju}
import java.lang.ref.WeakReference

private[scalanative] class SerializationCaches(
    val sharedArr128: Array[Byte],
    val sharedArr256: Array[Byte],
    val sharedArr512: Array[Byte],
    val sharedArr4096: Array[Byte],
    val internedStrings: ju.WeakHashMap[String, WeakReference[String]]
)

private[scalanative] object SerializationCaches {
  def empty(
      internedStrings: ju.WeakHashMap[String, WeakReference[String]]
  ): SerializationCaches = new SerializationCaches(
    new Array[Byte](128),
    new Array[Byte](256),
    new Array[Byte](512),
    new Array[Byte](4096),
    internedStrings
  )
}
