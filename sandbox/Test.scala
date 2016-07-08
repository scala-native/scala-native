import scalanative.native._, stdio._
import java.nio._
import java.nio.charset._

object Test  {
  def main(args: Array[String]): Unit = {
    val s = "A java string"
    val ca = s.toCharArray()
    val cb = CharBuffer.wrap(ca, 0, s.length())
    val buffer = Charset.defaultCharset().encode(cb)
    val bytes = buffer.array()
    bytes.foreach { b =>
      putc(b, stdio.stdout)
    }
  }
}
