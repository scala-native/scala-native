package scala.scalanative

/**
 * A `ClassLoader` that will let `parent` load classes that satisfy `allow`, and
 * throw a `ClassNotFoundException` for all other classes.
 */
class FilteredClassLoader(allow: String => Boolean, parent: ClassLoader)
    extends ClassLoader(parent) {

  override def loadClass(className: String, resolve: Boolean): Class[_] =
    if (allow(className))
      super.loadClass(className, resolve)
    else
      throw new ClassNotFoundException(className)

}
