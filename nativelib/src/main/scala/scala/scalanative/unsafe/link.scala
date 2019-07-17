package scala.scalanative
package unsafe

/** An annotation that is used to automatically link with
 *  native library whenever an annotated extern object is used.
 */
final class link(name: String) extends scala.annotation.StaticAnnotation
