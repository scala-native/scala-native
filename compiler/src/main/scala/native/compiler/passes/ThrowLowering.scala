package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Op.Throw
 */
trait ThrowLowering extends Pass
