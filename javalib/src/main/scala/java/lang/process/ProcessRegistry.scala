package java.lang.process

private[process] trait ProcessRegistry {

  /** Tries to complete the process.
   *  @param ec
   *    exit code, if non-negative; otherwise, request to check for it
   *  @return
   *    true if completed only now for non-negative [[ec]], and exited otherwise
   */
  def completeWith(pid: Long)(ec: Int): Boolean
}
