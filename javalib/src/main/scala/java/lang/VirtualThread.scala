package java.lang

final private[lang] class VirtualThread(
    name: String,
    characteristics: Int,
    task: Runnable
) extends Thread(name, characteristics) {

  // TODO: continuations-based thread implementation
  override def run(): Unit = throw new UnsupportedOperationException(
    "Running VirtualThreads is not yet supported"
  )

  override def getState(): Thread.State = Thread.State.NEW
}
