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


def analyze_graph(vertices):
    # initialize return set of characters
    alphabet = set()

    # process each vertex
    for vertex in vertices:

        # get string value for analysis
        value = vertex['actualValue']

        # extract and store control characters
        log.debug('*** Vertex {0:4d} ***'.format(vertex['id']))
        log.debug('String Value : "{0}"'.format(value))

        # process each char and its index
        for i, c in enumerate(value):
            log.debug('Index {0:3d}: \'{1}\''.format(i, c))

    # return alphabet set
    return alphabet


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


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
