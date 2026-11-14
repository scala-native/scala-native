package scala.scalanative.runtime

/** The stacks of a suspended one-shot continuation.
 *
 *  The garbage collector has those stacks scanned when it marks this object:
 *  delimcc_oneshot.c registers an object tracer for this class, see
 *  `gc/shared/GCHooks.h`. `stacks` must stay the only field: the tracer reads
 *  it as the first one, and fields are not laid out in declaration order.
 */
private[runtime] final class SuspendedStacks(var stacks: RawPtr) {

  /** Where `stacks` is stored: the runtime clears it on resume. Taken here, as
   *  `classFieldRawPtr` only finds the fields of a class compiled together with
   *  the call.
   */
  def stacksAddress: RawPtr = Intrinsics.classFieldRawPtr(this, "stacks")
}
