package scala.scalanative.runtime

package object testinterface {
  def drainNativeExecutionContext(): Unit = NativeExecutionContext.loop()
}
