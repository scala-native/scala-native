#include <thread>
#include <stdexcept>
#include <exception>
#include "host.h"
#include "scenarios.h"
int main(int argc, char **argv) {
    return run_host(argc, argv, scenarios,
                    sizeof(scenarios) / sizeof(scenarios[0]));
}
