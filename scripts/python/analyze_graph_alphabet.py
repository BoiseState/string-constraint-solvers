#!/usr/bin/env python

import argparse
import json
import logging
import os
import sys

# configure logging
file_name = os.path.basename(__file__).replace('.py', '')
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.DEBUG)

formatter = logging.Formatter(
    '[%(name)s:%(levelname)s]: %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)

# initialize special character list
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


def replace_special_chars_to_display(string):

    # replace special chars
    for c in SPECIAL_CHARS:
        replacement = display_special_char(c)
        string = string.replace(c, replacement)
        # string = string.encode('unicode_escape')

    return string


def analyze_graph(vertices):
    # initialize return set of characters
    alphabet = set()

    # process each vertex
    for vertex in vertices:

        # get string value for analysis
        value = vertex['actualValue']

        # log debug information
        log.debug('*** Vertex {0:4d} ***'.format(vertex['id']))
        log.debug('String Value : "{0}"'.format(
            replace_special_chars_to_display(value)))

        # extract and store control characters
        for special_c in SPECIAL_CHARS:
            if special_c in value:
                log.debug('Special Character Found: {0}'.format(
                    display_special_char(special_c)))
                alphabet.add(special_c)
                value = value.replace(special_c,
                                      display_special_char(special_c))

        # process each char and its index
        for i, c in enumerate(value):
            # log.debug('Index {0:3d}: \'{1}\''.format(i, c))
            alphabet.add(c)

    # log alphabet information
    log.debug('Alphabet: {0}'.format(alphabet))

    # return alphabet set
    return alphabet


def create_alphabet_declaration(alphabet):

    # process each symbol
    # for sym in alphabet:

    return ''




def main(arguments):

    # process command line args
    parser = argparse.ArgumentParser(prog=__doc__,
                                     description='Analyze a string constraint '
                                                 'graph for its minimal '
                                                 'alphabet')

    parser.add_argument('graph_file',
                        type=argparse.FileType('r'),
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

    # process graph file
    with args.graph_file as graph_file:

        # load graph file data
        data = json.load(graph_file)

        # analyze graph vertices for alphabet
        alphabet = set()
        if isinstance(data, dict):
            alphabet = analyze_graph(data['vertices'])
        else:
            alphabet = analyze_graph(data)

        # create alphabet declaration from set
        declaration = create_alphabet_declaration(alphabet)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
