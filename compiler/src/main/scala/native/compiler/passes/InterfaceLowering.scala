package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Type.InterfaceClass
 *  - Defn.Interface
 *  - Op.{FieldElem, MethodElem, AllocClass, Equals, HashCode} called on interfaces
 */
trait InterfaceLowering extends Pass
