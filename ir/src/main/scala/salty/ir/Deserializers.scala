package salty.ir

import java.nio.ByteBuffer
import salty.ir.Tag.Tag

object Deserializers {
  implicit class RichGet(val bb: ByteBuffer) extends AnyVal {
    import bb._

    def getTag: Tag = getInt

    def getType: Type = ???
    def getInstr: Instr = ???
    def getDefn: Defn = ???
    def getRel: Rel = ???
  }
}
