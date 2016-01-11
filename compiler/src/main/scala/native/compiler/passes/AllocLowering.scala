package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Op.Alloc
 */
trait AllocLowering extends Pass
