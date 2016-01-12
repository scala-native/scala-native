package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Op.Alloc
 */
trait AllocLowering extends Pass
