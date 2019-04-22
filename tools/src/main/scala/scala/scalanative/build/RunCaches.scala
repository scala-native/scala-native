package scala.scalanative.build

import scala.scalanative.nir.serialization.SerializationCaches

import java.{util => ju}
import java.lang.ref.WeakReference

private[scalanative] class RunCaches(
    val internedStrings: ju.WeakHashMap[String, WeakReference[String]],
    val serializationCaches: SerializationCaches
)

private[scalanative] object RunCaches {
  val empty: RunCaches = {
    val sharedInternedStrings =
      new ju.WeakHashMap[String, WeakReference[String]]()
    new RunCaches(
      sharedInternedStrings,
      SerializationCaches.empty(sharedInternedStrings)
    )
  }
}
