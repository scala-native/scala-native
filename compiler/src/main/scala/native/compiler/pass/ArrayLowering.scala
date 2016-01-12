package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.ArrayClass
 *  - Op.{AllocArray, ArrayLength, ArrayElem}
 */
trait ArrayLowering extends Pass
