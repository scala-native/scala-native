package native
package compiler
package pass

import native.nir._

object Lowering extends Pass
                   with IntrinsicLowering
                   with UnitLowering
                   with ThrowLowering
                   with InterfaceLowering
                   with ObjectLowering
                   with ArrayLowering
                   with ModuleLowering
                   with NothingLowering
                   with NullLowering
                   with StringLowering
