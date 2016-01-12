package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Val.String
 *  - Type.StringClass
 *  - Op.{StringConcat, FromString, ToString}
 */
trait StringLowering extends Pass
