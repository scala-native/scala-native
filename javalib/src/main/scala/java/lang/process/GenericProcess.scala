package java.lang.process

import scala.scalanative.unsafe._

abstract class GenericProcess extends java.lang.Process {
  private[lang] def checkResult(): CInt
}
