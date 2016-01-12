package native
package compiler
package pass

import native.nir._

/** Eliminates:
 *  - Op.{MonitorEnter, MonitorExit}
 */
trait MonitorLowering extends Pass
