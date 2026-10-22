package scala.scalanative
package codegen

import java.nio.file.Path

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

import org.junit.Assert._

import scala.scalanative.build.NativeConfig
import scala.scalanative.linker.ReachabilityAnalysis
import scalanative.build.{Config, ScalaNative}
import scalanative.util.Scope

/** Base class to test code generation */
abstract class CodeGenSpec extends OptimizerSpec {

  /** Inspect the same lowered NIR used by LLVM emission, before text formatting
   *  or LLVM optimization can hide entry ordering and exit paths.
   */
  def lowered[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity,
      library: Boolean = false
  )(f: Seq[nir.Defn] => T): T =
    optimize(entry, sources, setupConfig.compose(_.withBaseName(entry))) {
      case (config, optimized) =>
        val proxies =
          GenerateReflectiveProxies(optimized.dynimpls, optimized.defns)
        implicit val platform: PlatformInfo = PlatformInfo(config)
        implicit val metadata: Metadata =
          new Metadata(optimized, config, proxies)
        implicit val logger: build.Logger = config.logger
        val generated = Generate(
          if (library) None else ScalaNative.encodedMainClass(config),
          optimized.defns ++ proxies
        )
        f(Await.result(llvm.CodeGen.lower(generated), 1.minute))
    }

  /** Performs code generation on the given sources.
   *
   *  @param entry
   *    The entry point for the linker.
   *  @param sources
   *    Map from file name to file content representing all the code to compile
   *  @param fn
   *    A function to apply to the products of the compilation.
   *  @return
   *    The result of applying `fn` to the resulting file.
   */
  def codegen[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(
      f: (Config, ReachabilityAnalysis.Result, Seq[Path]) => T
  ): T =
    optimize(entry, sources, setupConfig.compose(_.withBaseName(entry))) {
      case (config, optimized) =>
        Scope { implicit in =>
          // Await each IR Future: the outer Future only starts them. Listing the
          // workdir early can see *-body.ll files that merge then deletes.
          val generators = Await.result(
            ScalaNative.codegen(config, optimized),
            1.minute
          )
          val outfiles =
            Await.result(Future.sequence(generators), 1.minute)

          assertTrue("Empty code generator output", outfiles.nonEmpty)

          f(config, optimized, outfiles)
        }
    }

}
