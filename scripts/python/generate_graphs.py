#!/usr/bin/env python
import argparse
import json
import logging
import os
import random
import re
import sys
import time

# set relevent path and file variables
file_name = os.path.basename(__file__).replace('.py', '')
project_dir = '{0}/../..'.format(os.path.dirname(__file__))
project_dir = os.path.normpath(project_dir)

# Configure Logging
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.ERROR)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s]: %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)

# Constants
predicates = [
    'contains!!Ljava/lang/CharSequence;',
    'contentEquals!!Ljava/lang/CharSequence;',
    'contentEquals!!Ljava/lang/StringBuffer;',
    'endsWith!!Ljava/lang/String;',
    'equals!!Ljava/lang/Object;',
    'equalsIgnoreCase!!Ljava/lang/String;',
    'isEmpty!!',
    'matches!!Ljava/lang/String;',
    'regionMatches!!ILjava/lang/String;II',
    'startsWith!!Ljava/lang/String;',
    'startsWith!!Ljava/lang/String;I'
]

# Global values
gen_globals = dict()


def reset_globals():
    gen_globals['boolean_constraints'] = None
    gen_globals['has_lower_case_op'] = False
    gen_globals['has_upper_case_op'] = False
    gen_globals['operations'] = None
    gen_globals['rand_seed'] = 1
    gen_globals['settings'] = None
    gen_globals['value_id_map'] = dict()
    gen_globals['vertices'] = list()


# Data Classes
# root node
class RootValue:
    def __init__(self, has_string, string=None, method=''):
        self.has_string = has_string
        self.string = string if string is not None else random_string(
            gen_globals['settings'].max_initial_length)
        self.method = method

    def get_value(self):
        if self.has_string:
            return '\"{0.string}\"!:!<{0.method}>'.format(self)

        return 'r1!:!{0.method}'.format(self)


# operation node
class OperationValue:
    def __init__(self, op, op_args=None, num=0):
        self.op = op
        self.num = num

        self.op_args = list()
        if op_args is not None:
            self.op_args.extend(op_args)

        self.args_known = dict()
        for x in self.op_args:
            self.args_known[x] = x != chr(0)

    def get_value(self):
        return '{0.op}!:!{0.num}'.format(self)


# boolean constraint node
class PredicateValue:
    def __init__(self, op, op_args=None, num=0, result=False, non_uniform=False):
        self.op = op
        self.op_args = list() if op_args is None else op_args
        self.num = num
        self.result = result
        self.non_uniform = non_uniform

        self.args_known = dict()
        for x in op_args:
            self.args_known[x] = x != chr(0)

    def get_value(self):
        return '{0.op}!:!{0.num}'.format(self)


# edge
class Edge:
    def __init__(self, source_id, target_id, edge_type):
        self.source_id = source_id
        self.target_id = target_id
        self.edge_type = edge_type


# vertex
class Vertex:
    def __init__(self, value, actual_value, node_id=0):
        self.value = value
        self.actual_value = actual_value
        self.node_id = node_id
        self.incoming_edges = list()

    def clone(self):
        # create new vertex from old
        new_vertex = Vertex(self.value,
                            self.actual_value,
                            generate_id(self.value, force=True))

        # copy incoming edge values
        new_vertex.incoming_edges.extend(self.incoming_edges)

        # return new vertex
        return new_vertex


# settings
class Settings:
    def __init__(self, options):
        self.allow_duplicates = False if options.no_duplicates else True
        self.alphabet = parse_alphabet_declaration(options.alphabet)
        self.depth = int(options.ops_depth)
        self.graph_name = options.graph_file
        self.id_counter = 2
        self.max_initial_length = int(options.length)
        self.op_counter = 0
        self.op_total = 1
        self.operations = options.operations

        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)

        # initialize inputs
        self.inputs = list()
        for in_str in options.inputs:
            if len(in_str) > self.max_initial_length:
                in_str = in_str[:self.max_initial_length]
            self.inputs.append(in_str)
        if options.empty_string:
            self.inputs.append('')
        self.non_uniform = False
        if options.non_uniform:
            self.non_uniform = True
            self.inputs.append(chr(0))
        # use unknown string if no other inputs specified
        if len(self.inputs) == 0 or options.unknown_string:
            self.inputs.append(chr(0))

        # set single graph option
        self.single_graph = False
        if options.single_graph:
            self.single_graph = True

    def get_ops_total(self):
        for i in range(0, self.depth + 2):
            self.op_total += (len(get_operations()) ** i)
        self.op_total *= len(self.inputs)

        return self.op_total


# operations array
def add_append_substring_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)

    # sb.append(string , offset , length )
    # sb.append(string , start , end )
    for i in range(0, len(symbol_string) - 2):
        # add append substring for char array
        ops.append(OperationValue('append!![CII',
                                  [symbol_string, str(i), '1']))

        # add append substring for char sequence
        ops.append(OperationValue('append!!Ljava/lang/CharSequence;II',
                                  [symbol_string, str(i), str(i + 1)]))


def add_append_operations(ops):
    # sb.append(string)
    # args = my_globals['settings'].alphabet
    args = sorted(gen_globals['settings'].alphabet)[:2]
    for c in args:
        ops.append(OperationValue('append!!Ljava/lang/String;', [c]))

    # add unknown string concatenation for non-uniform inputs
    if gen_globals['settings'].non_uniform:
        ops.append(OperationValue('concat!!Ljava/lang/String;', [chr(0)]))


def add_concat_operations(ops):
    # sb.concat(string)
    # args = my_globals['settings'].alphabet
    args = sorted(gen_globals['settings'].alphabet)[:2]
    for c in args:
        ops.append(OperationValue('concat!!Ljava/lang/String;', [c]))

    # add unknown string concatenation for non-uniform inputs
    if gen_globals['settings'].non_uniform:
        ops.append(OperationValue('concat!!Ljava/lang/String;', [chr(0)]))


def add_delete_char_at_operations(ops):
    # sb.deleteCharAt(index)
    for i in range(0, gen_globals['settings'].max_initial_length):
        ops.append(OperationValue('deleteCharAt!!I', [str(i)]))


