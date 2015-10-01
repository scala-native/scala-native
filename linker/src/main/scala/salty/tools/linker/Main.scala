package salty.tools.linker

import java.io._
import java.nio._
import java.nio.channels._
import salty.ir._

object Main extends App {
  val classpath = new Classpath(Seq(
    //"sandbox/target/scala-2.11/classes"))
    "/Users/Denys/.src/scala/build/quick/classes/compiler",
    "/Users/Denys/.src/scala/build/quick/classes/library",
    "/Users/Denys/.src/scala/build/quick/classes/reflect"))
  /*println("classpath:")
  classpath.resolve.foreach {
    case (n, p) =>
      println(s"  * ${n.fullString} at $p")
  }*/
  val de = new ClasspathDeserializer(classpath)
  val entry = Name.Class("scala.tools.nsc.Driver")
  val scope = de.deserializer(entry).get.scope
  //println("externs:")
  de.externs.foreach {
    case (_, Extern(name)) => println(s"extern ${name.fullString}")
  }
  val bb = ByteBuffer.allocateDirect(128 * 1024 * 1024)
  BinarySerializer(scope, bb)
  bb.flip
  val outfile = new File("out.assembly.salty")
  val channel = (new FileOutputStream(outfile)).getChannel()
  try channel.write(bb)
  finally channel.close
}
