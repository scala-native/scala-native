#if defined(SCALANATIVE_USING_CPP_EXCEPTIONS)

#include <exception>

// Scala Native compiles Scala's exception in C++-compatible
// manner under the hood. Every exception thrown on the Scala
// side is wrapped into ExceptionWrapper and only
// ExceptionWrapper-based exceptions can be caught by
// Scala code. We currently do not support catching arbitrary
// C++ exceptions.

typedef void *Exception;
typedef void (*OnCatchHandler)(Exception);
extern "C" OnCatchHandler
scalanative_Throwable_onCatchHandler(Exception exception);

namespace scalanative {
class ExceptionWrapper : public std::exception {
  public:
    ExceptionWrapper(Exception _obj) : obj(_obj) {}
    ~ExceptionWrapper() {
        OnCatchHandler handler = scalanative_Throwable_onCatchHandler(obj);
        if (handler) {
            handler(obj);
        }
    }
    Exception obj;
};
} // namespace scalanative

extern "C" {
void scalanative_throw(void *obj) { throw scalanative::ExceptionWrapper(obj); }
size_t scalanative_Throwable_sizeOfExceptionWrapper() { return 0; }
}

#endif