def add_delete_operations(ops):
    # sb.delete(start, end)
    for i in range(0, gen_globals['settings'].max_initial_length + 1):
        for j in range(i, gen_globals['settings'].max_initial_length + 1):
            ops.append(OperationValue('delete!!II', [str(i), str(j)]))


def add_insert_char_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)

    # sb.insert(index, string)
    for i in range(0, len(symbol_string) - 1):
        for c in gen_globals['settings'].alphabet:
            ops.append(OperationValue('insert!!IC', [str(i), c]))


def add_insert_string_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)

    # sb.insert(index, string)
    for i in range(0, len(symbol_string) - 1):
        for c in gen_globals['settings'].alphabet:
            # add insert for char array
            ops.append(OperationValue('insert!!I[C', [str(i), c]))

            # add insert for char sequence
            ops.append(OperationValue('insert!!ILjava/lang/CharSequence;',
                                      [str(i), c]))


def add_insert_substring_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)

    # sb.insert(index, string, offset, length)
    # sb.insert(index, string, start, end)
    for i in range(0, gen_globals['settings'].max_initial_length - 1):
        for j in range(0, len(symbol_string) - 1):
            # add insert substring for char array
            ops.append(OperationValue('insert!!I[CII',
                                      [str(i), symbol_string, str(j), '1']))

            # add insert substring for char sequence
            ops.append(OperationValue('insert!!ILjava/lang/CharSequence;II',
                                      [str(i), symbol_string, str(j), str(j + 1)]))


def add_replace_char_operations(ops):
    # s.replace(old, new)
    # args = my_globals['settings'].alphabet
    args = sorted(gen_globals['settings'].alphabet)[:2]
    for c1 in args:
        for c2 in args:
            if c1 != c2:
                ops.append(OperationValue('replace!!CC', [c1, c2]))

    ops.append(OperationValue('replace!!CC', [args[0], args[0]]))


def add_replace_string_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)
    # get target from first 2 symbols
    target = symbol_string[0:2]
    # get replacement from last 2 symbols
    replacement = symbol_string[len(symbol_string) - 3:len(symbol_string) - 1]

    # s.replace(target, replacement)
    ops.append(OperationValue(
        'replace!!Ljava/lang/CharSequence;Ljava/lang/CharSequence;',
        [target, replacement]))


def add_replace_regex_string_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)
    # get regex from first symbol
    regex = symbol_string[0:1] + '+'
    # get replacement from last 2 symbols
    replacement = symbol_string[len(symbol_string) - 3:len(symbol_string) - 1]

    # s.replaceFirst(regex, replacement)
    # s.replaceAll(regex, replacement)
    ops.append(
        OperationValue('replaceFirst!!Ljava/lang/String;Ljava/lang/String;',
                       [regex, replacement]))
    ops.append(
        OperationValue('replaceAll!!Ljava/lang/String;Ljava/lang/String;',
                       [regex, replacement]))


def add_replace_substring_operations(ops):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)
    # get string from first 2 symbols
    string = symbol_string[0:2]

    # replace(start, end, string)
    for i in range(0, gen_globals['settings'].max_initial_length - 1):
        for j in range(i, gen_globals['settings'].max_initial_length):
            ops.append(OperationValue('replace!!IILjava/lang/String;',
                                      [str(i), str(j), string]))


def add_reverse_operations(ops):
    # s.reverse()
    ops.append(OperationValue('reverse!!'))


def add_substring_operations(ops):
    # s.substring(start)
    for i in range(0, gen_globals['settings'].max_initial_length - 1):
        ops.append(OperationValue('substring!!I', [str(i)]))

    # s.substring(start, end)
    for i in range(0, gen_globals['settings'].max_initial_length - 1):
        for j in range(i, gen_globals['settings'].max_initial_length):
            ops.append(OperationValue('substring!!II', [str(i), str(j)]))


def add_to_lower_case_operations(ops):
    # s.toLowerCase()
    ops.append(OperationValue('toLowerCase!!'))


def add_to_string_operations(ops):
    # s.toString()
    ops.append(OperationValue('toString!!'))


def add_to_upper_case_operations(ops):
    # s.toUpperCase()
    ops.append(OperationValue('toUpperCase!!'))


def add_trim_operations(ops):
    # s.trim()
    ops.append(OperationValue('trim!!'))


# boolean constraints array
def add_contains_predicates(constraints, length=None):
    # s.contains(substr)
    # concrete args
    for c in gen_globals['settings'].alphabet:
        constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [c], result=True))
        constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [c], result=False))

    # uniform args
    constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [chr(0)], result=True))
    constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [chr(0)], result=False))

    # non uniform args
    constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [chr(0)], result=True, non_uniform=True))
    constraints.append(PredicateValue('contains!!Ljava/lang/CharSequence;', [chr(0)], result=False, non_uniform=True))


def add_ends_with_predicates(constraints):
    # s.endsWith(suffix)
    for c in gen_globals['settings'].alphabet:
        constraints.append(
            PredicateValue('endsWith!!Ljava/lang/String;', [c]))


def add_equals_predicates(constraints):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)
    # get string from first 2 symbols
    string = symbol_string[0:2]

    # s.contentEquals(str)
    # constraints.append(BooleanConstraintValue('contentEquals!!Ljava/lang/CharSequence;', [string]))
    # constraints.append(BooleanConstraintValue('contentEquals!!Ljava/lang/StringBuffer;', [string]))

    # s.equals(str)
    # concrete args
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [string], result=True))
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [string], result=False))

    # uniform args
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [chr(0)], result=True))
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [chr(0)], result=False))

    # non uniform args
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [chr(0)], result=True, non_uniform=True))
    constraints.append(PredicateValue('equals!!Ljava/lang/Object;', [chr(0)], result=False, non_uniform=True))


