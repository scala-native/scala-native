#include <complex.h>

/* C11 specification - http://www.iso-9899.info/n1570.html
 *
 * 6.2.5/13 Each complex type has the same representation and alignment
 * requirements as an array type containing exactly two elements of the
 * corresponding real type; the first element is equal to the real part, and the
 * second element to the imaginary part, of the complex number.
 *
 * Helper functions follow.
 */
float complex toFloatComplex(float snfc[2]) { return *(float complex *)snfc; }

double complex toDoubleComplex(double sndc[2]) {
    return *(double complex *)sndc;
}

float *toNativeFloatComplex(float complex fc, float res[2]) {
    float *fa = (float *)&fc;
    res[0] = *fa;
    res[1] = *(fa + 1);
    return res;
}

double *toNativeDoubleComplex(double complex dc, double res[2]) {
    double *da = (double *)&dc;
    res[0] = *da;
    res[1] = *(da + 1);
    return res;
}

// functions - modifies and returns passed in result array
float *scalanative_cacosf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(cacosf(toFloatComplex(snfc)), res);
}

double *scalanative_cacos(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(cacos(toDoubleComplex(sndc)), res);
}

float *scalanative_casinf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(casinf(toFloatComplex(snfc)), res);
}

double *scalanative_casin(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(casin(toDoubleComplex(sndc)), res);
}

float *scalanative_catanf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(catanf(toFloatComplex(snfc)), res);
}

double *scalanative_catan(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(catan(toDoubleComplex(sndc)), res);
}

float *scalanative_ccosf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(ccosf(toFloatComplex(snfc)), res);
}

double *scalanative_ccos(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(ccos(toDoubleComplex(sndc)), res);
}

float *scalanative_csinf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(csinf(toFloatComplex(snfc)), res);
}

double *scalanative_csin(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(csin(toDoubleComplex(sndc)), res);
}

float *scalanative_ctanf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(ctanf(toFloatComplex(snfc)), res);
}

double *scalanative_ctan(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(ctan(toDoubleComplex(sndc)), res);
}

float *scalanative_cacoshf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(cacoshf(toFloatComplex(snfc)), res);
}

double *scalanative_cacosh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(cacosh(toDoubleComplex(sndc)), res);
}

float *scalanative_casinhf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(casinhf(toFloatComplex(snfc)), res);
}

double *scalanative_casinh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(casinh(toDoubleComplex(sndc)), res);
}

float *scalanative_catanhf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(catanhf(toFloatComplex(snfc)), res);
}

double *scalanative_catanh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(catanh(toDoubleComplex(sndc)), res);
}

float *scalanative_ccoshf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(ccoshf(toFloatComplex(snfc)), res);
}

double *scalanative_ccosh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(ccosh(toDoubleComplex(sndc)), res);
}

float *scalanative_csinhf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(csinhf(toFloatComplex(snfc)), res);
}

double *scalanative_csinh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(csinh(toDoubleComplex(sndc)), res);
}

float *scalanative_ctanhf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(ctanhf(toFloatComplex(snfc)), res);
}

double *scalanative_ctanh(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(ctanh(toDoubleComplex(sndc)), res);
}

float *scalanative_cexpf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(cexpf(toFloatComplex(snfc)), res);
}

double *scalanative_cexp(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(cexp(toDoubleComplex(sndc)), res);
}

float *scalanative_clogf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(clogf(toFloatComplex(snfc)), res);
}

double *scalanative_clog(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(clog(toDoubleComplex(sndc)), res);
}

float scalanative_cabsf(float snfc[2]) { return cabsf(toFloatComplex(snfc)); }

double scalanative_cabs(double sndc[2]) { return cabs(toDoubleComplex(sndc)); }

float *scalanative_cpowf(float x[2], float y[2], float res[2]) {
    return toNativeFloatComplex(cpowf(toFloatComplex(x), toFloatComplex(y)),
                                res);
}

double *scalanative_cpow(double x[2], double y[2], double res[2]) {
    return toNativeDoubleComplex(cpow(toDoubleComplex(x), toDoubleComplex(y)),
                                 res);
}

float *scalanative_csqrtf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(csqrtf(toFloatComplex(snfc)), res);
}

double *scalanative_csqrt(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(csqrt(toDoubleComplex(sndc)), res);
}

float scalanative_cargf(float snfc[2]) { return cargf(toFloatComplex(snfc)); }

double scalanative_carg(double sndc[2]) { return carg(toDoubleComplex(sndc)); }

float scalanative_cimagf(float snfc[2]) { return cimagf(toFloatComplex(snfc)); }

double scalanative_cimag(double sndc[2]) {
    return cimag(toDoubleComplex(sndc));
}

float *scalanative_conjf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(conjf(toFloatComplex(snfc)), res);
}

double *scalanative_conj(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(conj(toDoubleComplex(sndc)), res);
}

float *scalanative_cprojf(float snfc[2], float res[2]) {
    return toNativeFloatComplex(cprojf(toFloatComplex(snfc)), res);
}

double *scalanative_cproj(double sndc[2], double res[2]) {
    return toNativeDoubleComplex(cproj(toDoubleComplex(sndc)), res);
}

float scalanative_crealf(float snfc[2]) { return crealf(toFloatComplex(snfc)); }

double scalanative_creal(double sndc[2]) {
    return creal(toDoubleComplex(sndc));
}
