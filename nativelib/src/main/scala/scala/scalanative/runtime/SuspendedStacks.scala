package scala.scalanative.runtime

/** The stacks of a suspended one-shot continuation.
 *
 *  The GC has them scanned when it marks this object, see
 *  `gc/shared/GCHooks.h`. `stacks` must stay the only field: the tracer reads
 *  the first one, and fields are not laid out in declaration order.
 */
private[runtime] final class SuspendedStacks(var stacks: RawPtr) {

  /** Address of `stacks`, which the runtime clears on resume. Taken here:
   *  `classFieldRawPtr` only finds fields of a class compiled with the call.
   */
  def stacksAddress: RawPtr = Intrinsics.classFieldRawPtr(this, "stacks")
}
