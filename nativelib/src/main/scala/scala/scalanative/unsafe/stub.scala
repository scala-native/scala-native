package scala.scalanative
package unsafe

import scala.annotation.StaticAnnotation

/** An annotation that is used to indicate that a given method
 *  is provided as a stub, but is not currently supported. These
 *  methods are not discovered by the linker by default, but will
 *  be discovered only if a special flag is enabled.
 */
final class stub extends StaticAnnotation
