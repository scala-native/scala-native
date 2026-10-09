package build

// scalafmt: { maxColumn = 120 }

import sbt.CacheImplicits.given
import sbt.Keys._
import sbt._
import sbt.nio.Keys._
import sbt.nio.file.Glob

import java.io.StringWriter
import java.util.Map

import freemarker.template.{Configuration, TemplateExceptionHandler}
import xsbti.HashedVirtualFileRef

object VarHandleTestSources {
  private val templateDirectory = settingKey[File]("VarHandle unit-test template directory")
  val generateVarHandleTests =
    taskKey[Seq[HashedVirtualFileRef]]("Generate the shared, statically typed VarHandle test matrices")

  private val generatorInputs = Def.task {
    val converter = fileConverter.value
    (Test / generateVarHandleTests / inputFileStamps).value.map {
      case (path, _) => converter.toVirtualFile(path): HashedVirtualFileRef
    }
  }

  val settings: Seq[Setting[_]] = Def.settings(
    Test / templateDirectory := (ThisBuild / baseDirectory).value /
      "unit-tests/shared/src/test/require-jdk9/org/scalanative/testsuite/javalib/invoke",
    Test / generateVarHandleTests / fileInputs := {
      val templates = ((Test / templateDirectory).value ** "VarHandle*.ftl").get()
      val root = (ThisBuild / baseDirectory).value
      val buildInputs = List(root / "project/VarHandleTestSources.scala", root / "project/build.sbt")
      (templates ++ buildInputs).map(file => Glob(file.toPath))
    },
    Test / generateVarHandleTests := {
      val inputs = generatorInputs.value
      require(inputs.nonEmpty, "VarHandle test generator source is missing")
      val output = (Test / sourceManaged).value / "varhandle"
      val converter = fileConverter.value
      val includeStatic = CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
      val sources =
        if ((Global / Settings.javaVersion).value >= 9)
          generate((Test / templateDirectory).value, includeStatic)
        else Nil
      streams.value.log.info(s"Generating ${sources.size} VarHandle test sources")
      sources.map {
        case (name, content) =>
          val file = output / name
          IO.write(file, content)
          Def.declareOutput(converter.toVirtualFile(file.toPath)): HashedVirtualFileRef
      }
    },
    Test / sourceGenerators += Def.task {
      val converter = fileConverter.value
      (Test / generateVarHandleTests).value.map(file => converter.toPath(file).toFile)
    }.taskValue
  )

  private def generate(directory: File, includeStatic: Boolean): Seq[(String, String)] = {
    val configuration = new Configuration(Configuration.VERSION_2_3_35)
    configuration.setDirectoryForTemplateLoading(directory)
    configuration.setDefaultEncoding("UTF-8")
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
    configuration.setLogTemplateExceptions(false)
    configuration.setWrapUncheckedExceptions(true)
    configuration.setFallbackOnNullLoopVariable(false)

    val matrices = List("Matrix", "DiscardedMatrix", "WideningMatrix", "BoxingMatrix", "OperandMatrix")
    val shapes = if (includeStatic) List(false, true) else List(false)
    val templates = (for (matrix <- matrices; isStatic <- shapes)
      yield (s"VarHandle${matrix}Test.scala.ftl", isStatic)) :+
      ("VarHandlePrimitiveLookupTest.scala.ftl", false)

    templates.map {
      case (name, isStatic) =>
        val output = new StringWriter
        configuration.getTemplate(name).process(Map.of("isStatic", Boolean.box(isStatic)), output)
        val generatedName =
          if (isStatic) name.replace("VarHandle", "VarHandleStatic").stripSuffix(".ftl")
          else name.stripSuffix(".ftl")
        generatedName -> output.toString
    }
  }
}
