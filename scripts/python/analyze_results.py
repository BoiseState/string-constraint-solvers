#! /usr/bin/env python
import logging

import argparse
import csv
import fnmatch
import numpy
import os
import re
import scipy
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
GLOB['len-match'] = dict()
GLOB['len-match'][1] = re.compile('.*-01.csv')
GLOB['len-match'][2] = re.compile('.*-02.csv')
GLOB['len-match'][3] = re.compile('.*-03.csv')


ORDER_COLUMNS = {
    'Bin': 1,
    'Selection': 2,
    'Unbounded': 3,
    'Bounded': 4,
    'Aggregate': 5,
    'Weighted': 6
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
        self.file_pattern = options.data_files


def set_options(arguments):
    # process command line args
    analyze_parser = argparse.ArgumentParser(prog=__doc__,
                                             description='Analyze results.')

    analyze_parser.add_argument('-d',
                                '--debug',
                                help='Display debug messages for this script.',
                                action="store_true")

    analyze_parser.add_argument('data_files',
                                help="A Unix shell-style pattern which is "
                                     "used to match a set of result files.")

    GLOB['Settings'] = Settings(analyze_parser.parse_args(arguments))


def read_csv_data(file_path):
    # initialize rows list
    rows = list()

    # read csv rows
    log.debug('Reading in data from %s', file_path)
    with open(file_path, 'r') as csv_file:
        reader = csv.DictReader(csv_file, delimiter='\t',
                                quoting=csv.QUOTE_NONE, quotechar='|',
                                lineterminator='\n')
        for row in reader:
            n_row = list(row)
            n_row.insert(0, os.path.basename(file_path))
            rows.append(row)

    # return rows data
    return rows


def read_data_files(file_pattern):
    # initialize return dictionary
    return_data = list()

    # check for matching files and read csv data
    for f in os.listdir(data_dir):
        test_path = os.path.join(data_dir, f)
        if os.path.isfile(test_path) and fnmatch.fnmatch(f, file_pattern):
            return_data.extend(read_csv_data(test_path))

    return return_data


def get_data():
    # get lists of data files
    mc_data = read_data_files('mc-' + GLOB['Settings'].file_pattern)
    mc_time_data = read_data_files('mc-time-' + GLOB['Settings'].file_pattern)
    op_time_data = read_data_files('op-time-' + GLOB['Settings'].file_pattern)

    # return data
    return mc_data, mc_time_data, op_time_data


def order_columns(column):
    return ORDER_COLUMNS[column]


def get_latex_table(table, caption, label):
    lines = list()
    columns = sorted(next(iter(table)).keys(), key=order_columns)

    # create before table
    lines.append('\\begin{table}[h!]\n')
    lines.append(' ' * 4 + '\\centering\n')

    begin_tabu = ' ' * 4 + '\\begin{tabu}{|'
    for i in range(0, len(columns)):
        begin_tabu += ' c |'
    begin_tabu += '}\n'
    lines.append(begin_tabu)
    lines.append(' ' * 8 + '\\hline\n')

    # create column headers
    headers = ' ' * 8
    for i, column in enumerate(columns):
        if i != 0:
            headers += '& '
        headers += '\\textbf{' + column + '} '
    headers += '\\\\\n'
    lines.append(headers)
    lines.append(' ' * 8 + '\\hline\n')

    # create columns
    for row in table:
        out_row = ' ' * 8
        for i, column in enumerate(columns):
            if i != 0:
                out_row += '& '
            out_row += '{0} '.format(row.get(column))
        out_row += '\\\\\n'
        lines.append(out_row)
        lines.append(' ' * 8 + '\\hline\n')

    # create after table
    lines.append(' ' * 4 + '\\end{tabu}\n')
    lines.append(' ' * 4 + '\\caption{' + caption + '}\n')
    lines.append(' ' * 4 + '\\label{tab:' + label + '}\n')
    lines.append('\\end{table}\n')

    return lines


def output_latex_tables(tables):
    table_list = list()

    for table, caption, label in tables:
        table_list.append(get_latex_table(table, caption, label))

    before_lines = list()
    before_lines.append('\\documentclass [11pt]{article}\n')
    before_lines.append('\n')
    before_lines.append('\\usepackage[utf8]{inputenc}\n')
    before_lines.append('\\usepackage[justification=centering]{caption}\n')
    before_lines.append('\\usepackage{tabu}\n')
    before_lines.append('\n')
    before_lines.append('\\begin{document}\n')
    before_lines.append('\n')

    after_lines = list()
    after_lines.append('\\end{document}\n')

    separator_lines = ['\n']

    out_path = os.path.join(project_dir, 'data', 'tables.tex')
    with open(out_path, 'w') as out_file:
        out_file.writelines(before_lines)

        for lines in table_list:
            out_file.writelines(lines)
            out_file.writelines(separator_lines)

        out_file.writelines(after_lines)


def filter_disagree(row, prefix, disagree=True):
    return disagree or row.get(prefix + ' Agree')


def filter_input_type(row, input_type=None):
    return input_type is None or row.get('Input Type') == input_type


def filter_length(row, length=None):
    return length is None \
           or GLOB.get('len-match').get(length).match(row.get('File'))


def filter_operation(row, operation=None, exclusive=False, arg_type=None, ):
    return operation is None \
           or ('Op' in row and row.get('Op') == operation) \
           or (exclusive
               and ((row.get('Op 1') == operation
                     and row.get('Op 2') == operation
                     and (arg_type is None
                          or row.get('Op 2 Arg') == arg_type))
                    or (row.get('Op 1') == operation
                        and row.get('Op 2') == ''
                        and (arg_type is None
                             or row.get('Op 2 Arg') == arg_type)))) \
           or (not exclusive
               and ((row.get('Op 1') == operation
                     and (arg_type is None
                          or row.get('Op 1 Arg') == arg_type))
                    or (row.get('Op 2') == operation
                        and (arg_type is None
                             or row.get('Op 2 Arg') == arg_type))))


def filter_predicate(row, predicate, arg_type=None):
    return predicate is None \
           or (row.get('Pred') == predicate
               and (arg_type is None
                    or row.get('Pred Arg') == arg_type))


def get_percent_differences(rows, disagree=True, bins=None, branch=None,
                            input_type=None, length=None, operation=None,
                            exclusive_op=None, op_arg_type=None,
                            predicate=None, pred_arg_type=None):
    # initialize structures
    if bins is None:
        bins = [0, 10, 30, 50, 100]
    results = list()

    # get all diff values
    u_diffs = list()
    b_diffs = list()
    a_diffs = list()
    w_diffs = list()

    def get_reduction(column):
        def reduction(values, row):
            if row.get('Op 1') != '' \
                    and filter_disagree(row, column[0], disagree) \
                    and filter_input_type(row, input_type) \
                    and filter_length(row, length) \
                    and filter_operation(row, operation, exclusive_op,
                                         op_arg_type) \
                    and filter_predicate(row, predicate, pred_arg_type):
                values.append(float(row.get(column)))
            return values

        return reduction

    if branch is None or branch:
        u_diffs = reduce(get_reduction('U T Diff'), rows, u_diffs)
        b_diffs = reduce(get_reduction('B T Diff'), rows, b_diffs)
        a_diffs = reduce(get_reduction('A T Diff'), rows, a_diffs)
        w_diffs = reduce(get_reduction('W T Diff'), rows, w_diffs)

    if branch is None or not branch:
        u_diffs = reduce(get_reduction('U F Diff'), rows, u_diffs)
        b_diffs = reduce(get_reduction('B F Diff'), rows, b_diffs)
        a_diffs = reduce(get_reduction('A F Diff'), rows, a_diffs)
        w_diffs = reduce(get_reduction('W F Diff'), rows, w_diffs)

    def reduce_hist(bin_map, per_diff):
        sorted_keys = sorted(bin_map.keys())
        for i, val in enumerate(sorted_keys):
            if (100 * per_diff) <= val:
                bin_map[val] += 1
                return bin_map
        bin_map[sorted_keys[-1]] += 1
        return bin_map

    u_hist = dict()
    b_hist = dict()
    a_hist = dict()
    w_hist = dict()
    for p in bins:
        u_hist[p] = 0
        b_hist[p] = 0
        a_hist[p] = 0
        w_hist[p] = 0

    u_hist = reduce(reduce_hist, u_diffs, u_hist)
    b_hist = reduce(reduce_hist, b_diffs, b_hist)
    a_hist = reduce(reduce_hist, a_diffs, a_hist)
    w_hist = reduce(reduce_hist, w_diffs, w_hist)

    for p in bins:
        u_per_diff = 100 * (float(u_hist[p]) / len(u_diffs))
        b_per_diff = 100 * (float(b_hist[p]) / len(b_diffs))
        a_per_diff = 100 * (float(a_hist[p]) / len(a_diffs))
        w_per_diff = 100 * (float(w_hist[p]) / len(w_diffs))
        results.append({
            'Bin': '{0:d}\\%'.format(p),
            'Unbounded': '{0:.1f}\\%'.format(u_per_diff),
            'Bounded': '{0:.1f}\\%'.format(b_per_diff),
            'Aggregate': '{0:.1f}\\%'.format(a_per_diff),
            'Weighted': '{0:.1f}\\%'.format(w_per_diff)
        })

    return results


def get_agreement(rows, input_type=None, length=None, operation=None,
                  exclusive_op=None, op_arg_type=None,
                  predicate=None, pred_arg_type=None):
    results = dict()

    # get all diff values
    u_agreements = list()
    b_agreements = list()
    a_agreements = list()
    w_agreements = list()

    def get_reduction(column, skip_agree=False):
        def reduction(values, row):
            if (skip_agree or row.get(column) == 'True') \
                    and row.get('Op 1') != '' \
                    and filter_input_type(row, input_type) \
                    and filter_length(row, length) \
                    and filter_operation(row, operation, exclusive_op,
                                         op_arg_type) \
                    and filter_predicate(row, predicate, pred_arg_type):
                values.append(row.get(column))
            return values

        return reduction

    valid_rows = list()
    valid_rows = reduce(get_reduction('Id', skip_agree=True), rows, valid_rows)

    u_agreements = reduce(get_reduction('U Agree'), rows, u_agreements)
    b_agreements = reduce(get_reduction('B Agree'), rows, b_agreements)
    a_agreements = reduce(get_reduction('A Agree'), rows, a_agreements)
    w_agreements = reduce(get_reduction('W Agree'), rows, w_agreements)

    u_per = 100 * (float(len(u_agreements)) / len(valid_rows))
    b_per = 100 * (float(len(b_agreements)) / len(valid_rows))
    a_per = 100 * (float(len(a_agreements)) / len(valid_rows))
    w_per = 100 * (float(len(w_agreements)) / len(valid_rows))

    results['Unbounded'] = '{0:.1f}\\%'.format(u_per)
    results['Bounded'] = '{0:.1f}\\%'.format(b_per)
    results['Aggregate'] = '{0:.1f}\\%'.format(a_per)
    results['Weighted'] = '{0:.1f}\\%'.format(w_per)

    return results


def analyze_accuracy(mc_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Model Count Accuracy')

    log.debug('Calculating Percent Differences - acc_diff_All')
    tables.append((get_percent_differences(mc_rows),
                   'Frequency of Accuracy Difference for All Constraints',
                   'acc_diff_All'))
    log.debug('Calculating Percent Differences - acc_diff_True')
    tables.append((get_percent_differences(mc_rows, branch=True),
                   'Frequency of Accuracy Difference for True Branch '
                   'Constraints',
                   'acc_diff_True'))
    log.debug('Calculating Percent Differences - acc_diff_False')
    tables.append((get_percent_differences(mc_rows, branch=False),
                   'Frequency of Accuracy Difference for False Branch '
                   'Constraints',
                   'acc_diff_False'))

    log.debug('Calculating Percent Differences - acc_diff_concrete')
    tables.append((get_percent_differences(mc_rows, input_type="Concrete"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Concrete Input String',
                   'acc_diff_concrete'))
    log.debug('Calculating Percent Differences - acc_diff_simple_unknown')
    tables.append((get_percent_differences(mc_rows, input_type="Simple"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Simple Unknown Input String',
                   'acc_diff_simple_unknown'))
    log.debug('Calculating Percent Differences - acc_diff_branch_unknown')
    tables.append((get_percent_differences(mc_rows, input_type="Branching"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Branching Unknown Input String',
                   'acc_diff_branch_unknown'))

    log.debug('Calculating Percent Differences - acc_diff_1')
    tables.append((get_percent_differences(mc_rows, length=1),
                   'Frequency of Accuracy Difference for Constraints '
                   'Following an Input String of Length 1',
                   'acc_diff_1'))
    log.debug('Calculating Percent Differences - acc_diff_2')
    tables.append((get_percent_differences(mc_rows, length=2),
                   'Frequency of Accuracy Difference for Constraints '
                   'Following an Input String of Length 2',
                   'acc_diff_2'))
    log.debug('Calculating Percent Differences - acc_diff_3')
    tables.append((get_percent_differences(mc_rows, length=3),
                   'Frequency of Accuracy Difference for Constraints '
                   'Following an Input String of Length 3',
                   'acc_diff_3'))

    log.debug('Calculating Percent Differences - acc_diff_incl_concat_all')
    tables.append((get_percent_differences(mc_rows, operation="concat"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{concat} Operations for All Args',
                   'acc_diff_incl_concat_all'))
    log.debug('Calculating Percent Differences - acc_diff_incl_concat_con')
    tables.append((get_percent_differences(mc_rows, operation="concat",
                                           op_arg_type="Concrete"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{concat} Operations for Concrete Args',
                   'acc_diff_incl_concat_con'))
    log.debug('Calculating Percent Differences - acc_diff_incl_concat_simp')
    tables.append((get_percent_differences(mc_rows, operation="concat",
                                           op_arg_type="Simple"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{concat} Operations for Simple Unknown Args',
                   'acc_diff_incl_concat_simp'))
    # log.debug('Calculating Percent Differences - acc_diff_incl_concat_branch')
    # tables.append((get_percent_differences(mc_rows, operation="concat",
    #                                        op_arg_type="Branching"),
    #                'Frequency of Accuracy Difference for Constraints Including'
    #                ' \\texttt{concat} Operations for Branching Unknown Args',
    #                'acc_diff_incl_concat_branch'))
    log.debug('Calculating Percent Differences - acc_diff_incl_delete')
    tables.append((get_percent_differences(mc_rows, operation="delete"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{delete} Operations',
                   'acc_diff_incl_delete'))
    log.debug('Calculating Percent Differences - acc_diff_incl_replace')
    tables.append((get_percent_differences(mc_rows, operation="replace"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{replace} Operations',
                   'acc_diff_incl_replace'))
    log.debug('Calculating Percent Differences - acc_diff_incl_reverse')
    tables.append((get_percent_differences(mc_rows, operation="reverse"),
                   'Frequency of Accuracy Difference for Constraints Including'
                   ' \\texttt{reverse} Operations',
                   'acc_diff_incl_reverse'))

    log.debug('Calculating Percent Differences - acc_diff_excl_concat_all')
    tables.append((get_percent_differences(mc_rows, operation="concat",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{concat} Operations for All Args',
                   'acc_diff_excl_concat_all'))
    log.debug('Calculating Percent Differences - acc_diff_excl_concat_con')
    tables.append((get_percent_differences(mc_rows, operation="concat",
                                           op_arg_type="Concrete",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{concat} Operations for Concrete Args',
                   'acc_diff_excl_concat_con'))
    log.debug('Calculating Percent Differences - acc_diff_excl_concat_simp')
    tables.append((get_percent_differences(mc_rows, operation="concat",
                                           op_arg_type="Simple",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{concat} Operations for Simple Unknown Args',
                   'acc_diff_excl_concat_simp'))
    log.debug('Calculating Percent Differences - acc_diff_excl_concat_branch')
    # tables.append((get_percent_differences(mc_rows, operation="concat",
    #                                        op_arg_type="Branching",
    #                                        exclusive_op=True),
    #                'Frequency of Accuracy Difference for Constraints of Only'
    #                ' \\texttt{concat} Operations for Branching Unknown Args',
    #                'acc_diff_excl_concat_branch'))
    log.debug('Calculating Percent Differences - acc_diff_excl_delete')
    tables.append((get_percent_differences(mc_rows, operation="delete",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{delete} Operations',
                   'acc_diff_excl_delete'))
    log.debug('Calculating Percent Differences - acc_diff_excl_replace')
    tables.append((get_percent_differences(mc_rows, operation="replace",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{replace} Operations',
                   'acc_diff_excl_replace'))
    log.debug('Calculating Percent Differences - acc_diff_excl_reverse')
    tables.append((get_percent_differences(mc_rows, operation="reverse",
                                           exclusive_op=True),
                   'Frequency of Accuracy Difference for Constraints of Only'
                   ' \\texttt{reverse} Operations',
                   'acc_diff_excl_reverse'))

    log.debug('Calculating Percent Differences - acc_diff_contains_all')
    tables.append((get_percent_differences(mc_rows, predicate="contains"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{contains} Predicates for All Args',
                   'acc_diff_contains_all'))
    log.debug('Calculating Percent Differences - acc_diff_contains_con')
    tables.append((get_percent_differences(mc_rows, predicate="contains",
                                           pred_arg_type="Concrete"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{contains} Predicates for Concrete Args',
                   'acc_diff_contains_con'))
    log.debug('Calculating Percent Differences - acc_diff_contains_simp')
    tables.append((get_percent_differences(mc_rows, predicate="contains",
                                           pred_arg_type="Simple"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{contains} Predicates for Simple Unknown Args',
                   'acc_diff_contains_simp'))
    log.debug('Calculating Percent Differences - acc_diff_contains_branch')
    tables.append((get_percent_differences(mc_rows, predicate="contains",
                                           pred_arg_type="Branching"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{contains} Predicates for Branching Unknown '
                   'Args',
                   'acc_diff_contains_branch'))
    log.debug('Calculating Percent Differences - acc_diff_equals_all')
    tables.append((get_percent_differences(mc_rows, predicate="equals"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{equals} Predicates for All Args',
                   'acc_diff_equals_all'))
    log.debug('Calculating Percent Differences - acc_diff_equals_con')
    tables.append((get_percent_differences(mc_rows, predicate="equals",
                                           pred_arg_type="Concrete"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{equals} Predicates for Concrete Args',
                   'acc_diff_equals_con'))
    log.debug('Calculating Percent Differences - acc_diff_equals_simp')
    tables.append((get_percent_differences(mc_rows, predicate="equals",
                                           pred_arg_type="Simple"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{equals} Predicates for Simple Unknown Args',
                   'acc_diff_equals_simp'))
    log.debug('Calculating Percent Differences - acc_diff_equals_branch')
    tables.append((get_percent_differences(mc_rows, predicate="equals",
                                           pred_arg_type="Branching"),
                   'Frequency of Accuracy Difference for Constraints Containing'
                   ' a \\texttt{equals} Predicates for Branching Unknown Args',
                   'acc_diff_equals_branch'))

    # agreement
    agree_list = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    log.debug('Getting Agreements - All')
    temp = get_agreement(mc_rows)
    temp['Selection'] = 'All'
    agree_list.append(temp)

    agree_list.append(blank_row)

    log.debug('Getting Agreements - Concrete')
    temp = get_agreement(mc_rows, input_type="Concrete")
    temp['Selection'] = 'Concrete'
    agree_list.append(temp)
    log.debug('Getting Agreements - Simple')
    temp = get_agreement(mc_rows, input_type="Simple")
    temp['Selection'] = 'Simple'
    agree_list.append(temp)
    log.debug('Getting Agreements - Branching')
    temp = get_agreement(mc_rows, input_type="Branching")
    temp['Selection'] = 'Branching'
    agree_list.append(temp)

    agree_list.append(blank_row)

    log.debug('Getting Agreements - Length 1')
    temp = get_agreement(mc_rows, length=1)
    temp['Selection'] = 'Length 1'
    agree_list.append(temp)
    log.debug('Getting Agreements - Length 2')
    temp = get_agreement(mc_rows, length=2)
    temp['Selection'] = 'Length 2'
    agree_list.append(temp)
    log.debug('Getting Agreements - Length 3')
    temp = get_agreement(mc_rows, length=3)
    temp['Selection'] = 'Length 3'
    agree_list.append(temp)

    agree_list.append(blank_row)

    log.debug('Getting Agreements - Includes \\texttt{concat} for All Args')
    temp = get_agreement(mc_rows, operation="concat")
    temp['Selection'] = 'Includes \\texttt{concat} for All Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - Includes \\texttt{concat} for Concrete Args')
    temp = get_agreement(mc_rows, operation="concat", op_arg_type="Concrete")
    temp['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - Includes \\texttt{concat} for Simple'
              ' Unknown Args')
    temp = get_agreement(mc_rows, operation="concat", op_arg_type="Simple")
    temp['Selection'] = 'Includes \\texttt{concat} for Simple Unknown Args'
    agree_list.append(temp)
    # log.debug('Getting Agreements - Includes \\texttt{concat} for Branching '
    #           'Unknown Args')
    # temp = get_agreement(mc_rows, operation="concat", op_arg_type="Branching")
    # temp['Selection'] = 'Includes \\texttt{concat} for Branching Unknown Args'
    # agree_list.append(temp)
    log.debug('Getting Agreements - Includes \\texttt{delete}')
    temp = get_agreement(mc_rows, operation="delete")
    temp['Selection'] = 'Includes \\texttt{delete}'
    agree_list.append(temp)
    log.debug('Getting Agreements - Includes \\texttt{replace}')
    temp = get_agreement(mc_rows, operation="replace")
    temp['Selection'] = 'Includes \\texttt{replace}'
    agree_list.append(temp)
    log.debug('Getting Agreements - Includes \\texttt{reverse}')
    temp = get_agreement(mc_rows, operation="reverse")
    temp['Selection'] = 'Includes \\texttt{reverse}'
    agree_list.append(temp)

    agree_list.append(blank_row)

    log.debug('Getting Agreements - Only \\texttt{concat} for All Args')
    temp = get_agreement(mc_rows, operation="concat", exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{concat} for All Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - Only \\texttt{concat} for Concrete Args')
    temp = get_agreement(mc_rows, operation="concat", op_arg_type="Concrete",
                         exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{concat} for Concrete Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - Only \\texttt{concat} for Simple '
              'Unknown Args')
    temp = get_agreement(mc_rows, operation="concat", op_arg_type="Simple",
                         exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{concat} for Simple Unknown Args'
    agree_list.append(temp)
    # log.debug('Getting Agreements - Only \\texttt{concat} for Branching '
    #           'Unknown Args')
    # temp = get_agreement(mc_rows, operation="concat", op_arg_type="Branching",
    #                      exclusive_op=True)
    # temp['Selection'] = 'Only \\texttt{concat} for Branching Unknown Args'
    # agree_list.append(temp)
    log.debug('Getting Agreements - Only \\texttt{delete}')
    temp = get_agreement(mc_rows, operation="delete", exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{delete}'
    agree_list.append(temp)
    log.debug('Getting Agreements - Only \\texttt{replace}')
    temp = get_agreement(mc_rows, operation="replace", exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{replace}'
    agree_list.append(temp)
    log.debug('Getting Agreements - Only \\texttt{reverse}')
    temp = get_agreement(mc_rows, operation="reverse", exclusive_op=True)
    temp['Selection'] = 'Only \\texttt{reverse}'
    agree_list.append(temp)

    agree_list.append(blank_row)

    log.debug('Getting Agreements - \\texttt{contains} for All Args')
    temp = get_agreement(mc_rows, predicate="contains")
    temp['Selection'] = '\\texttt{contains} for All Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{contains} for Concrete Args')
    temp = get_agreement(mc_rows, predicate="contains",
                         pred_arg_type="Concrete")
    temp['Selection'] = '\\texttt{contains} for Concrete Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{contains} for Simple Args')
    temp = get_agreement(mc_rows, predicate="contains", pred_arg_type="Simple")
    temp['Selection'] = '\\texttt{contains} for Simple Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{contains} for Branching Args')
    temp = get_agreement(mc_rows, predicate="contains",
                         pred_arg_type="Branching")
    temp['Selection'] = '\\texttt{contains} for Branching Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{equals} for All Args')
    temp = get_agreement(mc_rows, predicate="equals")
    temp['Selection'] = '\\texttt{equals} for All Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{equals} for Concrete Args')
    temp = get_agreement(mc_rows, predicate="equals", pred_arg_type="Concrete")
    temp['Selection'] = '\\texttt{equals} for Concrete Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{equals} for Simple Args')
    temp = get_agreement(mc_rows, predicate="equals", pred_arg_type="Simple")
    temp['Selection'] = '\\texttt{equals} for Simple Args'
    agree_list.append(temp)
    log.debug('Getting Agreements - \\texttt{equals} for Branching Args')
    temp = get_agreement(mc_rows, predicate="equals", pred_arg_type="Branching")
    temp['Selection'] = '\\texttt{equals} for Branching Args'
    agree_list.append(temp)

    tables.append((agree_list,
                   'Frequency of Branch Selection Agreement for Constraints',
                   'acc_agree'))

    return tables


def get_perf_metrics(rows, column_suffixes, input_type=None, length=None,
                     operation=None, exclusive_op=None, op_arg_type=None,
                     predicate=None, pred_arg_type=None):
    avg_results = dict()
    median_results = dict()
    range_results = dict()
    std_dev_results = dict()

    # get all diff values
    u_times = list()
    b_times = list()
    a_times = list()
    w_times = list()

    def get_reduction(columns):
        def reduction(values, row):
            if row.get('Op 1') != '' \
                    and filter_input_type(row, input_type) \
                    and filter_length(row, length) \
                    and filter_operation(row, operation, exclusive_op,
                                         op_arg_type) \
                    and filter_predicate(row, predicate, pred_arg_type):
                for column in columns:
                    values.append(int(row.get(column)))
            return values

        return reduction

    u_columns = list()
    b_columns = list()
    a_columns = list()
    w_columns = list()

    for suffix in column_suffixes:
        u_columns.append('U ' + suffix)
        b_columns.append('B ' + suffix)
        a_columns.append('A ' + suffix)
        w_columns.append('W ' + suffix)

    u_times = reduce(get_reduction(u_columns), rows, u_times)
    b_times = reduce(get_reduction(b_columns), rows, b_times)
    a_times = reduce(get_reduction(a_columns), rows, a_times)
    w_times = reduce(get_reduction(w_columns), rows, w_times)

    u_times_np = numpy.asarray(u_times)
    b_times_np = numpy.asarray(b_times)
    a_times_np = numpy.asarray(a_times)
    w_times_np = numpy.asarray(w_times)

    u_avg = numpy.mean(u_times_np)
    b_avg = numpy.mean(b_times_np)
    a_avg = numpy.mean(a_times_np)
    w_avg = numpy.mean(w_times_np)

    avg_results['Unbounded'] = '{0:.1f}'.format(u_avg)
    avg_results['Bounded'] = '{0:.1f}'.format(b_avg)
    avg_results['Aggregate'] = '{0:.1f}'.format(a_avg)
    avg_results['Weighted'] = '{0:.1f}'.format(w_avg)

    u_median = numpy.median(u_times_np)
    b_median = numpy.median(b_times_np)
    a_median = numpy.median(a_times_np)
    w_median = numpy.median(w_times_np)

    median_results['Unbounded'] = '{0:.1f}'.format(u_median)
    median_results['Bounded'] = '{0:.1f}'.format(b_median)
    median_results['Aggregate'] = '{0:.1f}'.format(a_median)
    median_results['Weighted'] = '{0:.1f}'.format(w_median)

    u_range = (numpy.amin(u_times_np), numpy.amax(u_times_np))
    b_range = (numpy.amin(b_times_np), numpy.amax(b_times_np))
    a_range = (numpy.amin(a_times_np), numpy.amax(a_times_np))
    w_range = (numpy.amin(w_times_np), numpy.amax(w_times_np))

    range_results['Unbounded'] = '{0:.1f} - {1:.1f}'.format(u_range[0],
                                                            u_range[1])
    range_results['Bounded'] = '{0:.1f} - {1:.1f}'.format(b_range[0],
                                                          b_range[1])
    range_results['Aggregate'] = '{0:.1f} - {1:.1f}'.format(a_range[0],
                                                            a_range[1])
    range_results['Weighted'] = '{0:.1f} - {1:.1f}'.format(w_range[0],
                                                           w_range[1])

    u_std_dev = numpy.std(u_times_np)
    b_std_dev = numpy.std(b_times_np)
    a_std_dev = numpy.std(a_times_np)
    w_std_dev = numpy.std(w_times_np)

    std_dev_results['Unbounded'] = '{0:.1f}'.format(u_std_dev)
    std_dev_results['Bounded'] = '{0:.1f}'.format(b_std_dev)
    std_dev_results['Aggregate'] = '{0:.1f}'.format(a_std_dev)
    std_dev_results['Weighted'] = '{0:.1f}'.format(w_std_dev)

    return avg_results, median_results, range_results, std_dev_results


def analyze_mc_performance(mc_time_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Model Count Performance')

    # mc performance
    avg_list = list()
    median_list = list()
    range_list = list()
    std_dev_list = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    log.debug('Getting MC Timings - All Constraints')
    results = get_perf_metrics(mc_time_rows, ['T MC Time', 'F MC Time'])
    results[0]['Selection'] = 'All Constraints'
    results[1]['Selection'] = 'All Constraints'
    results[2]['Selection'] = 'All Constraints'
    results[3]['Selection'] = 'All Constraints'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    avg_list.append(blank_row)
    median_list.append(blank_row)
    range_list.append(blank_row)
    std_dev_list.append(blank_row)

    log.debug('Getting MC Timings - True Branch Constraints')
    results = get_perf_metrics(mc_time_rows, ['T MC Time'])
    results[0]['Selection'] = 'True Branch Constraints'
    results[1]['Selection'] = 'True Branch Constraints'
    results[2]['Selection'] = 'True Branch Constraints'
    results[3]['Selection'] = 'True Branch Constraints'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    log.debug('Getting MC Timings - False Branch Constraints')
    results = get_perf_metrics(mc_time_rows, ['F MC Time'])
    results[0]['Selection'] = 'False Branch Constraints'
    results[1]['Selection'] = 'False Branch Constraints'
    results[2]['Selection'] = 'False Branch Constraints'
    results[3]['Selection'] = 'False Branch Constraints'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    avg_list.append(blank_row)
    median_list.append(blank_row)
    range_list.append(blank_row)
    std_dev_list.append(blank_row)

    log.debug('Getting MC Timings - Constraints for Input Strings of Length 1')
    results = get_perf_metrics(mc_time_rows, ['T MC Time', 'F MC Time'],
                               length=1)
    results[0]['Selection'] = 'Constraints for Input Strings of Length 1'
    results[1]['Selection'] = 'Constraints for Input Strings of Length 1'
    results[2]['Selection'] = 'Constraints for Input Strings of Length 1'
    results[3]['Selection'] = 'Constraints for Input Strings of Length 1'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    log.debug('Getting MC Timings - Constraints for Input Strings of Length 2')
    results = get_perf_metrics(mc_time_rows, ['T MC Time', 'F MC Time'],
                               length=2)
    results[0]['Selection'] = 'Constraints for Input Strings of Length 2'
    results[1]['Selection'] = 'Constraints for Input Strings of Length 2'
    results[2]['Selection'] = 'Constraints for Input Strings of Length 2'
    results[3]['Selection'] = 'Constraints for Input Strings of Length 2'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    log.debug('Getting MC Timings - Constraints for Input Strings of Length 3')
    results = get_perf_metrics(mc_time_rows, ['T MC Time', 'F MC Time'],
                               length=3)
    results[0]['Selection'] = 'Constraints for Input Strings of Length 3'
    results[1]['Selection'] = 'Constraints for Input Strings of Length 3'
    results[2]['Selection'] = 'Constraints for Input Strings of Length 3'
    results[3]['Selection'] = 'Constraints for Input Strings of Length 3'
    avg_list.append(results[0])
    median_list.append(results[1])
    range_list.append(results[2])
    std_dev_list.append(results[3])

    tables.append((avg_list, 'Average Model Counting Times',
                   'mc_perf_avg'))
    tables.append((median_list, 'Median Model Counting Times',
                   'mc_perf_median'))
    tables.append((range_list, 'Model Counting Time Ranges',
                   'mc_perf_range'))
    tables.append((std_dev_list, 'Standard Deviation for Model Counting Times',
                   'mc_perf_std_dev'))

    return tables


def analyze_solve_performance(mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    log.debug('Calculating Constraint Solving Performance')

    acc_avg_list = list()
    acc_median_list = list()
    acc_range_list = list()
    acc_std_dev_list = list()

    log.debug('Getting Cumulative Solving Performance - All Constraints')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'])
    results[0]['Selection'] = 'All Constraints'
    results[1]['Selection'] = 'All Constraints'
    results[2]['Selection'] = 'All Constraints'
    results[3]['Selection'] = 'All Constraints'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    acc_avg_list.append(blank_row)
    acc_median_list.append(blank_row)
    acc_range_list.append(blank_row)
    acc_std_dev_list.append(blank_row)

    log.debug('Getting Cumulative Solving Performance - Concrete Input Strings')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'],
                               input_type="Concrete")
    results[0]['Selection'] = 'Concrete Input Strings'
    results[1]['Selection'] = 'Concrete Input Strings'
    results[2]['Selection'] = 'Concrete Input Strings'
    results[3]['Selection'] = 'Concrete Input Strings'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Simple Unknown '
              'Input Strings')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'],
                               input_type="Simple")
    results[0]['Selection'] = 'Simple Unknown Input Strings'
    results[1]['Selection'] = 'Simple Unknown Input Strings'
    results[2]['Selection'] = 'Simple Unknown Input Strings'
    results[3]['Selection'] = 'Simple Unknown Input Strings'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Branching Unknown '
              'Input Strings')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'],
                               input_type="Branching")
    results[0]['Selection'] = 'Branching Unknown Input Strings'
    results[1]['Selection'] = 'Branching Unknown Input Strings'
    results[2]['Selection'] = 'Branching Unknown Input Strings'
    results[3]['Selection'] = 'Branching Unknown Input Strings'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    acc_avg_list.append(blank_row)
    acc_median_list.append(blank_row)
    acc_range_list.append(blank_row)
    acc_std_dev_list.append(blank_row)

    log.debug('Getting Cumulative Solving Performance - Input Strings of'
              ' Length 1')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], length=1)
    results[0]['Selection'] = 'Input Strings of Length 1'
    results[1]['Selection'] = 'Input Strings of Length 1'
    results[2]['Selection'] = 'Input Strings of Length 1'
    results[3]['Selection'] = 'Input Strings of Length 1'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Input Strings of'
              ' Length 2')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], length=2)
    results[0]['Selection'] = 'Input Strings of Length 2'
    results[1]['Selection'] = 'Input Strings of Length 2'
    results[2]['Selection'] = 'Input Strings of Length 2'
    results[3]['Selection'] = 'Input Strings of Length 2'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Input Strings of'
              ' Length 3')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], length=3)
    results[0]['Selection'] = 'Input Strings of Length 3'
    results[1]['Selection'] = 'Input Strings of Length 3'
    results[2]['Selection'] = 'Input Strings of Length 3'
    results[3]['Selection'] = 'Input Strings of Length 3'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    acc_avg_list.append(blank_row)
    acc_median_list.append(blank_row)
    acc_range_list.append(blank_row)
    acc_std_dev_list.append(blank_row)

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for All Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat")
    results[0]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for All Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Concrete Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Concrete")
    results[0]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Simple Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Simple")
    results[0]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Branching Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Branching")
    results[0]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{delete}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="delete")
    results[0]['Selection'] = 'Includes \\texttt{delete}'
    results[1]['Selection'] = 'Includes \\texttt{delete}'
    results[2]['Selection'] = 'Includes \\texttt{delete}'
    results[3]['Selection'] = 'Includes \\texttt{delete}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{replace}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="replace")
    results[0]['Selection'] = 'Includes \\texttt{replace}'
    results[1]['Selection'] = 'Includes \\texttt{replace}'
    results[2]['Selection'] = 'Includes \\texttt{replace}'
    results[3]['Selection'] = 'Includes \\texttt{replace}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{reverse}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="reverse")
    results[0]['Selection'] = 'Includes \\texttt{reverse}'
    results[1]['Selection'] = 'Includes \\texttt{reverse}'
    results[2]['Selection'] = 'Includes \\texttt{reverse}'
    results[3]['Selection'] = 'Includes \\texttt{reverse}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    acc_avg_list.append(blank_row)
    acc_median_list.append(blank_row)
    acc_range_list.append(blank_row)
    acc_std_dev_list.append(blank_row)

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for All Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for All Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for All Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Concrete Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Concrete", exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Concrete Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Simple Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Simple", exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Simple Unknown' \
                              ' Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{concat} for Branching Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="concat",
                               op_arg_type="Branching", exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[1]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[2]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    results[3]['Selection'] = 'Includes \\texttt{concat} for Branching ' \
                              'Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{delete}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="delete",
                               exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{delete}'
    results[1]['Selection'] = 'Includes \\texttt{delete}'
    results[2]['Selection'] = 'Includes \\texttt{delete}'
    results[3]['Selection'] = 'Includes \\texttt{delete}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{replace}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="replace",
                               exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{replace}'
    results[1]['Selection'] = 'Includes \\texttt{replace}'
    results[2]['Selection'] = 'Includes \\texttt{replace}'
    results[3]['Selection'] = 'Includes \\texttt{replace}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - Includes '
              '\\texttt{reverse}')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], operation="reverse",
                               exclusive_op=True)
    results[0]['Selection'] = 'Includes \\texttt{reverse}'
    results[1]['Selection'] = 'Includes \\texttt{reverse}'
    results[2]['Selection'] = 'Includes \\texttt{reverse}'
    results[3]['Selection'] = 'Includes \\texttt{reverse}'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    tables.append(acc_avg_list)
    tables.append(acc_median_list)
    tables.append(acc_range_list)
    tables.append(acc_std_dev_list)

    log.debug('Getting Cumulative Solving Performance - \\texttt{contains} for'
              ' All Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="contains")
    results[0]['Selection'] = '\\texttt{contains} for All Args'
    results[1]['Selection'] = '\\texttt{contains} for All Args'
    results[2]['Selection'] = '\\texttt{contains} for All Args'
    results[3]['Selection'] = '\\texttt{contains} for All Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{contains} for'
              ' Concrete Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="contains",
                               pred_arg_type="Concrete")
    results[0]['Selection'] = '\\texttt{contains} for Concrete Args'
    results[1]['Selection'] = '\\texttt{contains} for Concrete Args'
    results[2]['Selection'] = '\\texttt{contains} for Concrete Args'
    results[3]['Selection'] = '\\texttt{contains} for Concrete Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{contains} for'
              ' Simple Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="contains",
                               pred_arg_type="Simple")
    results[0]['Selection'] = '\\texttt{contains} for Simple Unknown Args'
    results[1]['Selection'] = '\\texttt{contains} for Simple Unknown Args'
    results[2]['Selection'] = '\\texttt{contains} for Simple Unknown Args'
    results[3]['Selection'] = '\\texttt{contains} for Simple Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{contains} for'
              ' Branching Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="contains",
                               pred_arg_type="Branching")
    results[0]['Selection'] = '\\texttt{contains} for Branching Unknown Args'
    results[1]['Selection'] = '\\texttt{contains} for Branching Unknown Args'
    results[2]['Selection'] = '\\texttt{contains} for Branching Unknown Args'
    results[3]['Selection'] = '\\texttt{contains} for Branching Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{equals} for'
              ' All Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="equals")
    results[0]['Selection'] = '\\texttt{equals} for All Args'
    results[1]['Selection'] = '\\texttt{equals} for All Args'
    results[2]['Selection'] = '\\texttt{equals} for All Args'
    results[3]['Selection'] = '\\texttt{equals} for All Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{equals} for'
              ' Concrete Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="equals",
                               pred_arg_type="Concrete")
    results[0]['Selection'] = '\\texttt{equals} for Concrete Args'
    results[1]['Selection'] = '\\texttt{equals} for Concrete Args'
    results[2]['Selection'] = '\\texttt{equals} for Concrete Args'
    results[3]['Selection'] = '\\texttt{equals} for Concrete Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{equals} for'
              ' Simple Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="equals",
                               pred_arg_type="Simple")
    results[0]['Selection'] = '\\texttt{equals} for Simple Unknown Args'
    results[1]['Selection'] = '\\texttt{equals} for Simple Unknown Args'
    results[2]['Selection'] = '\\texttt{equals} for Simple Unknown Args'
    results[3]['Selection'] = '\\texttt{equals} for Simple Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    log.debug('Getting Cumulative Solving Performance - \\texttt{equals} for'
              ' Branching Unknown Args')
    results = get_perf_metrics(mc_time_rows, ['Acc Time'], predicate="equals",
                               pred_arg_type="Branching")
    results[0]['Selection'] = '\\texttt{equals} for Branching Unknown Args'
    results[1]['Selection'] = '\\texttt{equals} for Branching Unknown Args'
    results[2]['Selection'] = '\\texttt{equals} for Branching Unknown Args'
    results[3]['Selection'] = '\\texttt{equals} for Branching Unknown Args'
    acc_avg_list.append(results[0])
    acc_median_list.append(results[1])
    acc_range_list.append(results[2])
    acc_std_dev_list.append(results[3])

    tables.append((acc_avg_list, 'Average Constraint Solving Times',
                   'solve_perf_acc_avg'))
    tables.append((acc_median_list, 'Median Constraint Solving Times',
                   'solve_perf_acc_median'))
    tables.append((acc_range_list, 'Constraint Solving Time Ranges',
                   'solve_perf_acc_range'))
    tables.append((acc_std_dev_list, 'Standard Deviation for Constraint '
                                     'Solving Times',
                   'solve_perf_acc_std_dev'))

    op_avg_list = list()
    op_median_list = list()
    op_range_list = list()
    op_std_dev_list = list()

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{concat}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="concat")
    results[0]['Selection'] = '\\texttt{concat}'
    results[1]['Selection'] = '\\texttt{concat}'
    results[2]['Selection'] = '\\texttt{concat}'
    results[3]['Selection'] = '\\texttt{concat}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{concat} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="concat",
                               length=1)
    results[0]['Selection'] = '\\texttt{concat} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{concat} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{concat} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{concat} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{concat} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="concat",
                               length=2)
    results[0]['Selection'] = '\\texttt{concat} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{concat} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{concat} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{concat} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{concat} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="concat",
                               length=3)
    results[0]['Selection'] = '\\texttt{concat} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{concat} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{concat} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{concat} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    op_avg_list.append(blank_row)
    op_median_list.append(blank_row)
    op_range_list.append(blank_row)
    op_std_dev_list.append(blank_row)

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{delete}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="delete")
    results[0]['Selection'] = '\\texttt{delete}'
    results[1]['Selection'] = '\\texttt{delete}'
    results[2]['Selection'] = '\\texttt{delete}'
    results[3]['Selection'] = '\\texttt{delete}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{delete} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="delete",
                               length=1)
    results[0]['Selection'] = '\\texttt{delete} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{delete} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{delete} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{delete} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{delete} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="delete",
                               length=2)
    results[0]['Selection'] = '\\texttt{delete} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{delete} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{delete} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{delete} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{delete} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="delete",
                               length=3)
    results[0]['Selection'] = '\\texttt{delete} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{delete} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{delete} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{delete} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    op_avg_list.append(blank_row)
    op_median_list.append(blank_row)
    op_range_list.append(blank_row)
    op_std_dev_list.append(blank_row)

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{replace}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="replace")
    results[0]['Selection'] = '\\texttt{replace}'
    results[1]['Selection'] = '\\texttt{replace}'
    results[2]['Selection'] = '\\texttt{replace}'
    results[3]['Selection'] = '\\texttt{replace}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{replace} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="replace",
                               length=1)
    results[0]['Selection'] = '\\texttt{replace} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{replace} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{replace} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{replace} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{replace} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="replace",
                               length=2)
    results[0]['Selection'] = '\\texttt{replace} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{replace} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{replace} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{replace} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{replace} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="replace",
                               length=3)
    results[0]['Selection'] = '\\texttt{replace} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{replace} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{replace} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{replace} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    op_avg_list.append(blank_row)
    op_median_list.append(blank_row)
    op_range_list.append(blank_row)
    op_std_dev_list.append(blank_row)

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{reverse}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="reverse")
    results[0]['Selection'] = '\\texttt{reverse}'
    results[1]['Selection'] = '\\texttt{reverse}'
    results[2]['Selection'] = '\\texttt{reverse}'
    results[3]['Selection'] = '\\texttt{reverse}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{reverse} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="reverse",
                               length=1)
    results[0]['Selection'] = '\\texttt{reverse} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{reverse} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{reverse} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{reverse} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{reverse} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="reverse",
                               length=2)
    results[0]['Selection'] = '\\texttt{reverse} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{reverse} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{reverse} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{reverse} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{reverse} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="reverse",
                               length=3)
    results[0]['Selection'] = '\\texttt{reverse} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{reverse} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{reverse} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{reverse} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    op_avg_list.append(blank_row)
    op_median_list.append(blank_row)
    op_range_list.append(blank_row)
    op_std_dev_list.append(blank_row)

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{contains}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="contains")
    results[0]['Selection'] = '\\texttt{contains}'
    results[1]['Selection'] = '\\texttt{contains}'
    results[2]['Selection'] = '\\texttt{contains}'
    results[3]['Selection'] = '\\texttt{contains}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{contains} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="contains",
                               length=1)
    results[0]['Selection'] = '\\texttt{contains} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{contains} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{contains} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{contains} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{contains} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="contains",
                               length=2)
    results[0]['Selection'] = '\\texttt{contains} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{contains} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{contains} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{contains} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{contains} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="contains",
                               length=3)
    results[0]['Selection'] = '\\texttt{contains} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{contains} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{contains} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{contains} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    op_avg_list.append(blank_row)
    op_median_list.append(blank_row)
    op_range_list.append(blank_row)
    op_std_dev_list.append(blank_row)

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{equals}')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="equals")
    results[0]['Selection'] = '\\texttt{equals}'
    results[1]['Selection'] = '\\texttt{equals}'
    results[2]['Selection'] = '\\texttt{equals}'
    results[3]['Selection'] = '\\texttt{equals}'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{equals} for Input Strings of Length 1')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="equals",
                               length=1)
    results[0]['Selection'] = '\\texttt{equals} for Input Strings of Length 1'
    results[1]['Selection'] = '\\texttt{equals} for Input Strings of Length 1'
    results[2]['Selection'] = '\\texttt{equals} for Input Strings of Length 1'
    results[3]['Selection'] = '\\texttt{equals} for Input Strings of Length 1'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{equals} for Input Strings of Length 2')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="equals",
                               length=2)
    results[0]['Selection'] = '\\texttt{equals} for Input Strings of Length 2'
    results[1]['Selection'] = '\\texttt{equals} for Input Strings of Length 2'
    results[2]['Selection'] = '\\texttt{equals} for Input Strings of Length 2'
    results[3]['Selection'] = '\\texttt{equals} for Input Strings of Length 2'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    log.debug('Getting Operation and Predicate Performance - '
              '\\texttt{equals} for Input Strings of Length 3')
    results = get_perf_metrics(op_time_rows, ['Op Time'], operation="equals",
                               length=3)
    results[0]['Selection'] = '\\texttt{equals} for Input Strings of Length 3'
    results[1]['Selection'] = '\\texttt{equals} for Input Strings of Length 3'
    results[2]['Selection'] = '\\texttt{equals} for Input Strings of Length 3'
    results[3]['Selection'] = '\\texttt{equals} for Input Strings of Length 3'
    op_avg_list.append(results[0])
    op_median_list.append(results[1])
    op_range_list.append(results[2])
    op_std_dev_list.append(results[3])

    tables.append((op_avg_list, 'Average Operation and Predicate Times',
                   'solve_perf_op_avg'))
    tables.append((op_median_list, 'Median Operation and Predicate Times',
                   'solve_perf_op_median'))
    tables.append((op_range_list, 'Operation and Predicate Time Ranges',
                   'solve_perf_op_range'))
    tables.append((op_std_dev_list, 'Standard Deviation for Operation and '
                                    'Predicate Times',
                   'solve_perf_op_std_dev'))

    return tables


