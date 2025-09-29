package scala.scalanative.runtime

import scala.collection.mutable

import scala.scalanative.unsafe.{Zone, toCString}

object DeleteOnExit {
  private val toDeleteSet: mutable.Set[String] = mutable.Set.empty

  lazy val setupShutdownHook = Runtime.getRuntime().addShutdownHook {
    val t = new Thread(() => {
      Zone.acquire { implicit z =>
        toDeleteSet.foreach(f => ffi.remove(toCString(f)))
      }
    })
    t.setName("shutdown-hook:delete-on-exit")
    t
  }
  def addFile(name: String) = toDeleteSet.synchronized {
    toDeleteSet.add(name)
    setupShutdownHook
  }
}
