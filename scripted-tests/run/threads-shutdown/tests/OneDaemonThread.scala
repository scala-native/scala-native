object OneDaemonThread {
  def main(args: Array[String]): Unit = {
    val daemonThread = new Thread("JustMindingMyOwnBusiness") {
      override def run() = {
        synchronized {
          while (true) {
            wait()
          }
        }
      }
    }
    daemonThread.setDaemon(true)
    assert(daemonThread.isDaemon)
    daemonThread.start()
  }
}
