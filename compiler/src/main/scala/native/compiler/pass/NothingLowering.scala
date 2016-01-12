package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.NothingClass
 */
trait NothingLowering extends Pass
