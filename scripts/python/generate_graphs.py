#!/usr/bin/env python
import argparse
import json
import logging
import os
import random
import re
import sys
import time

# Configure Logging
file_name = os.path.basename(__file__).replace('.py', '')
log = logging.getLogger(file_name)
log.setLevel(logging.DEBUG)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.DEBUG)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s]: %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)


# Data Classes
# root node
class RootValue:
    def __init__(self, has_string, string=None, method=''):
        self.has_string = has_string
        self.string = string if string is not None else random_string(4)
        self.method = method

    def get_value(self):
        if self.has_string:
            return '\"{0.string}\"!:!<{0.method}>'.format(self)

        return 'r1!:!{0.method}'.format(self)


# operation node
class OperationValue:
    def __init__(self, op, op_args=list(), num=0):
        self.op = op
        self.op_args = op_args
        self.num = num

    def get_value(self):
        return '{0.op}!:!{0.num}'.format(self)


# boolean constraint node
class BooleanConstraintValue:
    def __init__(self, op, op_args=list(), num=0):
        self.op = op
        self.op_args = op_args
        self.num = num

    def get_value(self):
        return '{0.op}!:!{0.num}'.format(self)


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


# edge
class Edge:
    def __init__(self, source_id, target_id, edge_type):
        self.source_id = source_id
        self.target_id = target_id
        self.edge_type = edge_type


# Initialize Arrays
# operations array
operations = [
    OperationValue('append!!Ljava/lang/String;', ['A']),
    OperationValue('append!![CII', ['ABC', '1', '2']),
    OperationValue('concat!!Ljava/lang/String;', ['B']),
    OperationValue('deleteCharAt!!I', ['0']),
    OperationValue('delete!!II', ['0', '1']),
    OperationValue('insert!!IC', ['0', 'D']),
    OperationValue('insert!!I[C', ['0', 'AB']),
    OperationValue('insert!!I[CII', ['0', 'ABCD', '2', '3']),
    OperationValue('replace!!CC', ['A', 'B']),
    OperationValue('replace!!CC', ['A', '2']),
    OperationValue('replace!!CC', ['1', 'B']),
    OperationValue('replace!!CC', ['1', '2']),
    OperationValue('replace!!Ljava/lang/CharSequence;Ljava/lang/CharSequence;',
                   ['AB', 'CD']),
    OperationValue('reverse!!'),
    OperationValue('substring!!I', ['1']),
    OperationValue('substring!!II', ['0', '1']),
    OperationValue('toLowerCase!!'),
    OperationValue('toString!!'),
    OperationValue('toUpperCase!!'),
    OperationValue('trim!!')
]

# boolean constraints array
boolean_constraints = [
    BooleanConstraintValue('contains!!Ljava/lang/CharSequence;', ['C']),
    BooleanConstraintValue('endsWith!!Ljava/lang/String;', ['D']),
    BooleanConstraintValue('equals!!Ljava/lang/Object;', ['ABCD']),
    BooleanConstraintValue('equalsIgnoreCase!!Ljava/lang/String;', ['ABCD']),
    BooleanConstraintValue('isEmpty!!'),
    BooleanConstraintValue('startsWith!!Ljava/lang/String;', ['A'])
]

# Global values
allow_duplicates = True
alphabet = {'a', 'b', 'c', 'd', 'A', 'B', 'C', 'D'}
depth = 1
id_counter = 1
op_counter = 0
op_total = 0
value_id_map = dict()
vertices = list()

# calculate op total
for i in range(0, depth + 2):
    op_total += len(operations) ** i


# id generator
def generate_id(value, force=False):
    # specify id_counter is the global variable
    global id_counter

    # if id already generated for value
    if not force and value in value_id_map:
        # return that value
        return value_id_map.get(value)

    # generate new id from id counter
    new_id = id_counter

    # increment id counter
    id_counter += 1

    # return new id
    return new_id


def random_char():
    alpha_list = list(alphabet)
    return random.choice(alpha_list).upper()


def random_string(length):
    chars = list()
    for num in range(1, length):
        chars.append(random_char())
    return ''.join(chars)


