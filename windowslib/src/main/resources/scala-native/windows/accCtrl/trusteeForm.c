#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <accctrl.h>

int scalanative_trustee_is_sid() { return TRUSTEE_IS_SID; }
int scalanative_trustee_is_name() { return TRUSTEE_IS_NAME; }
int scalanative_trustee_bad_form() { return TRUSTEE_BAD_FORM; }
int scalanative_trustee_is_objects_and_sid() {
    return TRUSTEE_IS_OBJECTS_AND_SID;
}
int scalanative_trustee_is_objects_and_name() {
    return TRUSTEE_IS_OBJECTS_AND_NAME;
}

#endif // defined(_WIN32)