def add_equals_ignore_case_predicates(constraints):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)
    # get string from first 2 symbols
    string = symbol_string[0:2]

    # s.equalsIgnoreCase(str)
    constraints.append(PredicateValue('equalsIgnoreCase!!Ljava/lang/String;', [string], result=True))
    constraints.append(PredicateValue('equalsIgnoreCase!!Ljava/lang/String;', [string], result=False))


def add_is_empty_predicates(constraints):
    # s.isEmpty()
    constraints.append(PredicateValue('isEmpty!!'))


def add_matches_predicates(constraints):
    # s.matches(regex known)
    for c in gen_globals['settings'].alphabet:
        constraints.append(PredicateValue('matches!!Ljava/lang/String;'), [c])


def add_region_matches_predicates(constraints):
    # join all symbols from alphabet
    symbol_string = ''.join(gen_globals['settings'].alphabet)

    # s.regionMatches(toffset, other, ooffset, length)
    for i in range(0, gen_globals['settings'].max_initial_length):
        for j in range(0, len(symbol_string)):
            for k in range(1, min(gen_globals['settings'].max_initial_length - i, len(symbol_string) - j)):
                constraints.append(PredicateValue('regionMatches!!ILjava/lang/String;II'), [str(i), symbol_string, str(j), str(k)])


def add_starts_with_predicates(constraints):
    # s.startsWith(prefix)
    for c in gen_globals['settings'].alphabet:
        constraints.append(
            PredicateValue('startsWith!!Ljava/lang/String;', [c]))


def add_starts_with_offset_predicates(constraints):
    # s.startsWith(prefix, offset)
    for c in gen_globals['settings'].alphabet:
        for i in range(0, gen_globals['settings'].max_initial_length):
            constraints.append(
                PredicateValue('startsWith!!Ljava/lang/String;I',
                               [c, str(i)]))


def get_operations():
    # check for existing operations value
    operations = gen_globals['operations']
    if operations is not None:
        return operations

    # initialize operations list
    ops_list = list()

    # === Operations ===
    # appendSubstring
    if 'append-substring' in gen_globals['settings'].operations:
        add_append_substring_operations(ops_list)
    if 'append' in gen_globals['settings'].operations:
        add_append_operations(ops_list)
    if 'concat' in gen_globals['settings'].operations:
        add_concat_operations(ops_list)
    if 'delete-char-at' in gen_globals['settings'].operations:
        add_delete_char_at_operations(ops_list)
    if 'delete' in gen_globals['settings'].operations:
        add_delete_operations(ops_list)
    if 'insert-char' in gen_globals['settings'].operations:
        add_insert_char_operations(ops_list)
    if 'insert-string' in gen_globals['settings'].operations:
        add_insert_string_operations(ops_list)
    if 'insert-substring' in gen_globals['settings'].operations:
        add_insert_substring_operations(ops_list)
    if 'replace-char' in gen_globals['settings'].operations:
        add_replace_char_operations(ops_list)
    if 'replace-string' in gen_globals['settings'].operations:
        add_replace_string_operations(ops_list)
    if 'replace-regex-string' in gen_globals['settings'].operations:
        add_replace_regex_string_operations(ops_list)
    if 'replace-substring' in gen_globals['settings'].operations:
        add_replace_substring_operations(ops_list)
    if 'reverse' in gen_globals['settings'].operations:
        add_reverse_operations(ops_list)
    if 'substring' in gen_globals['settings'].operations:
        add_substring_operations(ops_list)
    if 'to-lower-case' in gen_globals['settings'].operations:
        add_to_lower_case_operations(ops_list)
    if 'to-string' in gen_globals['settings'].operations:
        add_to_string_operations(ops_list)
    if 'to-upper-case' in gen_globals['settings'].operations:
        add_to_upper_case_operations(ops_list)
    if 'trim' in gen_globals['settings'].operations:
        add_trim_operations(ops_list)

    # set global operations from ops_list
    gen_globals['operations'] = ops_list

    # return operations array
    return ops_list


def get_boolean_constraints():
    # check for existing boolean constraints value
    boolean_constraints = gen_globals['boolean_constraints']
    if boolean_constraints is not None:
        return boolean_constraints

    # initialize operations list
    constraints_list = list()

    # add boolean constraint instances
    if 'contains' in gen_globals['settings'].operations:
        add_contains_predicates(constraints_list)
    if 'ends-with' in gen_globals['settings'].operations:
        add_ends_with_predicates(constraints_list)
    if 'equals' in gen_globals['settings'].operations:
        add_equals_predicates(constraints_list)
    if 'equals-ignore-case' in gen_globals['settings'].operations:
        add_equals_ignore_case_predicates(constraints_list)
    if 'is-empty' in gen_globals['settings'].operations:
        add_is_empty_predicates(constraints_list)
    if 'matches' in gen_globals['settings'].operations:
        add_matches_predicates(constraints_list)
    if 'region-matches' in gen_globals['settings'].operations:
        add_region_matches_predicates(constraints_list)
    if 'starts-with' in gen_globals['settings'].operations:
        add_starts_with_predicates(constraints_list)
    if 'starts-with-offset' in gen_globals['settings'].operations:
        add_starts_with_offset_predicates(constraints_list)

    # set global operations from ops_list
    gen_globals['boolean_constraints'] = constraints_list

    # return operations array
    return constraints_list


# id generator
def generate_id(value, force=False):
    # specify id_counter is the global variable

    # if id already generated for value
    if not force and value in gen_globals['value_id_map']:
        # return that value
        return gen_globals['value_id_map'].get(value)

    # generate new id from id counter
    new_id = gen_globals['settings'].id_counter

    # increment id counter
    gen_globals['settings'].id_counter += 1

    # return new id
    return new_id


def random_length(min_length=None, max_length=None):
    random.seed(gen_globals['rand_seed'])
    gen_globals['rand_seed'] += 1
    if min_length is None:
        min_length = 0
    if max_length is None:
        max_length = gen_globals['settings'].max_initial_length
    return random.randint(min_length, max_length)


def random_char():
    random.seed(gen_globals['rand_seed'])
    gen_globals['rand_seed'] += 1
    alpha_list = list(gen_globals['settings'].alphabet)
    return random.choice(alpha_list).upper()


