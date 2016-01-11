package native
package compiler
package passes

import native.nir._

/** Eliminates:
 *  - Op.{MonitorEnter, MonitorExit}
 */
trait MonitorLowering extends Pass
