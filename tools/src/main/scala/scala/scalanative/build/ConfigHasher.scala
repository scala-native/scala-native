package scala.scalanative.build

import java.nio.file.{FileVisitOption, Files, Path, Paths}
import java.security.MessageDigest

object ConfigHasher {
  import merkle.*

  def apply(c: Config): MerkleTree = hasher(c)

  private val helper =
    new merkle.Builder(merkle.Hasher.messageDigest("SHA-256"))

  import helper.*

  private val hasher = nest[Config]("Config") { c =>
    Seq(
      string("baseDir", c.baseDir.toString),
      bool("testConfig", c.testConfig),
      string("workDir", c.workDir.toString),
      string("moduleName", c.moduleName),
      string("baseName", c.baseName),
      string("artifactName", c.artifactName),
      path("artifactPath", c.artifactPath, ToBytes.FileMtime),
      string("buildPath", c.buildPath.toString()),
      sortedPaths("classpath", c.classPath, ToBytes.TreeMtime),
      string("mainClass", c.mainClass.getOrElse("<none>")),
      nativeConfigBuilder(c.compilerConfig)
    )
  }

  private val nativeConfigBuilder = nest[NativeConfig]("NativeConfig") { nc =>
    Seq(
      string("gc", nc.gc.name),
      string("mode", nc.mode.name.toString),
      string("clang", nc.clang.toString),
      string("clangPP", nc.clangPP.toString),
      strings("linkingOptions", nc.linkingOptions),
      strings("compileOptions", nc.compileOptions),
      strings("cOptions", nc.cOptions),
      strings("cppOptions", nc.cppOptions),
      string("largetTriple", nc.targetTriple.getOrElse("<none>")),
      string("buildTarget", nc.buildTarget.toString),
      bool("linkStubs", nc.linkStubs),
      string("LTO", nc.lto.toString),
      bool("check", nc.check),
      bool("dump", nc.dump),
      bool("checkFeatures", nc.checkFeatures),
      bool("checkFatalWarnings", nc.checkFatalWarnings),
      bool("optimize", nc.optimize),
      bool("useIncrementalCompilation", nc.useIncrementalCompilation),
      string("sanitizer", nc.sanitizer.map(_.name).getOrElse("<none>")),
      string("multithreading", nc.multithreading.toString),
      bool("embedResources", nc.embedResources),
      strings("resourceIncludePatterns", nc.resourceIncludePatterns),
      strings("resourceExcludePatterns", nc.resourceExcludePatterns),
      mapOfAny("linktimeProperties", nc.linktimeProperties)
      // TODO: handle optimizerConfig
      // TODO: handle semanticsConfig
      // TODO: handle sourceLevelDebuggingConfig
    )
  }

  private def mapOfAny(label: String, m: Map[String, Any]) = {
    node(
      label,
      m.toList.sortBy(_._1).map {
        case (name, value) =>
          leaf(name, value.toString.getBytes, ToBytes.Str)
      }
    )

  }

}
