object LongLivingThread {
  def main(args: Array[String]): Unit = {
    println("Main thread starts")
    val mainThread = Thread.currentThread()
    val longLivingThread = new Thread("LivesALongTime") {
      override def run() = {
        println("Waiting for main thread to finish")
        mainThread.join()
        Thread.sleep(100)
        println("Other thread is still alive")
      }
    }
    assert(!longLivingThread.isDaemon)
    longLivingThread.start()
    println("Main thread ends")
  }
}
