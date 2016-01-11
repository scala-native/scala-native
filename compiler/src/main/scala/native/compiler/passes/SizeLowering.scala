package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Type.Size
 *  - Op.Size
 */
trait SizeLowering extends Pass
