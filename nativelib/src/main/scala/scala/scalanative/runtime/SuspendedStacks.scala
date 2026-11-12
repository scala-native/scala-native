package scala.scalanative.runtime

/** The stacks of a suspended one-shot continuation.
 *
 *  The garbage collector scans those stacks when it marks this object, see
 *  `gc/shared/SuspendedStacks.h`. It reads `stacks` as the first field.
 */
private[runtime] final class SuspendedStacks(var stacks: RawPtr)