def random_string(length=None):
    if length is None:
        length = gen_globals['settings'].max_initial_length
    chars = list()
    for num in range(0, length):
        chars.append(random_char())
    return ''.join(chars)


def get_all_strings(max_length):
    strings = set()
    strings.add('')
    prev_set = set()
    prev_set.add('')
    for i in range(0, max_length):
        next_set = set()
        for prev in prev_set:
            for c in gen_globals['settings'].alphabet:
                new_str = prev + c
                strings.add(new_str)
                next_set.add(new_str)

        prev_set = next_set
    return strings


def perform_concat(string, op):
    # randomize arg value if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        max_len = gen_globals['settings'].max_initial_length
        str_len = random_length(0, max_len)
        op.op_args[0] = random_string(str_len)
        op.args_known[op.op_args[0]] = False

    # return concatenated string
    return string + op.op_args[0]


def perform_append_substring(string, op):
    # randomize arg value if unknown
    arg_length = random_length()
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = random_string(arg_length)

    # randomize start value if unknown
    start_index = random_length(max_length=arg_length)
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = str(start_index)

    # randomize end value if unknown
    if len(op.op_args[2]) == 1 and ord(op.op_args[2]) == 0:
        # adjust end if char array operation
        if op.op == 'append!![CII':
            op.op_args[2] = str(random_length(0, arg_length - start_index))
        else:
            op.op_args[2] = str(random_length(start_index, arg_length))

    # get start and end as numbers
    start = int(op.op_args[1])
    end = int(op.op_args[2])

    # adjust end if char array operation
    if op.op == 'append!![CII':
        end = int(op.op_args[2]) + start

    # get substring value
    substr_value = op.op_args[0][start:end]

    # return concatenated string
    return string + substr_value


def perform_delete_char_at(string, op):
    # randomize index if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(max_length=len(string)))

    # get index as a number
    index = int(op.op_args[0])

    # return deleted string
    return string[:index] + string[(index + 1):]


def perform_delete(string, op):
    # randomize start index if unknown
    start_index = random_length(max_length=len(string))
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(start_index)

    # randomize end index if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = str(random_length(start_index, len(string)))

    # get indices as numbers
    start = int(op.op_args[0])
    end = int(op.op_args[1])

    # return deleted string
    return string[:start] + string[(end + 1):]


def perform_insert_char(string, op):
    # randomize index if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(max_length=len(string)))

    # randomize char if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_char()

    # get index as a number
    index = int(op.op_args[0])

    # get insert character
    insert = op.op_args[1]

    # return inserted string
    return string[:index] + insert + string[(index + 1):]


def perform_insert_string(string, op):
    # randomize index if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(max_length=len(string)))

    # randomize char if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_string()

    # get index as a number
    index = int(op.op_args[0])

    # get insert character
    insert = op.op_args[1]

    # return inserted string
    return string[:index] + insert + string[(index + 1):]


def perform_insert_substring(string, op):
    # randomize index if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(max_length=len(string)))

    # randomize arg value if unknown
    arg_length = random_length()
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_string(arg_length)

    # randomize start value if unknown
    start_index = random_length(max_length=arg_length)
    if len(op.op_args[2]) == 1 and ord(op.op_args[2]) == 0:
        op.op_args[2] = str(start_index)

    # randomize end value if unknown
    if len(op.op_args[3]) == 1 and ord(op.op_args[3]) == 0:
        # adjust end if char array operation
        if op.op == 'insert!!I[CII':
            op.op_args[3] = str(random_length(0, arg_length - start_index))
        else:
            op.op_args[3] = str(random_length(start_index, arg_length))

    # get index, start, and end as numbers
    index = int(op.op_args[0])
    start = int(op.op_args[2])
    end = int(op.op_args[3])

    # adjust end if char array operation
    if op.op == 'insert!!I[CII':
        end = int(op.op_args[3]) + start

    # return inserted string
    return string[:index] + op.op_args[1][start:end] + string[(index + 1):]


def perform_replace_char(string, op):
    # randomize old char if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = random_char()
    # randomize new char if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_char()

    # get old and new characters
    old = op.op_args[0]
    new = op.op_args[1]

    return string.replace(old, new)


def perform_replace_regex(string, op):
    # randomize regex if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = random_char() + '+'
    # randomize replacement if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_string()

    # get target and replacement strings
    regex = op.op_args[0]
    replacement = op.op_args[1]

    if op.op == 'replaceFirst!!Ljava/lang/String;Ljava/lang/String;':
        return re.sub(regex, replacement, string, count=1)
    else:
        return re.sub(regex, replacement, string)


def perform_replace_string(string, op):
    # randomize target if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = random_string(len(string))
    # randomize replacement if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = random_string()

    # get target and replacement strings
    target = op.op_args[0]
    replacement = op.op_args[1]

    # return replaced string
    return string.replace(target, replacement)


def perform_replace_substring(string, op):
    # randomize start index if unknown
    start_index = random_length(max_length=len(string))
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(start_index)
    # randomize end index if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[1]) == 0:
        op.op_args[1] = str(random_length(start_index, len(string)))
    # randomize replacement if unknown
    if len(op.op_args[2]) == 1 and ord(op.op_args[2]) == 0:
        op.op_args[2] = random_string()

    # get indices as numbers
    start = int(op.op_args[0])
    end = int(op.op_args[1])

    # return replaced string
    return string[:start] + op.op_args[2] + string[end:]


def perform_reverse(string):
    # return reversed string
    return string[::-1]


def perform_suffix(string, op):
    # randomize index if unknown
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(max_length=len(string)))

    # get index as a number
    start = int(op.op_args[0])

    # return substring suffix
    return string[start:]


