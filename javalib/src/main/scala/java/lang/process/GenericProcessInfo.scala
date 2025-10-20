package java.lang.process

import java.time.{Instant, Duration}
import java.util as ju
import ju.Optional

private[process] object GenericProcessInfo {
  def apply(builder: ProcessBuilder): GenericProcessInfo = {
    val cmd = builder.command()
    if (cmd.isEmpty())
      new GenericProcessInfo(
        cmdOpt = Optional.empty[String](),
        argsOpt = Optional.empty[Array[String]]()
      )
    else {
      val size = cmd.size()
      new GenericProcessInfo(
        cmdOpt = Optional.of(cmd.get(0)),
        argsOpt =
          Optional.of(cmd.subList(1, size).toArray(new Array[String](size - 1)))
      )
    }
  }
}

private[process] class GenericProcessInfo(
    cmdOpt: Optional[String],
    argsOpt: Optional[Array[String]]
) extends ProcessHandle.Info {
  val createdAt: Long = System.nanoTime()

  override def command(): Optional[String] = cmdOpt
  override def arguments(): Optional[Array[String]] = argsOpt
  override def commandLine(): Optional[String] =
    // For comprehension variant does not compile on Scala 2.12
    cmdOpt.flatMap[String] { cmd =>
      argsOpt.map[String] { args =>
        s"$cmd ${args.mkString(" ")}"
      }
    }

  override def user(): Optional[String] =
    Optional.ofNullable(System.getProperty("user.name"))

  // Instant not implemented
  override def startInstant(): Optional[Instant] =
    Optional.of(Instant.ofEpochMilli(createdAt / 1000))
  override def totalCpuDuration(): Optional[Duration] = Optional.empty()

  override def toString: String = {
    val args =
      argsOpt.orElseGet(() => Array.empty[String]).asInstanceOf[Array[AnyRef]]
    s"[user: ${user()}, cmd: ${cmdOpt}, args: ${ju.Arrays.toString(args)}"
  }
}
