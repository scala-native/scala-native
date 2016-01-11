package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Val.Class
 *  - Type.ClassClass
 *  - Op.{AsInstanceOf, IsInstanceOf, GetClass}
 */
trait TagLowering extends Pass
