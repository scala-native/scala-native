import java.io.File

object SetDeleteOnExit {
  def main(args: Array[String]): Unit = {
    new File(args.head).deleteOnExit()
  }
}
