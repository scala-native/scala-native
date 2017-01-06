import scala.scalanative.native._, stdio._
import java.io.File
import java.io.CFile

import scala.collection.mutable

object FileTest {
  def main(args: Array[String]): Unit = {
    filePathTest()
    fileNotCreatedDoesNotExists()
    fileCanBeCreated()
    canNotCreateTwoTimeTheSameFile
  }

  def filePathTest(): Unit = {
    val s = "test"
    val f = new File(s)
    assert(f.getPath() equals s)
  }

  def fileNotCreatedDoesNotExists(): Unit = {
    val s = "newNotExistsFileTest"
    val f = new File(s)
    assert(f.exists() == false)
  }

  def fileCanBeCreated(): Unit = {
    val s = "newExistsFileTest.txt"
    val f = new File(s)
    assert(f.createNewFile())
    assert(f.exists() == true)
    f.delete()
    assert(f.exists() == false)
  }

  def canNotCreateTwoTimeTheSameFile(): Unit = {
    val s = "newExistsFileTest"
    val f = new File(s)
    assert(f.createNewFile())
    assert(!f.createNewFile())
    f.delete()
    assert(f.exists() == false)
  }
//Need toLowerCase and toUpperCase to make those work.
  /*
  def compareToTest(): Unit = {
    val f = new File("test.txt")
    val f1 = new File("File/test.txt")
    val f2 = new File("test.txt")
    var value = f.compareTo(f1)
    assert(value > 0)
    value = f.compareTo(f2)
    assert(value == 0)
  }

def equalsTest(): Unit = {
    val f = new File("test.txt")
    val f1 = new File("test1.txt")
    var isEqual = f.equals(f)
    assert(isEqual)
    isEqual = f.equals(f1)
    assert(!isEqual)
  }*/
}
