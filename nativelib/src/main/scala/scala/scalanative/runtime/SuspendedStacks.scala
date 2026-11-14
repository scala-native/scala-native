package scala.scalanative.runtime

/** The stacks of a suspended one-shot continuation.
 *
 *  The garbage collector has those stacks scanned when it marks this object:
 *  delimcc_oneshot.c registers an object tracer for this class, see
 *  `gc/shared/GCHooks.h`. It reads `stacks` as the first field.
 */
private[runtime] final class SuspendedStacks(var stacks: RawPtr)
