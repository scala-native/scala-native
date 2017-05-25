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
float complex toComplexF(float sncf[2]) { return *(float complex *)sncf; }

double complex toComplex(double snc[2]) { return *(double complex *)snc; }

float *toNativeComplexF(float complex fc, float sncf[2]) {
    float *fa = (float *)&fc;
    sncf[0] = *fa;
    sncf[1] = *(fa + 1);
    return sncf;
}

double *toNativeComplex(double complex c, double snc[2]) {
    double *da = (double *)&c;
    snc[0] = *da;
    snc[1] = *(da + 1);
    return snc;
}

// functions - modifies passed in array
float *scalanative_cacosf(float sncf[2]) {
    return toNativeComplexF(cacosf(toComplexF(sncf)), sncf);
}

double *scalanative_cacos(double snc[2]) {
    return toNativeComplex(cacos(toComplex(snc)), snc);
}

float *scalanative_casinf(float sncf[2]) {
    return toNativeComplexF(casinf(toComplexF(sncf)), sncf);
}

double *scalanative_casin(double snc[2]) {
    return toNativeComplex(casin(toComplex(snc)), snc);
}

float *scalanative_catanf(float sncf[2]) {
    return toNativeComplexF(catanf(toComplexF(sncf)), sncf);
}

double *scalanative_catan(double snc[2]) {
    return toNativeComplex(catan(toComplex(snc)), snc);
}

float *scalanative_ccosf(float sncf[2]) {
    return toNativeComplexF(ccosf(toComplexF(sncf)), sncf);
}

double *scalanative_ccos(double snc[2]) {
    return toNativeComplex(ccos(toComplex(snc)), snc);
}

float *scalanative_csinf(float sncf[2]) {
    return toNativeComplexF(csinf(toComplexF(sncf)), sncf);
}

double *scalanative_csin(double snc[2]) {
    return toNativeComplex(csin(toComplex(snc)), snc);
}

float *scalanative_ctanf(float sncf[2]) {
    return toNativeComplexF(ctanf(toComplexF(sncf)), sncf);
}

double *scalanative_ctan(double snc[2]) {
    return toNativeComplex(ctan(toComplex(snc)), snc);
}

float *scalanative_cacoshf(float sncf[2]) {
    return toNativeComplexF(cacoshf(toComplexF(sncf)), sncf);
}

double *scalanative_cacosh(double snc[2]) {
    return toNativeComplex(cacosh(toComplex(snc)), snc);
}

float *scalanative_casinhf(float sncf[2]) {
    return toNativeComplexF(casinhf(toComplexF(sncf)), sncf);
}

double *scalanative_casinh(double snc[2]) {
    return toNativeComplex(casinh(toComplex(snc)), snc);
}

float *scalanative_catanhf(float sncf[2]) {
    return toNativeComplexF(catanhf(toComplexF(sncf)), sncf);
}

double *scalanative_catanh(double snc[2]) {
    return toNativeComplex(catanh(toComplex(snc)), snc);
}

float *scalanative_ccoshf(float sncf[2]) {
    return toNativeComplexF(ccoshf(toComplexF(sncf)), sncf);
}

double *scalanative_ccosh(double snc[2]) {
    return toNativeComplex(ccosh(toComplex(snc)), snc);
}

float *scalanative_csinhf(float sncf[2]) {
    return toNativeComplexF(csinhf(toComplexF(sncf)), sncf);
}

double *scalanative_csinh(double snc[2]) {
    return toNativeComplex(csinh(toComplex(snc)), snc);
}

float *scalanative_ctanhf(float sncf[2]) {
    return toNativeComplexF(ctanhf(toComplexF(sncf)), sncf);
}

double *scalanative_ctanh(double snc[2]) {
    return toNativeComplex(ctanh(toComplex(snc)), snc);
}

float *scalanative_cexpf(float sncf[2]) {
    return toNativeComplexF(cexpf(toComplexF(sncf)), sncf);
}

double *scalanative_cexp(double snc[2]) {
    return toNativeComplex(cexp(toComplex(snc)), snc);
}

float *scalanative_clogf(float sncf[2]) {
    return toNativeComplexF(clogf(toComplexF(sncf)), sncf);
}

double *scalanative_clog(double snc[2]) {
    return toNativeComplex(clog(toComplex(snc)), snc);
}

float scalanative_cabsf(float sncf[2]) { return cabsf(toComplexF(sncf)); }

double scalanative_cabs(double snc[2]) { return cabs(toComplex(snc)); }

// first array gets modified for cpow(f) functions
float *scalanative_cpowf(float sncf[2], float sncf2[2]) {
    return toNativeComplexF(cpowf(toComplexF(sncf), toComplexF(sncf2)), sncf);
}

double *scalanative_cpow(double snc[2], double snc2[2]) {
    return toNativeComplex(cpow(toComplex(snc), toComplex(snc2)), snc);
}

float *scalanative_csqrtf(float sncf[2]) {
    return toNativeComplexF(csqrtf(toComplexF(sncf)), sncf);
}

double *scalanative_csqrt(double snc[2]) {
    return toNativeComplex(csqrt(toComplex(snc)), snc);
}

float scalanative_cargf(float sncf[2]) { return cargf(toComplexF(sncf)); }

double scalanative_carg(double snc[2]) { return carg(toComplex(snc)); }

float scalanative_cimagf(float sncf[2]) { return cimagf(toComplexF(sncf)); }

double scalanative_cimag(double snc[2]) { return cimag(toComplex(snc)); }

float *scalanative_conjf(float sncf[2]) {
    return toNativeComplexF(conjf(toComplexF(sncf)), sncf);
}

double *scalanative_conj(double snc[2]) {
    return toNativeComplex(conj(toComplex(snc)), snc);
}

float *scalanative_cprojf(float sncf[2]) {
    return toNativeComplexF(cprojf(toComplexF(sncf)), sncf);
}

double *scalanative_cproj(double snc[2]) {
    return toNativeComplex(cproj(toComplex(snc)), snc);
}

float scalanative_crealf(float sncf[2]) { return crealf(toComplexF(sncf)); }

double scalanative_creal(double snc[2]) { return creal(toComplex(snc)); }
