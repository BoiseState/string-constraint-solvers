#!/usr/bin/env python

import argparse
import csv
import fnmatch
import logging
import os
import platform
import re
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

OP_GROUPS = {
    'Injective': [
        'init',
        'reverse',
        'toString',
        'trimToSize'
    ],
    '+ Struct Alt': [
        'append',
        'concat',
        'insert'
    ],
    '- Struct Alt': [
        'delete',
        'deleteCharAt',
        'setLength',
        'subSequence',
        'substring',
        'trim'
    ],
    'Substitution': [
        'replace',
        'setCharAt',
        'toLowerCase',
        'toUpperCase'
    ],
    'Contains Predicate': [
        'contains',
        'endsWith',
        'startsWith'
    ],
    'Equals Predicate': [
        'equals',
        'contentEquals',
        'equalsIgnoreCase',
        'isEmpty'
    ]
}
INPUT_TYPES = (
    'Concrete',
    'Uniform',
    'Non-Uniform'
)


OP_TYPES = (
    ('Injective', 'Concrete', ''),
    ('Injective', 'Uniform', ''),
    ('Injective', 'Non-Uniform', ''),
    ('+ Struct Alt', 'Concrete', 'Concrete'),
    ('+ Struct Alt', 'Concrete', 'Uniform'),
    ('+ Struct Alt', 'Concrete', 'Non-Uniform'),
    ('+ Struct Alt', 'Uniform', 'Concrete'),
    ('+ Struct Alt', 'Uniform', 'Uniform'),
    ('+ Struct Alt', 'Uniform', 'Non-Uniform'),
    ('+ Struct Alt', 'Non-Uniform', 'Concrete'),
    ('+ Struct Alt', 'Non-Uniform', 'Uniform'),
    ('+ Struct Alt', 'Non-Uniform', 'Non-Uniform'),
    ('- Struct Alt', 'Concrete', ''),
    ('- Struct Alt', 'Uniform', ''),
    ('- Struct Alt', 'Non-Uniform', ''),
    ('Substitution', 'Concrete', ''),
    ('Substitution', 'Uniform', ''),
    ('Substitution', 'Non-Uniform', ''),
)

REGEX = dict()
REGEX['init_known'] = re.compile('^<S:(?P<id>\d+)> = \\\\".*\\\\"'
                                 '{(?P<time>\d+)}$')
REGEX['init_unknown'] = re.compile('^<S:(?P<id>\d+)> = <init>'
                                   '{(?P<time>\d+)}$')
REGEX['op'] = re.compile('^\[(?P<const_id>\d+)\]<S:(?P<base_id>\d+)>.'
                         '(?P<op>\w+)\((?P<args>.*)\){(?P<time>\d+)}$')
REGEX['arg_str_known'] = re.compile('^\\\\"(?P<string>\w*)\\\\"$')
REGEX['arg_str_unknown'] = re.compile('^<(S|CS):(?P<id>\d+)>$')
REGEX['arg_char'] = re.compile('^\'(?P<char>\w)\'$')
REGEX['arg_num'] = re.compile('^(?P<num>\d+)$')


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


class Operation:
    def __init__(self, const_id, op, time, input_type, base_id=None, args=None):
        self.op_id = const_id
        self.time = time
        self.input_type = input_type
        self.op = op.strip()

        self.base_id = base_id
        if base_id is None:
            self.base_id = const_id

        self.args = list()
        if args is not None:
            self.args.extend(args)

        self.op_group = ''
        for group in OP_GROUPS.keys():
            if self.op in OP_GROUPS[group]:
                self.op_group = group


class OperationArgument:
    def __init__(self, arg_type, arg_id=None, value=None):
        self.arg_id = arg_id
        self.arg_type = arg_type
        self.value = value

    def get_string(self):
        val = self.value if self.value is not None else self.arg_id
        return '{0}:{1}'.format(self.arg_type, val)


