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
    u'[%(name)s:%(levelname)s]: %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)


class Settings:
    def __init__(self, options):
        self.result_file_pattern = options.result_files


def get_options(arguments):
    # process command line args
    parser = argparse.ArgumentParser(prog=__doc__,
                                     description='Analyze results csv files '
                                                 'for each different '
                                                 'automaton model version.')

    parser.add_argument('-f',
                        '--result-files',
                        default='.*',
                        help='A regular expression which is used to match the '
                             'result file names.')

    return Settings(parser.parse_args(arguments))


def main(arguments):
    # get option from arguments
    settings = get_options(arguments)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