def perform_substring(string, op):
    # randomize start if unknown
    start_index = random_length(max_length=len(string))
    if len(op.op_args[0]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(start_index)
    # randomize start if unknown
    if len(op.op_args[1]) == 1 and ord(op.op_args[0]) == 0:
        op.op_args[0] = str(random_length(start_index, len(string)))

    # get indices as numbers
    start = int(op.op_args[0])
    end = int(op.op_args[1])

    # return substring
    return string[start:end]


def perform_to_lower_case(string):
    # return string in lowercase
    return string.lower()


def perform_to_string(string):
    # return string
    return string[::1]


def perform_to_upper_case(string):
    # return string in uppercase
    return string.upper()


def perform_trim(string):
    # return trimmed string
    return string.strip()


def perform_contains(string, pred):
    if pred.result:
        is_unknown = pred.args_known[pred.op_args[0]]
        contained_strings = [x for x in get_all_strings(1) if x in string and len(x) > 0]
        if contained_strings:
            del pred.args_known[pred.op_args[0]]
            contained_string = random.choice(contained_strings)
            pred.op_args[0] = contained_string
            pred.args_known[pred.op_args[0]] = is_unknown
        else:
            pred.op_args[0] = string
            pred.args_known[pred.op_args[0]] = is_unknown
    else:
        # randomize arg value if unknown
        if (len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0) \
                or pred.op_args[0] in string:
            is_uknown = pred.args_known[pred.op_args[0]]
            not_contained_strings = [x for x in get_all_strings(len(string)) if x not in string]
            if not_contained_strings:
                not_contained_string = random.choice(not_contained_strings)
                pred.op_args[0] = not_contained_string
            else:
                pred.op_args[0] = random_string(random_length(0, gen_globals['settings'].max_initial_length))
            pred.args_known[pred.op_args[0]] = is_uknown

    # return true or false for boolean constraint
    if pred.op_args[0] in string:
        return 'true'
    else:
        return 'false'


def perform_ends_with(string, pred):
    # randomize arg value if unknown
    if len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0:
        max_len = gen_globals['settings'].max_initial_length
        str_len = random_length(0, max_len)
        pred.op_args[0] = random_string(str_len)
        pred.args_known[pred.op_args[0]] = False

    # return true or false for boolean constraint
    if string.endswith(pred.op_args[0]):
        return 'true'
    else:
        return 'false'


def perform_equals(string, pred):
    if pred.result:
        is_unknown = pred.args_known.pop(pred.op_args[0])
        pred.op_args[0] = string
        pred.args_known[pred.op_args[0]] = is_unknown
    else:
        # randomize arg value if unknown
        if (len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0) \
                or pred.op_args[0] == string:
            is_uknown = pred.args_known.pop(pred.op_args[0])
            not_contained_strings = [x for x in get_all_strings(len(string)) if x != string]
            if not_contained_strings:
                not_contained_string = random.choice(not_contained_strings)
                pred.op_args[0] = not_contained_string
            else:
                pred.op_args[0] = random_string(random_length(0, gen_globals['settings'].max_initial_length))
            pred.args_known[pred.op_args[0]] = is_uknown

    # return true or false for boolean constraint
    if string == pred.op_args[0]:
        return 'true'
    else:
        return 'false'


def perform_equals_ignore_case(string, pred):
    if pred.result:
        is_unknown = pred.args_known.pop(pred.op_args[0])
        contained_strings = [x for x in get_all_strings(len(string)) if x.lower() == string.lower()]
        contained_string = random.choice(contained_strings)
        pred.op_args[0] = contained_string
        pred.args_known[pred.op_args[0]] = is_unknown
    else:
        # randomize arg value if unknown
        if (len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0) \
                or pred.op_args[0] == string:
            is_uknown = pred.args_known.pop(pred.op_args[0])
            not_contained_strings = [x for x in get_all_strings(len(string)) if x.lower() != string.lower()]
            if not_contained_strings:
                not_contained_string = random.choice(not_contained_strings)
                pred.op_args[0] = not_contained_string
            else:
                pred.op_args[0] = random_string(random_length(0, gen_globals['settings'].max_initial_length))
            pred.args_known[pred.op_args[0]] = is_uknown

# return true or false for boolean constraint
    if string.lower() == pred.op_args[0].lower():
        return 'true'
    else:
        return 'false'


def perform_is_empty(string):
    # return true or false for boolean constraint
    if string == '':
        return 'true'
    else:
        return 'false'


def perform_starts_with(string, pred):
    # randomize arg value if unknown
    if len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0:
        max_len = gen_globals['settings'].max_initial_length
        str_len = random_length(0, max_len)
        pred.op_args[0] = random_string(str_len)
        pred.args_known[pred.op_args[0]] = False

    # return true or false for boolean constraint
    if string.startswith(pred.op_args[0]):
        return 'true'
    else:
        return 'false'


def perform_starts_with_offset(string, pred):
    # randomize arg value if unknown
    if len(pred.op_args[0]) == 1 and ord(pred.op_args[0]) == 0:
        max_len = gen_globals['settings'].max_initial_length
        str_len = random_length(0, max_len)
        pred.op_args[0] = random_string(str_len)
        pred.args_known[pred.op_args[0]] = False
    # randomize offset if unknown
    if len(pred.op_args[1]) == 1 and ord(pred.op_args[1]) == 0:
        pred.op_args[1] = random_length(max_length=len(string))

    # return true or false for boolean constraint
    if string[pred.op_args[1]:].startswith(pred.op_args[0]):
        return 'true'
    else:
        return 'false'


def perform_op(original_value, op):
    # operations
    if op.op in ['append!!Ljava/lang/String;', 'concat!!Ljava/lang/String;']:
        return perform_concat(original_value, op)
    elif op.op in ['append!![CII', 'append!!Ljava/lang/CharSequence;II']:
        return perform_append_substring(original_value, op)
    elif op.op == 'deleteCharAt!!I':
        return perform_delete_char_at(original_value, op)
    elif op.op == 'delete!!II':
        return perform_delete(original_value, op)
    elif op.op == 'insert!!IC':
        return perform_insert_char(original_value, op)
    elif op.op in ['insert!!I[C', 'insert!!ILjava/lang/CharSequence;']:
        return perform_insert_string(original_value, op)
    elif op.op in ['insert!!I[CII', 'insert!!ILjava/lang/CharSequence;II']:
        return perform_insert_substring(original_value, op)
    elif op.op == 'replace!!CC':
        return perform_replace_char(original_value, op)
    elif op.op == 'replace!!Ljava/lang/CharSequence;Ljava/lang/CharSequence;':
        return perform_replace_string(original_value, op)
    elif op.op in ['replaceAll!!Ljava/lang/String;Ljava/lang/String;',
                   'replaceFirst!!Ljava/lang/String;Ljava/lang/String;']:
        return perform_replace_regex(original_value, op)
    elif op.op == 'replace!!IILjava/lang/String;':
        return perform_replace_substring(original_value, op)
    elif op.op == 'reverse!!':
        return perform_reverse(original_value)
    elif op.op == 'substring!!I':
        return perform_suffix(original_value, op)
    elif op.op == 'substring!!II':
        return perform_substring(original_value, op)
    elif op.op == 'toLowerCase!!':
        return perform_to_lower_case(original_value)
    elif op.op == 'toString!!':
        return perform_to_string(original_value)
    elif op.op == 'toUpperCase!!':
        return perform_to_upper_case(original_value)
    elif op.op == 'trim!!':
        return perform_trim(original_value)


def perform_predicate(value, const):
    # determine boolean constraint to perform
    if const.op == 'contains!!Ljava/lang/CharSequence;':
        return perform_contains(value, const)
    elif const.op == 'endsWith!!Ljava/lang/String;':
        return perform_ends_with(value, const)
    elif const.op in ['contentEquals!!Ljava/lang/CharSequence;',
                      'contentEquals!!Ljava/lang/StringBuffer;',
                      'equals!!Ljava/lang/Object;']:
        return perform_equals(value, const)
    elif const.op == 'equalsIgnoreCase!!Ljava/lang/String;':
        return perform_equals_ignore_case(value, const)
    elif const.op == 'isEmpty!!':
        return perform_is_empty(value)
    elif const.op == 'startsWith!!Ljava/lang/String;':
        return perform_starts_with(value, const)
    elif const.op == 'startsWith!!Ljava/lang/String;':
        return perform_starts_with_offset(value, const)


def add_operation(t, countdown, v_list=None):
    new_v_list = v_list is None

    # for each operation
    ops_collection = get_operations()
    for op in ops_collection:

        # check and create vertices list
        if new_v_list:
            v_list = list()
            gen_globals['vertices'].append(v_list)
            v_list.append(t)

        gen_globals['settings'].op_counter += 1

        # if my_globals['settings'].op_total > 100 and my_globals['settings'].op_counter % (
        #             my_globals['settings'].op_total / 100) == 0:
        #     percent = my_globals['settings'].op_counter * 100 / my_globals['settings'].op_total
        #     log.debug('Operation Addition Progress: {0}%'.format(percent))

        # get actual value resulting from op
        actual_val = perform_op(t.actual_value, op)

        # for each operation argument
        arg_ids = list()
        for j, arg in enumerate(op.op_args):
            # create root value for arg
            if not op.args_known[arg]:
                arg_value = RootValue(False, method="getStringValue!!")
            else:
                arg_value = RootValue(True, arg, "init")

            # create vertex
            arg_val = arg_value.get_value()
            arg_vertex = Vertex(arg_val, arg, generate_id(arg_val))
            arg_ids.append(arg_vertex.node_id)

            # add arg vertices list
            v_list.append(arg_vertex)

        # create vertex for operation
        value = op.get_value()
        op_vertex = Vertex(value, actual_val, generate_id(value))

        # add operation vertices list
        v_list.append(op_vertex)

        # add edge to collection
        edge = Edge(t.node_id, op_vertex.node_id, 't')
        op_vertex.incoming_edges.append(edge)

        # for each operation argument
        for j, arg_id in enumerate(arg_ids):

            # add edge to collection
            edge_type = 's{0}'.format(j + 1)
            arg_edge = Edge(arg_id, op_vertex.node_id, edge_type)
            op_vertex.incoming_edges.append(arg_edge)

        # if countdown not reached
        if countdown > 1:
            # add another of each operation
            add_operation(op_vertex, countdown - 1, v_list)

        # add a boolean constraint
        add_bool_constraint(op_vertex, v_list)


def add_bool_constraint(t, v_list, allow_duplicates=False, predicate_list=None):
    if predicate_list is None:
        predicate_list = get_boolean_constraints()

    # for each boolean constraint
    for pred in predicate_list:

        # if no duplicates allowed
        if not gen_globals['settings'].allow_duplicates and not allow_duplicates:
            t = t.clone()
            v_list.append(t)

        # get actual value resulting from op
        actual_val = perform_predicate(t.actual_value, pred)

        # for each operation argument
        arg_ids = list()
        for k, arg in enumerate(pred.op_args):
            # create root value for arg
            if not pred.args_known[arg]:
                arg_value = RootValue(False, method="getStringValue!!")
            else:
                arg_value = RootValue(True, arg, "init")

            # create vertex
            arg_val = arg_value.get_value()
            do_force = not gen_globals['settings'].allow_duplicates
            arg_vertex = Vertex(arg_val, arg, generate_id(arg_val, force=do_force))
            arg_ids.append(arg_vertex.node_id)

            # make argument non uniform
            if pred.non_uniform:
                contains_predicates = list()
                add_contains_predicates(contains_predicates)
                contains_predicates = contains_predicates[:1]
                add_bool_constraint(arg_vertex, v_list, allow_duplicates=True, predicate_list=contains_predicates)

            # add arg vertex to collection
            v_list.append(arg_vertex)

        # create vertex for operation
        value = pred.get_value()
        pred_vertex = Vertex(value, actual_val, generate_id(value))

        # add operation vertex to collection
        v_list.append(pred_vertex)

        # add edge to collection
        edge = Edge(t.node_id, pred_vertex.node_id, 't')
        pred_vertex.incoming_edges.append(edge)

        # for each operation argument
        for k, arg_id in enumerate(arg_ids):
            # add edge to collection
            edge_type = 's{0}'.format(k + 1)
            arg_edge = Edge(arg_id, pred_vertex.node_id, edge_type)
            pred_vertex.incoming_edges.append(arg_edge)


# main function
def get_options(arguments):
    # process command line args
    generate_parser = argparse.ArgumentParser(prog=__doc__,
                                              description='Generate '
                                                          'artificial string '
                                                          'constraint graphs '
                                                          'for us '
                                                          'with the string '
                                                          'constraint '
                                                          'solver framework')

    generate_parser.add_argument('-a',
                                 '--alphabet',
                                 default='A-D',
                                 help='An alphabet declaration using the a '
                                      'comma '
                                      'separated list of either singe '
                                      'characters or '
                                      'character ranges using the \'-\' '
                                      'character as '
                                      'in "A-D".')

    generate_parser.add_argument('-d',
                                 '--debug',
                                 help="Display debug messages for script.",
                                 action="store_true")

    generate_parser.add_argument('-e',
                                 '--empty-string',
                                 help='Include empty string value in list of '
                                      'input '
                                      'strings used to generate graphs.',
                                 action='store_true')

    generate_parser.add_argument('-g',
                                 '--graph-file',
                                 default='gen',
                                 help='The name of the generated graph file.')

    generate_parser.add_argument('-c',
                                 '--non-uniform',
                                 help='Include non-uniform string before all '
                                      'subsequent string operations.',
                                 action='store_true')

    generate_parser.add_argument('-i',
                                 '--inputs',
                                 nargs='+',
                                 default=list(),
                                 help='List of input strings to use to '
                                      'generate '
                                      'graphs, each input string is used to '
                                      'generate a '
                                      'full set of graphs.')

    generate_parser.add_argument('-l',
                                 '--length',
                                 default=2,
                                 help='The maximum length of generated '
                                      'initial strings.')

    generate_parser.add_argument('-n',
                                 '--no-duplicates',
                                 help='Ensure that there are no duplicates of '
                                      'source '
                                      'constraints for each boolean '
                                      'constraint.',
                                 action='store_true')

    generate_parser.add_argument('-o',
                                 '--operations',
                                 nargs='+',
                                 default=list(),
                                 help='List of operations and predicates to '
                                      'use in the generation of the graph. The'
                                      ' available string operations are:\n'
                                      '\t- append-substring\n'
                                      '\t- append\n'
                                      '\t- concat\n'
                                      '\t- delete-char-at\n'
                                      '\t- delete\n'
                                      '\t- insert-char\n'
                                      '\t- insert-string\n'
                                      '\t- insert-substring\n'
                                      '\t- replace-char\n'
                                      '\t- replace-string\n'
                                      '\t- replace-regex-string\n'
                                      '\t- replace-substring\n'
                                      '\t- reverse\n'
                                      '\t- substring\n'
                                      '\t- to-lower-case\n'
                                      '\t- to-string\n'
                                      '\t- to-upper-case\n'
                                      '\t- trim\n'
                                      'The available string predicates are:\n'
                                      '\t- contains\n'
                                      '\t- ends-with\n'
                                      '\t- equals\n'
                                      '\t- equals-ignore-case\n'
                                      '\t- is-empty\n'
                                      '\t- matches\n'
                                      '\t- region-matches\n'
                                      '\t- starts-with\n'
                                      '\t- starts-with-offset'
                                 )

    generate_parser.add_argument('-p',
                                 '--ops-depth',
                                 default=1,
                                 help='The maximum number of operations that '
                                      'will be '
                                      'performed in sequence before a '
                                      'constraint is '
                                      'reached in the generated graph.')

    generate_parser.add_argument('-s',
                                 '--single-graph',
                                 help='Force output into a single graph file.',
                                 action='store_true')

    generate_parser.add_argument('-u',
                                 '--unknown-string',
                                 help='Include unknown string value in list '
                                      'of input '
                                      'strings used to generate graphs.',
                                 action='store_true')

    return generate_parser.parse_args(arguments)


def parse_alphabet_declaration(alphabet_declaration):
    # initialize symbol set
    symbol_set = set()

    # detect comma characters
    if ',,' in alphabet_declaration:

        # detect single comma in alphabet declaration
        if ',,,' in alphabet_declaration or \
                alphabet_declaration.startswith(',,') or \
                alphabet_declaration.endswith(',,'):
            # add to symbol set
            symbol_set.add(',')

            # remove comma from alphabet declaration
            alphabet_declaration = alphabet_declaration.replace(',,', '')

        # detect commas as part of range in alphabet declaration
        pattern = '^.*,?(?:,(?P<range1>,-.|.-,)|(?P<range2>,-.|.-,),),?.*'
        match = re.match(pattern, alphabet_declaration)

        if match:
            # get matching char range
            char_range = match.group('range1')
            if char_range is None:
                char_range = match.group('range2')

            # add char range to symbol set
            start = char_range[0]
            end = char_range[2]
            for c in range(ord(start), ord(end)):
                symbol_set.add(chr(c))

            # remove range from alphabet declaration
            alphabet_declaration.replace(char_range, '')

    # split string
    symbol_declarations = alphabet_declaration.split(',')

    # process symbol declarations
    for sd in symbol_declarations:

        # process singe char declaration
        if len(sd) == 1:
            symbol_set.add(sd[0])

        # process char range declaration
        elif len(sd) == 3 and sd[1] == '-':
            start = sd[0]
            end = sd[2]
            for c in range(ord(start), ord(end) + 1):
                symbol_set.add(chr(c))

        # invalid alphabet declaration
        else:
            raise ValueError('alphabet declaration is invalid')

    # initialize set of symbols to add
    to_add = set()

    # ensure correct lower case letters are included in alphabet
    if gen_globals['has_lower_case_op']:
        for c in symbol_set:
            c_lower = c.lower()
            # if lower equivilant
            if c != c_lower:
                to_add.add(c_lower)

    # ensure correct upper case letters are included in alphabet
    if gen_globals['has_upper_case_op']:
        for c in symbol_set:
            c_upper = c.upper()
            # if lower equivilant
            if c != c_upper:
                to_add.add(c_upper)

    # update symbol list with all additions
    symbol_set.update(to_add)

    # return symbol set
    return symbol_set


def create_alphabet_declaration(alphabet):
    # initialize variables
    symbol_list = list(alphabet)
    ranges = list()
    prev = symbol_list.pop(0)
    start = prev

    # process each symbol
    for sym in symbol_list:

        # if starting new range
        if ord(sym) - ord(prev) != 1:
            # add previous range pair
            range_pair = (start, prev)
            ranges.append(range_pair)

            # set new min
            start = sym

        # set prev from current sym
        prev = sym

    # add final range pair
    range_pair = (start, prev)
    ranges.append(range_pair)

    # transform range pairs into strings
    range_strings = list()
    for s, e in ranges:
        if s == e:
            range_strings.append(s)
        else:
            range_string = '{0}-{1}'.format(s, e)
            range_strings.append(range_string)

    # return single string of ranges
    return ','.join(range_strings)


def get_root_verticies(settings_inst):
    for value in settings_inst.inputs:
        # create root node value
        if len(value) == 1 and ord(value) == 0:
            root_value = RootValue(False, method="getStringValue!!")
        else:
            root_value = RootValue(True, value, "init")

        # create vertex from root node
        val = root_value.get_value()
        vertex = Vertex(val, root_value.string, generate_id(val))
        gen_globals['vertices'].append(vertex)
    # TODO: Add contains vertex for non-uniform initial string


def main(arguments):
    reset_globals()

    # get option from arguments
    options = get_options(arguments)

    # get settings
    gen_globals['settings'] = Settings(options)

    # clean existing files
    dir_path = os.path.join(project_dir, 'graphs', 'synthetic')
    if not os.path.exists(dir_path):
        log.debug('creating output dir: %s', dir_path)
        os.makedirs(dir_path)

    if gen_globals['settings'].depth == 1 \
            or gen_globals['settings'].single_graph:
        prev_file = gen_globals['settings'].graph_name + '.json'
        try:
            os.remove(os.path.join(dir_path, prev_file))
        except OSError:
            pass
    else:
        for f in os.listdir(dir_path):
            if re.search(gen_globals['settings'].graph_name + '.*\.json', f):
                remove_file = os.path.join(dir_path, f)
                log.debug('Removing Previous Graph File: %s', remove_file)
                os.remove(remove_file)

    # for each input value
    vertices_collection = []
    for value in gen_globals['settings'].inputs:

        # create root node value
        if len(value) == 1 and ord(value) == 0:
            root_value = RootValue(False, method="getStringValue!!")
        else:
            root_value = RootValue(True, value, "init")

        # create vertex from root node
        val = root_value.get_value()
        root_vertex = Vertex(val, root_value.string, generate_id(val))

        init_v_list = list()

        if gen_globals['settings'].non_uniform \
                and len(value) == 1 \
                and ord(value) == 0:
            contains_predicates = list()
            add_contains_predicates(contains_predicates)
            contains_predicates = contains_predicates[:1]
            gen_globals['vertices'].append(init_v_list)
            add_bool_constraint(root_vertex,
                                init_v_list,
                                allow_duplicates=True,
                                predicate_list=contains_predicates)

        # add operations to the vertex
        add_operation(root_vertex, gen_globals['settings'].depth)

        # add bool contraints for initial string
        if gen_globals['settings'].non_uniform \
                and len(value) == 1 \
                and ord(value) == 0:
            gen_globals['settings'].non_uniform = False
        else:
            gen_globals['vertices'].append(init_v_list)
            add_bool_constraint(root_vertex, init_v_list)

        log.debug('*** {0} Operations Added ***'.format(gen_globals['settings'].op_counter))
        num_v = 0
        for v_list in gen_globals['vertices']:
            num_v += len(v_list)
        v_counter = 0

        vertices_collection = list()

        for v_list in gen_globals['vertices']:

            # initialize vertex list
            vertex_list = list()

            # for each vertex
            for v in v_list:

                v_counter += 1
                # if num_v > 100 and v_counter % (num_v / 100) == 0:
                #     percent = v_counter * 100 / num_v
                #     log.debug('Vertex Creation Progress: {0}%'.format(percent))

                # initialize vertex
                vertex = {
                    'incomingEdges': list(),
                    'sourceConstraints': list(),
                    'timeStamp': int(time.time()),
                    'value': v.value,
                    'actualValue': v.actual_value,
                    'num': 0,
                    'type': 0,
                    'id': v.node_id
                }

                # for each edge
                for edge in v.incoming_edges:
                    # create incoming edge
                    inc_edge = {
                        'source': edge.source_id,
                        'type': edge.edge_type
                    }

                    # add the incoming edge to the vertex
                    vertex['incomingEdges'].append(inc_edge)

                vertex_list.append(vertex)

            # add current vertex list to collection
            vertices_collection.append(vertex_list)

        # flatten vertices collection into single nested list if simple
        if gen_globals['settings'].depth == 1 \
                or gen_globals['settings'].single_graph:
            # get collection of verticies
            v_collection = list()
            for v_list in vertices_collection:
                for v in v_list:
                    v_collection.append(v)
            vertices_collection = [v_collection]

    for j, v_list in enumerate(vertices_collection):
        # create graph dictionary
        graph = {
            'vertices': v_list,
            'alphabet': {
                'declaration': create_alphabet_declaration(
                    gen_globals['settings'].alphabet),
                'size': len(gen_globals['settings'].alphabet)
            }
        }

        # write out update graph file
        gfname = '{0}-{1:02d}.json'.format(gen_globals['settings'].graph_name, j + 1)
        if gen_globals['settings'].depth == 1 \
                or gen_globals['settings'].single_graph:
            gfname = gen_globals['settings'].graph_name + '.json'

        file_path = os.path.join(dir_path, gfname)
        with open(file_path, 'w') as graph_file:
            log.debug('Creating Graph File: %s', graph_file.name)
            json.dump(graph, graph_file)

    # cleanup global variables


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
