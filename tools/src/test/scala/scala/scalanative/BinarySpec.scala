package scala.scalanative

import llvm.LLVM
import java.io.File
import java.nio.file.Files.{createTempDirectory, createTempFile}
import scala.sys.process.{Process, ProcessLogger}
import tools.Config
import optimizer.Driver
import io.VirtualFile

/**
 * Base class to test:
 *  - Producing a binary file from scala code
 *  - Running the binary file.
 */
abstract class BinarySpec extends CodeGenSpec {

  /** The default options to pass to clang */
  val defaultClangOptions: Seq[String] = {
    val lrt = Option(sys props "os.name") match {
      case Some("Linux") => Seq("-lrt")
      case _             => Seq()
    }
    LLVM.includes ++ LLVM.libs ++ lrt
  }

  /**
   * Compiles the given sources and produce an executable binary file.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile.
   * @param driver  The driver that defines the pipeline.
   * @param linkage Given a native library, provide the linkage kind (static or
   *                dynamic). Defaults to dynamic.
   * @param opts    The options to pass to clang.
   * @param fn      A function to apply to the products of the compilation.
   * @return The result of applying `fn` to the result of compilation.
   */
  def makeBinary[T](entry: String,
                    sources: Map[String, String],
                    driver: Driver = Driver(),
                    linkage: Map[String, String] = Map.empty,
                    opts: Seq[String] = defaultClangOptions)(
      fn: (Config, Seq[nir.Attr.Link], File) => T): T =
    codegen(entry, sources, driver) {
      case (config, links, llFile) =>
        val clangpp   = LLVM.discover("clang++", Seq(("3", "8"), ("3", "7")))
        val target    = createTempDirectory("native-test-target").toFile()
        val nativelib = new File(sys.props("scalanative.nativelib.dir"))
        val binary    = createTempFile("native-binary", null).toFile()
        val logger    = ProcessLogger(_ => (), println _)
        val appll     = write(llFile)

        LLVM.compileLl(clangpp,
                       target,
                       nativelib,
                       appll,
                       binary,
                       links.map(_.name),
                       linkage,
                       opts,
                       logger)

        fn(config, links, binary)
    }

  /**
   * Compiles and runs the given sources.
   *
   * @param entry The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile.
   * @param driver  The driver that defines the pipeline.
   * @param linkage Given a native library, provide the linkage kind (static or
   *                dynamic). Defaults to dynamic.
   * @param opts    The options to pass to clang.
   * @param fn      A function to apply to the output of the run.
   * @return The result of applying `fn` to the output of the run.
   */
  def run[T](entry: String,
             sources: Map[String, String],
             driver: Driver = Driver(),
             linkage: Map[String, String] = Map.empty,
             opts: Seq[String] = defaultClangOptions)(
      fn: (Int, Seq[String], Seq[String]) => T): T =
    makeBinary(entry, sources, driver, linkage, opts) {
      case (_, _, binary) =>
        val outLines = scala.collection.mutable.Buffer.empty[String]
        val errLines = scala.collection.mutable.Buffer.empty[String]
        val logger   = ProcessLogger(outLines += _, errLines += _)
        val exitCode = Process(binary.getAbsolutePath) ! logger

        fn(exitCode, outLines, errLines)
    }

  private def write(virtual: VirtualFile): File = {
    val out = createTempFile("native-codegen", ".ll").toFile()
    val channel =
      java.nio.channels.Channels.newChannel(new java.io.FileOutputStream(out))
    channel.write(virtual.contents)
    out
  }

}
