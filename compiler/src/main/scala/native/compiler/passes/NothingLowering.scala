package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Type.NothingClass
 */
trait NothingLowering extends Pass
