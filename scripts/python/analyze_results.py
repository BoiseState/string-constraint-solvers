#! /usr/bin/env python
import fnmatch

import argparse
import csv
import logging
import os
import sys

# set relevent path and file variables
file_name = os.path.basename(__file__).replace('.py', '')
project_dir = '{0}/../..'.format(os.path.dirname(__file__))
project_dir = os.path.normpath(project_dir)
data_dir = os.path.join(project_dir, 'results', 'model-count', 'all')

# configure logging
log = logging.getLogger(file_name)
log.setLevel(logging.ERROR)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.ERROR)

formatter = logging.Formatter(
    u'[%(name)s:%(levelname)s] %(message)s')
ch.setFormatter(formatter)

log.addHandler(ch)

# globals
GLOB = dict()


class Settings:
    def __init__(self, options):
        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)

        # initialize result file pattern
        self.file_pattern = options.result_files


def set_options(arguments):
    # process command line args
    gather_parser = argparse.ArgumentParser(prog=__doc__,
                                            description='Analyze results.')

    gather_parser.add_argument('-d',
                               '--debug',
                               help='Display debug messages for this script.',
                               action="store_true")

    gather_parser.add_argument('-f',
                               '--data-files',
                               default='*',
                               help='A Unix shell-style pattern which is '
                                    'used to '
                                    'match a set of result files.')

    GLOB['Settings'] = Settings(gather_parser.parse_args(arguments))


def read_csv_data(file_path):
    # initialize rows list
    rows = list()

    # read csv rows
    with open(file_path, 'r') as csv_file:
        reader = csv.DictReader(csv_file,
                                delimiter='\t',
                                quoting=csv.QUOTE_NONE,
                                quotechar='|',
                                lineterminator='\n')
        for row in reader:
            rows.append(row)

    # return rows data
    return rows


def read_data_files(file_pattern):
    # initialize return dictionary
    return_data = list()

    # check for matching files and read csv data
    for f in os.listdir(data_dir):
        test_path = os.path.join(data_dir, f)
        if os.path.isfiles(test_path) and fnmatch.fnmatch(f, file_pattern):
            return_data[f] = read_csv_data(test_path)

    return return_data


def get_data():
    # get lists of data files
    mc_data = read_data_files('mc-' + GLOB['Settings'].file_pattern)
    mc_time_data = read_data_files('mc-time' + GLOB['Settings'].file_pattern)
    op_time_data = read_data_files('op-time' + GLOB['Settings'].file_pattern)

    # return data
    return mc_data, mc_time_data, op_time_data


def perform_analysis(mcs, mc_times, op_times):
    pass


def main(arguments):
    # set options from args
    set_options(arguments)

    # read data
    mc_data, mc_time_data, op_time_data = get_data()

    # perform analysis
    perform_analysis(mc_data, mc_time_data, op_time_data)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
