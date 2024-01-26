import scalanative.libc.stdio
import scalanative.unsafe.*
import scalanative.unsigned.*

opaque type cmark_event_type = CUnsignedInt
object cmark_event_type:
  inline def define(inline a: Long): cmark_event_type = a.toUInt
  val CMARK_EVENT_NONE = define(0)
  
@main def hello(): Unit =
  val evtype = cmark_event_type.CMARK_EVENT_NONE
  stdio.printf(c"bla: %s, hello: %d", evtype)
  