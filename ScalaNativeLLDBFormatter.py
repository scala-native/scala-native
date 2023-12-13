# Custom LLDB formatter dedicated for Scala types, allowing to present data requring more information then available by default in DWARF.
# Usage: from LLDB console run `command script import ScalaNativeLLDBFormatter.py`
 
# Based on CodeLLBD Rust formatters https://github.com/vadimcn/codelldb/blob/master/formatters/rust.py

from __future__ import print_function, division
import sys
import logging
import lldb
import weakref

if sys.version_info[0] == 2:
    # python2-based LLDB accepts utf8-encoded ascii strings only.
    def to_lldb_str(s): return s.encode('utf8', 'backslashreplace') if isinstance(s, unicode) else s
    range = xrange
else:
    to_lldb_str = str

log = logging.getLogger(__name__)

module = sys.modules[__name__]
scala_category = None

max_string_summary_langth = 1024

def initialize_category(debugger, internal_dict):
    global module, scala_category, max_string_summary_langth

    scala_category = debugger.CreateCategory('Scala')
    scala_category.SetEnabled(True)

    attach_synthetic_to_type(StringSynthProvider, 'java.lang.String', False)

    attach_synthetic_to_type(ArraySynthProvider, r'^scala.scalanative.runtime.(\w+Array)&', True)
    attach_synthetic_to_type(ArraySynthProvider, r'^(scala.Array)\[.*\]$', True)
  
def attach_synthetic_to_type(synth_class, type_name, is_regex=False):
    global module, scala_category
    log.debug('attaching synthetic %s to "%s", is_regex=%s', synth_class.__name__, type_name, is_regex)
    synth = lldb.SBTypeSynthetic.CreateWithClassName(__name__ + '.' + synth_class.__name__)
    synth.SetOptions(lldb.eTypeOptionCascade)
    scala_category.AddTypeSynthetic(lldb.SBTypeNameSpecifier(type_name, is_regex), synth)

    def summary_fn(valobj, dict): return get_synth_summary(synth_class, valobj, dict)
    # LLDB accesses summary fn's by name, so we need to create a unique one.
    summary_fn.__name__ = '_get_synth_summary_' + synth_class.__name__
    setattr(module, summary_fn.__name__, summary_fn)
    attach_summary_to_type(summary_fn, type_name, is_regex)


def attach_summary_to_type(summary_fn, type_name, is_regex=False):
    global module, scala_category
    # log.debug('attaching summary %s to "%s", is_regex=%s', summary_fn.__name__, type_name, is_regex)
    summary = lldb.SBTypeSummary.CreateWithFunctionName(__name__ + '.' + summary_fn.__name__)
    summary.SetOptions(lldb.eTypeOptionCascade)
    scala_category.AddTypeSummary(lldb.SBTypeNameSpecifier(type_name, is_regex), summary)


# 'get_summary' is annoyingly not a part of the standard LLDB synth provider API.
# This trick allows us to share data extraction logic between synth providers and their sibling summary providers.
def get_synth_summary(synth_class, valobj, dict):
    try:
        obj_id = valobj.GetIndexOfChildWithName('$$object-id$$')
        summary = ScalaSynthProvider.synth_by_id[obj_id].get_summary()
        return to_lldb_str(summary)
    except Exception as e:
        log.exception('%s', e)
        raise


# Chained GetChildMemberWithName lookups
def gcm(valobj, *chain):
    for name in chain:
        idx = valobj.GetIndexOfChildWithName(name)
        valobj = valobj.GetChildAtIndex(idx)
    return valobj


# Get a pointer out of core::ptr::Unique<T>
def array_as_pointer(arrayObj):
    element_type = arrayObj.GetType().GetArrayElementType()
    asPointer = arrayObj.address_of.Cast(element_type.GetPointerType())
    return asPointer

def string_from_ptr(pointer, length,encoding = 'utf-16', bytesPerChar = 2):
    if length <= 0:
        return u''
    error = lldb.SBError()
    process = pointer.GetProcess()
    data = process.ReadMemory(pointer.GetValueAsUnsigned(), length * bytesPerChar, error)
    if error.Success():
        return data.decode(encoding, 'replace')
    else:
        raise Exception('ReadMemory error: %s', error.GetCString())


def get_template_params(type_name):
    params = []
    level = 0
    start = 0
    for i, c in enumerate(type_name):
        if c == '<':
            level += 1
            if level == 1:
                start = i + 1
        elif c == '>':
            level -= 1
            if level == 0:
                params.append(type_name[start:i].strip())
        elif c == ',' and level == 1:
            params.append(type_name[start:i].strip())
            start = i + 1
    return params


