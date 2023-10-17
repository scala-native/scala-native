import java.nio.charset.spi.CharsetProvider
import java.util.ServiceLoader
import java.nio.charset.Charset
import java.{util => ju}
import scala.scalanative.runtime.Intrinsics
import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

class MockCharsetProvider extends CharsetProvider{
  override def charsets(): ju.Iterator[Charset] = new ju.Iterator[Charset] {
    override def hasNext(): Boolean = false
    override def next(): Charset = ???
  }
  override def charsetForName(charsetName: String): Charset = null
}

class DisabledCharsetProvider extends CharsetProvider{
  override def charsets(): ju.Iterator[Charset] = new ju.Iterator[Charset] {
    override def hasNext(): Boolean = false
    override def next(): Charset = ???
  }
  override def charsetForName(charsetName: String): Charset = null
}

object Test {
  def main(args: Array[String]): Unit = {
    val loader = ServiceLoader.load(classOf[CharsetProvider])
    println(loader)
    loader.forEach(println)
  }
}
