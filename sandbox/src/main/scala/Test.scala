//> using resourceDir ../resources
import java.nio.charset.spi.CharsetProvider
import java.util.ServiceLoader
import java.nio.charset.Charset
import java.{util => ju}
// import scala.scalanative.runtime.Intrinsics
// import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

object MockCharsetProvider{
  println(s"initialize $this")
}
class MockCharsetProvider extends CharsetProvider {
  println("MockCharset constructor")
  // val  = MockCharsetProvider
  override def charsets(): ju.Iterator[Charset] = new ju.Iterator[Charset] {
    override def hasNext(): Boolean = false
    override def next(): Charset = ???
  }
  override def charsetForName(charsetName: String): Charset = null
}

package foo {
  package bar {
    class DisabledCharsetProvider extends CharsetProvider {
      override def charsets(): ju.Iterator[Charset] = new ju.Iterator[Charset] {
        override def hasNext(): Boolean = false
        override def next(): Charset = ???
      }
      override def charsetForName(charsetName: String): Charset = null
    }
  }
}

trait MyService

object Test {
  def main(args: Array[String]): Unit = {
    val loader = ServiceLoader.load(classOf[CharsetProvider])
    println(loader)
    loader.stream().filter(_.`type`() == null).forEach{x => println(x -> x.`type`()); x.get()}
    println()
    loader.stream().forEach{x => println(x -> x.`type`()); x.get()}
  }
}
