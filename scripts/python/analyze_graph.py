#!/usr/bin/env python

import argparse
import json
import logging
import re

import os
import sys

file_name = os.path.basename(__file__).replace('.py', '')
project_dir = '{0}/../..'.format(os.path.dirname(__file__))
project_dir = os.path.normpath(project_dir)

# configure logging
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.DEBUG)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s]: %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)

# Globals
PREDICATES = (
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
)

SPECIAL_CHARS = [u'\b', u'\f', u'\n', u'\r', u'\t', u'\'', u'\"', u'\\\\']


def display_special_char(character):
    # return display equivalent for each character
    if character == '\b':
        return '\\b'
    elif character == '\f':
        return '\\f'
    elif character == '\n':
        return '\\n'
    elif character == '\r':
        return '\\r'
    elif character == '\t':
        return '\\t'
    elif character == '\'':
        return '\\\''
    elif character == '\"':
        return '\\"'
    elif character == '\\\\':
        return '\\\\\\\\'
    else:
        return character


def replace_special_chars_to_display(string):
    # replace special chars
    for c in SPECIAL_CHARS:
        replacement = display_special_char(c)
        string = string.replace(c, replacement)

    return string


def analyze_graph(vertices):
    # initialize return structures
    alphabet = set()
    max_length = 0
    concrete_strings = set()
    unknown_strings = 0

    # process each vertex
    for vertex in vertices:
        # log.debug('*** Vertex {0:4d} ***'.format(vertex['id']))

        # get string value for analysis
        actual_value = vertex['actualValue']
        value = vertex['value']

        # if root node
        if len(vertex['incomingEdges']) == 0:
            # if unknown string
            if value.startswith('r') or value.startswith('$r'):
                unknown_strings += 1
            else:
                concrete_strings.add(actual_value)

        # determine if value is None/null
        if actual_value is not None:
            # log.debug(u'String Value : "{0}"'.format(
            #     replace_special_chars_to_display(actual_value)))

            # extract and store control characters
            for special_c in SPECIAL_CHARS:
                if special_c in actual_value:
                    # log.debug(u'Special Character Found: {0}'.format(
                    #     display_special_char(special_c)))
                    if special_c == '\\\\':
                        alphabet.add(92)
                    else:
                        alphabet.add(ord(special_c))
                    actual_value = \
                        actual_value.replace(special_c,
                                             display_special_char(special_c))

            # process each char and its index
            for i, c in enumerate(actual_value):
                # log.debug(u'Index {0:3d}: \'{1}\''.format(i, c))
                alphabet.add(ord(c))

                # ensure lower case letter in alphabet also
                if 'A' <= c <= 'Z':
                    alphabet.add(ord(c.lower()))

                # ensure lower case letter in alphabet also
                if 'a' <= c <= 'z':
                    alphabet.add(ord(c.upper()))

            # check max length
            if len(actual_value) > max_length:
                max_length = len(actual_value)
        # else:
        #     log.debug(u'String Value : None')

    # log alphabet information
    for sym in alphabet:
        dc = display_special_char(unichr(sym))
        log.debug(u'Alphabet Symbol: {0:d} = {1}'.format(sym, dc))

    # return alphabet set
    return sorted(alphabet), max_length, concrete_strings, unknown_strings


def create_alphabet_declaration(alphabet):
    # initialize variables
    ranges = list()
    prev = alphabet.pop(0)
    start = prev

    # process each symbol
    for sym in alphabet:

        # if starting new range
        if sym - prev != 1:
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
            range_strings.append(unichr(s))
        else:
            range_string = u'{0}-{1}'.format(unichr(s), unichr(e))
            range_strings.append(range_string)

    # print debug information
    for s in range_strings:
        log.debug(u'Alphabet Sub-range: {0}'.format(s))

    # return single string of ranges
    return ','.join(range_strings)


def add_subgraph_to_vertices(v, sg_vertices, out_edges, v_map, in_edge=False):
        sg_vertices.append(v)
        edges = list()
        if v.get('id') in out_edges:
            for o_id, o_type in out_edges.get(v.get('id')):
                edges.append((o_id, o_type, False))
        for e in v.get('incomingEdges'):
            edges.append((e.get('source'), e.get('type'), True))
        for dest_id, dest_type, is_in_edge in edges:
            d_v = v_map.get(dest_id)
            if d_v not in sg_vertices and (not in_edge or (v.get('value').split('!:!')[0] == 'r1' and d_v.get('value').startswith('contains') and dest_id - 2 == v.get('id'))):
                add_subgraph_to_vertices(d_v, sg_vertices, out_edges, v_map,
                                         is_in_edge)