def get_solver_key(x):
    if x in SOLVER_ORDER.keys():
        return SOLVER_ORDER[x]
    else:
        return 6


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


def get_operations(x):
    # initialize operations list
    ops_list = list()

    input_type = 'Concrete'
    op_strings = x.split('->')
    # parse initial operation string
    match_init = REGEX['init_known'].match(op_strings[0].strip())
    if not match_init:
        match_init = REGEX['init_unknown'].match(op_strings[0].strip())
        input_type = 'Uniform'
    base_id = match_init.group('id')
    time = match_init.group('time')
    ops_list.append(Operation(base_id, 'init', time, input_type))

    # parse remaining operation strings
    for i, op_string in enumerate(op_strings[1:]):
        match_op = REGEX['op'].match(op_string.strip())
        const_id = match_op.group('const_id')
        base_id = match_op.group('base_id')
        time = match_op.group('time')
        op = match_op.group('op')
        args = match_op.group('args')
        arg_list = list()
        for arg in args.split(','):
            match = REGEX['arg_str_known'].match(arg.strip())
            if match:
                value = match.group('string')
                arg_list.append(OperationArgument('str', value=value))
                continue
            match = REGEX['arg_str_unknown'].match(arg.strip())
            if match:
                arg_id = match.group('id')
                arg_list.append(OperationArgument('str', arg_id=arg_id))
                continue
            match = REGEX['arg_char'].match(arg.strip())
            if match:
                value = match.group('char')
                arg_list.append(OperationArgument('char', value=value))
                continue
            match = REGEX['arg_num'].match(arg.strip())
            if match:
                value = match.group('num')
                arg_list.append(OperationArgument('num', value=value))

        if i == 0 and op == 'contains':
            input_type = 'Non-Uniform'

        ops_list.append(Operation(const_id,
                                  op,
                                  time,
                                  input_type,
                                  base_id,
                                  arg_list))

    return ops_list


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


def produce_mc_output_data(data_map, solvers, f_name):
    # initialize csv field names
    field_names = list()
    field_names.append('Id')
    field_names.append('Operation')

    # set field names for each solver
    sorted_solvers = sorted(solvers, key=get_solver_key)
    in_fields = list()
    t_fields = list()
    f_fields = list()
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        in_fields.append(prefix + ' IN MC')
        t_fields.append(prefix + ' T MC')
        f_fields.append(prefix + ' F MC')
    field_names.extend(in_fields)
    field_names.extend(t_fields)
    field_names.extend(f_fields)

    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in data_map.get(next(iter(solvers))).keys():
        # initialize row
        row = dict()
        # add operation
        operations = data_map.get('unbounded').get(op_id).get('PREV OPS')
        ops_list = get_operations(operations)
        # for op in ops_list:
        #     log.debug('*** operation %s ***', op_id)
        #     log.debug('operation %s - op: %s', op_id, op.op)
        #     log.debug('operation %s - time: %s', op_id, op.time)
        #     log.debug('operation %s - input_type: %s', op_id, op.input_type)
        #     log.debug('operation %s - base_id: %s', op_id, op.base_id)
        #     log.debug('operation %s - op_group: %s', op_id, op.op_group)
        #     for i, arg in enumerate(op.args):
        #         log.debug('operation %s - arg %d - arg_id: %s',
        #                   op_id,
        #                   i,
        #                   arg.arg_id)
        #         log.debug('operation %s - arg %d - arg_type: %s',
        #                   op_id,
        #                   i,
        #                   arg.arg_type)
        #         log.debug('operation %s - arg %d - arg_value: %s',
        #                   op_id,
        #                   i,
        #                   arg.value)

        operations = re.sub('{\d+}', '', operations)
        operations = re.sub('\[\d+\]', '', operations)
        operations = operations.replace('\\"', '"')
        row['Operation'] = operations
        row['Id'] = op_id
        for solver in solvers:
            data_row = data_map.get(solver).get(op_id)
            prefix = solver.upper()[0]
            row[prefix + ' IN MC'] = data_row.get('IN COUNT')
            row[prefix + ' T MC'] = data_row.get('T COUNT')
            row[prefix + ' F MC'] = data_row.get('F COUNT')

        # add row to output rows
        output_rows.append(row)

    # sort output rows
    output_rows = sorted(output_rows, key=lambda x: int(x.get('Id')))

    # get output file path
    csv_file_path = os.path.join(project_dir,
                                 'results',
                                 SETTINGS.reporter,
                                 'all',
                                 'mc-' + f_name)
    log.info('Outputting data to: %s', csv_file_path)
    with open(csv_file_path, 'w') as csv_file:
        # create csv writer with field names
        writer = csv.DictWriter(csv_file,
                                field_names,
                                delimiter='\t',
                                quoting=csv.QUOTE_NONE,
                                quotechar='|',
                                lineterminator='\n')
        # output header
        writer.writeheader()
        # output rows
        writer.writerows(output_rows)


