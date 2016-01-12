package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Type.Size
 *  - Op.Size
 */
trait SizeLowering extends Pass
