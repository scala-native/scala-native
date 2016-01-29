package native
package compiler
package pass

import native.nir._

class Lowering(val entryModule: Global)
  extends Pass
  with IntrinsicLowering
  with MainLowering
  with UnitLowering
  with ThrowLowering
  with InterfaceLowering
  with ObjectLowering
  with ArrayLowering
  with ModuleLowering
  with NothingLowering
  with StringLowering