def get_all_op_data(data_map, solvers):
    # initialize return data
    return_data = dict()

    # for each constraint id
    for const_id in data_map.get('unbounded').keys():
        # get ops info list for each solver from previous ops info
        ops_lists = dict()
        for solver in solvers:
            prev_ops = data_map.get(solver).get(const_id).get('PREV OPS')
            ops_lists[solver] = get_operations(prev_ops)

        # for each op info in unbounded ops list
        for op_info in ops_lists.get('unbounded'):
            if op_info.op_id in return_data:
                op_map = return_data.get(op_info)
                op_map['const_ids'].add(const_id)
            else:
                # add op info data to new op map
                op_map = dict()
                op_map['input_type'] = op_info.input_type
                op_map['op'] = op_info.op
                op_map['op_group'] = op_info.op_group
                op_map['base_id'] = op_info.base_id
                op_map['const_ids'] = set()
                op_map['const_ids'].add(const_id)

                # add args strings
                op_map['args'] = list()
                for arg in op_info.args:
                    op_map['args'].append(arg.get_string())

                # add solver time
                op_map['time'] = dict()
                for solver in solvers:
                    op_map['time'][solver] = ops_lists[solver].time

                # add op map to solver op data
                return_data[op_info.op_id] = op_map

    # return data
    return return_data


def produce_time_output_data(data_map, solvers, f_name):
    # get all op data
    op_data = get_all_op_data(data_map, solvers)

    # initialize csv field names
    field_names = list()
    field_names.append('Op Id')
    field_names.append('Const Ids')
    field_names.append('Op')
    field_names.append('Op Group')
    field_names.append('In Type')
    field_names.append('Args')
    field_names.append('Base Id')

    # set field names for each solver
    sorted_solvers = sorted(solvers, cmp=get_solver_key)
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' Op Time')

    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in op_data.keys():
        # get op map
        op_map = op_data[op_id]
        # initialize row
        row = dict()
        row['Op Id'] = op_id
        row['Const Ids'] = ', '.join(op_map.get('const_ids'))
        row['Op'] = op_map.get('op')
        row['Op Group'] = op_map.get('op_group')
        row['In Type'] = op_map.get('input_type')
        row['Base Id'] = op_map.get('base_id')
        row['Args'] = ', '.join(op_map.get('args'))
        for solver in solvers:
            prefix = solver.upper()[0]
            row[prefix + ' Op Time'] = op_map.get('time').get(solver)

        # add row to output rows
        output_rows.append(row)

    # sort output rows
    output_rows = sorted(output_rows, cmp=lambda x: int(x.get('Id')))

    # get output file path
    csv_file_path = os.path.join(project_dir,
                                 'results',
                                 SETTINGS.reporter,
                                 'all',
                                 'time-' + f_name)
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

    # output mc data
    produce_mc_output_data(data_map, solvers, f_name)

    # output time data
    produce_time_output_data(data_map, solvers, f_name)


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
                              'all')
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    # gather result sets
    gather_result_sets(result_file_sets)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
