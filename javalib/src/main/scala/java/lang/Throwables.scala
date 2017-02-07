package java.lang

import scala.collection.mutable
import scalanative.native._
import scalanative.runtime.unwind

class Throwable(s: String, private var e: Throwable)
    extends Object
    with java.io.Serializable {
  def this() = this(null, null)
  def this(s: String) = this(s, null)
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)

  private var stackTrace: Array[StackTraceElement] = _

  fillInStackTrace()

  def initCause(cause: Throwable): Throwable = {
    e = cause
    this
  }

  def getMessage(): String = s

  def getCause(): Throwable = e

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

class AbstractMethodError(s: String) extends IncompatibleClassChangeError(s) {
  def this() = this(null)
}

class AssertionError private (s: String, e: Throwable) extends Error(s, e) {
  def this() = this(null, null)
  def this(o: Object) = this(o.toString, null)
  def this(b: scala.Boolean) = this(b.toString, null)
  def this(c: scala.Char) = this(c.toString, null)
  def this(i: scala.Int) = this(i.toString, null)
  def this(l: scala.Long) = this(l.toString, null)
  def this(f: scala.Float) = this(f.toString, null)
  def this(d: scala.Double) = this(d.toString, null)
}

class BootstrapMethodError(s: String, e: Throwable) extends LinkageError(s) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class ClassCircularityError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

class ClassFormatError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

class Error(s: String, e: Throwable) extends Throwable(s, e) {
  def this() = this(null, null)
  def this(s: String) = this(s, null)
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
}

class ExceptionInInitializerError private (s: String, private val e: Throwable)
    extends LinkageError(s) {
  def this(thrown: Throwable) = this(null, thrown)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
  def getException(): Throwable      = e
  override def getCause(): Throwable = e
}

class IllegalAccessError(s: String) extends IncompatibleClassChangeError(s) {
  def this() = this(null)
}

class IncompatibleClassChangeError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

class InstantiationError(s: String) extends IncompatibleClassChangeError(s) {
  def this() = this(null)
}

class InternalError(s: String) extends VirtualMachineError(s) {
  def this() = this(null)
}

class LinkageError(s: String) extends Error(s) {
  def this() = this(null)
}

class NoClassDefFoundError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

class NoSuchFieldError(s: String) extends IncompatibleClassChangeError(s) {
  def this() = this(null)
}

class NoSuchMethodError(s: String) extends IncompatibleClassChangeError(s) {
  def this() = this(null)
}

class OutOfMemoryError(s: String) extends VirtualMachineError(s) {
  def this() = this(null)
}

class StackOverflowError(s: String) extends VirtualMachineError(s) {
  def this() = this(null)
}

class UnknownError(s: String) extends VirtualMachineError(s) {
  def this() = this(null)
}

class UnsatisfiedLinkError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

class UnsupportedClassVersionError(s: String) extends ClassFormatError(s) {
  def this() = this(null)
}

class VerifyError(s: String) extends LinkageError(s) {
  def this() = this(null)
}

abstract class VirtualMachineError(s: String) extends Error(s) {
  def this() = this(null)
}

/* java.lang.*Exception.java */

class ArithmeticException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class ArrayIndexOutOfBoundsException(s: String)
    extends IndexOutOfBoundsException(s) {
  def this(index: Int) = this("Array index out of range: " + index)
  def this() = this(null)
}

class ArrayStoreException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class ClassCastException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class ClassNotFoundException(s: String, e: Throwable)
    extends ReflectiveOperationException(s) {
  def this(s: String) = this(s, null)
  def this() = this(null, null)
  def getException(): Throwable      = e
  override def getCause(): Throwable = e
}

class CloneNotSupportedException(s: String) extends Exception(s) {
  def this() = this(null)
}

class EnumConstantNotPresentException(e: Class[_ <: Enum[_]], c: String)
    extends RuntimeException(e.getName() + "." + c) {
  def enumType(): Class[_ <: Enum[_]] = e
  def constantName(): String          = c
}

class Exception(s: String, e: Throwable) extends Throwable(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class IllegalAccessException(s: String)
    extends ReflectiveOperationException(s) {
  def this() = this(null)
}

class IllegalArgumentException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class IllegalMonitorStateException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class IllegalStateException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class IllegalThreadStateException(s: String)
    extends IllegalArgumentException(s) {
  def this() = this(null)
}

class IndexOutOfBoundsException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class InstantiationException(s: String)
    extends ReflectiveOperationException(s) {
  def this() = this(null)
}

class InterruptedException(s: String) extends Exception(s) {
  def this() = this(null)
}

class NegativeArraySizeException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class NoSuchFieldException(s: String) extends ReflectiveOperationException(s) {
  def this() = this(null)
}

class NoSuchMethodException(s: String)
    extends ReflectiveOperationException(s) {
  def this() = this(null)
}

class NullPointerException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}

class NumberFormatException(s: String) extends IllegalArgumentException(s) {
  def this() = this(null)
}

class ReflectiveOperationException(s: String, e: Throwable)
    extends Exception(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class RejectedExecutionException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class RuntimeException(s: String, e: Throwable) extends Exception(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class SecurityException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}

class StringIndexOutOfBoundsException(s: String)
    extends IndexOutOfBoundsException(s) {
  def this(index: Int) = this("String index out of range: " + index)
  def this() = this(null)
}

class TypeNotPresentException(t: String, e: Throwable)
    extends RuntimeException("Type " + t + " not present", e) {
  def typeName(): String = t
}

class UnsupportedOperationException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this() = this(null, null)
  def this(s: String) = this(s, null)
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
}
