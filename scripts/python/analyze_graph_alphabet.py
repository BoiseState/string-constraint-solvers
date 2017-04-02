#!/usr/bin/env python

import argparse
import json
import logging
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

# initialize special character list
SPECIAL_CHARS = [u'\b', u'\f', u'\n', u'\r', u'\t', u'\'', u'\"', u'\\\\']

MAX_LENGTH = 0


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
    # initialize return set of characters
    alphabet = set()

    # process each vertex
    for vertex in vertices:
        log.debug('*** Vertex {0:4d} ***'.format(vertex['id']))

        # get string value for analysis
        value = vertex['actualValue']

        # determine if value is None/null
        if value is None:
            log.debug(u'String Value : None')
        else:
            log.debug(u'String Value : "{0}"'.format(
                replace_special_chars_to_display(value)))

            # extract and store control characters
            for special_c in SPECIAL_CHARS:
                if special_c in value:
                    log.debug(u'Special Character Found: {0}'.format(
                        display_special_char(special_c)))
                    if special_c == '\\\\':
                        alphabet.add(92)
                    else:
                        alphabet.add(ord(special_c))
                    value = value.replace(special_c,
                                          display_special_char(special_c))

            # process each char and its index
            for i, c in enumerate(value):
                log.debug(u'Index {0:3d}: \'{1}\''.format(i, c))
                alphabet.add(ord(c))

                # ensure lower case letter in alphabet also
                if 'A' <= c <= 'Z':
                    alphabet.add(ord(c.lower()))

                # ensure lower case letter in alphabet also
                if 'a' <= c <= 'z':
                    alphabet.add(ord(c.upper()))

            # check max length
            if len(value) > MAX_LENGTH:
                global MAX_LENGTH
                MAX_LENGTH = len(value)

    # log alphabet information
    for sym in alphabet:
        dc = display_special_char(unichr(sym))
        log.debug(u'Alphabet Symbol: {0:d} = {1}'.format(sym, dc))

    # return alphabet set
    return sorted(alphabet)


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

    # analyze graph vertices for alphabet
    alphabet = analyze_graph(vertices)

    # create alphabet declaration from set
    declaration = create_alphabet_declaration(alphabet)

    # get graph file structure
    graph = {
        'vertices': vertices,
        'alphabet': {
            'declaration': declaration,
            'size': len(alphabet),
            'max': MAX_LENGTH
        }
    }

    # write out update graph file
    with open(graph_path, 'w') as graph_file:
        json.dump(graph, graph_file)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
