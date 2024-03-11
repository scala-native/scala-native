package scala.scalanative.build

/** An object describing configuration of the Scala Native semantics. */
sealed trait SemanticsConfig {
//format: off
  /** Controls behaviour of final fields and their complaince with the Java Memory Model. 
   *  The outputs of the program would depend of compliance level:
   *  - [[JVMMemoryModelCompliance.Strict]] all final fields are synchronized - ensures safe publication,but it might lead to runtime performance overhead.
   *  - [[JVMMemoryModelCompliance.None]] final fields are never synchronized - no runtime overhead when accessing final fields, but it might lead to unexpected state in highly concurrent programs.
   *  - [[JVMMemoryModelCompliance.Relaxed]] (default) only fields marked with scala.scalanative.annotation.safePublish are synchronized.
   */
  def finalFields: JVMMemoryModelCompliance
  /** Sets the behaviour of final fields and their complaince with the Java Memory Model
   *   The outputs of the program would depend of compliance level:
   *  - [[JVMMemoryModelCompliance.Strict]] all final fields are synchronized - ensures safe publication,but it might lead to runtime performance overhead.
   *  - [[JVMMemoryModelCompliance.None]] final fields are never synchronized - no runtime overhead when accessing final fields, but it might lead to unexpected state in highly concurrent programs.
   *  - [[JVMMemoryModelCompliance.Relaxed]] (default) only fields marked with scala.scalanative.annotation.safePublish are synchronized.
   */
  def withFinalFields(value: JVMMemoryModelCompliance): SemanticsConfig

  /**
    * Controls behaviour of calls to extern methods when executing in multithreading mode.
    * When executing extern functions Garbage Collector needs to be notified about the internal state of thread, it's required to correctly track reachable objects and skip waiting for threads executing unmanged code.
    * When disabled (default) only calls to methods annotated with `scala.scalanative.unsafe.blocking` would notify the GC - it allows to reduce overhead of extern method calls, but might lead to deadlocks or longer GC pauses when waiting for unannotated blocking function call.
    * When enabled every invocation of foreign function would notify the GC about the thread state which guarantess no deadlocks introduced by waiting for threads executing foreign code, but might reduce overall performance.
    */
  def strictExternCallSemantics: Boolean
  /**
    * Sets behaviour of calls to extern methods when executing in multithreading mode.
    * When executing extern functions Garbage Collector needs to be notified about the internal state of thread, it's required to correctly track reachable objects and skip waiting for threads executing unmanged code.
    * When disabled only calls to methods annotated with `scala.scalanative.unsafe.blocking` would notify the GC - it allows to reduce overhead of extern method calls, but might lead to deadlocks or longer GC pauses when waiting for unannotated blocking function call.
    * When enabled every invocation of foreign function would notify the GC about the thread state which guarantess no deadlocks introduced by waiting for threads executing foreign code, but might reduce overall performance.
    */
  def withStrictExternCallSemantics(value: Boolean): SemanticsConfig
// format: on

  private[scalanative] def show(indent: String): String
}

object SemanticsConfig {
  val default: SemanticsConfig =
    Impl(
      finalFields = JVMMemoryModelCompliance.Relaxed,
      strictExternCallSemantics = false
    )

  private[build] case class Impl(
      finalFields: JVMMemoryModelCompliance,
      strictExternCallSemantics: Boolean
  ) extends SemanticsConfig {
    override def withFinalFields(
        value: JVMMemoryModelCompliance
    ): SemanticsConfig = copy(finalFields = value)
    override def withStrictExternCallSemantics(
        value: Boolean
    ): SemanticsConfig = copy(strictExternCallSemantics = value)

    override def toString: String = show(indent = " ")
    override private[scalanative] def show(indent: String): String = {
      s"""SemanticsConfig(
          |$indent- finalFields: ${finalFields}
          |$indent- strictExternCallSemantics: ${strictExternCallSemantics}
          |$indent)""".stripMargin
    }
  }
}

sealed abstract class JVMMemoryModelCompliance {
  final def isStrict = this == JVMMemoryModelCompliance.Strict
  final def isRelaxed = this == JVMMemoryModelCompliance.Relaxed
  final def isNone = this == JVMMemoryModelCompliance.None
}
object JVMMemoryModelCompliance {

  /** Guide toolchain to ignore JVM memory model specification */
  case object None extends JVMMemoryModelCompliance

  /** Guide toolchain to use a relaxed JVM memory model specification, typically
   *  guided with source code annotations
   */
  case object Relaxed extends JVMMemoryModelCompliance

  /** Guide toolchain to strictly follow JVM memory model */
  case object Strict extends JVMMemoryModelCompliance

}
