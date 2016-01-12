package native
package compiler
package pass

import native.nir._

/** Desugars throws into calls to runtime implementation
 *  of throw with corresponding undefined terminator after it.
 *
 *  Eliminates:
 *  - Op.Throw
 */
trait ThrowLowering extends Pass
