package scala.scalanative.posix

import scala.scalanative.native.{CInt, CStruct5, extern}
import scala.scalanative.posix.time.timespec

@extern
object sched {

  type sched_param = CStruct5[CInt, CInt, timespec, timespec, CInt]

}
