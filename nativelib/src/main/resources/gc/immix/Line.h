#ifndef IMMIX_LINE_H
#define IMMIX_LINE_H


#include "headers/ObjectHeader.h"
#include "headers/LineHeader.h"
#include "headers/BlockHeader.h"

ObjectHeader* line_header_getFirstObject(LineHeader*);
void line_header_update(BlockHeader* blockHeader, word_t* objectStart);

#endif //IMMIX_LINE_H
