package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Val.Null
 *  - Op.Zero[T] where T is class
 *  - Type.NullClass
 */
trait NullLowering extends Pass
