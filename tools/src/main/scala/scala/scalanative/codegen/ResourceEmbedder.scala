package scala.scalanative
package codegen

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult._
import java.nio.file.Files
import java.nio.file.Files._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.EnumSet
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalanative.build.Config
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.util.Scope

private[scalanative] object ResourceEmbedder {

  private case class ClasspathFile(
      accessPath: Path,
      pathName: String,
      classpathDirectory: VirtualDirectory
  )

  def apply(config: Config): Seq[nir.Defn.Var] = Scope { implicit scope =>
    val classpath = config.classPath
    def toGlob(pattern: String) = s"glob:$pattern"

    // Internal patterns which should be always excluded and never logged
    val internalExclusionPatterns =
      Seq(
        "/scala-native/**",
        "/LICENSE",
        "/NOTICE",
        "/BUILD",
        "/rootdoc.txt",
        "/META-INF/**",
        "/**.class",
        "/**.nir",
        "/**.tasty"
      ).map(toGlob)

    val includePatterns =
      config.compilerConfig.resourceIncludePatterns.map(toGlob)
      // explicitly enabled pattern overwrites exclude pattern
    val excludePatterns = {
      (config.compilerConfig.resourceExcludePatterns).map(toGlob) ++
        internalExclusionPatterns
    }.diff(includePatterns)

    implicit val position: nir.SourcePosition = nir.SourcePosition.NoPosition

    val notInIncludePatterns =
      s"Not matched by any include pattern: [${includePatterns.map(pat => s"'$pat'").mkString(", ")}]"
    case class IgnoreReason(reason: String, shouldLog: Boolean = true)
    case class Matcher(matcher: PathMatcher, pattern: String)

    /** If the return value is defined, the given path should be ignored. If
     *  it's None, the path should be included.
     */
    def shouldIgnore(
        includeMatchers: Seq[Matcher],
        excludeMatchers: Seq[Matcher]
    )(path: Path): Option[IgnoreReason] =
      includeMatchers
        .find(_.matcher.matches(path))
        .map(_.pattern)
        .map { includePattern =>
          excludeMatchers
            .find(_.matcher.matches(path))
            .map(_.pattern)
            .map(excludePattern =>
              IgnoreReason(
                s"Matched by '$includePattern', but excluded by '$excludePattern'",
                shouldLog = !internalExclusionPatterns.contains(excludePattern)
              )
            )
        }
        .getOrElse {
          Some(
            IgnoreReason(
              notInIncludePatterns,
              shouldLog = !excludeMatchers
                .find(_.matcher.matches(path))
                .exists(matcher =>
                  internalExclusionPatterns.contains(matcher.pattern)
                )
            )
          )
        }

    val foundFiles =
      if (config.compilerConfig.embedResources) {
        classpath.flatMap { classpath =>
          val virtualDir = VirtualDirectory.real(classpath)
          def makeMatcher(pattern: String) =
            Matcher(
              matcher = virtualDir.pathMatcher(pattern),
              pattern = pattern
            )
          val includeMatchers = includePatterns.map(makeMatcher)
          val excludeMatchers = excludePatterns.map(makeMatcher)
          val applyPathMatchers =
            shouldIgnore(includeMatchers, excludeMatchers)(_)
          virtualDir.files
            .flatMap { path =>
              // Use the same path separator on all OSs
              val pathString = path.toString().replace(File.separator, "/")
              val (pathName, correctedPath) =
                if (!pathString.startsWith("/")) { // local file
                  ("/" + pathString, classpath.resolve(path))
                } else { // other file (f.e in jar)
                  (pathString, path)
                }

              applyPathMatchers(path) match {
                case Some(IgnoreReason(reason, shouldLog)) =>
                  if (shouldLog)
                    config.logger.debug(s"Did not embed: $pathName - $reason")
                  None
                case None =>
                  if (isSourceFile((path))) None
                  else if (Files.isDirectory(correctedPath)) None
                  else Some(ClasspathFile(path, pathName, virtualDir))
              }
            }
        }
      } else Seq.empty

    def filterEqualPathNames(
        path: List[ClasspathFile]
    ): List[ClasspathFile] = {
      path
        .foldLeft((Set.empty[String], List.empty[ClasspathFile])) {
          case ((visited, filtered), (file @ ClasspathFile(_, pathName, _)))
              if !visited.contains(pathName) =>
            (visited + pathName, file :: filtered)
          case (state, _) => state
        }
        ._2
    }

    val embeddedFiles = filterEqualPathNames(foundFiles.toList)

    val pathValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val encodedPath = pathName.toString.getBytes().map(nir.Val.Byte(_))
        nir.Val.ArrayValue(nir.Type.Byte, encodedPath.toSeq)
    }

    val contentValues = embeddedFiles.map {
      case ClasspathFile(accessPath, pathName, virtDir) =>
        val fileBuffer = virtDir.read(accessPath)
        val encodedContent = fileBuffer.array().map(nir.Val.Byte(_))
        nir.Val.ArrayValue(nir.Type.Byte, encodedContent.toSeq)
    }

    def generateArrayVar(name: String, arrayValue: nir.Val.ArrayValue) = {
      nir.Defn.Var(
        nir.Attrs.None,
        extern(name),
        nir.Type.Ptr,
        nir.Val.Const(
          arrayValue
        )
      )
    }

    def generateExtern2DArray(name: String, content: Seq[nir.Val.Const]) = {
      generateArrayVar(
        name,
        nir.Val.ArrayValue(
          nir.Type.Ptr,
          content
        )
      )
    }

    def generateExternLongArray(name: String, content: Seq[nir.Val.Int]) = {
      generateArrayVar(
        name,
        nir.Val.ArrayValue(
          nir.Type.Int,
          content
        )
      )
    }

    val generated =
      Seq(
        generateExtern2DArray(
          "__scala_native_resources_all_path",
          pathValues.toIndexedSeq.map(nir.Val.Const(_))
        ),
        generateExtern2DArray(
          "__scala_native_resources_all_content",
          contentValues.toIndexedSeq.map(nir.Val.Const(_))
        ),
        generateExternLongArray(
          "__scala_native_resources_all_path_lengths",
          pathValues.toIndexedSeq.map(path => nir.Val.Int(path.values.length))
        ),
        generateExternLongArray(
          "__scala_native_resources_all_content_lengths",
          contentValues.toIndexedSeq.map(content =>
            nir.Val.Int(content.values.length)
          )
        ),
        nir.Defn.Var(
          nir.Attrs.None,
          extern("__scala_native_resources_amount"),
          nir.Type.Ptr,
          nir.Val.Int(contentValues.length)
        )
      )

    embeddedFiles.foreach { classpathFile =>
      config.logger.info("Embedded resource: " + classpathFile.pathName)
    }

    generated
  }

  private def extern(id: String): nir.Global.Member =
    nir.Global.Member(nir.Global.Top("__"), nir.Sig.Extern(id))

  private val sourceExtensions =
    Seq(
      ".class",
      ".tasty",
      ".nir",
      ".jar",
      ".scala",
      ".java"
    )

  private def isSourceFile(path: Path): Boolean = {
    if (path.getFileName == null) false
    else sourceExtensions.exists(path.getFileName.toString.endsWith(_))
  }
}
