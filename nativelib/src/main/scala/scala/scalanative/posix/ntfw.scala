package scala.scalanative.posix

import scala.scalanative.native.{extern, struct, CString, FunctionPtr4, Ptr}

// http://man7.org/linux/man-pages/man3/nftw.3.html

object ftw {
  val FTW_PHYS         = 1
  val FTW_MOUNT        = 2
  val FTW_CHDIR        = 4
  val FTW_DEPTH        = 8
  val FTW_ACTIONRETVAL = 16

  val FTW_F   = 0
  val FTW_D   = 1
  val FTW_DNR = 2
  val FTW_NS  = 3
  val FTW_SL  = 4
  val FTW_DP  = 5
  val FTW_SLN = 6

  val FTW_CONTINUE      = 0
  val FTW_SKIP_SIBLINGS = 1
  val FTW_SKIP_SUBTREE  = 2
  val FTW_STOP          = 3
}

@struct class FTW private (val base: Int, val level: Int)

@extern
object Nftw {
  def nftw(
      dirpath: CString,
      fn: FunctionPtr4[CString, Ptr[stat], Int, Ptr[FTW], Int],
      nopenfd: Int,
      flags: Int
  ): Int = extern
}
