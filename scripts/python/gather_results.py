#!/usr/bin/env python

import argparse
import csv
import fnmatch
import logging
# set relevent path and file variables
import re
import sys

import os

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

# initialize settings and matricies variables
SETTINGS = None
VERIFICATION_MATRICIES = {
    'concat': (('-', '-', '>', '>', '='),
               ('-', '-', '>', '>', '-'),
               ('<', '<', '-', '=', '<'),
               ('<', '<', '=', '-', '<'),
               ('=', '-', '>', '>', '-')),
    'delete': (('-', '>', '>', '>', '='),
               ('<', '-', '=', '<', '<'),
               ('<', '=', '-', '<', '<'),
               ('<', '>', '>', '-', '<'),
               ('=', '>', '>', '>', '-')),
    'insert': (('-', '-', '>', '>', '='),
               ('-', '-', '>', '>', '-'),
               ('<', '<', '-', '=', '<'),
               ('<', '<', '=', '-', '<'),
               ('=', '-', '>', '>', '-')),
    'injective': (('-', '=', '=', '=', '='),
                  ('=', '-', '=', '=', '='),
                  ('=', '=', '-', '=', '='),
                  ('=', '=', '=', '-', '='),
                  ('=', '=', '=', '=', '-')),
    'replace': (('-', '>', '>', '>', '='),
                ('<', '-', '=', '=', '<'),
                ('<', '=', '-', '=', '<'),
                ('<', '=', '=', '-', '<'),
                ('=', '>', '>', '>', '-')),
    'setCharAt': (('-', '>', '>', '>', '='),
                  ('<', '-', '=', '=', '<'),
                  ('<', '=', '-', '=', '<'),
                  ('<', '=', '=', '-', '<'),
                  ('=', '>', '>', '>', '-')),
    'setLength': (('-', '>', '>', '>', '='),
                  ('<', '-', '=', '=', '<'),
                  ('<', '=', '-', '<', '<'),
                  ('<', '=', '>', '-', '<'),
                  ('=', '>', '>', '>', '-')),
    'substring': (('-', '-', '>', '>', '='),
                  ('-', '-', '>', '-', '-'),
                  ('<', '<', '-', '<', '<'),
                  ('<', '-', '>', '-', '<'),
                  ('=', '-', '>', '>', '-')),
    'trim': (('-', '>', '>', '>', '='),
             ('<', '-', '=', '<', '<'),
             ('<', '=', '-', '<', '<'),
             ('<', '>', '>', '-', '<'),
             ('=', '>', '>', '>', '-'))
}

SOLVER_ORDER = {
    'concrete': 1,
    'unbounded': 2,
    'bounded': 3,
    'aggregate': 4,
    'weighted': 5
}


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
    if x in SOLVER_ORDER.keys() and y in SOLVER_ORDER.keys():
        return SOLVER_ORDER[x] - SOLVER_ORDER[y]
    elif x in SOLVER_ORDER.keys():
        return SOLVER_ORDER[x] - 6
    elif y in SOLVER_ORDER.keys():
        return 6 - SOLVER_ORDER[y]
    else:
        return 0


def compare_rows(x, y):
    # get both operations
    op_x = x.get('Operation')
    op_y = y.get('Operation')

    # extract operations using regular expression
    regex_pattern = '<S:\d+> = (?:<init>|\".*?\")\{\d\}' \
                    '(?: -> <S:\d+>.(\w+\(.*?\))\{\d\})?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\))\{\d\})?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\))\{\d\})?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\))\{\d\})?' \
                    '(?: -> <S:\d+>.(\w+\(.*?\))\{\d\})?'
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


def get_last_operation(x):
    # extract operations using regular expression
    regex_init = '^<S:\d+> = <init>$'
    regex_op = '^<S:\d+>.(?P<op>\w+)\(.*\)$'

    op_str = x.split('->')
    if re.match(regex_init, op_str.strip()):
        return 'init'
    op_match = re.match(regex_op, op_str.strip())
    return op_match.group['op']


