package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Op.{Box, Unbox}
 *  - Type.{Character, Boolean, Byte, Short, Integer, Long, Float, Double}Class
 */
trait BoxLowering extends Pass
