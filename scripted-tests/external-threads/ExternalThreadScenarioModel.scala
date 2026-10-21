import sbt._

import scala.scalanative.build.{BuildTarget, GC, Mode, NativeConfig}

object ExternalThreadScenarioModel {
  def dataLines(lines: Seq[String]): Seq[String] =
    lines.map(_.trim).filterNot(line => line.isEmpty || line.startsWith("#"))

  sealed trait ThreadMode { def accepts(multithreaded: Boolean): Boolean }
  case object AllThreads extends ThreadMode { def accepts(mt: Boolean) = true }
  case object SingleThread extends ThreadMode { def accepts(mt: Boolean) = !mt }
  case object MultipleThreads extends ThreadMode {
    def accepts(mt: Boolean) = mt
  }

  sealed abstract class Host(val name: String)
  case object C extends Host("c")
  case object Cpp extends Host("cpp")
  def host(name: String): Host = name match {
    case "c"   => C
    case "cpp" => Cpp
    case other => sys.error("Unknown host language: " + other)
  }

  sealed abstract class Outcome(val name: String, val timeoutSeconds: Long)
  case object Success extends Outcome("success", 60)
  case object Fatal extends Outcome("fatal", 60)

  final case class Scenario(
      id: String,
      threads: ThreadMode,
      hosts: Set[Host],
      outcome: Outcome,
      milestones: Seq[String],
      diagnostic: Option[String],
      collectors: Set[GC],
      initialize: Boolean
  ) {
    require(id.matches("[A-Z]+-[0-9]{2}"), "Invalid scenario ID: " + id)
    require(
      outcome != Fatal || diagnostic.nonEmpty,
      "Fatal scenario needs an exact diagnostic"
    )
    def accepts(cfg: NativeConfig): Boolean =
      threads.accepts(cfg.multithreading.contains(true)) &&
        (collectors.isEmpty || collectors(cfg.gc))

  }

  object Scenario {
    def parse(line: String): Scenario = line.split("\t", -1).toList match {
      case id :: threads :: hosts :: outcome :: milestones :: diagnostic :: collectors :: initialize :: Nil =>
        Scenario(
          id,
          threads match {
            case "all"    => AllThreads
            case "single" => SingleThread
            case "multi"  => MultipleThreads
            case other    => sys.error("Unknown thread mode: " + other)
          },
          hosts match {
            case "both" => Set(C, Cpp)
            case name   => Set(host(name))
          },
          outcome match {
            case "success" => Success
            case "fatal"   => Fatal
            case other     => sys.error("Unknown outcome: " + other)
          },
          milestones.split(",").filter(_.nonEmpty).toSeq,
          Option(diagnostic).filter(value => value.nonEmpty && value != "-"),
          if (collectors == "all") Set.empty
          else collectors.split(",").map(GC(_)).toSet,
          Configuration.boolean(initialize)
        )
      case _ => sys.error("Expected eight scenario fields: " + line)
    }
  }

  final case class Configuration(
      name: String,
      gc: GC,
      mode: Mode,
      multithreading: Boolean,
      target: BuildTarget,
      strict: Boolean,
      cppExceptions: Boolean
  ) {
    def configure(base: NativeConfig): NativeConfig = {
      val cppOptions = base.cppOptions.filterNot(option =>
        Set("-fcxx-exceptions", "-fno-cxx-exceptions")(option)
      )
      base
        .withGC(gc)
        .withMode(mode)
        .withMultithreading(multithreading)
        .withBuildTarget(target)
        .withSemanticsConfig(_.withStrictExternCallSemantics(strict))
        .withCppOptions(
          cppOptions ++ (if (cppExceptions) Seq("-fcxx-exceptions") else Nil)
        )
    }
  }
  object Configuration {
    def boolean(value: String): Boolean = value match {
      case "true"  => true
      case "false" => false
      case other => sys.error("Expected boolean configuration field: " + other)
    }
    def parse(line: String): Configuration = line.split("\t", -1).toList match {
      case name :: gc :: mode :: mt :: target :: strict :: cpp :: Nil =>
        Configuration(
          name,
          GC(gc),
          Mode(mode),
          boolean(mt),
          target match {
            // BuildTarget has no string parser; these are the fixture's library aliases.
            case "static" => BuildTarget.libraryStatic
            case "shared" => BuildTarget.libraryDynamic
            case other    => sys.error("Unknown library target: " + other)
          },
          boolean(strict),
          boolean(cpp)
        )
      case _ => sys.error("Expected seven configuration fields: " + line)
    }
  }

}
