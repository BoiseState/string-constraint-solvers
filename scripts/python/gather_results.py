#!/usr/bin/env python

import argparse
import csv
import fnmatch
import logging
import re
import sys

import os

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

# Global Values
GLOB = dict()
GLOB['Settings'] = None
GLOB['uneven-ids'] = set()

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
    'Addative': [
        'append',
        'concat',
        'insert'
    ],
    'Substractive': [
        'delete',
        'deleteCharAt',
        'setLength',
        'subSequence',
        'substring',
        'trim'
    ],
    'Substitutive': [
        'replace',
        'setCharAt',
        'toLowerCase',
        'toUpperCase'
    ],
    'Partial Match': [
        'contains',
        'endsWith',
        'startsWith'
    ],
    'Full Match': [
        'equals',
        'contentEquals',
        'equalsIgnoreCase',
        'isEmpty'
    ]
}

INPUT_TYPES = (
    'Simple',
    'Even',
    'Uneven'
)

OP_TYPES = (
    'Injective(<Simple>)',
    'Injective(<Even>)',
    'Injective(<Uneven>)',
    'P_Struct_Alt(<Simple>,<Simple>)',
    'P_Struct_Alt(<Simple>,<Even>)',
    'P_Struct_Alt(<Simple>,<Uneven>)',
    'P_Struct_Alt(<Even>,<Simple>)',
    'P_Struct_Alt(<Even>,<Even>)',
    'P_Struct_Alt(<Even>,<Uneven>)',
    'P_Struct_Alt(<Uneven>,<Simple>)',
    'P_Struct_Alt(<Uneven>,<Even>)',
    'P_Struct_Alt(<Uneven>,<Uneven>)',
    'N_Struct_Alt(<Simple>)',
    'N_Struct_Alt(<Even>)',
    'N_Struct_Alt(<Uneven>)',
    'Substitution(<Simple>)',
    'Substitution(<Even>)',
    'Substitution(<Uneven>)'
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

        self.single_file = False
        if options.single_out_file:
            self.single_file = True


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
    def __init__(self, arg_type, arg_id=None, value=None, uneven=None):
        self.arg_id = arg_id
        self.arg_type = arg_type
        self.value = value
        self.uneven = uneven

    def get_string(self):
        val = self.value if self.value is not None else self.arg_id
        return '{0}:{1}'.format(self.arg_type, val)


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

    gather_parser.add_argument('-s',
                               '--single-out-file',
                               help='Enforces a single output file for each '
                                    'type of result.',
                               action="store_true")

    gather_parser.add_argument('-f',
                               '--result-files',
                               default='*',
                               help='A Unix shell-style pattern which is '
                                    'used to '
                                    'match a set of result files.')

    # set settings variable from parsed options
    GLOB['Settings'] = Settings(gather_parser.parse_args(arguments))


def get_solver_key(x):
    if x in SOLVER_ORDER.keys():
        return SOLVER_ORDER[x]
    else:
        return 6


def get_op_string(ops_list=None, op_num=None, op=None):
    op_str = ''
    op_arg_str = ''
    if op is None:
        if ops_list[1].op == 'contains' and len(ops_list) > (2 + op_num):
            op = ops_list[(1 + op_num)]
        elif len(ops_list) > (1 + op_num):
            op = ops_list[op_num]

    if op is not None:
        op_str = op.op

        # arg
        if op_str in ['concat', 'contains', 'equals']:
            if op.args[0].value is not None:
                op_arg_str = "Simple"
            elif op.args[0].arg_id in GLOB['uneven-ids']:
                op_arg_str = "Uneven"
            else:
                op_arg_str = "Even"
        elif op_str == 'delete':
            if op.args[0].value == op.args[1].value:
                op_arg_str = 'same'
            else:
                op_arg_str = 'diff'
        elif op_str == 'replace':
            if op.args[0].value == op.args[1].value:
                op_arg_str = 'same'
            else:
                op_arg_str = 'diff'
        else:
            op_arg_str = 'none'

    return op_str, op_arg_str


def get_predicate(ops_list):
    pred_str = ops_list[-1].op
    pred_arg_str = ''
    pred_args = ops_list[-1].args
    if len(pred_args) > 0:
        if pred_args[0].value is not None:
            pred_arg_str = "Simple"
        elif pred_args[0].arg_id in GLOB['uneven-ids']:
            pred_arg_str = "Uneven"
        else:
            pred_arg_str = "Even"

    return pred_str, pred_arg_str


def get_operations(x):
    # initialize operations list
    ops_list = list()

    input_type = 'Simple'
    op_strings = x.split('->')
    # parse initial operation string
    match_init = REGEX['init_known'].match(op_strings[0].strip())
    if not match_init:
        match_init = REGEX['init_unknown'].match(op_strings[0].strip())
        input_type = 'Even'
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
            input_type = 'Uneven'

        ops_list.append(Operation(const_id,
                                  op,
                                  time,
                                  input_type,
                                  base_id,
                                  arg_list))

    return ops_list


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
        for i, op_info in enumerate(ops_lists.get('unbounded')):
            if op_info.op_id in return_data:
                op_map = return_data.get(op_info.op_id)[0]
                op_map.get('const_ids').add(const_id)
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
                    op_map['time'][solver] = ops_lists.get(solver)[i].time

                # add op map to solver op data
                return_data[op_info.op_id] = (op_map, op_info)

    # return data
    return return_data


def identify_uneven_args(data_map):
    # for each constraint id
    for const_id in data_map.get('unbounded').keys():
        prev_ops = data_map.get('unbounded').get(const_id).get('PREV OPS')
        ops_list = get_operations(prev_ops)

        if len(ops_list) == 2 and ops_list[1].op == 'contains':
            GLOB['uneven-ids'].add(ops_list[0].op_id)


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
    result_dir = os.path.join(project_dir, 'results', 'model-count')

    # for all items in reporter results directory
    for d in os.listdir(result_dir):
        # if item is a directory and not analysis
        test_dir = os.path.join(result_dir, d)
        if os.path.isdir(test_dir) and d != 'analysis':
            # get file set
            log.debug('getting result files for %s solver', d)
            result_files = get_result_files(test_dir,
                                            GLOB['Settings'].result_file_pattern)

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


def produce_mc_csv_data(data_map, solvers):
    # initialize output row list
    mc_rows = list()

    # for each operation id
    for op_id in data_map.get('concrete').keys():
        # initialize row
        row = dict()
        # add operation
        operations = data_map.get('unbounded').get(op_id).get('PREV OPS')
        ops_list = get_operations(operations)
        operations = re.sub('{\d+}', '', operations)
        operations = re.sub('\[\d+\]', '', operations)
        operations = operations.replace('\\"', '"')
        op1, op1_arg = get_op_string(ops_list, 1)
        op2, op2_arg = get_op_string(ops_list, 2)
        pred, pred_arg = get_predicate(ops_list)
        row['Operation'] = operations
        row['Id'] = op_id
        row['Input Type'] = ops_list[-1].input_type
        row['Op 1'] = op1
        row['Op 1 Arg'] = op1_arg
        row['Op 2'] = op2
        row['Op 2 Arg'] = op2_arg
        row['Pred'] = pred
        row['Pred Arg'] = pred_arg
        for solver in solvers:
            data_row = data_map.get(solver).get(op_id)
            prefix = solver.upper()[0]
            row[prefix + ' In MC'] = data_row.get('IN COUNT')
            row[prefix + ' T MC'] = data_row.get('T COUNT')
            row[prefix + ' F MC'] = data_row.get('F COUNT')

        # add row to output rows
        mc_rows.append(row)

    # return sorted rows
    return sorted(mc_rows, key=lambda x: int(x.get('Id')))


def produce_mc_time_csv_data(data_map, solvers):
    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in data_map.get(next(iter(solvers))).keys():
        # initialize row
        row = dict()
        # add operation
        operations = data_map.get('unbounded').get(op_id).get('PREV OPS')
        ops_list = get_operations(operations)
        operations = re.sub('{\d+}', '', operations)
        operations = re.sub('\[\d+\]', '', operations)
        operations = operations.replace('\\"', '"')
        op1, op1_arg = get_op_string(ops_list, 1)
        op2, op2_arg = get_op_string(ops_list, 2)
        pred, pred_arg = get_predicate(ops_list)
        row['Operation'] = operations
        row['Id'] = op_id
        row['Input Type'] = ops_list[-1].input_type
        row['Op 1'] = op1
        row['Op 1 Arg'] = op1_arg
        row['Op 2'] = op2
        row['Op 2 Arg'] = op2_arg
        row['Pred'] = pred
        row['Pred Arg'] = pred_arg
        for solver in solvers:
            data_row = data_map.get(solver).get(op_id)
            prefix = solver.upper()[0]
            row[prefix + ' Acc Time'] = data_row.get('ACC IN TIME')
            row[prefix + ' T MC Time'] = data_row.get('T MC TIME')
            row[prefix + ' F MC Time'] = data_row.get('F MC TIME')
            row[prefix + ' T Pred Time'] = data_row.get('T PRED TIME')
            row[prefix + ' F Pred Time'] = data_row.get('F PRED TIME')

        # add row to output rows
        output_rows.append(row)

    # return sorted output rows
    return sorted(output_rows, key=lambda x: int(x.get('Id')))


def produce_op_time_csv_data(data_map, solvers):
    # get all op data
    op_data = get_all_op_data(data_map, solvers)

    # initialize output row list
    output_rows = list()

    # for each operation id
    for op_id in op_data.keys():
        # get op map
        op_map, op_info = op_data[op_id]
        op_str, op_arg = get_op_string(op=op_info)
        # initialize row
        row = dict()
        row['Op Id'] = op_id
        row['Op'] = op_map.get('op')
        row['Op Group'] = op_map.get('op_group')
        row['In Type'] = op_map.get('input_type')
        row['Base Id'] = op_map.get('base_id')
        row['Args'] = op_arg
        for solver in solvers:
            prefix = solver.upper()[0]
            row[prefix + ' Op Time'] = op_map.get('time').get(solver)

        # add row to output rows
        output_rows.append(row)

    # sort output rows
    return sorted(output_rows, key=lambda x: int(x.get('Op Id')))


def get_mc_field_names(solvers):
    # initialize csv field names
    field_names = list()

    if GLOB['Settings'].single_file:
        field_names.append('File')

    field_names.append('Id')
    field_names.append('Operation')

    # set field names for each solver
    sorted_solvers = sorted(solvers, key=get_solver_key)
    in_fields = list()
    t_fields = list()
    f_fields = list()
    t_p_fields = list()
    f_p_fields = list()
    t_diff_fields = list()
    f_diff_fields = list()
    agree_fields = list()
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        in_fields.append(prefix + ' In MC')
        t_fields.append(prefix + ' T MC')
        f_fields.append(prefix + ' F MC')
    field_names.extend(in_fields)
    field_names.extend(t_fields)
    field_names.extend(f_fields)
    field_names.extend(t_p_fields)
    field_names.extend(f_p_fields)
    field_names.extend(t_diff_fields)
    field_names.extend(f_diff_fields)
    field_names.extend(agree_fields)

    field_names.append('Input Type')
    field_names.append('Op 1')
    field_names.append('Op 1 Arg')
    field_names.append('Op 2')
    field_names.append('Op 2 Arg')
    field_names.append('Pred')
    field_names.append('Pred Arg')

    return field_names


def get_mc_time_field_names(solvers):
    # initialize csv field names
    field_names = list()

    if GLOB['Settings'].single_file:
        field_names.append('File')

    field_names.append('Id')
    field_names.append('Operation')

    # set field names for each solver
    sorted_solvers = sorted(solvers, key=get_solver_key)
    in_fields = list()
    t_fields = list()
    f_fields = list()
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        in_fields.append(prefix + ' Acc Time')
        t_fields.append(prefix + ' T MC Time')
        f_fields.append(prefix + ' F MC Time')
        t_fields.append(prefix + ' T Pred Time')
        f_fields.append(prefix + ' F Pred Time')
    field_names.extend(in_fields)
    field_names.extend(t_fields)
    field_names.extend(f_fields)

    field_names.append('Input Type')
    field_names.append('Op 1')
    field_names.append('Op 1 Arg')
    field_names.append('Op 2')
    field_names.append('Op 2 Arg')
    field_names.append('Pred')
    field_names.append('Pred Arg')

    return field_names


def get_op_time_field_names(solvers):
    # initialize csv field names
    field_names = list()

    if GLOB['Settings'].single_file:
        field_names.append('File')

    field_names.append('Op Id')
    field_names.append('Op')
    field_names.append('Op Group')
    field_names.append('In Type')
    field_names.append('Args')
    field_names.append('Base Id')

    # set field names for each solver
    sorted_solvers = sorted(solvers, key=get_solver_key)
    for solver in sorted_solvers:
        prefix = solver.upper()[0]
        field_names.append(prefix + ' Op Time')

    return field_names


def gather_results(f_name, file_set):
    # get solvers from file_set
    solvers = list(file_set.keys())

    # get data map from files
    data_map = get_data_map_from_csv_files(f_name, file_set)

    identify_uneven_args(data_map)

    csv_rows = dict()

    # output mc data
    csv_rows['mc'] = produce_mc_csv_data(data_map, solvers)

    # output mc time data
    csv_rows['mc-time'] = produce_mc_time_csv_data(data_map, solvers)

    # output time data
    csv_rows['op-time'] = produce_op_time_csv_data(data_map, solvers)

    return csv_rows


def gather_result_sets(result_file_sets):
    csv_data = dict()
    # for each file name in the result file sets
    for f_name in sorted(result_file_sets.keys()):
        # get file set
        file_set = result_file_sets.get(f_name)
        log.debug('getting result files for %s', f_name)
        # gather file set
        csv_data[f_name] = gather_results(f_name, file_set)

    return csv_data


def output_csv_file(output_rows, field_names, f_name):
    # get output file path
    out_f_name = f_name
    if GLOB['Settings'].single_file:
        out_f_name = re.sub('(?P<pre>\w+)-A[BCDE]-\d+(-\d+)?(?P<suf>.csv)',
                            '\g<pre>\g<suf>',
                            f_name)
    csv_file_path = os.path.join(project_dir,
                                 'results',
                                 'model-count',
                                 'all',
                                 out_f_name)

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


def output_csv_files(csv_data, solvers):
    mc_field_names = get_mc_field_names(solvers)
    mc_time_field_names = get_mc_time_field_names(solvers)
    op_time_field_names = get_op_time_field_names(solvers)

    if GLOB['Settings'].single_file:
        mc_rows = list()
        mc_time_rows = list()
        op_time_rows = list()
        for f_name in csv_data.keys():
            for row in csv_data.get(f_name).get('mc'):
                row['File'] = f_name
                mc_rows.append(row)

            for row in csv_data.get(f_name).get('mc-time'):
                row['File'] = f_name
                mc_time_rows.append(row)

            for row in csv_data.get(f_name).get('op-time'):
                row['File'] = f_name
                op_time_rows.append(row)

        f_name = next(iter(csv_data.keys()))
        output_csv_file(mc_rows, mc_field_names, 'mc-' + f_name)
        output_csv_file(mc_time_rows, mc_time_field_names, 'mc-time-' + f_name)
        output_csv_file(op_time_rows, op_time_field_names, 'op-time-' + f_name)
    else:
        for f_name in csv_data.keys():
            mc_rows = csv_data.get(f_name).get('mc')
            mc_time_rows = csv_data.get(f_name).get('mc-time')
            op_time_rows = csv_data.get(f_name).get('op-time')
            output_csv_file(mc_rows, mc_field_names, 'mc-' + f_name)
            output_csv_file(mc_time_rows, mc_time_field_names,
                            'mc-time-' + f_name)
            output_csv_file(op_time_rows, op_time_field_names,
                            'op-time-' + f_name)


def main(arguments):
    # get option from arguments
    set_options(arguments)

    # get result files to gather
    result_file_sets = get_result_file_sets()

    # ensure output directory exists
    output_dir = os.path.join(project_dir,
                              'results',
                              'model-count',
                              'all')
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    # gather result sets
    csv_file_data = gather_result_sets(result_file_sets)

    # output csv files
    solvers = next(iter(result_file_sets.values())).keys()
    output_csv_files(csv_file_data, solvers)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
