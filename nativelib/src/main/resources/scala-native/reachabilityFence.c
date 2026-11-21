// Keeps `obj` live up to this call, even when inlined: the collectors scan
// stacks and registers conservatively, so a live pointer keeps it reachable.
// See java.lang.ref.Reference.reachabilityFence.
void scalanative_reachability_fence(void *obj) {
    __asm__ volatile("" : : "r"(obj) : "memory");
}