# recursive graph constructor function
def perform_op(original_value, op):
    # determine operation
    if op.op in ['append!!Ljava/lang/String;', 'concat!!Ljava/lang/String;']:

        # get argument value
        arg_value = op.op_args[0]

        # return concatenated string
        return original_value + arg_value

    elif op.op == 'append!![CII':

        # get initial argument value
        arg_value = op.op_args[0]

        # get offset and length as numbers
        offset = int(op.op_args[1])
        length = int(op.op_args[2])

        # get substring value
        substr_value = arg_value[offset:(offset + length)]

        # return concatenated string
        return original_value + substr_value

    elif op.op == 'deleteCharAt!!I':

        # get index as a number
        index = int(op.op_args[0])

        # return deleted string
        return original_value[:index] + original_value[(index + 1):]

    elif op.op == 'delete!!II':

        # get indices as numbers
        start = int(op.op_args[0])
        end = int(op.op_args[1])

        # return deleted string
        return original_value[:start] + original_value[(end + 1):]

    elif op.op == 'insert!!IC':

        # get index as a number
        index = int(op.op_args[0])

        # get insert character
        insert = op.op_args[1]

        # return inserted string
        return original_value[:index] + insert + original_value[(index + 1):]

    elif op.op == 'insert!!I[C':

        # get index as a number
        index = int(op.op_args[0])

        # get insert string
        insert = op.op_args[1]

        # return inserted string
        return original_value[:index] + insert + original_value[(index + 1):]

    elif op.op == 'insert!!I[CII':

        # get index as a number
        index = int(op.op_args[0])

        # get offset and length as numbers
        offset = int(op.op_args[2])
        length = int(op.op_args[3])

        # get insert substring
        arg_string = op.op_args[1]
        insert = arg_string[offset:(offset + length)]

        # return inserted string
        return original_value[:index] + insert + original_value[(index + 1):]

    elif op.op == 'replace!!CC':

        # get find and replace characters
        find = op.op_args[0]
        replace = op.op_args[1]

        # initialize known flags
        find_known = True
        replace_known = True

        # determine if find is a known character
        if ord('0') <= ord(find) <= ord('9'):
            find_known = False

        # determine if replace is a known character
        if ord('0') <= ord(replace) <= ord('9'):
            replace_known = False

        # perform operation depending on known arguments
        if find_known and replace_known:
            return original_value.replace(find, replace)
        elif find_known:
            return original_value.replace(find, random_char())
        elif replace_known:
            return original_value.replace(random_char(), replace)
        else:
            return original_value.replace(random_char(), random_char())

    elif op.op == 'replace!!Ljava/lang/CharSequence;Ljava/lang/CharSequence;':

        # get find and replace strings
        find = op.op_args[0]
        replace = op.op_args[1]

        # return replaced string
        return original_value.replace(find, replace)

    elif op.op == 'reverse!!':

        # return reversed string
        return original_value[::-1]

    elif op.op == 'substring!!I':

        # get index as a number
        start = int(op.op_args[0])

        # return substring suffix
        return original_value[start:]

    elif op.op == 'substring!!II':

        # get indices as numbers
        start = int(op.op_args[0])
        end = int(op.op_args[1])

        # return substring
        return original_value[start:end]

    elif op.op == 'toLowerCase!!':

        # return string in lowercase
        return original_value.lower()

    elif op.op == 'toString!!':

        # return string
        return original_value[::1]

    elif op.op == 'toUpperCase!!':

        # return string in uppercase
        return original_value.upper()

    elif op.op == 'trim!!':

        # return trimmed string
        return original_value.strip()


def perform_const(value, const):
    # determine boolean constraint to perform
    if const.op == 'contains!!Ljava/lang/CharSequence;':

        # get arg string
        arg_string = const.op_args[0]

        # return true or false for boolean constraint
        return 'true' if value.find(arg_string) > -1 else 'false'

    elif const.op == 'endsWith!!Ljava/lang/String;':

        # get arg string
        arg_string = const.op_args[0]

        # return true or false for boolean constraint
        return 'true' if value.endswith(arg_string) else 'false'

    elif const.op == 'equals!!Ljava/lang/Object;':

        # get arg string
        arg_string = const.op_args[0]

        # return true or false for boolean constraint
        return 'true' if value == arg_string else 'false'

    elif const.op == 'equalsIgnoreCase!!Ljava/lang/String;':

        # get arg string
        arg_string = const.op_args[0]

        # return true or false for boolean constraint
        return 'true' if value.lower() == arg_string.lower() else 'false'

    elif const.op == 'isEmpty!!':

        # return true or false for boolean constraint
        return 'true' if value == '' else 'false'

    elif const.op == 'startsWith!!Ljava/lang/String;':

        # get arg string
        arg_string = const.op_args[0]

        # return true or false for boolean constraint
        return 'true' if value.startswith(arg_string) else 'false'


def add_operation(t, countdown, v_list=None):
    global op_counter

    new_v_list = v_list is None

    # for each operation
    for op in operations:

        # check and create vertices list
        if new_v_list:
            v_list = list()
            vertices.append(v_list)
            v_list.append(t)

        op_counter += 1
        if op_counter % (op_total / 100) == 0:
            percent = op_counter * 100 / op_total
            log.debug('Operation Addition Progress: {0}%'.format(percent))

        # get actual value resulting from op
        actual_val = perform_op(t.actual_value, op)

        # create vertex for operation
        value = op.get_value()
        op_vertex = Vertex(value, actual_val, generate_id(value))

        # add operation vertices list
        v_list.append(op_vertex)

        # add edge to collection
        edge = Edge(t.node_id, op_vertex.node_id, 't')
        op_vertex.incoming_edges.append(edge)

        # for each operation argument
        for j, arg in enumerate(op.op_args):
            # create root value for arg
            arg_value = RootValue(True, arg, "init")

            # create vertex
            arg_val = arg_value.get_value()
            arg_vertex = Vertex(arg_val, arg, generate_id(arg_val))

            # add arg vertices list
            v_list.append(arg_vertex)

            # add edge to collection
            edge_type = 's{0}'.format(j + 1)
            arg_edge = Edge(arg_vertex.node_id, op_vertex.node_id, edge_type)
            op_vertex.incoming_edges.append(arg_edge)

        # if countdown not reached
        if countdown > 1:
            # add another of each operation
            add_operation(op_vertex, countdown - 1, v_list)

        # add a boolean constraint
        add_bool_constraint(op_vertex, v_list)


