#ifndef delimcc_h_INCLUDED
#define delimcc_h_INCLUDED

typedef unsigned long ContLabel;

typedef struct ContBoundaryLabel {
    ContLabel id;
} ContBoundaryLabel;

typedef struct Continuation Continuation;

typedef struct ContResult {
    void *in;
    Continuation *cont;
} ContResult;

// ContFn = ContBoundaryLabel -> any -> any
typedef void *ContFn(ContBoundaryLabel, void *);

typedef void *SuspendFn(Continuation *, void *);

// cont_boundary : ContFn -> any -> Result
// void cont_boundary(ContFn *, void *, ContResult *);

void *cont_boundary(ContFn *, void *);

// cont_suspend[T, R] : BoundaryLabel[T] -> T -> R
// void *cont_suspend(ContBoundaryLabel b, void *in);

void *cont_suspend(ContBoundaryLabel b, SuspendFn *f, void *arg);

// resume[T, R] : Continuation[T, R] -> R -> Result
// void cont_resume(Continuation *cont, void *out, ContResult *r);

void *cont_resume(Continuation *cont, void *out);

// free a continuation.
void cont_free(Continuation *cont);

#endif // delimcc_h_INCLUDED
