package scala.scalanative.runtime

package object junit {
  def drainNativeExecutionContext(): Unit = NativeExecutionContext.loop()
}
