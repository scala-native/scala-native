import java.io.{File, IOException}

object CreateNewFileTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(!willBeCreatedFile.exists())
    assert(willBeCreatedFile.createNewFile())
    assert(willBeCreatedFile.exists())

    val exceptionThrown =
      try {
        new File(nonexistentDirectory, "somefile").createNewFile(); false
      } catch {
        case _: IOException => true
      }

    assert(exceptionThrown)

  }
}
