package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Val.Intrinsic
 */
trait IntrinsicLowering extends Pass
