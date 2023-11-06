#include <exception>

namespace scalanative {
class ExceptionWrapper : public std::exception {
  public:
    ExceptionWrapper(void *_obj) : obj(_obj) {}
    void *obj;
};
}