package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

class SaltyDeserializer(bb: ByteBuffer) {
  import bb._

  def getBuiltin(): Builtin = ???
  def getDefn(): Defn = ???
  def getInstr(): Instr = ???
  def getParam(): Param = ???
  def getNext(): Next = ???
  def getName(): Name = ???
  def getOp(): Op = ???
  def getType(): Type = ???
  def getVal(): Val = ???
}
