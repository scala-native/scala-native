import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

object FileOutputStreamTest {

  def main(args: Array[String]): Unit = {
    args(0) match {
      case "owerwriteConstructor" => constructorWithFileTest()
      case "appendConstructor"    => constructWithAppendTrueTest()
    }
  }

  //when passing only a file, it is in mode WRONLY, so overwriting.
  def constructorWithFileTest(): Unit = {
    val file: File            = new File("test.txt")
    val fop: FileOutputStream = new FileOutputStream(file)
    val content               = "Hello World"
    fop.write(content.getBytes())
    fop.close()
  }

  def constructWithAppendTrueTest(): Unit = {
    val file: File            = new File("test.txt")
    val fop: FileOutputStream = new FileOutputStream(file, true)
    val content               = " Hello World"
    fop.write(content.getBytes())
    fop.close()
  }
}
