package scala.scalanative
package annotation

import scala.scalanative.meta.LinktimeInfo.contendedPaddingWidth

/** Allows to align field or class layout to expected size reflected in number
 *  of bytes. Can be aliased as `Contended` for cross-compiling with the JVM.
 *  @param size
 *    Size of the alignment represented in number of bytes
 *  @param group
 *    Optional tag allowing to put multiple fields in the same aligned memory
 *    area
 */
final class align(size: Int, group: String)
    extends scala.annotation.StaticAnnotation {
  def this(size: Int) = this(size, "")

  // JVM  Contended compat

  /** Dynamic, platform specific alignment. Can be used as replacement JVM
   *  \@Contended
   */
  def this(group: String) = this(contendedPaddingWidth, group)

  /** Dynamic, platform specific alignment. Can be used as replacement JVM
   *  \@Contended
   */
  def this() = this(contendedPaddingWidth, "")
}
