#!/usr/bin/env python
import json
import logging
import os
import random
import sys

# Configure Logging
import time

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
    def __init__(self, has_string, string='', method=''):
        self.has_string = has_string
        self.string = string
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


# edge
class Edge:
    def __init__(self, source_id, target_id, edge_type):
        self.source_id = source_id
        self.target_id = target_id
        self.edge_type = edge_type


# Initialize Arrays
# input array
inputs = ['ABCD']

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
    OperationValue('toLowercase!!'),
    OperationValue('toString!!'),
    OperationValue('toUppercase!!'),
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
depth = 2
alphabet = {'a', 'b', 'c', 'd', 'A', 'B', 'C', 'D'}
op_counter = 0
id_counter = 1
value_id_map = dict()
vertices = list()
op_total = 0
for i in range(0, depth + 2):
    op_total += len(operations) ** i


# id generator
def generate_id(value):
    # specify id_counter is the global variable
    global id_counter

    # if id already generated for value
    if value_id_map.has_key(value):
        # return that value
        return value_id_map.get(value)

    # generate new id from id counter
    new_id = id_counter

    # increment id counter
    id_counter += 1

    # return new id
    return new_id


# recursive graph constructor function
def random_char():
    alpha_list = list(alphabet)
    return random.choice(alpha_list).upper()


def perform_op(orignal_value, op):
    # determine operation
    if (op.op == 'append!!Ljava/lang/String;' or
                op.op == 'concat!!Ljava/lang/String;'):

        # get argument value
        arg_value = op.op_args[0]

        # return concatenated string
        return orignal_value + arg_value

    elif op.op == 'append!![CII':

        # get initial argument value
        arg_value = op.op_args[0]

        # get offset and length as numbers
        offset = int(op.op_args[1])
        length = int(op.op_args[2])

        # get substring value
        substr_value = arg_value[offset:(offset + length)]

        # return concatenated string
        return orignal_value + substr_value

    elif op.op == 'deleteCharAt!!I':

        # get index as a number
        index = int(op.op_args[0])

        # return deleted string
        return orignal_value[:index] + orignal_value[(index + 1):]

    elif op.op == 'delete!!II':

        # get indices as numbers
        start = int(op.op_args[0])
        end = int(op.op_args[1])

        # return deleted string
        return orignal_value[:start] + orignal_value[(end + 1):]

    elif op.op == 'insert!!IC':

        # get index as a number
        index = int(op.op_args[0])

        # get insert character
        insert = op.op_args[1]

        # return inserted string
        return orignal_value[:index] + insert + orignal_value[(index + 1):]

    elif op.op == 'insert!!I[C':

        # get index as a number
        index = int(op.op_args[0])

        # get insert string
        insert = op.op_args[1]

        # return inserted string
        return orignal_value[:index] + insert + orignal_value[(index + 1):]

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
        return orignal_value[:index] + insert + orignal_value[(index + 1):]

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
            return orignal_value.replace(find, replace)
        elif find_known:
            return orignal_value.replace(find, random_char())
        elif replace_known:
            return orignal_value.replace(random_char(), replace)
        else:
            return orignal_value.replace(random_char(), random_char())

    elif op.op == 'replace!!Ljava/lang/CharSequence;Ljava/lang/CharSequence;':

        # get find and replace strings
        find = op.op_args[0]
        replace = op.op_args[1]

        # return replaced string
        return orignal_value.replace(find, replace)

    elif op.op == 'reverse!!':

        # return reversed string
        return orignal_value[::-1]

    elif op.op == 'substring!!I':

        # get index as a number
        start = int(op.op_args[0])

        # return substring suffix
        return orignal_value[start:]

    elif op.op == 'substring!!II':

        # get indices as numbers
        start = int(op.op_args[0])
        end = int(op.op_args[1])

        # return substring
        return orignal_value[start:end]

    elif op.op == 'toLowercase!!':

        # return string in lowercase
        return orignal_value.lower()

    elif op.op == 'toString!!':

        # return string
        return orignal_value[::1]

    elif op.op == 'toUppercase!!':

        # return string in uppercase
        return orignal_value.upper()

    elif op.op == 'trim!!':

        # return trimmed string
        return orignal_value.strip()


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
        if countdown > 0:
            # add another of each operation
            add_operation(op_vertex, countdown - 1, v_list)

        # add a boolean constraint
        add_bool_constraint(op_vertex, v_list)


def add_bool_constraint(t, v_list):
    # for each boolean constraint
    for const in boolean_constraints:

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
            arg_vertex = Vertex(arg_val, arg, generate_id(arg_val))

            # add arg vertex to collection
            v_list.append(arg_vertex)

            # add edge to collection
            edge_type = 's{0}'.format(k + 1)
            arg_edge = Edge(arg_vertex.node_id, const_vertex.node_id, edge_type)
            const_vertex.incoming_edges.append(arg_edge)


# main function
def main(arguments):

    # initialize counter
    counter = 1

    # for each input value
    for value in inputs:
        # create root node value
        root_value = RootValue(True, value, "init")

        # create vertex from root node
        val = root_value.get_value()
        root_vertex = Vertex(val, value, generate_id(val))

        # add operations to the vertex
        add_operation(root_vertex, depth)

        log.debug('*** {0} Operations Added ***'.format(op_counter))
        num_v = 0
        for v_list in vertices:
            num_v += len(v_list)
        v_counter = 0

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

            # create graph dictionary
            graph = {
                'vertices': vertex_list,
                'alphabet': {
                    'declaration': 'A-D,a-d',
                    'size': 8
                }
            }

            # write out update graph file
            file_path = '{0}/../../graphs/gen{1:02d}.json'.format(
                os.path.dirname(__file__), counter)
            with open(file_path, 'w') as graph_file:
                json.dump(graph, graph_file)

            # increment counter
            counter += 1


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
