package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Val.String
 *  - Type.StringClass
 *  - Op.{StringConcat, FromString, ToString}
 */
trait StringLowering extends Pass
