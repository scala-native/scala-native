#include <time.h>
#include <stdio.h>

int main() {
    tzset();
    time_t rawtime;
    struct tm timeinfo;
    
    time(&rawtime);
    localtime_r(&rawtime, &timeinfo);
    printf("is_dst has value %i \n", timeinfo.tm_isdst);
    printf("timezone has value %li \n", timezone);
    printf("%s \n", asctime(&timeinfo));
}
