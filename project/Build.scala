import sbt.{File, taskKey}

object Build {
  val fetchScalaSource =
    taskKey[File]("Fetches the scala source for the current scala version")
}
