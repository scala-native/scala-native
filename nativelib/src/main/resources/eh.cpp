#ifndef _WIN32
    #include <exception>
#endif

// Scala Native compiles Scala's exception in C++-compatible
// manner under the hood. Every exception thrown on the Scala
// side is wrapped into ExceptionWrapper and only
// ExceptionWrapper-based exceptions can be caught by
// Scala code. We currently do not support catching arbitrary
// C++ exceptions.

namespace scalanative {
    class ExceptionWrapper
#ifndef _WIN32
    : public std::exception
#endif
    {
    public:
        ExceptionWrapper(void* _obj): obj(_obj) {}
        void* obj;
    };
}

extern "C" {
    void scalanative_throw(void* obj) {
#ifndef _WIN32
        throw scalanative::ExceptionWrapper(obj);
#endif
    }

    // need some c plus plus
    // snprintf is unresolved external on win
#ifdef _WIN32
    //#include <cstdarg>
    int snprintf( char * s, size_t n, const char * format, ... )
    {
        int ret = 0;

        // Declare a va_list type variable
        //std::va_list myargs;

        // Initialise the va_list variable with the ... after fmt
        //std::va_start(myargs, format);

        // Forward the '...'
        //ret = std::snprintf(s, n, format, myargs);

        // Clean up the va_list
        //std::va_end(myargs);

        return ret;
    }
#endif
}
