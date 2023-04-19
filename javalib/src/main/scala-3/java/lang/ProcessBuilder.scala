// Due to enums source-compatibility reasons `ProcessBuilder` was split into two
// seperate files. `ProcessBuilder` contains constructors and Scala version specific
// definition of enums. `ProcessBuilderImpl` defines actual logic of ProcessBuilder
// that should be shared between both implementations
// Make sure to sync content of this file with its Scala 3 counterpart

package java.lang

import java.util.List
import java.io.File
import java.util.Arrays
import java.lang.process.ProcessBuilderImpl

final class ProcessBuilder(_command: List[String])
    extends ProcessBuilderImpl(_command) {
  def this(command: Array[String]) = {
    this(Arrays.asList(command))
  }

}

object ProcessBuilder {
  abstract class Redirect {
    def file(): File = null

    def `type`(): Redirect.Type

    override def equals(other: Any): scala.Boolean = other match {
      case that: Redirect => file() == that.file() && `type`() == that.`type`()
      case _              => false
    }

    override def hashCode(): Int = {
      var hash = 1
      hash = hash * 31 + file().hashCode()
      hash = hash * 31 + `type`().hashCode()
      hash
    }
  }

  object Redirect {
    private class RedirectImpl(tpe: Redirect.Type, redirectFile: File)
        extends Redirect {
      override def `type`(): Type = tpe

      override def file(): File = redirectFile

      override def toString =
        s"Redirect.$tpe${if (redirectFile != null) s": ${redirectFile}" else ""}"
    }

    val INHERIT: Redirect = new RedirectImpl(Type.INHERIT, null)

    val PIPE: Redirect = new RedirectImpl(Type.PIPE, null)

    def appendTo(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.APPEND, file)
    }

    def from(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.READ, file)
    }

    def to(file: File): Redirect = {
      if (file == null) throw new NullPointerException()
      new RedirectImpl(Type.WRITE, file)
    }

    enum Type extends Enum[Type]() {
      case PIPE extends Type
      case INHERIT extends Type
      case READ extends Type
      case WRITE extends Type
      case APPEND extends Type
    }
  }
}
