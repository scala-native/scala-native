package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Type.Unit
 */
trait UnitLowering extends Pass
