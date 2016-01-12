package native
package compiler
package pass

import native.nir._

object Lowering extends Pass
                   with AllocLowering
                   with ArrayLowering
                   with BoxLowering
                   with InterfaceLowering
                   with ClassLowering
                   with ModuleLowering
                   with MonitorLowering
                   with NothingLowering
                   with NullLowering
                   with StringLowering
                   with TagLowering
                   with ThrowLowering
                   with UnitLowering
