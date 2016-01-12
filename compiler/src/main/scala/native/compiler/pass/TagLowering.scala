package native
package compiler
package pass

import native.nir._

/** Numbers all the classes in the module and
 *  assigns a unique id (tag) to each one of them.
 *  Tags are used to implement j.l.Class, instance checks
 *  and casts.
 *
 *  Eliminates:
 *  - Val.Class
 *  - Type.ClassClass
 *  - Op.{AsInstanceOf, IsInstanceOf, GetClass}
 */
trait TagLowering extends Pass
