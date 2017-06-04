import scala.scalanative.cpplib.GreenThread

class MyThread extends GreenThread {
  override def run(): Unit = {
    // crash the GC because it doesn't support forks
    //println("My thread");
    
    Test.counter += 1
    // switch to other thread if exists
    GreenThread.threadYield

    Test.counter += 1
    GreenThread.threadYield

    Test.counter += 1
    GreenThread.threadYield

    Test.counter += 1
    GreenThread.threadYield
  }
}

object Test {
  var counter = 0;
  def main(args: Array[String]): Unit = {
    val myThreadArray = Array.fill(1024)(new MyThread)

    println(s"Counter starts at $counter")

    myThreadArray.foreach( {
      _.fork();
    })

    println(s"Counter in between fork/join is $counter")

    myThreadArray.foreach( {
      _.join();
    })

    println(s"Counter after join is $counter")

    GreenThread.threadStop()
  }
}
