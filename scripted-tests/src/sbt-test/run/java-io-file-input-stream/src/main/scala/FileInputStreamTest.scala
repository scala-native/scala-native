import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

object FileInputStreamTest {

  def main(args: Array[String]): Unit = {
    val file: File           = new File("test.txt")
    val fip: FileInputStream = new FileInputStream(file)
    val nbytes               = fip.available()
    assert(nbytes == 4)
    val bytes = new Array[Byte](nbytes)
    fip.read(bytes)
    assert(new String(bytes, "UTF-8") equals "test")
  }
}
