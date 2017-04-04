package java.lang

import scala.collection.mutable
import scalanative.native._
import scalanative.runtime.unwind

class Throwable(message: String, private var cause: Throwable)
    extends Object
    with java.io.Serializable {
  def this() = this(null, null)
  def this(message: String) = this(message, null)
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)

  private var stackTrace: Array[StackTraceElement] = _

  fillInStackTrace()

  def initCause(cause: Throwable): Throwable = {
    this.cause = cause
    this
  }

  def getMessage(): String = message

  def getCause(): Throwable = cause

  def getLocalizedMessage(): String = getMessage()

  def fillInStackTrace(): Throwable = {
    val cursor  = stackalloc[scala.Byte](2048)
    val context = stackalloc[scala.Byte](2048)
    val offset  = stackalloc[scala.Byte](8)
    val name    = stackalloc[CChar](256)
    var buffer  = mutable.ArrayBuffer.empty[StackTraceElement]

    unwind.get_context(context)
    unwind.init_local(cursor, context)
    while (unwind.step(cursor) > 0) {
      unwind.get_proc_name(cursor, name, 256, offset)
      buffer += new StackTraceElement(fromCString(name))
    }

    this.stackTrace = buffer.toArray
    this
  }

  def getStackTrace(): Array[StackTraceElement] = {
    if (stackTrace eq null) {
      stackTrace = Array.empty
    }
    stackTrace
  }

  def setStackTrace(stackTrace: Array[StackTraceElement]): Unit = {
    var i = 0
    while (i < stackTrace.length) {
      if (stackTrace(i) eq null)
        throw new NullPointerException()
      i += 1
    }

    this.stackTrace = stackTrace.clone()
  }

  def printStackTrace(): Unit =
    printStackTrace(System.err)

  def printStackTrace(ps: java.io.PrintStream): Unit =
    printStackTrace(ps.println(_: String))

  def printStackTrace(pw: java.io.PrintWriter): Unit =
    printStackTrace(pw.println(_: String))

  private def printStackTrace(println: String => Unit): Unit = {
    // Init to empty stack trace if it's null
    getStackTrace()

    // Print curent stack trace
    println(toString)
    if (stackTrace.nonEmpty) {
      var i = 0
      while (i < stackTrace.length) {
        println("\tat " + stackTrace(i))
        i += 1
      }
    } else {
      println("\t<no stack trace available>")
    }

    // Print causes
    var parentStack = stackTrace
    var throwable   = getCause()
    while (throwable != null) {
      println("Caused by: " + throwable)

      val currentStack = throwable.stackTrace
      if (currentStack.nonEmpty) {
        val duplicates = countDuplicates(currentStack, parentStack)
        var i          = 0
        while (i < currentStack.length - duplicates) {
          println("\tat " + currentStack(i))
          i += 1
        }
        if (duplicates > 0) {
          println("\t... " + duplicates + " more")
        }
      } else {
        println("\t<no stack trace available>")
      }

      parentStack = currentStack
      throwable = throwable.getCause
    }
  }

  private def countDuplicates(currentStack: Array[StackTraceElement],
                              parentStack: Array[StackTraceElement]): Int = {
    var duplicates  = 0
    var parentIndex = parentStack.length - 1
    var i           = currentStack.length - 1
    while (i >= 0 && parentIndex >= 0) {
      val parentFrame = parentStack(parentIndex)
      if (parentFrame == currentStack(i)) {
        duplicates += 1
      } else {
        return duplicates
      }
      i -= 1
      parentIndex -= 1
    }
    duplicates
  }

  override def toString(): String = {
    val className = getClass.getName
    val message   = getMessage()
    if (message eq null) className
    else className + ": " + message
  }
}

class ThreadDeath() extends Error()

/* java.lang.*Error.java */

class AbstractMethodError(message: String)
    extends IncompatibleClassChangeError(message) {
  def this() = this(null)
}

class AssertionError private (message: String, cause: Throwable)
    extends Error(message, cause) {
  def this() = this(null, null)
  def this(o: Object) = this(o.toString, null)
  def this(b: scala.Boolean) = this(b.toString, null)
  def this(c: scala.Char) = this(c.toString, null)
  def this(i: scala.Int) = this(i.toString, null)
  def this(l: scala.Long) = this(l.toString, null)
  def this(f: scala.Float) = this(f.toString, null)
  def this(d: scala.Double) = this(d.toString, null)
}

class BootstrapMethodError(message: String, cause: Throwable)
    extends LinkageError(message) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class ClassCircularityError(message: String) extends LinkageError(message) {
  def this() = this(null)
}

class ClassFormatError(message: String) extends LinkageError(message) {
  def this() = this(null)
}

class Error(message: String, cause: Throwable)
    extends Throwable(message, cause) {
  def this() = this(null, null)
  def this(message: String) = this(message, null)
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
}

class ExceptionInInitializerError private (message: String,
                                           private val cause: Throwable)
    extends LinkageError(message) {
  def this(thrown: Throwable) = this(null, thrown)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
  def getException(): Throwable      = cause
  override def getCause(): Throwable = cause
}

