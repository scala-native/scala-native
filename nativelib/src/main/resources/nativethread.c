#include <sys/types.h>
#include <stdint.h>
#include <pthread.h>
#include <sched.h>
#include <signal.h>
#include <stdlib.h>
#include <stdio.h>

pthread_attr_t PTHREAD_DEFAULT_ATTR;
struct sched_param PTHREAD_DEFAULT_SCHED_PARAM;
int PTHREAD_DEFAULT_POLICY;
size_t PTHREAD_DEFAULT_STACK_SIZE;
int initialized = 0;

void init() {
	pthread_attr_init(&PTHREAD_DEFAULT_ATTR);
	pthread_attr_getschedparam(&PTHREAD_DEFAULT_ATTR, &PTHREAD_DEFAULT_SCHED_PARAM);
	pthread_attr_getschedpolicy(&PTHREAD_DEFAULT_ATTR, &PTHREAD_DEFAULT_POLICY);
	pthread_attr_getstacksize(&PTHREAD_DEFAULT_ATTR, &PTHREAD_DEFAULT_STACK_SIZE);

	initialized = 1;
}

int get_max_priority() {
	if(!initialized) init();
	return sched_get_priority_max(PTHREAD_DEFAULT_POLICY);
}

int get_min_priority() {
	if(!initialized) init();
	return sched_get_priority_min(PTHREAD_DEFAULT_POLICY);
}

int get_norm_priority() {
	if(!initialized) init();
	return PTHREAD_DEFAULT_SCHED_PARAM.sched_priority;
}

size_t get_stack_size() {
	if(!initialized) init();
	return PTHREAD_DEFAULT_STACK_SIZE;
}

void set_priority(pthread_t thread, int priority) {
	struct sched_param param;
	int policy;

	pthread_getschedparam(thread, &policy, &param);
	param.sched_priority = priority;

	pthread_setschedparam(thread, policy, &param);
}

/*
 * Thread suspend handling
*/

// ported from http://ptgmedia.pearsoncmg.com/images/0201633922/sourcecode/susp.c

pthread_mutex_t mut = PTHREAD_MUTEX_INITIALIZER;
volatile int sentinel = 0;
pthread_once_t once = PTHREAD_ONCE_INIT;
pthread_t *array = NULL, null_pthread = {0};
int bottom = 0;
int inited = 0;

/*
 * Handle SIGUSR1 in the target thread, to suspend it until
 * receiving SIGUSR2 (resume).
 */
void
suspend_signal_handler (int sig)
{
    sigset_t signal_set;

    /*
     * Block all signals except SIGUSR2 while suspended.
     */
    sigfillset (&signal_set);
    sigdelset (&signal_set, SIGUSR2);
    sentinel = 1;
    sigsuspend (&signal_set);

    return;
}

/*
 * Handle SIGUSR2 in the target thread.
 */
void
resume_signal_handler (int sig)
{
    return;
}

/*
 * Dynamically initialize the "suspend package" when first used
 * (called by pthread_once).
 */
void
suspend_init_routine (void)
{
    int status;
    struct sigaction sigusr1, sigusr2;

    /*
     * Allocate the suspended threads array. This array is used
     * to guarentee idempotency
     */
    bottom = 10;
    array = (pthread_t*) calloc (bottom, sizeof (pthread_t));

    /*
     * Install the signal handlers for suspend/resume.
     */
    sigusr1.sa_flags = 0;
    sigusr1.sa_handler = suspend_signal_handler;

    sigemptyset (&sigusr1.sa_mask);
    sigusr2.sa_flags = 0;
    sigusr2.sa_handler = resume_signal_handler;
    sigusr2.sa_mask = sigusr1.sa_mask;

    status = sigaction (SIGUSR1, &sigusr1, NULL);
    if (status == -1)
        perror("Installing suspend handler");

    status = sigaction (SIGUSR2, &sigusr2, NULL);
    if (status == -1)
        perror("Installing resume handler");

    inited = 1;
    return;
}

/*
 * Suspend a thread by sending it a signal (SIGUSR1), which will
 * block the thread until another signal (SIGUSR2) arrives.
 *
 * Multiple calls to thd_suspend for a single thread have no
 * additional effect on the thread -- a single thd_continue
 * call will cause it to resume execution.
 */
int
thd_suspend (pthread_t target_thread)
{
    int status;
    int i = 0;

    /*
     * The first call to thd_suspend will initialize the
     * package.
     */
    status = pthread_once (&once, suspend_init_routine);
    if (status != 0)
        return status;

    /*
     * Serialize access to suspend, makes life easier
     */
    status = pthread_mutex_lock (&mut);
    if (status != 0)
        return status;

    /*
     * Threads that are suspended are added to the target_array;
     * a request to suspend a thread already listed in the array
     * is ignored. Sending a second SIGUSR1 would cause the
     * thread to re-suspend itself as soon as it is resumed.
     */
    while (i < bottom)
        if (array[i++] == target_thread) {
            status = pthread_mutex_unlock (&mut);
            return status;
        }

    i = 0;
    while (array[i] != 0)
        i++;

    if (i == bottom) {
        array = (pthread_t*) realloc (
            array, (++bottom * sizeof (pthread_t)));
        if (array == NULL) {
            pthread_mutex_unlock (&mut);
            perror("Null pointer");
        }

        array[bottom] = null_pthread;   /* Clear new entry */
    }

    /*
     * Clear the sentinel and signal the thread to suspend.
     */
    sentinel = 0;
    status = pthread_kill (target_thread, SIGUSR1);
    if (status != 0) {
        pthread_mutex_unlock (&mut);
        return status;
    }

    /*
     * Wait for the sentinel to change.
     */
    while (sentinel == 0)
        sched_yield ();

    array[i] = target_thread;

    status = pthread_mutex_unlock (&mut);
    return status;
}

/*
 * Resume a suspended thread by sending it SIGUSR2 to break
 * it out of the sigsuspend() in which it's waiting. If the
 * target thread isn't suspended, return with success.
 */
int
thd_continue (pthread_t target_thread)
{
    int status;
    int i = 0;

    status = pthread_mutex_lock (&mut);
    if (status != 0)
        return status;

    if (!inited) {
        status = pthread_mutex_unlock (&mut);
        return status;
    }

    /*
     * Make sure the thread is in the suspend array. If not, it
     * hasn't been suspended (or it has already been resumed).
     */
    while (array[i] != target_thread && i < bottom)
        i++;

    if (i >= bottom) {
        pthread_mutex_unlock (&mut);
        return 0;
    }

    /*
     * Signal the thread to continue, and remove the thread from
     * the suspended array.
     */
    status = pthread_kill (target_thread, SIGUSR2);
    if (status != 0) {
        pthread_mutex_unlock (&mut);
        return status;
    }

    array[i] = 0;               /* Clear array element */
    status = pthread_mutex_unlock (&mut);
    return status;
}

/*
 * End of Thread suspend
 */
