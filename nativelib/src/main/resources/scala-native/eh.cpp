#if defined(SCALANATIVE_USING_CPP_EXCEPTIONS)

#include <cstdlib>
#include <cstdio>
#include <exception>
#include <mutex>

#include "eh_diagnostics.h"
#include "string_constants.h"

#if defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
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
extern "C" void scalanative_Throwable_showStackTrace(Exception exception);

namespace scalanative {
class ExceptionWrapper : public std::exception {
  public:
    ExceptionWrapper(Exception _obj) : obj(_obj) {}
    Exception obj;
};
} // namespace scalanative

extern "C" {

static std::terminate_handler previous_terminate_handler = NULL;
static std::once_flag terminate_handler_once;

static void scalanative_terminate_handler() {
#if defined(__SCALANATIVE_DELIMCC)
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
#endif
    scalanative_eh_enter_abort_dump();
    Exception obj = NULL;
    std::exception_ptr eptr = std::current_exception();
    if (eptr != nullptr) {
        try {
            std::rethrow_exception(eptr);
        } catch (scalanative::ExceptionWrapper &e) {
            obj = e.obj;
        } catch (std::exception &e) {
            fprintf(stderr, "%s C++ std::exception: %s\n", snFatalErrorPrefix,
                    e.what());
        } catch (...) {
            fprintf(stderr, "%s unknown C++ exception in std::terminate\n",
                    snFatalErrorPrefix);
        }
    } else {
        fprintf(stderr, "%s std::terminate with no current exception\n",
                snFatalErrorPrefix);
    }

    fprintf(stderr, "exception object=%p\n", (void *)obj);
    scalanative_eh_dump_abort_context(
        "std::terminate (uncaught exception / unwind failed)", -1);
    if (obj && scalanative_eh_java_thread_available())
        scalanative_Throwable_showStackTrace(obj);
    fflush(stderr);
    fflush(stdout);
    if (previous_terminate_handler != NULL &&
        previous_terminate_handler != scalanative_terminate_handler) {
        previous_terminate_handler();
    }
    std::abort();
}

static void scalanative_install_terminate_handler() {
    std::call_once(terminate_handler_once, []() {
        previous_terminate_handler =
            std::set_terminate(scalanative_terminate_handler);
    });
}

#if defined(__GNUC__) || defined(__clang__)
__attribute__((constructor))
#endif
static void
scalanative_eh_cpp_init() {
    scalanative_install_terminate_handler();
}

#if defined(__SCALANATIVE_DELIMCC)
void scalanative_continuation_exception_terminate_handler_install(void) {
    scalanative_install_terminate_handler();
}
#endif

void scalanative_throw(void *obj) {
    scalanative_install_terminate_handler();
    throw scalanative::ExceptionWrapper(obj);
}
size_t scalanative_Throwable_sizeOfExceptionWrapper() { return 0; }
void scalanative_Exception_onCatch(Exception self) {
    if (self) {
        OnCatchHandler handler = scalanative_Throwable_onCatchHandler(self);
        if (handler)
            handler(self);
    }
}
}
#endif