class IllegalAccessError(message: String)
    extends IncompatibleClassChangeError(message) {
  def this() = this(null)
}

class IncompatibleClassChangeError(message: String)
    extends LinkageError(message) {
  def this() = this(null)
}

class InstantiationError(message: String)
    extends IncompatibleClassChangeError(message) {
  def this() = this(null)
}

class InternalError(message: String) extends VirtualMachineError(message) {
  def this() = this(null)
}

class LinkageError(message: String) extends Error(message) {
  def this() = this(null)
}

class NoClassDefFoundError(message: String) extends LinkageError(message) {
  def this() = this(null)
}

class NoSuchFieldError(message: String)
    extends IncompatibleClassChangeError(message) {
  def this() = this(null)
}

class NoSuchMethodError(message: String)
    extends IncompatibleClassChangeError(message) {
  def this() = this(null)
}

class OutOfMemoryError(message: String) extends VirtualMachineError(message) {
  def this() = this(null)
}

class StackOverflowError(message: String)
    extends VirtualMachineError(message) {
  def this() = this(null)
}

class UnknownError(message: String) extends VirtualMachineError(message) {
  def this() = this(null)
}

class UnsatisfiedLinkError(message: String) extends LinkageError(message) {
  def this() = this(null)
}

class UnsupportedClassVersionError(message: String)
    extends ClassFormatError(message) {
  def this() = this(null)
}

class VerifyError(message: String) extends LinkageError(message) {
  def this() = this(null)
}

abstract class VirtualMachineError(message: String) extends Error(message) {
  def this() = this(null)
}

/* java.lang.*Exception.java */

class ArithmeticException(message: String) extends RuntimeException(message) {
  def this() = this(null)
}

class ArrayIndexOutOfBoundsException(message: String)
    extends IndexOutOfBoundsException(message) {
  def this(index: Int) = this("Array index out of range: " + index)
  def this() = this(null)
}

class ArrayStoreException(message: String) extends RuntimeException(message) {
  def this() = this(null)
}

class ClassCastException(message: String) extends RuntimeException(message) {
  def this() = this(null)
}

class ClassNotFoundException(message: String, cause: Throwable)
    extends ReflectiveOperationException(message) {
  def this(message: String) = this(message, null)
  def this() = this(null, null)
  def getException(): Throwable      = cause
  override def getCause(): Throwable = cause
}

class CloneNotSupportedException(message: String) extends Exception(message) {
  def this() = this(null)
}

class EnumConstantNotPresentException(enumType: Class[_ <: Enum[_]],
                                      constantName: String)
    extends RuntimeException(enumType.getName() + "." + constantName) {
  def enumType(): Class[_ <: Enum[_]] = enumType
  def constantName(): String          = constantName
}

class Exception(message: String, cause: Throwable)
    extends Throwable(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class IllegalAccessException(message: String)
    extends ReflectiveOperationException(message) {
  def this() = this(null)
}

class IllegalArgumentException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class IllegalMonitorStateException(message: String)
    extends RuntimeException(message) {
  def this() = this(null)
}

class IllegalStateException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class IllegalThreadStateException(message: String)
    extends IllegalArgumentException(message) {
  def this() = this(null)
}

class IndexOutOfBoundsException(message: String)
    extends RuntimeException(message) {
  def this() = this(null)
}

class InstantiationException(message: String)
    extends ReflectiveOperationException(message) {
  def this() = this(null)
}

class InterruptedException(message: String) extends Exception(message) {
  def this() = this(null)
}

class NegativeArraySizeException(message: String)
    extends RuntimeException(message) {
  def this() = this(null)
}

class NoSuchFieldException(message: String)
    extends ReflectiveOperationException(message) {
  def this() = this(null)
}

class NoSuchMethodException(message: String)
    extends ReflectiveOperationException(message) {
  def this() = this(null)
}

class NullPointerException(message: String) extends RuntimeException(message) {
  def this() = this(null)
}

class NumberFormatException(message: String)
    extends IllegalArgumentException(message) {
  def this() = this(null)
}

class ReflectiveOperationException(message: String, cause: Throwable)
    extends Exception(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class RejectedExecutionException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class RuntimeException(message: String, cause: Throwable)
    extends Exception(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class SecurityException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
  def this(message: String) = this(message, null)
  def this() = this(null, null)
}

class StringIndexOutOfBoundsException(message: String)
    extends IndexOutOfBoundsException(message) {
  def this(index: Int) = this("String index out of range: " + index)
  def this() = this(null)
}

class TypeNotPresentException(t: String, e: Throwable)
    extends RuntimeException("Type " + t + " not present", e) {
  def typeName(): String = t
}

class UnsupportedOperationException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {
  def this() = this(null, null)
  def this(message: String) = this(message, null)
  def this(cause: Throwable) =
    this(if (cause == null) null else cause.toString, cause)
}
