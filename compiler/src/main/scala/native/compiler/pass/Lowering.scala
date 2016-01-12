package native
package compiler
package pass

import native.nir._

object Lowering extends Pass
                   with AllocLowering
                   with ArrayLowering
                   with BoxLowering
                   with ClassLowering
                   with InterfaceLowering
                   with ModuleLowering
                   with MonitorLowering
                   with NothingLowering
                   with NullLowering
                   with SizeLowering
                   with StringLowering
                   with TagLowering
                   with ThrowLowering
                   with UnitLowering

