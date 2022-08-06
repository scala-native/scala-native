package scala.scalanative.build
import java.io.{ByteArrayOutputStream, File, ObjectOutputStream, PrintWriter}
import java.nio.ByteBuffer
import java.nio.file.Path
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.language.implicitConversions
import scala.scalanative.nir.Defn

class IncCompilationContext(workDir: Path) {
  val package2hash: TrieMap[String, Long] = TrieMap[String, Long]()
  val pack2hashPrev: TrieMap[String, Long] = TrieMap[String, Long]()
  val changed: TrieMap[String, Long] = TrieMap[String, Long]()
  val dumpChanged: Path = workDir.resolve(Path.of("changed"))
  val dumpPackage2hash: Path = workDir.resolve(Path.of("package2hash"))

  def collectFromPrev(): Unit = {
    if (new java.io.File(dumpPackage2hash.toUri).exists) {
      val resultPrev = Source.fromFile(dumpPackage2hash.toUri)
      resultPrev.getLines().toList.foreach { vec =>
        {
          val eachLine = vec.strip().split(",")
          val packageName = eachLine(0)
          val hashCodeStr = eachLine(1)
          pack2hashPrev.put(packageName, hashCodeStr.toLong)
        }
      }
    }
  }
  collectFromPrev()

  def collectFromCurr(pack: String, defns: Seq[Defn]): Unit = {
    if (!package2hash.contains(pack)) {
      val hash =
        defns.foldLeft(0)((prevhash, defn) => prevhash + defn.hashCode())
      package2hash.put(pack, hash)
      if (!pack2hashPrev.contains(pack) || pack2hashPrev(pack) != hash) {
        changed.put(pack, hash)
      }
    }
  }

  def isChanged(pack: String): Boolean = {
    changed.contains(pack)
  }

  def dump(): Unit = {
    // dump the result in the current execution
    val pwHash = new PrintWriter(
      new File(dumpPackage2hash.toUri)
    )
    package2hash.toList.foreach {
      case (pack, name) => pwHash.write(pack + "," + name + "\n")
    }
    pwHash.close()

    // This file is used for debug.
    val pwChanged = new PrintWriter(
      new File(dumpChanged.toUri)
    )
    changed.toList.foreach { case (pack, _) => pwChanged.write(pack + "\n") }
    pwChanged.close()
  }
}
