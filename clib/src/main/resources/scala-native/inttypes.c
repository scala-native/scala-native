#include <inttypes.h>

void scalanative_inttypes_imaxdiv(intmax_t q, intmax_t d, imaxdiv_t *result) {
    *result = imaxdiv(q, d);
}
