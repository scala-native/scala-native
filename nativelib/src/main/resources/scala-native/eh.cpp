#include <exception>
#include "eh.h"

// Scala Native compiles Scala's exception in C++-compatible
// manner under the hood. Every exception thrown on the Scala
// side is wrapped into ExceptionWrapper and only
// ExceptionWrapper-based exceptions can be caught by
// Scala code. We currently do not support catching arbitrary
// C++ exceptions.

extern "C" {
void scalanative_throw(void *obj) { throw scalanative::ExceptionWrapper(obj); }
}
