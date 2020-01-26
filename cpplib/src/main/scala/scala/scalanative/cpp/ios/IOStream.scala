package scala.scalanative.cpp.ios
import scalanative.unsafe._
import scala.scalanative.cpp._

class IOStream(obj: NativeObject) extends CppObject(obj) {
  def this() = this(NullObj)

  def sync(): Unit = IOStreamNative.sync()

  def valid(): Boolean = (obj != NullObj)

  def streambuf_in_avail(): ios.Streamsize = IOStreamNative.streambuf_in_avail()

  def ignore(count: Streamsize): Unit = IOStreamNative.ignore(count)

  def read(buf: Ptr[_], count: Streamsize): Streamsize = IOStreamNative.read(getNativeObject(), buf, count)

  def write(buf: Ptr[_], count: Streamsize): Streamsize = IOStreamNative.write(getNativeObject(), buf, count)
}

object IOStream {
  val stdin: IOStream = new IOStream(IOStreamNative.stdin)
  val stdout: IOStream = new IOStream(IOStreamNative.stdout)
  val stderr: IOStream = new IOStream(IOStreamNative.stderr)
}

@extern
object IOStreamNative {

  @name("scalanative_cpp_ios_stdin")
  def stdin(): NativeObject = extern

  @name("scalanative_cpp_ios_stdout")
  def stdout(): NativeObject = extern

  @name("scalanative_cpp_ios_stderr")
  def stderr(): NativeObject = extern

  @name("scalanative_cpp_ios_iostream_sync")
  def sync(): Unit = extern

  @name("scalanative_cpp_ios_iostream_streambuf_in_avail")
  def streambuf_in_avail(): Streamsize = extern

  @name("scalanative_cpp_ios_iostream_read")
  def read(obj: NativeObject, buf: Ptr[_], count: Streamsize): Streamsize = extern

  @name("scalanative_cpp_ios_iostream_write")
  def write(obj: NativeObject, buf: Ptr[_], count: Streamsize): Streamsize = extern

  @name("scalanative_cpp_ios_iostream_ignore")
  def ignore(count: Streamsize): Unit = extern
}
