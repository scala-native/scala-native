package scala.scalanative
package runtime

class ThreadManager {

}

object ThreadManager {

  // return errors ?

  def suspend(thread: Thread): Unit = {
    // check if not already suspended and suspsend with signals
  }

  def resume(thread: Thread): Unit = {
    // check if suspended and resume with signals
  }

}
