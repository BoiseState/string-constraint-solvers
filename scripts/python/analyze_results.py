#!/usr/bin/env python

import argparse
import fnmatch
import json
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

        # initialize reporter choice
        if options.mc_reporter:
            self.reporter = 'model-count'
        if options.sat_reporter:
            self.reporter = 'sat'


def get_options(arguments):
    # process command line args
    parser = argparse.ArgumentParser(prog=__doc__,
                                     description='Analyze results csv files '
                                                 'for each different '
                                                 'automaton model version.')

    parser.add_argument('-d',
                        '--debug',
                        help='Display debug messages for this script.',
                        action="store_true")

    parser.add_argument('-f',
                        '--result-files',
                        default='*',
                        help='A Unix shell-style pattern which is used to '
                             'match a set of result files.')

    # reporter argument group
    reporters = parser.add_mutually_exclusive_group(required=True)

    reporters.add_argument('-m',
                           '--mc-reporter',
                           help='Analyze result files from the model count '
                                'reporter.',
                           action='store_true')

    reporters.add_argument('-s',
                           '--sat-reporter',
                           help='Analyze result files from the sat reporter.',
                           action='store_true')

    return Settings(parser.parse_args(arguments))


def get_result_files(dir_path, file_pattern):
    # initialize result file set as dictionary
    files = dict()

    # for all items in reporter results directory
    for f in os.listdir(dir_path):
        # if item is a file and matches the file pattern
        file_path = os.path.join(dir_path, f)
        if os.path.isfile(file_path) and fnmatch.fnmatch(f, file_pattern):
            # add file to file set
            files[f] = file_path
            log.debug('included graph file: %s/%s', dir_path, f)

    # return file set
    return files


def get_result_file_sets(settings):
    # initialize list of result file sets
    file_sets = list()

    # get correct reporter result directory
    result_dir = os.path.join(project_dir, 'results', settings.reporter)

    # for all items in reporter results directory
    for d in os.listdir(result_dir):
        # if item is a directory
        if os.path.isdir(os.path.join(result_dir, d)):
            # get file set
            log.debug('getting result files for %s solver', d)
            result_files = get_result_files(os.path.join(result_dir, d),
                                            settings.result_file_pattern)

            # add file set to result file list
            file_sets.append(result_files)

    # return result file sets
    return file_sets


def analyze_result_sets(result_file_sets):
    # get set of all common file names in each file set
    file_name_set = sorted(
        reduce(lambda x, y: x.intersection(y.keys()), result_file_sets[1:],
               set(result_file_sets[0].keys())))
    log.debug('file name set: %s', file_name_set)


def main(arguments):
    # get option from arguments
    settings = get_options(arguments)

    # get result files to analyze
    result_file_sets = get_result_file_sets(settings)

    # analyze result sets
    analyze_result_sets(result_file_sets)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
