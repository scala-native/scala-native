package java.io

import java.util.LinkedList
import java.util.HashMap

//implementation of the few used methods from
//org.apache.harmony.luni.internal.io.FileCanonPathCache
object FileCanonPathCache {

  private class CacheElement private () {
    var canonicalPath: String = _
    var timestamp: Long       = _

    def this(canonPath: String) = {
      this()
      canonicalPath = canonPath
      timestamp = System.currentTimeMillis()
    }
  }

  @volatile
  var timeout: Long = 30000;

  private val CACHE_SIZE: Int = 256

  private val cache: HashMap[String, CacheElement] =
    new HashMap[String, CacheElement](CACHE_SIZE)

  private val list: LinkedList[String] = new LinkedList[String]()

  def get(path: String): String = {
    var localTimeout: Long = timeout;
    if (localTimeout == 0) {
      return null
    }

    var element: CacheElement = null
    synchronized {
      element = cache.get(path)
    }

    if (element == null) {
      return null
    }

    var time: Long = System.nanoTime() / 1000;
    if (time - element.timestamp > localTimeout) {
      // remove all elements older than this one
      synchronized /* (lock) */ {
        if (cache.get(path) != null) {
          var oldest: String = null
          do {
            oldest = list.removeFirst()
            cache.remove(oldest)
          } while (!path.equals(oldest))
        }
      }
      return null
    }

    return element.canonicalPath
  }

  def put(path: String, canonicalPath: String): Unit = {
    if (timeout != 0) {
      var element: CacheElement = new CacheElement(canonicalPath)
      synchronized {
        if (cache.size() >= CACHE_SIZE) {
          val oldest: String = list.removeFirst()
          cache.remove(oldest)
        }
        cache.put(path, element)
        list.addLast(path)
      }
    }
  }
}
