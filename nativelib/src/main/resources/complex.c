#include <complex.h>

// native complex structs
typedef struct scalanative_complexf {
    float re;
    float im;
} scalanative_complexf;

typedef struct scalanative_complex {
    double re;
    double im;
} scalanative_complex;

typedef struct scalanative_complexd {
    long double re;
    long double im;
} scalanative_complexd;

// helpers
float complex toComplexF(scalanative_complexf *sncf) {
    return (float complex){sncf->re, sncf->im};
}

double complex toComplex(scalanative_complex *sncf) {
    return (double complex){sncf->re, sncf->im};
}

// modifies passed in struct
scalanative_complexf *toNativeComplexF(float complex c,
                                       scalanative_complexf *sncf) {
    sncf->re = crealf(c);
    sncf->im = cimagf(c);
    return sncf;
}

scalanative_complex *toNativeComplex(double complex c,
                                     scalanative_complex *snc) {
    snc->re = creal(c);
    snc->im = cimag(c);
    return snc;
}

// functions - modifies passed in struct
scalanative_complexf *scalanative_cacosf(scalanative_complexf *sncf) {
    return toNativeComplexF(cacosf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_cacos(scalanative_complex *snc) {
    return toNativeComplex(cacos(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_casinf(scalanative_complexf *sncf) {
    return toNativeComplexF(casinf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_casin(scalanative_complex *snc) {
    return toNativeComplex(casin(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_catanf(scalanative_complexf *sncf) {
    return toNativeComplexF(catanf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_catan(scalanative_complex *snc) {
    return toNativeComplex(catan(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_ccosf(scalanative_complexf *sncf) {
    return toNativeComplexF(ccosf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_ccos(scalanative_complex *snc) {
    return toNativeComplex(ccos(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_csinf(scalanative_complexf *sncf) {
    return toNativeComplexF(csinf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_csin(scalanative_complex *snc) {
    return toNativeComplex(csin(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_ctanf(scalanative_complexf *sncf) {
    return toNativeComplexF(ctanf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_ctan(scalanative_complex *snc) {
    return toNativeComplex(ctan(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_cacoshf(scalanative_complexf *sncf) {
    return toNativeComplexF(cacoshf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_cacosh(scalanative_complex *snc) {
    return toNativeComplex(cacosh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_casinhf(scalanative_complexf *sncf) {
    return toNativeComplexF(casinhf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_casinh(scalanative_complex *snc) {
    return toNativeComplex(casinh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_catanhf(scalanative_complexf *sncf) {
    return toNativeComplexF(catanhf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_catanh(scalanative_complex *snc) {
    return toNativeComplex(catanh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_ccoshf(scalanative_complexf *sncf) {
    return toNativeComplexF(ccoshf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_ccosh(scalanative_complex *snc) {
    return toNativeComplex(ccosh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_csinhf(scalanative_complexf *sncf) {
    return toNativeComplexF(csinhf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_csinh(scalanative_complex *snc) {
    return toNativeComplex(csinh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_ctanhf(scalanative_complexf *sncf) {
    return toNativeComplexF(ctanhf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_ctanh(scalanative_complex *snc) {
    return toNativeComplex(ctanh(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_cexpf(scalanative_complexf *sncf) {
    return toNativeComplexF(cexpf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_cexp(scalanative_complex *snc) {
    return toNativeComplex(cexp(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_clogf(scalanative_complexf *sncf) {
    return toNativeComplexF(clogf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_clog(scalanative_complex *snc) {
    return toNativeComplex(clog(toComplex(snc)), snc);
}

float scalanative_cabsf(scalanative_complexf *sncf) {
    return cabsf(toComplexF(sncf));
}

double scalanative_cabs(scalanative_complex *snc) {
    return cabs(toComplex(snc));
}

// first struct gets modified
scalanative_complexf *scalanative_cpowf(scalanative_complexf *sncf,
                                        scalanative_complexf *sncf2) {
    return toNativeComplexF(cpowf(toComplexF(sncf), toComplexF(sncf2)), sncf);
}

// first struct gets modified
scalanative_complex *scalanative_cpow(scalanative_complex *snc,
                                      scalanative_complex *snc2) {
    return toNativeComplex(cpow(toComplex(snc), toComplex(snc2)), snc);
}

scalanative_complexf *scalanative_csqrtf(scalanative_complexf *sncf) {
    return toNativeComplexF(csqrtf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_csqrt(scalanative_complex *snc) {
    return toNativeComplex(csqrt(toComplex(snc)), snc);
}

float scalanative_cargf(scalanative_complexf *sncf) {
    return cargf(toComplexF(sncf));
}

double scalanative_carg(scalanative_complex *snc) {
    return carg(toComplex(snc));
}

float scalanative_cimagf(scalanative_complexf *sncf) {
    return cimagf(toComplexF(sncf));
}

double scalanative_cimag(scalanative_complex *snc) {
    return cimag(toComplex(snc));
}

scalanative_complexf *scalanative_conjf(scalanative_complexf *sncf) {
    return toNativeComplexF(conjf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_conj(scalanative_complex *snc) {
    return toNativeComplex(conj(toComplex(snc)), snc);
}

scalanative_complexf *scalanative_cprojf(scalanative_complexf *sncf) {
    return toNativeComplexF(cprojf(toComplexF(sncf)), sncf);
}

scalanative_complex *scalanative_cproj(scalanative_complex *snc) {
    return toNativeComplex(cproj(toComplex(snc)), snc);
}

float scalanative_crealf(scalanative_complexf *sncf) {
    return crealf(toComplexF(sncf));
}

double scalanative_creal(scalanative_complex *snc) {
    return creal(toComplex(snc));
}