def obj_summary(valobj, unavailable='{...}'):
    summary = valobj.GetSummary()
    if summary is not None:
        return summary
    summary = valobj.GetValue()
    if summary is not None:
        return summary
    return unavailable


def sequence_summary(childern, maxsize=32):
    s = ''
    for child in childern:
        if len(s) > 0:
            s += ', '
        s += obj_summary(child)
        if len(s) > maxsize:
            s += ', ...'
            break
    return s


def tuple_summary(obj, skip_first=0):
    fields = [obj_summary(obj.GetChildAtIndex(i)) for i in range(skip_first, obj.GetNumChildren())]
    return '(%s)' % ', '.join(fields)


# ----- Summaries -----

def tuple_summary_provider(valobj, dict={}):
    return tuple_summary(valobj)


# ----- Synth providers ------


class ScalaSynthProvider(object):
    synth_by_id = weakref.WeakValueDictionary()
    next_id = 0

    def __init__(self, valobj, dict={}):
        self.valobj = valobj
        self.obj_id = ScalaSynthProvider.next_id
        ScalaSynthProvider.synth_by_id[self.obj_id] = self
        ScalaSynthProvider.next_id += 1

    def update(self):
        return True

    def has_children(self):
        return False

    def num_children(self):
        return 0

    def get_child_at_index(self, index):
        return None

    def get_child_index(self, name):
        if name == '$$object-id$$':
            return self.obj_id

        try:
            return self.get_index_of_child(name)
        except Exception as e:
            log.exception('%s', e)
            raise

    def get_summary(self):
        return None


class ArrayLikeSynthProvider(ScalaSynthProvider):
    '''Base class for providers that represent array-like objects'''

    def update(self):
        self.ptr, self.len = self.ptr_and_len(self.valobj)  # type: ignore
        self.item_type = self.ptr.GetType().GetPointeeType()
        self.item_size = self.item_type.GetByteSize()

    def ptr_and_len(self, obj):
        pass  # abstract

    def num_children(self):
        return self.len

    def has_children(self):
        return True

    def get_child_at_index(self, index):
        try:
            if not 0 <= index < self.len:
                return None
            offset = index * self.item_size
            return self.ptr.CreateChildAtOffset('[%s]' % index, offset, self.item_type)
        except Exception as e:
            log.exception('%s', e)
            raise

    def get_index_of_child(self, name):
        return int(name.lstrip('[').rstrip(']'))

    def get_summary(self):
        return '(%d)' % (self.len,)


class ArraySynthProvider(ArrayLikeSynthProvider):
    def ptr_and_len(self, arr):
        return (
          array_as_pointer(gcm(arr, 'values')),
          gcm(arr, 'scala.scalanative.runtime.ArrayHeader', 'length').GetValueAsUnsigned()
        )

    def get_summary(self):
        return 'Array[%d](%s)' % (self.len, sequence_summary((self.get_child_at_index(i) for i in range(self.len))))


# Base class for *String providers
class StringLikeSynthProvider(ArrayLikeSynthProvider):
    def get_child_at_index(self, index):
        ch = ArrayLikeSynthProvider.get_child_at_index(self, index)
        ch.SetFormat(lldb.eFormatChar)
        return ch

    def get_summary(self):
        strval = string_from_ptr(self.ptr, min(self.len, max_string_summary_langth))
        if self.len > max_string_summary_langth:
            strval += u'...'
        return u'"%s"' % strval

class StringSynthProvider(StringLikeSynthProvider):
    def ptr_and_len(self, valobj):
        offset = gcm(valobj, "offset").GetValueAsUnsigned()
        count = gcm(valobj, "count").GetValueAsUnsigned()
        arrayUnsized = gcm(valobj, "value", "values")
        # Quick, hot path
        if(offset == 0):
          return (array_as_pointer(arrayUnsized),count)
        elementType = arrayUnsized.GetType().GetArrayElementType()
        arrayAddr = arrayUnsized.GetLoadAddress()
        offsetAddr = arrayAddr + offset * elementType.GetByteSize()
        pointerToOffset = arrayUnsized.CreateValueFromAddress("data", offsetAddr, elementType.GetPointerType())
        return (pointerToOffset, count)
  

def __lldb_init_module(debugger_obj, internal_dict): # pyright: ignore
    log.info('Initializing')
    initialize_category(debugger_obj, internal_dict)