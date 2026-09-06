import java.util.concurrent.TimeUnit

enablePlugins(ScalaNativePlugin)

Compile / mainClass := Some("Main")

nativeConfig ~= { _.withMultithreading(true) }

scalaVersion := {
  val version = System.getProperty("scala.version")
  if (version == null)
    sys.error("The system property 'scala.version' is not defined")
  version
}

def nativeExecutable(
    value: Any
)(implicit conv: xsbti.FileConverter): java.io.File = value match {
  case file: java.io.File        => file
  case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
}

lazy val runHeapExhaustion =
  taskKey[Unit]("Run with a constrained managed heap")

runHeapExhaustion := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val executable = nativeExecutable((Compile / nativeLink).value)
  val builder = new ProcessBuilder(executable.getAbsolutePath)
  builder.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  builder.environment().put("GC_MAXIMUM_HEAP_SIZE", "2M")
  builder.inheritIO()
  val process = builder.start()
  process.getOutputStream.close()
  if (!process.waitFor(60, TimeUnit.SECONDS)) {
    process.destroyForcibly()
    sys.error("Heap exhaustion test timed out")
  }
  if (process.exitValue() != 0)
    sys.error(s"Heap exhaustion test exited with ${process.exitValue()}")
}
