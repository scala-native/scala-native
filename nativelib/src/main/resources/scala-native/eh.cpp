#if defined(SCALANATIVE_USING_CPP_EXCEPTIONS)

#include <cstdlib>
#include <exception>
#include <mutex>
#include "externalThreads.h"
extern "C" {
// Keep this translation unit independent of the collector's C-only headers.
bool scalanative_externalGC_addRoots(void *low, void *high);
void scalanative_GC_remove_roots(void *low, void *high);
void scalanative_externalGC_admit(void);
}
#include <cstdio>

#if defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
#include "string_constants.h"
#include <cstdio>
#endif

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
    ExceptionWrapper(Exception _obj, bool root) : obj(_obj), rooted(root) {
        addRoot();
    }
    ExceptionWrapper(const ExceptionWrapper &other)
        : std::exception(other), obj(other.obj), rooted(other.rooted) {
        addRoot();
    }
    ~ExceptionWrapper() override {
        if (rooted)
            scalanative_GC_remove_roots(&obj, &obj + 1);
    }
    Exception obj;

  private:
    bool rooted = false;
    void addRoot() {
        if (rooted && !scalanative_externalGC_addRoots(&obj, &obj + 1)) {
            fprintf(stderr, "Scala Native external thread: exception root "
                            "allocation failure\n");
            std::abort();
        }
    }
};
} // namespace scalanative

extern "C" {
#if defined(__SCALANATIVE_DELIMCC)
/*
 * Continuation exception escape (C++): when a resumed body throws
 * scalanative::ExceptionWrapper and no handler in the continuation catches it,
 * the C++ unwinder cannot cross the longjmp boundary (resumed code runs on a
 * copied stack), so it runs out of frames and calls std::terminate(). We
 * install a custom terminate handler once at load time (process-wide). When
 * we're in a continuation-resume context
 * (scalanative_continuation_exception_handler set by delimcc.c), it extracts
 * the current exception (via std::current_exception) and longjmps to the
 * resumer instead of terminating.
 */
static std::terminate_handler default_terminate_handler = NULL;
static std::once_flag continuation_terminate_handler_once;

static void continuation_terminate_handler() {
    ContinuationExceptionHandler ceh =
        scalanative_continuation_exception_handler();
    if (ceh.env != NULL && ceh.exception_slot != NULL) {
        std::exception_ptr eptr = std::current_exception();
        if (eptr != nullptr) {
            try {
                std::rethrow_exception(eptr);
            } catch (scalanative::ExceptionWrapper &e) {
                jmp_buf *env = ceh.env;
                *ceh.exception_slot = e.obj;
                scalanative_continuation_exception_handler_clear();
                longjmp(*env, 1);
            } catch (...) {
                /* not our exception, fall through */
            }
        }
    }
    if (default_terminate_handler)
        default_terminate_handler();
    fprintf(stderr,
            SN_FATAL_ERROR_MSG(
                "Failed to throw exception, not found a valid catch handler "
                "for exception when unwinding execution stack.\n"));
    fflush(stderr);
    std::abort();
}

void scalanative_continuation_exception_terminate_handler_install(void) {
    std::call_once(continuation_terminate_handler_once, []() {
        default_terminate_handler =
            std::set_terminate(continuation_terminate_handler);
    });
}
#endif

void scalanative_throw(void *obj) { throw scalanative::ExceptionWrapper(obj); }
[[noreturn]] void
scalanative_leave_managed_and_throw(scalanative_thread_scope_t scope,
                                    void *obj) {
    scalanative::ExceptionWrapper exception(obj, true);
    scalanative_leave_managed(scope);
    throw exception;
}
size_t scalanative_Throwable_sizeOfExceptionWrapper() { return 0; }
void scalanative_Exception_onCatch(Exception self) {
    scalanative_externalGC_admit();
    if (self) {
        OnCatchHandler handler = scalanative_Throwable_onCatchHandler(self);
        if (handler)
            handler(self);
    }
}
}
#endif
