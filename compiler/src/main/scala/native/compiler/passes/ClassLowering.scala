package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Type.{ObjectClass, Class}
 *  - Defn.Class
 *  - Op.{FieldElem, MethodElem, AllocClass, Equals, HashCode} called on classes
 */
trait ClassLowering extends Pass
