#! /usr/bin/env python
import argparse

import logging
import os
import sys

# set relevent path and file variables
file_name = os.path.basename(__file__).replace('.py', '')
project_dir = '{0}/../..'.format(os.path.dirname(__file__))
project_dir = os.path.normpath(project_dir)

# configure logging
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.ERROR)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s] %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)


class Settings:
    def __init__(self, options):
        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)

        # initialize result file pattern
        self.result_file_pattern = options.result_files


def set_options(arguments):
    # process command line args
    gather_parser = argparse.ArgumentParser(prog=__doc__,
                                            description='Analyze results.')

    gather_parser.add_argument('-d',
                               '--debug',
                               help='Display debug messages for this script.',
                               action="store_true")

    gather_parser.add_argument('-f',
                               '--result-files',
                               default='*',
                               help='A Unix shell-style pattern which is '
                                    'used to '
                                    'match a set of result files.')

    return Settings(gather_parser.parse_args(arguments))


def main(arguments):
    settings = set_options(arguments)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