def split_graph(vertices):
    vertices_list = list()

    in_edges = dict()
    out_edges = dict()
    v_map = dict()

    # get graph data
    for pred_v in vertices:
        v_map[pred_v.get('id')] = pred_v
        for e in pred_v.get('incomingEdges'):
            source_id = e.get('source')
            # add out edges
            out_set = out_edges.get(source_id)
            if out_set is None:
                out_set = set()
                out_edges[source_id] = out_set
            out_set.add((pred_v.get('id'), e.get('type')))

    def is_root(vertex):
        return len(vertex.get('incomingEdges')) == 0 \
               and all(y == 't' for x, y in out_edges.get(vertex.get('id')))

    # get root vertex
    cur_v = None
    not_root = list()
    queue = list()
    queue.append(vertices[0])
    while len(queue) > 0:
        cur_v = queue.pop(0)
        if is_root(cur_v):
            break
        not_root.append(cur_v)
        if len(cur_v.get('incomingEdges')) > 0:
            # get base incoming vertex
            new_v = None
            for e in cur_v.get('incomingEdges'):
                in_v = v_map.get(e.get('source'))
                if in_v not in not_root and in_v not in queue:
                    queue.append(in_v)
        else:
            # get base outgoing vertex
            for dest_id, dest_type in out_edges.get(cur_v.get('id')):
                dest_v = v_map.get(dest_id)
                if dest_v not in not_root and dest_v not in queue:
                    queue.append(dest_v)
    root_v = cur_v
    log.debug('root found: %s', root_v)

    # split vertices
    non_split_vertices = list()
    non_split_vertices.append(root_v)
    complex_vertex = None
    predicates = list()
    for dest_id, dest_type in out_edges.get(root_v.get('id')):
        dest_v = v_map.get(dest_id)
        # check for complex contains creation predicate
        if dest_v.get('value').startswith('contains') \
                and dest_id == root_v.get('id') + 2:
            complex_vertex = dest_v
        elif dest_v.get('value').split('!:!')[0] in PREDICATES:
            add_subgraph_to_vertices(dest_v, predicates, out_edges,
                                     v_map, non_split_vertices)
        else:  # split into subgraphs
            for child_id, child_type in out_edges.get(dest_id):
                child_v = v_map.get(child_id)
                sg_vertices = list()
                sg_vertices.append(dest_v)
                for e in filter(lambda x: x.get('source') != root_v.get('id'),
                                dest_v.get('incomingEdges')):
                    add_subgraph_to_vertices(v_map.get(e.get('source')),
                                             sg_vertices, out_edges, v_map,
                                             True)
                add_subgraph_to_vertices(child_v, sg_vertices, out_edges, v_map)
                vertices_list.append(sg_vertices)

    # add any complex vertices to unsplit vertices
    if complex_vertex is not None:
        non_split_vertices.append(complex_vertex)
        non_split_vertices.append(v_map.get(root_v.get('id') + 1))

    # add non split vertices to each subgraph
    for vl in vertices_list:
        to_remove = [x for x in vl if x in non_split_vertices]
        for item in to_remove:
            vl.remove(item)
        vl.extend(non_split_vertices)

    return vertices_list


def main(arguments):
    # process command line args
    parser = argparse.ArgumentParser(prog=__doc__,
                                     description='Analyze a string constraint '
                                                 'graph for its minimal '
                                                 'alphabet')

    parser.add_argument('graph_file',
                        help="The json file which contains the string "
                             "constraint graph gathered from dynamic symbolic "
                             "execution.",
                        metavar="<Graph File>")

    parser.add_argument('-d',
                        '--debug',
                        help="Display debug messages for script.",
                        action="store_true")

    parser.add_argument('-s',
                        '--split',
                        help="Split graph into sub graphs.",
                        action="store_true")

    args = parser.parse_args(arguments)

    # check debug flag
    if args.debug:
        log.setLevel(logging.DEBUG)
        ch.setLevel(logging.DEBUG)
        log.debug('Args: {0}'.format(args))

    # process graph file
    graph_path = os.path.join(project_dir, 'graphs', args.graph_file)
    with open(graph_path, 'r') as graph_file:

        # load graph file data
        data = json.load(graph_file)

        # get vertices from graph file
        if isinstance(data, dict):
            vertices = data['vertices']
        else:
            vertices = data

    # initialize graph and vertices lists
    graphs = list()
    vertices_list = list()

    # split graph if required
    if args.split:
        vertices_list = split_graph(vertices)
    else:
        vertices_list.append(vertices)

    for i, vertex_list in enumerate(vertices_list):

        # analyze graph vertices for alphabet
        alphabet, max_length, c_strings, u_strings = analyze_graph(vertex_list)

        # create alphabet declaration from set
        declaration = create_alphabet_declaration(alphabet)

        # get graph file structure
        graph = {
            'vertices': vertex_list,
            'alphabet': {
                'concrete_strings': len(c_strings),
                'declaration': declaration,
                'max': max_length,
                'size': len(alphabet),
                'unknown_strings': u_strings
            }
        }
        g_path = graph_path
        if len(vertices_list) > 1:
            graph_filename = os.path.basename(graph_path).split('.')[0]
            graph_dir = os.path.dirname(graph_path)
            format_string = '{0}_{1:02d}.json'
            if len(vertices_list) > 99:
                format_string = '{0}_{1:03d}.json'
            if len(vertices_list) > 999:
                format_string = '{0}_{1:04d}.json'
            graph_file = format_string.format(graph_filename, i)
            g_path = os.path.join(graph_dir, graph_file)
        graphs.append((graph, g_path))

    # write out updated graph files
    for g, g_path in graphs:
        log.debug('Writing to graph: %s', g_path)
        with open(g_path, 'w') as graph_file:
            json.dump(g, graph_file)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
