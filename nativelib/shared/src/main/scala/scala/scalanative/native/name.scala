package scala.scalanative
package native

import scala.annotation.StaticAnnotation
import scala.annotation.meta.{field, getter, setter}

/** An annotation that is used to refer to differently-named native symbol. */
@field
@getter
@setter
final class name(name: String) extends StaticAnnotation
