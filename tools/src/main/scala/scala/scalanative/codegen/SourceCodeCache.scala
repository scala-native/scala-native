package scala.scalanative
package codegen

import scala.scalanative.io.VirtualDirectory
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.mutable
import scala.collection.concurrent.TrieMap
import scala.annotation.nowarn

class SourceCodeCache(config: build.Config) {
  lazy val sourceCodeDir = {
    val dir = config.workDir.resolve("sources")
    if (!Files.exists(dir)) Files.createDirectories(dir)
    dir
  }
  private val (customSourceRootJarFiles, customSourceRootDirs) =
    config.compilerConfig.sourceLevelDebuggingConfig.customSourceRoots
      .partition(_.getFileName().toString().endsWith(".jar"))
  private lazy val customSourceRootJars =
    customSourceRootJarFiles.flatMap(unpackSourcesJar)

  private lazy val classpathJarsSources: Map[Path, Path] = {
    def fromClassPath = config.classPath
      .zip(
        config.classPath.map { cp =>
          correspondingSourcesJar(cp).flatMap(unpackSourcesJar)
        }
      )
    def fromSourcesClassPath = config.sourcesClassPath
      .map(scp => sourcesJarToJar(scp) -> unpackSourcesJar(scp))

    (fromSourcesClassPath ++ fromClassPath).collect {
      case (nirSources, Some(scalaSources)) => nirSources -> scalaSources
    }.toMap
  }

  private val cache: mutable.Map[nir.SourceFile, Option[Path]] =
    TrieMap.empty
  private val loggedMissingSourcesForCp = mutable.Set.empty[Path]

  private val cwd = Paths.get(".").toRealPath()
  private lazy val localSourceDirs = {
    val directories = IndexedSeq.newBuilder[Path]
    Files.walkFileTree(
      cwd,
      java.util.EnumSet.of(FileVisitOption.FOLLOW_LINKS),
      Integer.MAX_VALUE,
      new SimpleFileVisitor[Path] {
        override def visitFile(
            file: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = FileVisitResult.CONTINUE

        override def preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          val sourcesStream = Files.newDirectoryStream(dir, "*.scala")
          val hasScalaSources =
            try sourcesStream.iterator().hasNext()
            finally sourcesStream.close()
          if (hasScalaSources) directories += dir
          FileVisitResult.CONTINUE
        }
      }
    )
    directories.result()
  }

  def findSources(
      source: nir.SourceFile.Relative,
      pos: nir.SourcePosition
  ): Option[Path] = {
    assert(
      config.compilerConfig.sourceLevelDebuggingConfig.enabled,
      "Sources shall not be reoslved in source level debuging is disabled"
    )
    assert(
      pos.source eq source,
      "invalid usage, `pos.source` shall eq `source`"
    )
    cache.getOrElseUpdate(
      pos.source, {
        // NIR sources are always put in the package name similarry to sources in jar
        // Reconstruct path as it might have been created in incompatibe file system
        val packageBasedSourcePath = {
          val filename = source.path.getFileName()
          Option(
            Paths
              .get(pos.nirSource.path.toString().stripPrefix("/"))
              .getParent()
          ).map(_.resolve(filename))
            .getOrElse(filename)
        }

        // most-likely for external dependency
        def fromCorrespondingSourcesJar = classpathJarsSources
          .get(pos.nirSource.directory)
          .map(_.resolve(packageBasedSourcePath))
          .find(Files.exists(_))

        // fallback, check other source dirs
        def fromAnySourcesJar =
          classpathJarsSources.values.iterator
            .map(_.resolve(packageBasedSourcePath))
            .find(Files.exists(_))

        // likekly for local sub-projects
        def fromRelativePath = {
          val filename = source.path.getFileName()
          source.directory
            .foldLeft(localSourceDirs.iterator) {
              case (it, sourceRelativeDir) =>
                it.filter(_.endsWith(sourceRelativeDir))
            }
            .map(_.resolve(filename))
            .find(Files.exists(_))
        }

        def fromCustomSourceRoots = {
          def asJar = customSourceRootJars.iterator
            .map(_.resolve(packageBasedSourcePath))
            .find(Files.exists(_))
          def asDir = customSourceRootDirs.iterator
            .flatMap { dir =>
              val subPathsCount = source.path.getNameCount()
              Seq
                .tabulate(subPathsCount - 1)(from =>
                  source.path.subpath(from, subPathsCount)
                )
                .iterator
                .map(dir.resolve(_))
            }
            .find(Files.exists(_))

          asJar.orElse(asDir)
        }

        fromCorrespondingSourcesJar
          .orElse(fromCustomSourceRoots)
          .orElse(fromRelativePath)
          .orElse(fromAnySourcesJar)
          .orElse {
            if (loggedMissingSourcesForCp.add(pos.nirSource.directory))
              config.logger.warn(
                s"Failed to resolve Scala sources for NIR symbols defined in ${pos.nirSource.directory} - they would be unavailable in debugger. You can try to add custom custom source directory or jars to config and try again."
              )
            None
          }
      }
    )
  }

  private def correspondingSourcesJar(jarPath: Path): Option[Path] = {
    val jarFileName = jarPath.getFileName()
    val sourcesSiblingJar = jarToSourcesJar(jarPath)
    Option(sourcesSiblingJar).filter(Files.exists(_))
  }

  private def unpackSourcesJar(jarPath: Path): Option[Path] = {
    val jarFileName = jarPath.getFileName()
    def outputPath = {
      val outputFileName = Paths.get(
        jarFileName
          .toString()
          .stripSuffix(".jar")
          .stripSuffix("-sources")
      )
      @nowarn
      val pathElements = {
        import scala.collection.JavaConverters._
        jarPath.iterator().asScala.toSeq.map(_.toString)
      }
      if (pathElements.contains("target")) outputFileName
      else {
        def subpathFrom(pivotSubpath: String): Option[Path] =
          pathElements.lastIndexOf(pivotSubpath) match {
            case -1 => None
            case idx =>
              Some(
                jarPath
                  .subpath(idx, jarPath.getNameCount())
                  .resolveSibling(outputFileName)
              )
          }
        subpathFrom("maven2")
          .orElse(subpathFrom("cache"))
          .orElse(subpathFrom("ivy2"))
          .getOrElse(outputFileName)
      }
    }
    if (!jarPath.getFileName().toString.endsWith(".jar")) None
    else if (!Files.exists(jarPath)) None
    else {
      val sourcesDir = sourceCodeDir.resolve(outputPath)
      def shouldUnzip = {
        !Files.exists(sourcesDir) ||
        Files
          .getLastModifiedTime(jarPath)
          .compareTo(Files.getLastModifiedTime(sourcesDir)) > 0
      }
      if (shouldUnzip) {
        build.IO.deleteRecursive(sourcesDir)
        build.IO.unzip(jarPath, sourcesDir)
      }
      Some(sourcesDir)
    }
  }

  private def sourcesJarToJar(sourcesJar: Path): Path = {
    sourcesJar.resolveSibling(
      sourcesJar.getFileName().toString().stripSuffix("-sources.jar") + "jar"
    )
  }
  private def jarToSourcesJar(sourcesJar: Path): Path = {
    sourcesJar.resolveSibling(
      sourcesJar.getFileName().toString().stripSuffix(".jar") + "-sources.jar"
    )
  }
}
