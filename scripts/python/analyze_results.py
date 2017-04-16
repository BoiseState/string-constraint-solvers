#! /usr/bin/env python
import logging

import argparse
import csv
import fnmatch
import os
import re
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


def filter_operation(row, operation=None):
    return operation is None \
           or (row.get('Op 1') == operation and row.get('Op 2') == operation) \
           or (row.get('Op 1') == operation and row.get('Op 2') == '')


def filter_predicate(row, predicate):
    return predicate is None or row.get('Pred') == predicate


def get_percent_differences(rows, disagree=True, bins=None, branch=None,
                            input_type=None, length=None, operation=None,
                            predicate=None):
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
                    and filter_operation(row, operation) \
                    and filter_predicate(row, predicate):
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
                  predicate=None):
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
                    and filter_operation(row, operation) \
                    and filter_predicate(row, predicate):
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

    log.debug('Calculating Percent Differences')
    tables.append((get_percent_differences(mc_rows),
                   'Frequency of Accuracy Difference for All Constraints',
                   'acc_diff_All'))
    tables.append((get_percent_differences(mc_rows, branch=True),
                   'Frequency of Accuracy Difference for True Branch '
                   'Constraints',
                   'acc_diff_True'))
    tables.append((get_percent_differences(mc_rows, branch=False),
                   'Frequency of Accuracy Difference for False Branch '
                   'Constraints',
                   'acc_diff_False'))

    tables.append((get_percent_differences(mc_rows, input_type="Concrete"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Concrete Input String',
                   'acc_diff_concrete'))
    tables.append((get_percent_differences(mc_rows, input_type="Uniform"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Simple Unknown Input String',
                   'acc_diff_simple_unknown'))
    tables.append((get_percent_differences(mc_rows, input_type="Non-Uniform"),
                   'Frequency of Accuracy Difference for Constraints Following'
                   ' a Branching Unknown Input String',
                   'acc_diff_branch_unknown'))

    tables.append((get_percent_differences(mc_rows, operation="concat"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{concat} Operation',
                   'acc_diff_concat'))
    tables.append((get_percent_differences(mc_rows, operation="delete"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{delete} Operation',
                   'acc_diff_delete'))
    tables.append((get_percent_differences(mc_rows, operation="replace"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{replace} Operation',
                   'acc_diff_replace'))
    tables.append((get_percent_differences(mc_rows, operation="reverse"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{reverse} Operation',
                   'acc_diff_reverse'))

    tables.append((get_percent_differences(mc_rows, predicate="contains"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{contains} Predicate',
                   'acc_diff_contains'))
    tables.append((get_percent_differences(mc_rows, predicate="equals"),
                   'Frequency of Accuracy Difference for Constraints '
                   'Containing a \\texttt{equals} Predicate',
                   'acc_diff_equals'))

    tables.append((get_percent_differences(mc_rows, length=1),
                   'Frequency of Accuracy Difference for Constraints '
                   'Following an Input String of Length 1',
                   'acc_diff_1'))
    tables.append((get_percent_differences(mc_rows, length=2),
                   'Frequency of Accuracy Difference for Constraints '
                   'Following an Input String of Length 2',
                   'acc_diff_2'))
    # tables.append((get_percent_differences(mc_rows, length=3),
    #                'Frequency of Accuracy Difference for Constraints '
    #                'Following an Input String of Length 3',
    #                'acc_diff_3'))

    # agreement
    agree_list = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    log.debug('Getting Agreements')

    temp = get_agreement(mc_rows)
    temp['Selection'] = 'All'
    agree_list.append(temp)

    agree_list.append(blank_row)

    temp = get_agreement(mc_rows, input_type="Concrete")
    temp['Selection'] = 'Concrete'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, input_type="Uniform")
    temp['Selection'] = 'Uniform'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, input_type="Non-Uniform")
    temp['Selection'] = 'Non-Uniform'
    agree_list.append(temp)

    agree_list.append(blank_row)

    temp = get_agreement(mc_rows, operation="concat")
    temp['Selection'] = '\\texttt{concat}'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, operation="delete")
    temp['Selection'] = '\\texttt{delete}'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, operation="replace")
    temp['Selection'] = '\\texttt{replace}'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, operation="reverse")
    temp['Selection'] = '\\texttt{reverse}'
    agree_list.append(temp)

    agree_list.append(blank_row)

    temp = get_agreement(mc_rows, predicate="contains")
    temp['Selection'] = '\\texttt{contains}'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, predicate="equals")
    temp['Selection'] = '\\texttt{equals}'
    agree_list.append(temp)

    agree_list.append(blank_row)

    temp = get_agreement(mc_rows, length=1)
    temp['Selection'] = 'Length 1'
    agree_list.append(temp)
    temp = get_agreement(mc_rows, length=2)
    temp['Selection'] = 'Length 2'
    agree_list.append(temp)
    # temp = get_agreement(mc_rows, length=3)
    # temp['Selection'] = 'Length 3'
    # agree_list.append(temp)

    tables.append((agree_list,
                   'Frequency of Branch Selection Agreement for Constraints',
                   'acc_agree'))

    return tables


def analyze_mc_performance(mc_rows, mc_time_rows):
    # initialize tables list
    tables = list()

    return tables


def analyze_solve_performance(mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    return tables


def perform_analysis(mc_rows, mc_time_rows, op_time_rows):
    tables = list()
    tables.extend(analyze_accuracy(mc_rows))

    tables.extend(analyze_mc_performance(mc_rows, mc_time_rows))

    tables.extend(analyze_solve_performance(mc_time_rows, op_time_rows))

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
