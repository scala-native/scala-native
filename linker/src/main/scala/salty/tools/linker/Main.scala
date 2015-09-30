package salty.tools.linker

import java.io._
import java.nio._
import java.nio.channels._
import salty.ir._

object Main extends App {
  val classpath = new Classpath(Seq(
    "/Users/Denys/.src/scala/build/quick/classes/compiler",
    "/Users/Denys/.src/scala/build/quick/classes/library",
    "/Users/Denys/.src/scala/build/quick/classes/reflect"))
  val de = new ClasspathDeserializer(classpath)
  val entry = Name.Nested(Name.Simple("scala.tools.nsc.Driver"), Name.Simple("main"))
  val scope = Scope(Map(entry -> de.resolve(entry).get))
  val bb = ByteBuffer.allocateDirect(16 * 1024 * 1024)
  GraphSerializer(scope, bb)
  bb.flip
  val outfile = new File("out.dot")
  val channel = (new FileOutputStream(outfile)).getChannel()
  try channel.write(bb)
  finally channel.close
}
