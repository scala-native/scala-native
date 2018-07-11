package walkfiletree

import benchmarks.{BenchmarkRunningTime, LongRunningTime}
import java.io.File
import java.nio.file._
import java.nio.file.attribute._

class WalkFileTreeBenchmark extends benchmarks.Benchmark[Unit] {
  val tmpFileTreePath = Files.createTempDirectory("scala-native-walkfiletree")
  val maxLevels       = 6

  override val runningTime: BenchmarkRunningTime = LongRunningTime

  override def check(a: Unit): Boolean = true

  override def setup(): Unit = {
    def createFilesAndDirs(currentPath: Path, currentLevel: Int): Unit = {
      if (currentLevel + 1 > maxLevels) {
        ()
      } else {
        // Create the current directory
        currentPath.toFile.mkdirs()

        // Create the files for this level
        (0 until 10)
          .map(f => currentPath.resolve(s"$f").toFile)
          .foreach(_.createNewFile())

        // Create the children directories for this level
        (0 until 2)
          .map(d => currentPath.resolve(s"dir$d"))
          .foreach { childDir =>
            createFilesAndDirs(childDir, currentLevel + 1)
          }
      }
    }

    createFilesAndDirs(tmpFileTreePath, 0)
    println(s"WalkFileTreeBenchmark setup under: $tmpFileTreePath")
  }

  // Adapted from:
  //  https://github.com/scala-native/scala-native/issues/888
  override def run(): Unit = {
    Files.walkFileTree(
      tmpFileTreePath,
      new SimpleFileVisitor[Path] {
        override def visitFile(file: Path,
                               attrs: BasicFileAttributes): FileVisitResult = {
          FileVisitResult.CONTINUE
        }
      }
    )
  }
}
