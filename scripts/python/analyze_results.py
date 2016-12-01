#!/usr/bin/env python

import argparse
import csv
import fnmatch
import json
import logging
import os
import sys

# set relevent path and file variables
import re

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

# initialize settings variable
SETTINGS = None


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


def compare_solvers(x, y):
    if x == y:
        return 0
    elif x == 'concrete':
        return -1
    elif y == 'concrete':
        return 1
    elif x == 'unbounded':
        return -1
    elif y == 'unbounded':
        return 1
    elif x == 'bounded':
        return -1
    elif y == 'bounded':
        return 1
    elif x == 'aggregate':
        return -1
    elif y == 'aggregate':
        return 1
    else:
        return 0


def compare_rows(x, y):
    # get both operations
    op_x = x.get('Operation')
    op_y = y.get('Operation')

    # extract operations using regular expression
    regex_pattern = '<S:\d+> = (?:<init>|\".*?\")' \
                    '(?: -> <S:\d+>.(\w+\(.*?\)))?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\)))?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\)))?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\)))?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\)))?'
    x_match = re.match(regex_pattern, op_x)
    y_match = re.match(regex_pattern, op_y)

    if x_match and y_match:
        x_op_count = len(x_match.groups()) - 1
        y_op_count = len(y_match.groups()) - 1
        for i in range(1, ):
            x_group = x_match.group(i)
            y_group = y_match.group(i)
            op_diff = cmp(x_group, y_group)

            if op_diff != 0:
                return op_diff
            elif y_op_count > x_op_count == i + 1:
                return -1
            elif i + 1 < y_op_count < x_op_count:
                return 1

    # return comparision of operation strings
    return cmp(op_x, op_y)


def get_operations(x):
    ops_list = list()

    # extract operations using regular expression
    regex_pattern = '<S:\d+> = (?:<init>|\".*?\")' \
                    '(?: -> <S:\d+>.(\w+)\(.*?\))?' \
                    '(?: -> <S:\d+>.(\w+)\(.*?\))?' \
                    '(?: -> <S:\d+>.(\w+)\(.*?\))?' \
                    '(?: -> <S:\d+>.(\w+)\(.*?\))?' \
                    '(?: -> <S:\d+>.(\w+)\(.*?\))?'
    x_match = re.match(regex_pattern, x)

    if x_match:
        for i in range(1, len(x_match.groups()) - 1):
            ops_list.append(x_match.group(i))

    # return comparision of operation strings
    return ops_list


def set_options(arguments):
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

    # set settings variable from parsed options
    global SETTINGS
    SETTINGS = Settings(parser.parse_args(arguments))


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


def get_result_file_sets():
    # initialize list of result file sets
    solver_file_sets = dict()

    # get correct reporter result directory
    result_dir = os.path.join(project_dir, 'results', SETTINGS.reporter)

    # for all items in reporter results directory
    for d in os.listdir(result_dir):
        # if item is a directory and not analysis
        if os.path.isdir(os.path.join(result_dir, d)) and d != 'analysis':
            # get file set
            log.debug('getting result files for %s solver', d)
            result_files = get_result_files(os.path.join(result_dir, d),
                                            SETTINGS.result_file_pattern)

            # add file set to result file map
            solver_file_sets[d] = result_files

    # initialize result file sets dictionary
    result_file_sets = dict()

    # for each solver
    for solver in solver_file_sets.keys():
        solver_files = solver_file_sets.get(solver)
        # for each file name in solver files
        for f_name in solver_files.keys():
            # get file set from result file sets
            file_map = result_file_sets.get(f_name)
            # if file map does not exist
            if file_map is None:
                # create new file map and add to result file sets
                file_map = dict()
                result_file_sets[f_name] = file_map
            # add file path to file map
            file_map[solver] = solver_files.get(f_name)

    # return result file sets
    return result_file_sets


def get_data_map_from_csv_files(f_name, file_set):
    # initialize data map
    data_map = dict()
    # for each solver
    for solver in file_set.keys():
        log.debug('loading csv data for solver %s and file %s', solver, f_name)
        # get file path from file set
        file_path = file_set.get(solver)
        # initialize row map
        row_map = dict()
        # open csv file for reading
        with open(file_path, 'r') as csv_file:
            reader = csv.DictReader(csv_file, delimiter='\t')
            # for each row in file
            for row in reader:
                # map row to row id
                row_id = row.get('ID')
                row_map[row_id] = row
        # add row map to data map dictionary
        data_map[solver] = row_map

    # return data map
    return data_map


