package scala.scalanative
package unsafe

import scala.annotation.meta.*

/** An annotation that is used to automatically define a macro when the
 *  annotated symbol is used.
 */
@field @getter @setter
final class define private () extends scala.annotation.StaticAnnotation {

  /** Define a macro like `-Dname` */
  def this(name: String) = this()

}
