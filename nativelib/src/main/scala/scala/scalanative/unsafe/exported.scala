package scala.scalanative.unsafe

/** An annotation that is used to mark methods that should be treated as library
 *  entry point
 */
final class exported(name: String) extends scala.annotation.StaticAnnotation {
  def this() = this(name = null)
}

/** An annotation that is used to mark static fields for which should be
 *  generated external accesor (entry points in library)
 */
final class exportedAccessor(getterName: String, setterName: String)
    extends scala.annotation.StaticAnnotation {
  def this(name: String) = this(getterName = name, null)
  def this() = this(null, null)
}