def verify_data(data_map, solvers, f_name):
    other_solvers = list(solvers)
    other_solvers.remove('concrete')

    # for each operation id
    for op_id in data_map.get(next(iter(solvers))).keys():
        # get operations
        ops = get_operations(data_map.get('concrete')
                             .get(op_id)
                             .get('PREV OPS'))

        delete = ['delete',
                  'deleteCharAt']
        non_injective = ['replace',
                         'setCharAt',
                         'subSequence',
                         'substring',
                         'toLowerCase',
                         'toUpperCase',
                         'trim']
        if set(ops).intersection(delete):
            # get base values
            in_count = data_map.get('bounded').get(op_id).get('IN COUNT')
            t_count = data_map.get('bounded').get(op_id).get('T COUNT')
            f_count = data_map.get('bounded').get(op_id).get('F COUNT')
            for solver in set(other_solvers).intersection(
                    ['aggregate, weighted']):
                data_row = data_map.get(solver).get(op_id)
                if in_count != data_row.get('IN COUNT'):
                    log.error('Incoming MC from %s solver for constraint '
                              '%s is not equal to bounded MC', solver, op_id)

                if t_count != data_row.get('T COUNT'):
                    log.error('True branch MC from %s solver for constraint '
                              '%s is not equal to bounded MC', solver, op_id)

                if f_count != data_row.get('F COUNT'):
                    log.error('False branch MC from %s solver for constraint '
                              '%s is not equal to bounded MC', solver, op_id)
        elif set(ops).intersection(non_injective):
            # get base values
            in_count = data_map.get('unbounded').get(op_id).get('IN COUNT')
            t_count = data_map.get('unbounded').get(op_id).get('T COUNT')
            f_count = data_map.get('unbounded').get(op_id).get('F COUNT')
            for solver in set(other_solvers).intersection(
                    ['bounded, aggregate, weighted']):
                data_row = data_map.get(solver).get(op_id)
                if in_count != data_row.get('IN COUNT'):
                    log.error('Incoming MC from %s solver for constraint '
                              '%s is not equal to unbounded MC', solver, op_id)

                if t_count != data_row.get('T COUNT'):
                    log.error('True branch MC from %s solver for constraint '
                              '%s is not equal to unbounded MC', solver, op_id)

                if f_count != data_row.get('F COUNT'):
                    log.error('False branch MC from %s solver for constraint '
                              '%s is not equal to unbounded MC', solver, op_id)
        else:
            # get base values
            in_count = data_map.get('concrete').get(op_id).get('IN COUNT')
            t_count = data_map.get('concrete').get(op_id).get('T COUNT')
            f_count = data_map.get('concrete').get(op_id).get('F COUNT')
            for solver in other_solvers:
                data_row = data_map.get(solver).get(op_id)
                if in_count != data_row.get('IN COUNT'):
                    log.error('Incoming MC from %s solver for constraint '
                              '%s is not equal to concrete MC', solver, op_id)

                if t_count != data_row.get('T COUNT'):
                    log.error('True branch MC from %s solver for constraint '
                              '%s is not equal to concrete MC', solver, op_id)

                if f_count != data_row.get('F COUNT'):
                    log.error('False branch MC from %s solver for constraint '
                              '%s is not equal to concrete MC', solver, op_id)


def produce_output_data(data_map, solvers, f_name):
    # initialize csv field names
    field_names = list()
    field_names.append('Id')
    field_names.append('Operation')

    # for solver in solvers:
    for solver in sorted(solvers, cmp=compare_solvers):
        # add field names for each solver
        field_names.append('{0} In Count'.format(solver))
        field_names.append('{0} True Count'.format(solver))
        field_names.append('{0} False Count'.format(solver))

    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in data_map.get(next(iter(solvers))).keys():
        # initialize row
        row = dict()
        # add operation
        row['Operation'] = data_map.get('concrete').get(op_id).get('PREV OPS')
        row['Id'] = op_id
        for solver in solvers:
            data_row = data_map.get(solver).get(op_id)
            row['{0} In Count'.format(solver)] = data_row.get('IN COUNT')
            row['{0} True Count'.format(solver)] = data_row.get('T COUNT')
            row['{0} False Count'.format(solver)] = data_row.get('F COUNT')

        # add row to output rows
        output_rows.append(row)

    # sort output rows
    output_rows = sorted(output_rows, cmp=compare_rows)

    # get output file path
    csv_file_path = os.path.join(project_dir,
                                 'results',
                                 SETTINGS.reporter,
                                 'analysis',
                                 f_name)
    with open(csv_file_path, 'w') as csv_file:
        # creat csv writer with field names
        writer = csv.DictWriter(csv_file, field_names, delimiter='\t')
        # output header
        writer.writeheader()
        # output rows
        writer.writerows(output_rows)


def analyze_results(f_name, file_set):
    # get solvers from file_set
    solvers = list(file_set.keys())
    # get data map from files
    data_map = get_data_map_from_csv_files(f_name, file_set)
    # verify data
    verify_data(data_map, solvers, f_name)
    # output data
    produce_output_data(data_map, solvers, f_name)


def analyze_result_sets(result_file_sets):
    # for each file name in the result file sets
    for f_name in sorted(result_file_sets.keys()):
        # get file set
        file_set = result_file_sets.get(f_name)
        log.debug('getting result files for %s', f_name)
        # analyze file set
        analyze_results(f_name, file_set)


def main(arguments):
    # get option from arguments
    set_options(arguments)

    # get result files to analyze
    result_file_sets = get_result_file_sets()

    # ensure output directory exists
    output_dir = os.path.join(project_dir,
                              'results',
                              SETTINGS.reporter,
                              'analysis')
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    # analyze result sets
    analyze_result_sets(result_file_sets)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