def set_options(arguments):
    # process command line args
    gather_parser = argparse.ArgumentParser(prog=__doc__,
                                            description='Gather results into '
                                                        'csv files for each '
                                                        'different automaton '
                                                        'model version.')

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

    # reporter argument group
    reporters = gather_parser.add_mutually_exclusive_group(required=True)

    reporters.add_argument('-m',
                           '--mc-reporter',
                           help='Gather result files from the model count '
                                'reporter.',
                           action='store_true')

    reporters.add_argument('-s',
                           '--sat-reporter',
                           help='Gather result files from the sat reporter.',
                           action='store_true')

    # set settings variable from parsed options
    global SETTINGS
    SETTINGS = Settings(gather_parser.parse_args(arguments))


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


def verify_matrix(op, data_map, solvers, op_id):
    pass


def verify_data(data_map, solvers):
    other_solvers = list(solvers)
    other_solvers.remove('concrete')

    # for each operation id
    for op_id in data_map.get('concrete').keys():
        # get operations
        op = get_last_operation(data_map.get('concrete')
                                .get(op_id)
                                .get('PREV OPS'))

        concat = ['append', 'concat']
        delete = ['delete', 'deleteCharAt']
        injective = ['reverse', 'toLowerCase', 'toUpperCase']
        substring = ['substring', 'subsequence']

        if op in concat:
            verify_matrix('concat', data_map, solvers, op_id)
        elif op in delete:
            verify_matrix('delete', data_map, solvers, op_id)
        elif op == 'insert':
            verify_matrix('insert', data_map, solvers, op_id)
        elif op in injective:
            verify_matrix('injective', data_map, solvers, op_id)
        elif op == 'replace':
            verify_matrix('replace', data_map, solvers, op_id)
        elif op == 'setCharAt':
            verify_matrix('setCharAt', data_map, solvers, op_id)
        elif op == 'setLength':
            verify_matrix('setLength', data_map, solvers, op_id)
        elif op in substring:
            verify_matrix('substring', data_map, solvers, op_id)
        elif op == 'trim':
            verify_matrix('trim', data_map, solvers, op_id)


def produce_output_data(data_map, solvers, f_name):
    # initialize csv field names
    field_names = list()
    field_names.append('Id')
    field_names.append('Operation')

    # set field names for each solver
    sorted_solvers = sorted(solvers, cmp=compare_solvers)
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' IN MC')
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' T MC')
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' F MC')
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' Time')
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' T Time')
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' F Time')

    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in data_map.get(next(iter(solvers))).keys():
        # initialize row
        row = dict()
        # add operation
        row['Operation'] = data_map.get('unbounded').get(op_id).get('PREV OPS')
        row['Id'] = op_id
        for solver in solvers:
            data_row = data_map.get(solver).get(op_id)
            prefix = solver.upper()[0]
            row[prefix + ' IN MC'] = data_row.get('IN COUNT')
            row[prefix + ' T MC'] = data_row.get('T COUNT')
            row[prefix + ' F MC'] = data_row.get('F COUNT')
            row[prefix + ' Time'] = data_row.get('ACC TIME')
            row[prefix + ' T Time'] = data_row.get('T TIME')
            row[prefix + ' F Time'] = data_row.get('F TIME')

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
    log.info('Outputting data to: %s', csv_file_path)
    with open(csv_file_path, 'w') as csv_file:
        # creat csv writer with field names
        writer = csv.DictWriter(csv_file, field_names, delimiter='\t')
        # output header
        writer.writeheader()
        # output rows
        writer.writerows(output_rows)


def gather_results(f_name, file_set):
    # get solvers from file_set
    solvers = list(file_set.keys())
    # get data map from files
    data_map = get_data_map_from_csv_files(f_name, file_set)
    # verify data
    # verify_data(data_map, solvers, f_name)
    # output data
    produce_output_data(data_map, solvers, f_name)


def gather_result_sets(result_file_sets):
    # for each file name in the result file sets
    for f_name in sorted(result_file_sets.keys()):
        # get file set
        file_set = result_file_sets.get(f_name)
        log.debug('getting result files for %s', f_name)
        # gather file set
        gather_results(f_name, file_set)


def main(arguments):
    # get option from arguments
    set_options(arguments)

    # get result files to gather
    result_file_sets = get_result_file_sets()

    # ensure output directory exists
    output_dir = os.path.join(project_dir,
                              'results',
                              SETTINGS.reporter,
                              'analysis')
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    # gather result sets
    gather_result_sets(result_file_sets)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