def analyze_comb_perf(mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    return tables


def analyze_acc_vs_mc_perf(mc_rows, mc_time_rows):
    # initialize tables list
    tables = list()

    return tables


def analyze_acc_vs_mc_perf(mc_rows, mc_time_rows):
    # initialize tables list
    tables = list()

    return tables


def analyze_acc_vs_solve_perf(mc_rows, mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    return tables


def analyze_acc_vs_comb_perf(mc_rows, mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    return tables


def perform_analysis(mc_rows, mc_time_rows, op_time_rows):
    tables = list()
    tables.extend(analyze_accuracy(mc_rows))

    tables.extend(analyze_mc_performance(mc_time_rows))

    tables.extend(analyze_solve_performance(mc_time_rows, op_time_rows))

    # tables.extend(analyze_comb_perf(mc_time_rows, op_time_rows))

    # tables.extend(analyze_acc_vs_mc_perf(mc_rows, mc_time_rows))

    # tables.extend(analyze_acc_vs_solve_perf(mc_rows, mc_time_rows,
    #                                         op_time_rows))

    # tables.extend(analyze_acc_vs_comb_perf(mc_rows, mc_time_rows,
    #                                        op_time_rows))

    output_latex_tables(tables)


def main(arguments):
    # set options from args
    set_options(arguments)

    # read data
    mc_data, mc_time_data, op_time_data = get_data()

    # perform analysis
    perform_analysis(mc_data, mc_time_data, op_time_data)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