def add_bool_constraint(t, v_list):
    # for each boolean constraint
    for const in boolean_constraints:

        # if no duplicates allowed
        if not allow_duplicates:
            t = t.clone()
            v_list.append(t)

        # get actual value resulting from op
        actual_val = perform_const(t.actual_value, const)

        # create vertex for operation
        value = const.get_value()
        const_vertex = Vertex(value, actual_val, generate_id(value))

        # add operation vertex to collection
        v_list.append(const_vertex)

        # add edge to collection
        edge = Edge(t.node_id, const_vertex.node_id, 't')
        const_vertex.incoming_edges.append(edge)

        # for each operation argument
        for k, arg in enumerate(const.op_args):
            # create root value for arg
            arg_value = RootValue(True, arg, "init")

            # create vertex
            arg_val = arg_value.get_value()
            do_force = not allow_duplicates
            arg_vertex = Vertex(arg_val,
                                arg,
                                generate_id(arg_val, force=do_force))

            # add arg vertex to collection
            v_list.append(arg_vertex)

            # add edge to collection
            edge_type = 's{0}'.format(k + 1)
            arg_edge = Edge(arg_vertex.node_id, const_vertex.node_id, edge_type)
            const_vertex.incoming_edges.append(arg_edge)


# main function
def get_options(arguments):
    # process command line args
    parser = argparse.ArgumentParser(prog=__doc__,
                                     description='Generate artificial string '
                                                 'constraint graphs for us '
                                                 'with the string constraint '
                                                 'solver framework')

    parser.add_argument('-o',
                        '--ops-depth',
                        default=1,
                        help='The maximum number of operations that will be '
                             'performed in sequence before a constraint is '
                             'reached in the generated graph.')

    parser.add_argument('-i',
                        '--inputs',
                        nargs='*',
                        default=list(),
                        help='List of input strings to use to generate '
                             'graphs, each input string is used to generate a '
                             'full set of graphs.')

    parser.add_argument('-u',
                        '--unknown-string',
                        help='Include unknown string value in list of input '
                             'strings used to generate graphs.',
                        action='store_true')

    parser.add_argument('-n',
                        '--no-duplicates',
                        help='Ensure that there are no duplicates of source '
                             'constraints for each boolean constraint.',
                        action='store_true')

    return parser.parse_args(arguments)


def main(arguments):
    # get option from arguments
    options = get_options(arguments)

    # initialize depth
    global depth
    depth = options.ops_depth

    # get list of input strings
    inputs = options.inputs

    # add unknown string input if specified
    if options.unknown_string:
        inputs.append(chr(0))

    # adjust duplicate option
    global allow_duplicates
    if options.no_duplicates:
        allow_duplicates = False

    # clean existing files
    dir_path = '{0}/../../graphs'.format(os.path.dirname(__file__))
    for f in os.listdir(dir_path):
        if re.search('gen.*\.json', f):
            os.remove(os.path.join(dir_path, f))

    # for each input value
    for value in inputs:

        # create root node value
        if ord(value) == 0:
            root_value = RootValue(False, method="getStringValue!!")
        else:
            root_value = RootValue(True, value, "init")

        # create vertex from root node
        val = root_value.get_value()
        root_vertex = Vertex(val, root_value.string, generate_id(val))

        # add operations to the vertex
        add_operation(root_vertex, depth)

        log.debug('*** {0} Operations Added ***'.format(op_counter))
        num_v = 0
        for v_list in vertices:
            num_v += len(v_list)
        v_counter = 0

        vertices_collection = list()

        for v_list in vertices:

            # initialize vertex list
            vertex_list = list()

            # for each vertex
            for v in v_list:

                v_counter += 1
                if v_counter % (num_v / 100) == 0:
                    percent = v_counter * 100 / num_v
                    log.debug('Vertex Creation Progress: {0}%'.format(percent))

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
        if depth == 1:
            v_list1 = vertices_collection[0]
            root_v = next(v for v in v_list1
                          if v['id'] == root_vertex.node_id)
            vertices_collection = [
                [v for v_list in vertices_collection for v in v_list
                 if v['id'] != root_vertex.node_id]]
            vertices_collection[0].append(root_v)

        for j, v_list in enumerate(vertices_collection):
            # create graph dictionary
            graph = {
                'vertices': v_list,
                'alphabet': {
                    'declaration': 'A-D,a-d',
                    'size': 8
                }
            }

            # write out update graph file
            file_path = '{0}/../../graphs/gen{1:02d}.json'.format(
                os.path.dirname(__file__), j + 1)
            with open(file_path, 'w') as graph_file:
                json.dump(graph, graph_file)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
