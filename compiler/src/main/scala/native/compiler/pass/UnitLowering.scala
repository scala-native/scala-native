package native
package compiler
package pass

import native.nir._

/** Eliminates unit type and unit value.
 *
 *  Eliminates:
 *  - Val.Unit
 *  - Type.Unit
 */
trait UnitLowering extends Pass
