package scala.scalanative
package codegen
package llvm

import java.io.File
import java.nio.file.{Path, Paths, Files}
import scala.collection.mutable
import scala.scalanative.build.Config
import scala.scalanative.build.ScalaNative.{dumpDefns, encodedMainClass}
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.build
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.util.{Scope, partitionBy, procs}
import java.nio.file.StandardCopyOption

import scala.scalanative.build.ScalaNative
import scala.scalanative.codegen.{Metadata => CodeGenMetadata}
import scala.concurrent._
import scala.util.Success

object CodeGen {

  /** Lower and generate code for given assembly. */
  def apply(config: build.Config, analysis: ReachabilityAnalysis.Result)(
      implicit ec: ExecutionContext
  ): Future[Seq[Path]] = {
    val defns = analysis.defns
    val proxies = GenerateReflectiveProxies(analysis.dynimpls, defns)

    implicit def logger: build.Logger = config.logger
    implicit val target: TargetInfo = TargetInfo(config)
    implicit val meta: CodeGenMetadata =
      new CodeGenMetadata(analysis, config, proxies)

    val generated = Generate(encodedMainClass(config), defns ++ proxies)
    val embedded = ResourceEmbedder(config)
    val lowered = lower(generated ++ embedded)
    lowered
      .andThen { case Success(defns) => dumpDefns(config, "lowered", defns) }
      .flatMap(emit(config, _))
  }

  private[scalanative] def lower(
      defns: Seq[nir.Defn]
  )(implicit
      meta: CodeGenMetadata,
      logger: build.Logger,
      ec: ExecutionContext
  ): Future[Seq[nir.Defn]] = {

    val loweringJobs = partitionBy(defns)(_.name).map {
      case (_, defns) => Future(Lower(defns))
    }

    Future
      .foldLeft(loweringJobs)(mutable.UnrolledBuffer.empty[nir.Defn]) {
        case (buffer, defns) => buffer ++= defns
      }
      .map(_.toSeq)
  }

  private final val EmptyPath = "__empty"

  /** Generate code for given assembly. */
  private def emit(config: build.Config, assembly: Seq[nir.Defn])(implicit
      meta: CodeGenMetadata,
      ec: ExecutionContext
  ): Future[Seq[Path]] =
    Scope { implicit in =>
      val env = assembly.map(defn => defn.name -> defn).toMap
      val outputDirPath = config.workDir.resolve("generated")
      Files.createDirectories(outputDirPath)
      val outputDir = VirtualDirectory.real(outputDirPath)
      val sourceCodeCache = new SourceCodeCache(config)

      def sourceDirOf(defn: nir.Defn): String = {
        val nirSource = defn.pos.nirSource
        if (nirSource.exists)
          s"${nirSource.directory}:${nirSource.path.getParent()}"
        else
          EmptyPath
      }

      // Partition into multiple LLVM IR files proportional to number
      // of available processesors. This prevents LLVM from optimizing
      // across IR module boundary unless LTO is turned on.
      def separate(): Future[Seq[Path]] =
        Future
          .traverse(partitionBy(assembly, procs)(sourceDirOf).toSeq) {
            case (id, defns) =>
              Future {
                val sorted = defns.sortBy(_.name)
                Impl(env, sorted, sourceCodeCache).gen(id.toString, outputDir)
              }
          }

      // Incremental compilation code generation
      def seperateIncrementally(): Future[Seq[Path]] = {
        val ctx = new IncrementalCodeGenContext(config.workDir)
        ctx.collectFromPreviousState()

        // Partition into multiple LLVM IR files per Scala source file originated from.
        // We previously partitioned LLVM IR files by package.
        // However, this caused issues with the Darwin linker when generating N_OSO symbols,
        // if a single Scala source file generates multiple LLVM IR files with the compilation unit DIEs
        // referencing the same Scala source file.
        // Because, the Darwin linker distinguishes compilation unit DIEs (debugging information entries)
        // by their DW_AT_name, DW_comp_dir attribute, and the object files' timestamps.
        // If the CU DIEs and timestamps are duplicated, the Darwin linker cannot distinguish the DIEs,
        // and one of the duplicates will be ignored.
        // As a result, the N_OSO symbol (which points to the object file path) is missing in the final binary,
        // and dsymutil fails to link some debug symbols from object files.
        // see: https://github.com/scala-native/scala-native/issues/3458#issuecomment-1701036738
        //
        // To address this issue, we partition into multiple LLVM IR files per Scala source file originated from.
        // This will ensure that each LLVM IR file only references a single Scala source file,
        // which will prevent the Darwin linker failing to generate N_OSO symbols.
        Future
          .traverse(assembly.groupBy(sourceDirOf).toSeq) {
            case (dir, defns) =>
              Future {
                val hash = dir.hashCode().toHexString
                val outFile = outputDirPath.resolve(s"$hash.ll")
                val ownerDirectory = outFile.getParent()

                ctx.addEntry(hash, defns)
                if (ctx.shouldCompile(hash)) {
                  val sorted = defns.sortBy(_.name)
                  if (!Files.exists(ownerDirectory))
                    Files.createDirectories(ownerDirectory)
                  Impl(env, sorted, sourceCodeCache).gen(hash, outputDir)
                } else {
                  assert(ownerDirectory.toFile.exists())
                  config.logger.debug(
                    s"Content of directory in $dir has not changed, skiping generation of $hash.ll"
                  )
                  outFile
                }
              }
          }
          .andThen {
            case _ =>
              // Save current state for next compilation run
              ctx.dump()
              ctx.clear()
          }
      }

      if (config.compilerConfig.useIncrementalCompilation)
        seperateIncrementally()
      else separate()
    }

  object Impl {
    import scala.scalanative.codegen.llvm.AbstractCodeGen
    import scala.scalanative.codegen.llvm.compat.os._

    def apply(
        env: Map[nir.Global, nir.Defn],
        defns: Seq[nir.Defn],
        sourcesCache: SourceCodeCache
    )(implicit
        meta: CodeGenMetadata
    ): AbstractCodeGen = {

      new AbstractCodeGen(env, defns) {
        override val os: OsCompat = {
          if (this.meta.target.targetsWindows) new WindowsCompat(this)
          else new UnixCompat(this)
        }
        override def sourceCodeCache: SourceCodeCache = sourcesCache
      }
    }
  }

  /** The symbols required by generation on `platform`. */
  def dependencies(target: TargetInfo): Seq[nir.Global] = {
    val buf = mutable.UnrolledBuffer.empty[nir.Global]
    buf ++= Lower.depends(target)
    buf ++= Generate.depends
    buf.toSeq
  }

}
