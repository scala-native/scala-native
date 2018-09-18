package scala.scalanative.runtime

import scala.collection.mutable
import scala.scalanative.native.{Zone, toCString}

object DeleteOnExit {
  private val toDeleteSet: mutable.Set[String] = mutable.Set.empty
  private val toDelete: mutable.ArrayBuffer[String] =
    mutable.ArrayBuffer.empty
  Shutdown.addHook(() =>
    toDelete.foreach { f =>
      Zone { implicit z =>
        libc.remove(toCString(f))
      }
  })
  def addFile(name: String) = toDelete.synchronized {
    if (toDeleteSet.add(name)) toDelete += name
  }
}
