package scala.scalanative.testinterface

/** A `ClassLoader` that returns pre-instanciated objects.
 *  We use it to bypass uses of reflection in the test interface.
 */
class PreloadedClassLoader(map: Map[String, AnyRef]) extends ClassLoader {
  def loadPreloaded(name: String): AnyRef =
    map.getOrElse(name, null)
}
