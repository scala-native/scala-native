package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

class SaltySerializer(buffer: ByteBuffer) {
  import buffer._

  def putBuiltin(builtin: Builtin) = ???
  def putDefn(value: Defn) = ???
  def putInstr(instr: Instr) = ???
  def putParam(param: Param) = ???
  def putNext(next: Next) = ???
  def putName(name: Name) = ???
  def putOp(op: Op) = ???
  def putType(ty: Type) = ???
  def putVal(value: Val) = ???
}
