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

// set the allocation function for Continuations and stack fragments.
// without calling this, malloc is the default allocation function.
void cont_set_alloc(void *(*alloc_f)(unsigned long));

// cont_boundary : ContFn -> any -> Result
// void cont_boundary(ContFn *, void *, ContResult *);

void *cont_boundary(ContFn *, void *);

// cont_suspend[T, R] : BoundaryLabel[T] -> T -> R
// void *cont_suspend(ContBoundaryLabel b, void *in);

void *cont_suspend(ContBoundaryLabel b, SuspendFn *f, void *arg);

// resume[T, R] : Continuation[T, R] -> R -> Result
// void cont_resume(Continuation *cont, void *out, ContResult *r);

void *cont_resume(Continuation *cont, void *out);

// free a continuation. Used only if malloc is used as the implementation of
// alloc function.
void cont_free(Continuation *cont);

#endif // delimcc_h_INCLUDED
