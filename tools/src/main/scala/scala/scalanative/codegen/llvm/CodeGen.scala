package scala.scalanative
package codegen
package llvm

import java.io.File
import java.nio.file.{Path, Paths, Files}
import scala.collection.mutable
import scala.scalanative.build.Config
import scala.scalanative.build.ScalaNative.{dumpDefns, encodedMainClass}
import scala.scalanative.build.Build
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.build
import scala.scalanative.build.IO
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.util.{Scope, partitionBy, procs}
import java.nio.file.StandardCopyOption

import scala.scalanative.codegen.{Metadata => CodeGenMetadata}
import scala.concurrent._
import scala.util.Success
import scala.scalanative.codegen.llvm.compat.os.OsCompat
import scala.scalanative.util.ShowBuilder

object CodeGen {
  type IRGenerator = Future[Path]
  type IRGenerators = Seq[IRGenerator]

  /** Lower and generate code for given assembly. */
  def apply(config: build.Config, analysis: ReachabilityAnalysis.Result)(
      implicit ec: ExecutionContext
  ): Future[IRGenerators] = {
    val defns = analysis.defns
    val proxies = GenerateReflectiveProxies(analysis.dynimpls, defns)

    implicit def logger: build.Logger = config.logger
    implicit val platform: PlatformInfo = PlatformInfo(config)
    implicit val meta: CodeGenMetadata =
      new CodeGenMetadata(analysis, config, proxies)

    val generated = Generate(encodedMainClass(config), defns ++ proxies)
    val embedded = ResourceEmbedder(config)
    val lowered = lower(generated ++ embedded)
    lowered
      .andThen { case Success(defns) => dumpDefns(config, "lowered", defns) }
      .map(emit(config, _))
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
  ): IRGenerators =
    Scope { implicit in =>
      val env = assembly.map(defn => defn.name -> defn).toMap
      val outputDirPath = config.workDir.resolve("generated")
      if (Build.userConfigHasChanged(config))
        IO.deleteRecursive(outputDirPath)
      Files.createDirectories(outputDirPath)
      val outputDir = VirtualDirectory.real(outputDirPath)
      val sourceCodeCache = new SourceCodeCache(config)

      def outputFileId(defn: nir.Defn): String =
        defn.pos.source.directory
          .getOrElse(EmptyPath)

      // Partition into multiple LLVM IR files proportional to number
      // of available processesors. This prevents LLVM from optimizing
      // across IR module boundary unless LTO is turned on.
      def separate(): IRGenerators =
        partitionBy(assembly, procs)(outputFileId).toSeq.map {
          case (id, defns) =>
            Future {
              val sorted = defns.sortBy(_.name)
              Impl(env, sorted, sourceCodeCache).gen(id.toString, outputDir)
            }
        }

      // Incremental compilation code generation
      def seperateIncrementally(): IRGenerators = {
        val ctx = new IncrementalCodeGenContext(config)
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
        val llvmIRGenerators = assembly.groupBy(outputFileId).toSeq.map {
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
        Future.sequence(llvmIRGenerators).andThen {
          case _ =>
            // Save current state for next compilation run
            ctx.dump()
            ctx.clear()
        }
        llvmIRGenerators
      }

      val maybeBuildInfoGenerator = new Impl.BuildInfoCodegen(env)
        .generateIfSupported(outputDir, config)
        .map(Future.successful)

      val llvmIRGenerators =
        if (config.compilerConfig.useIncrementalCompilation)
          seperateIncrementally()
        else separate()
      llvmIRGenerators ++ maybeBuildInfoGenerator
    }

  private object Impl {
    import scala.scalanative.codegen.llvm.AbstractCodeGen
    def apply(
        env: Map[nir.Global, nir.Defn],
        defns: Seq[nir.Defn],
        sourcesCache: SourceCodeCache
    )(implicit
        meta: CodeGenMetadata
    ): AbstractCodeGen = new StdCodeGen(env, defns, sourcesCache)

    private class StdCodeGen(
        env: Map[nir.Global, nir.Defn],
        defns: Seq[nir.Defn],
        sourcesCache: SourceCodeCache
    )(implicit
        meta: CodeGenMetadata
    ) extends AbstractCodeGen(env, defns) {
      override def sourceCodeCache: SourceCodeCache = sourcesCache
    }

    class BuildInfoCodegen(env: Map[nir.Global, nir.Defn])(implicit
        meta: CodeGenMetadata
    ) extends AbstractCodeGen(env, Nil) {
      import meta.config
      val buildInfos: Map[String, Any] = Map(
        "ASAN support" -> config.asan,
        "Debug metadata" -> config.sourceLevelDebuggingConfig.enabled,
        "Embed resources" -> config.embedResources,
        "GC" -> config.gc,
        "LTO" -> config.lto,
        "Link stubs" -> config.linkStubs,
        "Mode" -> config.mode,
        "Multithreading" -> config.multithreadingSupport,
        "Optimize" -> config.optimize
      )

      /* Enable feature only where known to work. Add to list as experience grows
       * FreeBSD uses elf format so it _should_ work, but it has not been
       * exercised.
       */

      def generateIfSupported(
          dir: VirtualDirectory,
          config: build.Config
      ): Option[Path] =
        if (config.targetsLinux) Some(gen("", dir))
        else None

      override def gen(unused: String, dir: VirtualDirectory): Path = {
        dir.write(Paths.get(s"__buildInfo.ll")) { writer =>
          implicit val metadata: MetadataCodeGen.Context =
            new MetadataCodeGen.Context(
              this,
              new ShowBuilder.FileShowBuilder(writer)
            )

          val snVersion = scala.scalanative.nir.Versions.current
          val compilerInfo = s"Scala Native v$snVersion"
          val buildInfo = buildInfos
            .map { case (key, value) => s"$key: $value" }
            .mkString(", ")

          import Metadata.conversions.{tuple, stringToStr}
          // From lld.llvm.org doc: readelf --string-dump .comment <output-file>
          dbg("llvm.ident")(tuple(s"$compilerInfo ($buildInfo)"))
        }
      }
      override def sourceCodeCache: SourceCodeCache =
        throw new UnsupportedOperationException()
    }
  }

  private[scalanative] def depends(implicit
      platform: PlatformInfo
  ): Seq[nir.Global] = {
    val buf = mutable.UnrolledBuffer.empty[nir.Global]
    buf ++= Lower.depends
    buf ++= Generate.depends
    buf.toSeq
  }
}
