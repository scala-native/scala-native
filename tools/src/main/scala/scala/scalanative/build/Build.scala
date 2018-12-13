package scala.scalanative
package build

import java.nio.file.{Path, Files}

/** Utility methods for building code using Scala Native. */
object Build {

  /** Run the complete Scala Native pipeline,
   *  LLVM optimizer and system linker, producing
   *  a native binary in the end.
   *
   *  For example, to produce a binary one needs
   *  a classpath, a working directory and a main
   *  class entry point:
   *
   *  {{{
   *  val classpath: Seq[Path] = ...
   *  val workdir: Path        = ...
   *  val main: String         = ...
   *
   *  val clang     = Discover.clang()
   *  val clangpp   = Discover.clangpp()
   *  val linkopts  = Discover.linkingOptions()
   *  val compopts  = Discover.compileOptions()
   *  val triple    = Discover.targetTriple(clang, workdir)
   *  val nativelib = Discover.nativelib(classpath).get
   *  val outpath   = workdir.resolve("out")
   *
   *  val config =
   *    Config.empty
   *      .withGC(GC.default)
   *      .withMode(Mode.default)
   *      .withClang(clang)
   *      .withClangPP(clangpp)
   *      .withLinkingOptions(linkopts)
   *      .withCompileOptions(compopts)
   *      .withTargetTriple(triple)
   *      .withNativelib(nativelib)
   *      .withMainClass(main)
   *      .withClassPath(classpath)
   *      .withLinkStubs(true)
   *      .withWorkdir(workdir)
   *
   *  Build.build(config, outpath)
   *  }}}
   *
   *  @param config  The configuration of the toolchain.
   *  @param outpath The path to the resulting native binary.
   *  @return `outpath`, the path to the resulting native binary.
   */
  def build(config: Config, outpath: Path): Path = config.logger.time("Total") {
    val driver  = optimizer.Driver.default(config.mode)
    val entries = ScalaNative.entries(config)
    val linked  = ScalaNative.link(config, entries)

    nir.Show.dump(linked.defns, "linked.nir")
    check(linked)

    if (linked.unavailable.nonEmpty) {
      linked.unavailable.map(_.show).sorted.foreach { signature =>
        config.logger.error(s"cannot link: $signature")
      }
      throw new BuildException("unable to link")
    }
    val classCount = linked.defns.count {
      case _: nir.Defn.Class | _: nir.Defn.Module => true
      case _                                      => false
    }
    val methodCount = linked.defns.count(_.isInstanceOf[nir.Defn.Define])
    config.logger.info(
      s"Discovered ${classCount} classes and ${methodCount} methods")

    val optimized =
      ScalaNative.optimize(config, linked, driver)

    check(optimized)

    IO.getAll(config.workdir, "glob:**.ll").foreach(Files.delete)
    ScalaNative.codegen(config, optimized)
    val generated = IO.getAll(config.workdir, "glob:**.ll")

    val unpackedLib = LLVM.unpackNativelib(config.nativelib, config.workdir)
    val objectFiles = config.logger.time("Compiling to native code") {
      val nativelibConfig =
        config.withCompileOptions("-O2" +: config.compileOptions)
      LLVM.compileNativelib(nativelibConfig, linked, unpackedLib)
      LLVM.compile(config, generated)
    }

    LLVM.link(config, linked, objectFiles, unpackedLib, outpath)
  }

  private def check(linked: scalanative.linker.Result): Unit = {
    import scala.collection.mutable
    import scalanative.nir._
    import scalanative.checker._
    val errors = Check(linked)
    if (errors.nonEmpty) {
      val grouped =
        mutable.Map.empty[Global, mutable.UnrolledBuffer[Check.Error]]
      errors.foreach { err =>
        val errs =
          grouped.getOrElseUpdate(err.name, mutable.UnrolledBuffer.empty)
        errs += err
      }
      grouped.foreach {
        case (name, errs) =>
          println("")
          println(s"Found ${errs.length} errors on ${name.show} :")
          println("")
          linked.defns
            .collectFirst {
              case defn if defn != null && defn.name == name => defn
            }
            .foreach { defn =>
              val str   = defn.show
              val lines = str.split("\n")
              lines.zipWithIndex.foreach {
                case (line, idx) =>
                  println(String.format("  %04d  ",
                                        java.lang.Integer.valueOf(idx)) + line)
              }
            }
          println("")
          errs.foreach { err =>
            println("  in " + err.ctx.reverse.mkString(" / ") + " : ")
            println("    " + err.msg)
          }

      }
      println(s"${errors.size} found")
    }
  }
}
