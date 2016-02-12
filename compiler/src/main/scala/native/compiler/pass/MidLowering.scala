package native
package compiler
package pass

import native.nir._

class MidLowering
  extends Pass
  with CopyLowering
  with UnitLowering
  with NothingLowering
