package scala.scalanative.codegen
package llvm

import java.io.File
import java.nio.file.{Path, Paths, Files}
import scala.collection.mutable
import scala.scalanative.build.Config
import scala.scalanative.build.ScalaNative.{dumpDefns, encodedMainClass}
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir._
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
    implicit val platform: PlatformInfo = PlatformInfo(config)
    implicit val meta: CodeGenMetadata =
      new CodeGenMetadata(analysis, config.compilerConfig, proxies)

    val generated = Generate(encodedMainClass(config), defns ++ proxies)
    val embedded = ResourceEmbedder(config)
    val lowered = lower(generated ++ embedded)
    lowered
      .andThen { case Success(defns) => dumpDefns(config, "lowered", defns) }
      .flatMap(emit(config, _))
  }

  private[scalanative] def lower(
      defns: Seq[Defn]
  )(implicit
      meta: CodeGenMetadata,
      logger: build.Logger,
      ec: ExecutionContext
  ): Future[Seq[Defn]] = {

    val loweringJobs = partitionBy(defns)(_.name).map {
      case (_, defns) => Future(Lower(defns))
    }

    Future
      .foldLeft(loweringJobs)(mutable.UnrolledBuffer.empty[Defn]) {
        case (buffer, defns) => buffer ++= defns
      }
      .map(_.toSeq)
  }

  /** Generate code for given assembly. */
  private def emit(config: build.Config, assembly: Seq[Defn])(implicit
      meta: CodeGenMetadata,
      ec: ExecutionContext
  ): Future[Seq[Path]] =
    Scope { implicit in =>
      val env = assembly.map(defn => defn.name -> defn).toMap
      val workDir = VirtualDirectory.real(config.workDir)

      // Partition into multiple LLVM IR files proportional to number
      // of available processesors. This prevents LLVM from optimizing
      // across IR module boundary unless LTO is turned on.
      def separate(): Future[Seq[Path]] =
        Future
          .traverse(partitionBy(assembly, procs)(_.name.top).toSeq) {
            case (id, defns) =>
              Future {
                val sorted = defns.sortBy(_.name)
                Impl(env, sorted).gen(id.toString, workDir)
              }
          }

      // Incremental compilation code generation
      def seperateIncrementally(): Future[Seq[Path]] = {
        def packageName(defn: Defn): String = {
          val name = defn.name.top.id
            .split('.')
            .init // last segment is class name
            .takeWhile(!_.contains("$")) // ignore nested classes
            .mkString(".")
          if (name.isEmpty) "__empty_package" else name
        }

        val ctx = new IncrementalCodeGenContext(config.workDir)
        ctx.collectFromPreviousState()
        Future
          .traverse(assembly.groupBy(packageName).toSeq) {
            case (packageName, defns) =>
              Future {
                val packagePath = packageName.replace(".", File.separator)
                val outFile = config.workDir.resolve(s"$packagePath.ll")
                val ownerDirectory = outFile.getParent()

                ctx.addEntry(packageName, defns)
                if (ctx.shouldCompile(packageName)) {
                  val sorted = defns.sortBy(_.name)
                  if (!Files.exists(ownerDirectory))
                    Files.createDirectories(ownerDirectory)
                  Impl(env, sorted).gen(packagePath, workDir)
                } else {
                  assert(ownerDirectory.toFile.exists())
                  config.logger.debug(
                    s"Content of package has not changed, skiping generation of $packagePath.ll"
                  )
                  config.workDir.resolve(s"$packagePath.ll")
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

    def apply(env: Map[Global, Defn], defns: Seq[Defn])(implicit
        meta: CodeGenMetadata
    ): AbstractCodeGen = {
      new AbstractCodeGen(env, defns) {
        override val os: OsCompat = {
          if (this.meta.platform.targetsWindows) new WindowsCompat(this)
          else new UnixCompat(this)
        }
      }
    }
  }

  def depends(implicit platform: PlatformInfo): Seq[Global] = {
    val buf = mutable.UnrolledBuffer.empty[Global]
    buf ++= Lower.depends
    buf ++= Generate.depends
    buf += Rt.Object.name member Rt.ScalaEqualsSig
    buf += Rt.Object.name member Rt.ScalaHashCodeSig
    buf += Rt.Object.name member Rt.JavaEqualsSig
    buf += Rt.Object.name member Rt.JavaHashCodeSig
    buf.toSeq
  }
}
