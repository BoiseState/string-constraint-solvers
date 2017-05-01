#! /usr/bin/env python
import argparse
import csv
import fnmatch
import json
import logging
import math
import re
import sys

import numpy
import os

# set relevant path and file variables
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
GLOB['len-match'][1] = re.compile('\w+-A[BCDE]-01(-\d+)?.csv')
GLOB['len-match'][2] = re.compile('\w+-A[BCDE]-02(-\d+)?.csv')
GLOB['len-match'][3] = re.compile('\w+-A[BCDE]-03(-\d+)?.csv')
GLOB['len-match'][4] = re.compile('\w+-A[BCDE]-04(-\d+)?.csv')
GLOB['alphabet-match'] = dict()
GLOB['alphabet-match']['AB'] = re.compile('\w+-AB-\d+(-\d+)?.csv')
GLOB['alphabet-match']['AC'] = re.compile('\w+-AC-\d+(-\d+)?.csv')
GLOB['alphabet-match']['AD'] = re.compile('\w+-AD-\d+(-\d+)?.csv')
GLOB['alphabet-match']['AE'] = re.compile('\w+-AE-\d+(-\d+)?.csv')

ANALYSIS_LIST = (
    'agree',
    'comb-time',
    'mc-stat',
    'mc-time',
    'op-time',
    'per-diff',
    'per-diff-vs-solve-time',
    'per-diff-vs-mc-time',
    'per-diff-vs-comb-time',
    'solve-time'
)

SOLVERS = (
    'Unbounded',
    'Bounded',
    'Aggregate',
    'Weighted'
)

OP_NORMS = {
    'concat': {
        'Simple': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 20, 2: 20, 3: 20, 4: 20},
            'AD': {1: 15, 2: 15, 3: 15, 4: 15},
            'AE': {1: 12, 2: 12, 3: 12, 4: 12}
        },
        'Even': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        },
        'Uneven': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        }
    },
    'delete': {
        'same': {
            'AB': {1: 30, 2: 20, 3: 15, 4: 12},
            'AC': {1: 30, 2: 20, 3: 15, 4: 12},
            'AD': {1: 30, 2: 20, 3: 15, 4: 12},
            'AE': {1: 30, 2: 20, 3: 15, 4: 12}
        },
        'diff': {
            'AB': {1: 60, 2: 20, 3: 10, 4: 6},
            'AC': {1: 60, 2: 20, 3: 10, 4: 6},
            'AD': {1: 60, 2: 20, 3: 10, 4: 6},
            'AE': {1: 60, 2: 20, 3: 10, 4: 6}
        }
    },
    'replace': {
        'same': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 20, 2: 20, 3: 20, 4: 20},
            'AD': {1: 15, 2: 15, 3: 15, 4: 15},
            'AE': {1: 12, 2: 12, 3: 12, 4: 12}
        },
        'diff': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 10, 2: 10, 3: 10, 4: 10},
            'AD': {1: 5, 2: 5, 3: 5, 4: 5},
            'AE': {1: 3, 2: 3, 3: 3, 4: 3}
        }
    },
    'reverse': {
        'none': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        }
    }
}

ORDER_COLUMNS = {
    'Bin': 1,
    'Selection': 2,
    'Unbounded': 3,
    'Bounded': 4,
    'Aggregate': 5,
    'Weighted': 6
}


# Classes
class Settings:
    def __init__(self, options):
        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)

        # determine analysis entries
        self.entries = list()
        for a in ANALYSIS_LIST:
            if a in options.analysis_list:
                self.entries.append(a)

        # initialize result file pattern
        self.file_pattern = options.data_files


def set_options(arguments):
    # process command line args
    analyze_parser = argparse.ArgumentParser(prog=__doc__,
                                             description='Analyze results.')

    analyses_help = 'List of analyses to run, includes any of the following: '\
                    + ','.join(ANALYSIS_LIST)
    analyze_parser.add_argument('-a',
                                '--analysis-list',
                                nargs='+',
                                default=list(),
                                help=analyses_help)

    analyze_parser.add_argument('-d',
                                '--debug',
                                help='Display debug messages for this script.',
                                action='store_true')

    analyze_parser.add_argument('data_files',
                                help='A Unix shell-style pattern which is '
                                     'used to match a set of result files.')

    GLOB['Settings'] = Settings(analyze_parser.parse_args(arguments))


def read_csv_data(file_path):
    # initialize rows list
    rows = list()

    # read csv rows
    log.debug('Reading in data from %s', file_path)
    with open(file_path, 'r') as csv_file:
        reader = csv.DictReader(csv_file,
                                delimiter='\t',
                                quoting=csv.QUOTE_NONE,
                                quotechar='|',
                                lineterminator='\n')
        for row in reader:
            normalize_row(row)
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


def get_entries():
    GLOB['entries'] = dict()

    def filter_entries(entry):
        return ('alphabet' not in entry or entry.get('alphabet') != 'AE') \
               and ('length' not in entry or entry.get('length') < 4)

    for e in GLOB['Settings'].entries:
        entry_file_path = os.path.join(project_dir,
                                       'data',
                                       'data-analysis-entries',
                                       '{0}-entries.json'.format(e))
        with open(entry_file_path, 'r') as entry_file:
            entries = json.load(entry_file)
            # entries = filter(filter_entries, entries)
            GLOB['entries'][e] = entries


def normalize_row(row):
    f_name = row.get('File')
    for a in ('AB', 'AC', 'AD', 'AE'):
        if GLOB.get('alphabet-match').get(a).match(f_name):
            for i in range(1, 5):
                if GLOB.get('len-match').get(i).match(f_name):
                    op_1 = row.get('Op 1')
                    op_2 = row.get('Op 2')
                    op_1_arg = row.get('Op 1 Arg')
                    op_2_arg = row.get('Op 2 Arg')
                    if op_1 and op_2:
                        norm_1 = OP_NORMS.get(op_1).get(op_1_arg).get(a).get(i)
                        norm_2 = OP_NORMS.get(op_2).get(op_2_arg).get(a).get(i)
                        norm = norm_1 * norm_2
                    elif op_1:
                        norm = OP_NORMS.get(op_1).get(op_1_arg).get(a).get(i)
                    else:
                        norm = 1
                    row['Norm'] = norm
                    return


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
    columns = sorted(table[0].keys(), key=order_columns)

    # create before table
    lines.append('\\begin{table}[h!]\n')
    lines.append(' ' * 4 + '\\centering\n')
    lines.append(' ' * 4 + '\\footnotesize\n')

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


def get_latex_plot_figure(caption, label):
    lines = list()

    lines.append('\\begin{figure}[h!]\n')
    lines.append(' ' * 4 + '\\centering\n')
    lines.append(' ' * 4 + '\\includegraphics[width=\\linewidth]{'
                 + label + '.png}')
    lines.append(' ' * 4 + '\\caption{' + caption + '}\n')
    lines.append(' ' * 4 + '\\label{tab:' + label + '}\n')
    lines.append('\\end{figure}\n')
    lines.append('\n\\clearpage\n')

    return lines


def output_latex(tables, plots):
    before_lines = list()
    before_lines.append('\\documentclass [11pt]{article}\n')
    before_lines.append('\n')
    before_lines.append('\\usepackage[utf8]{inputenc}\n')
    before_lines.append('\\usepackage[pdftex]{graphicx}\n')
    before_lines.append('\\usepackage{fullpage}\n')
    before_lines.append('\\usepackage[justification=centering]{caption}\n')
    before_lines.append('\\usepackage{tabu}\n')
    before_lines.append('\n')
    before_lines.append('\\graphicspath{{../plot-data/}}\n')
    before_lines.append('\n')
    before_lines.append('\\begin{document}\n')
    before_lines.append('\n')

    table_list = list()
    for table, caption, label in tables:
        table_list.append(get_latex_table(table, caption, label))

    figure_list = list()
    for data, plot_types, caption, label in plots:
        figure_list.append(get_latex_plot_figure(caption, label))

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


def output_plot_data_file(data, plot_types, label):
    plots = list()

    if 'boxplot' in plot_types:
        plots.append({
            'label': '{0}_boxplot',
            'columns': [(4, 'Values')],
            'first-columns': []
        })
    if 'histogram' in plot_types:
        plots.append({
            'label': '{0}_histogram',
            'columns': [(2, 'Values')],
            'first-columns': [(3, 'Bins')]
        })
    if 'scatter' in plot_types:
        plots.append({
            'label': '{0}_scatter',
            'columns': [(0, 'Per Diffs'), (1, 'Times'), (2, 'Weights')],
            'first-columns': []
        })

    for plot in plots:
        plot_data_file = plot['label'].format(label) + '.csv'
        data_file_path = os.path.join(project_dir, 'data', 'plot-data',
                                      plot_data_file)
        log.debug('Creating Plot Data File: %s', plot_data_file)
        with open(data_file_path, 'w') as csv_file:
            # write header
            for column in plot.get('first-columns'):
                csv_file.write(column[1])
                csv_file.write('\t')
            for s in SOLVERS:
                for column in plot.get('columns'):
                    csv_file.write('{0} {1}'.format(s[0], column[1]))
                    csv_file.write('\t')
            csv_file.write('\n')

            # write rows
            for i in range(data.get(SOLVERS[0])[plot.get('columns')[0][0]].size):
                for column in plot.get('first-columns'):
                    csv_file.write(str(data.get(SOLVERS[0])[column[0]][i]))
                    csv_file.write('\t')
                for s in SOLVERS:
                    for column in plot.get('columns'):
                        csv_file.write(str(data.get(s)[column[0]][i]))
                        csv_file.write('\t')
                csv_file.write('\n')


def output_plot_script(boxplot_lines, histogram_lines, scatter_lines):
    out_path = os.path.join(project_dir, 'data', 'plot-data', 'all-plots.gnu')
    with open(out_path, 'w') as out_file:
        # write boxplots
        out_file.write('set style fill solid 0.25 noborder\n')
        out_file.write('set style boxplot nooutliers\n')
        out_file.write('set style data boxplot\n')
        out_file.write('set datafile separator "\\t"\n')
        out_file.write('unset key\n')
        out_file.write('set border 2\n')
        out_file.write('set xtics ("Unbouned" 1, "Bounded" 2,'
                       ' "Aggregate" 3, "Weighted" 4)\n')
        out_file.write('set xtics nomirror\n')
        out_file.write('unset ytics\n')
        out_file.write('set logscale y 2\n')
        out_file.write('set term png\n')
        out_file.writelines(boxplot_lines)

        # write histograms
        out_file.write('set style data histogram\n')
        out_file.write('set style histogram clustered\n')
        out_file.write('set key on\n')
        out_file.write('unset xtics\n')
        out_file.write('set xtics nomirror rotate by -45\n')
        out_file.write('set logscale y 2\n')
        out_file.writelines(histogram_lines)

        # write scatters
        out_file.write('set style line\n')
        out_file.write('set style data dots\n')
        out_file.write('set logscale y 2\n')
        out_file.write('set term png size 1000, 1000\n')
        out_file.writelines(scatter_lines)


def get_script_lines(data, plot_types, label):
    boxplot_lines = list()
    hist_lines = list()
    scatter_lines = list()

    if 'boxplot' in plot_types:
        boxplot_lines.append('set output "' + label + '_boxplot.png"\n')
        boxplot_lines.append('plot "' + label + '_boxplot.csv" '
                             'using (1):(column(1)) ti col, \\\n'
                             '' * (len(label) + 17) +
                             '"" using (2):(column(2)) ti col, \\\n'
                             '' * (len(label) + 17) +
                             '"" using (3):(column(3)) ti col, \\\n'
                             '' * (len(label) + 17) +
                             '"" using (4):(column(4)) ti col\n')
    if 'histogram' in plot_types:
        hist_lines.append('set output "' + label + '_histogram.png"\n')
        hist_lines.append('plot "' + label + '_histogram.csv" '
                                             'using 2:xtic(1) ti col, \\\n'
                                             '' * (len(label) + 19) +
                                             '"" using 3 ti col, \\\n'
                                             '' * (len(label) + 19) +
                                             '"" using 4 ti col, \\\n'
                                             '' * (len(label) + 19) +
                                             '"" using 5 ti col\n')
    if 'scatter' in plot_types:
        scatter_lines.append('set output "' + label + '_scatter.png"\n')
        scatter_lines.append('plot "' + label + '_scatter.csv" '
                             'using "U Times":"U Per Diffs" ti '
                             '"Unbounded", \\\n'
                             '' * (len(label) + 17) +
                             '"" using "B Times":"B Per Diffs" ti '
                             '"Bounded", \\\n'
                             '' * (len(label) + 17) +
                             '"" using "A Times":"A Per Diffs" ti '
                             '"Aggregate", \\\n'
                             '' * (len(label) + 17) +
                             '"" using "W Times":"W Per Diffs" ti "Weighted"\n')

    return boxplot_lines, hist_lines, scatter_lines


def output_plot_files(files):
    script_lines = dict()
    script_lines['boxplot'] = list()
    script_lines['histogram'] = list()
    script_lines['scatter'] = list()

    # ensure output directories exists
    out_dir_path = os.path.join(project_dir, 'data', 'plot-data')
    if not os.path.isdir(out_dir_path):
        os.makedirs(out_dir_path)

    for data, plot_types, caption, label in files:
        output_plot_data_file(data, plot_types, label)

        new_lines = get_script_lines(data, plot_types, label)
        script_lines['boxplot'].extend(new_lines[0])
        script_lines['histogram'].extend(new_lines[1])
        script_lines['scatter'].extend(new_lines[2])

    output_plot_script(script_lines.get('boxplot'),
                       script_lines.get('histogram'),
                       script_lines.get('scatter'))


def filter_disagree(row, prefix, disagree=True):
    return disagree or row.get(prefix + ' Agree')


def filter_input_type(row, input_type=None):
    return input_type is None or row.get('Input Type') == input_type


def filter_length(row, length=None):
    return length is None \
           or GLOB.get('len-match').get(length).match(row.get('File'))


def filter_alphabet(row, alphabet=None):
    return alphabet is None \
           or GLOB.get('alphabet-match').get(alphabet).match(row.get('File'))


def filter_operation(row, operation=None, exclusive=False, arg_type=None, ):
    return operation is None \
           or ('Op' in row
               and row.get('Op') == operation
               and (arg_type is None
                    or row.get('Args') == arg_type)) \
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


def compute_mc_per(row, prefix, branch_sel=True):
    # compute percent difference
    b = ' T ' if branch_sel else ' F '
    in_mc = float(row.get(prefix + ' In MC'))
    b_mc = int(row.get(prefix + b + 'MC'))
    b_per = 0
    if in_mc > 0:
        b_per = b_mc / in_mc
    return b_per


def compute_per_diff(row, prefix, branch_sel=True):
    # compute percent difference
    b = ' T ' if branch_sel else ' F '
    c_in_mc = float(row.get('C In MC'))
    c_b_mc = int(row.get('C' + b + 'MC'))
    c_b_per = 0
    if c_in_mc > 0:
        c_b_per = c_b_mc / c_in_mc
    in_mc = float(row.get(prefix + ' In MC'))
    b_mc = int(row.get(prefix + b + 'MC'))
    b_per = 0
    if in_mc > 0:
        b_per = b_mc / in_mc
    return abs(c_b_per - b_per)


def compute_agreement(row, prefix):
    # compute agreement
    c_in_mc = float(row.get('C In MC'))
    c_t_mc = int(row.get('C T MC'))
    c_f_mc = int(row.get('C F MC'))
    c_t_per = 0
    if c_in_mc > 0:
        c_t_per = c_t_mc / c_in_mc
    c_f_per = 0
    if c_in_mc > 0:
        c_f_per = c_f_mc / c_in_mc
    in_mc = float(row.get(prefix + ' In MC'))
    t_mc = int(row.get(prefix + ' T MC'))
    f_mc = int(row.get(prefix + ' F MC'))
    t_per = 0
    if in_mc > 0:
        t_per = t_mc / in_mc
    f_per = 0
    if in_mc > 0:
        f_per = f_mc / in_mc

    return (c_t_per >= c_f_per and t_per >= f_per) or \
           (c_t_per < c_f_per and t_per < f_per)


def weighted_quantile(values, quantiles, sample_weight=None,
                      values_sorted=False, old_style=False):
    """ Very close to numpy.percentile, but supports weights.
    NOTE: quantiles should be in [0, 1]!
    :param values: numpy.array with data
    :param quantiles: array-like with many quantiles needed
    :param sample_weight: array-like of the same length as `array`
    :param values_sorted: bool, if True, then will avoid sorting of initial 
     array
    :param old_style: if True, will correct output to be consistent with
     numpy.percentile.
    :return: numpy.array with computed quantiles.
    """
    values = numpy.array(values)
    quantiles = numpy.array(quantiles)
    if sample_weight is None:
        sample_weight = numpy.ones(len(values))
    sample_weight = numpy.array(sample_weight)
    assert numpy.all(quantiles >= 0) and numpy.all(
        quantiles <= 1), 'quantiles should be in [0, 1]'

    if not values_sorted:
        sorter = numpy.argsort(values)
        values = values[sorter]
        sample_weight = sample_weight[sorter]

    weighted_quantiles = numpy.cumsum(sample_weight) - 0.5 * sample_weight
    if old_style:
        # To be convenient with numpy.percentile
        weighted_quantiles -= weighted_quantiles[0]
        weighted_quantiles /= weighted_quantiles[-1]
    else:
        weighted_quantiles /= numpy.sum(sample_weight)
    return numpy.interp(quantiles, weighted_quantiles, values)


def get_per_diffs(rows,
                  disagree=None,
                  bins=None,
                  branch=None,
                  raw=False,
                  input_type=None,
                  alphabet=None,
                  length=None,
                  operation=None,
                  exclusive_op=None,
                  op_arg_type=None,
                  predicate=None,
                  pred_arg_type=None):
    # initialize structures
    if bins is None:
        bins = [0.0, 0.000001, 0.01, 0.03, 0.05, 1.0]
    results = list()
    per_diff_map = dict()

    def get_filter(prefix):
        def per_diff_filter(row):
            return row.get('Op 1') != '' \
                   and filter_input_type(row, input_type) \
                   and filter_alphabet(row, alphabet) \
                   and filter_length(row, length) \
                   and filter_operation(row, operation, exclusive_op,
                                        op_arg_type) \
                   and filter_predicate(row, predicate, pred_arg_type) \
                   and (disagree is None or compute_agreement(row, prefix))

        return per_diff_filter

    for solver in SOLVERS:
        diffs = list()
        weights = list()
        filtered = filter(get_filter(solver[0]), rows)
        if branch is None or branch:
            diffs.extend(
                map(lambda x: compute_per_diff(x, solver[0]), filtered))
            weights.extend(map(lambda x: x.get('Norm'), filtered))

        if branch is None or not branch:
            diffs.extend(
                map(lambda x: compute_per_diff(x, solver[0], branch_sel=False),
                    filtered))
            weights.extend(map(lambda x: x.get('Norm'), filtered))

        diffs_np = numpy.asarray(diffs)
        weights_np = numpy.asarray(weights)

        if raw:
            per_diff_map[solver] = diffs_np
        else:
            per_diff_map[solver] = numpy.histogram(diffs_np, bins=bins,
                                                   weights=weights_np)

    if raw:
        return per_diff_map

    for i, p in enumerate(bins[1:]):
        result = dict()
        result['Bin'] = '{0:d}\\%'.format(int(100 * p))
        for solver in SOLVERS:
            count = numpy.sum(per_diff_map.get(solver)[0])
            per_diff = 100 * (per_diff_map.get(solver)[0][i] / float(count))
            result[solver] = '{0:.1f}\\%'.format(per_diff)
        results.append(result)

    return results


def get_agreement(rows,
                  input_type=None,
                  length=None,
                  alphabet=None,
                  operation=None,
                  exclusive_op=None,
                  op_arg_type=None,
                  predicate=None,
                  pred_arg_type=None):
    # initialize result dictionary
    results = dict()

    def agree_filter(row):
        return row.get('Op 1') != '' \
               and filter_input_type(row, input_type) \
               and filter_alphabet(row, alphabet) \
               and filter_length(row, length) \
               and filter_operation(row, operation, exclusive_op,
                                    op_arg_type) \
               and filter_predicate(row, predicate, pred_arg_type)

    filtered = filter(agree_filter, rows)

    for solver in SOLVERS:
        agree_rows = filter(lambda x: compute_agreement(x, solver[0]), filtered)
        agree_weights = map(lambda x: x.get('Norm'), agree_rows)
        all_weights = map(lambda x: x.get('Norm'), filtered)
        agree_count = sum(agree_weights)
        all_count = sum(all_weights)
        per = 100 * (float(agree_count) / all_count)
        results[solver] = '{0:.1f}\\%'.format(per)

    return results


def get_perf_metrics(rows,
                     mc_time=None,
                     acc_time=False,
                     pred_time=None,
                     op_time=False,
                     input_type=None,
                     length=None,
                     alphabet=None,
                     operation=None,
                     exclusive_op=None,
                     op_arg_type=None,
                     predicate=None,
                     pred_arg_type=None):
    avg_results = dict()
    median_results = dict()
    v_results = dict()
    s_d_results = dict()
    data_results = dict()

    def perf_metric_filter(r):
        return r.get('Op 1') != '' \
               and filter_input_type(r, input_type) \
               and filter_alphabet(r, alphabet) \
               and filter_length(r, length) \
               and filter_operation(r, operation, exclusive_op, op_arg_type) \
               and filter_predicate(r, predicate, pred_arg_type)

    filtered = filter(perf_metric_filter, rows)
    min_val = 0.
    max_val = 0.

    # get weights
    weights_np = numpy.asarray(map(lambda r: int(r.get('Norm')), filtered))
    if mc_time == 'both' or pred_time == 'both':
        weights_np = numpy.repeat(weights_np, 2)

    for solver in SOLVERS:
        times_np = numpy.empty([0, weights_np.size])
        s = solver[0].upper()

        # get mc times
        if mc_time is not None:
            mc_times_np = numpy.empty(0)
            if mc_time == 'both':
                temp = list()
                for row in filtered:
                    temp.append(int(row.get(s + ' T MC Time')))
                    temp.append(int(row.get(s + ' F MC Time')))
                mc_times_np = numpy.asarray(temp)
            else:
                if mc_time == 'true':
                    mc_times_np = numpy.asarray(
                        map(lambda r: int(r.get(s + ' T MC Time')), filtered))
                elif mc_time == 'false':
                    mc_times_np = numpy.asarray(
                        map(lambda r: int(r.get(s + ' F MC Time')), filtered))

                if pred_time == 'both':
                    mc_times_np = numpy.repeat(mc_times_np, 2)

            times_np = numpy.append(times_np, [mc_times_np], axis=0)

        # get acc times
        if acc_time:
            acc_times_np = numpy.asarray(
                map(lambda r: int(r.get(s + ' Acc Time')), filtered))

            if mc_time == 'both' or pred_time == 'both':
                acc_times_np = numpy.repeat(acc_times_np, 2)

            times_np = numpy.append(times_np, [acc_times_np], axis=0)

        # get pred times
        if pred_time is not None:
            pred_times_np = numpy.empty(0)
            if pred_time == 'both':
                temp = list()
                for row in filtered:
                    temp.append(int(row.get(s + ' T Pred Time')))
                    temp.append(int(row.get(s + ' F Pred Time')))
                pred_times_np = numpy.asarray(temp)
            else:
                if pred_time == 'true':
                    pred_times_np = numpy.asarray(
                        map(lambda r: int(r.get(s + ' T Pred Time')), filtered))
                elif pred_time == 'false':
                    pred_times_np = numpy.asarray(
                        map(lambda r: int(r.get(s + ' F Pred Time')), filtered))

                if pred_time == 'both':
                    pred_times_np = numpy.repeat(pred_times_np, 2)

            times_np = numpy.append(times_np, [pred_times_np], axis=0)

        # get op times
        if op_time:
            op_times = map(lambda r: int(r.get(s + ' Op Time')), filtered)
            times_np = numpy.append(times_np,
                                    [(numpy.asarray(op_times))],
                                    axis=0)

        # sum times
        if len(times_np.shape) == 2 and times_np.shape[0] > 1:
            times_np = numpy.sum(times_np, axis=0)
        else:
            times_np = times_np[0]

        l_min = numpy.nanmin(times_np)
        if l_min < min_val or min_val == 0:
            min_val = l_min
        l_max = numpy.nanmax(times_np)
        if l_max > max_val:
            max_val = l_max
        mean = numpy.average(times_np, weights=weights_np)
        avg_results[solver] = '{0:.1f}'.format(mean)

        quantiles = weighted_quantile(times_np,
                                      numpy.arange(0.0, 1.0, 0.01),
                                      sample_weight=weights_np)
        median_results[solver] = '{0:.1f}'.format(quantiles[len(quantiles)/2])
        # median_results[solver] = '{0:.1f}'.format(numpy.median(w_times_np))

        sq_d = numpy.apply_along_axis(lambda x: (x - mean) ** 2, 0, times_np)
        var_result = numpy.average(sq_d, weights=weights_np)
        v_results[solver] = '{0:.1f}'.format(var_result)
        # variance_results[solver] = '{0:.1f}'.format(numpy.var(w_times_np))

        s_d_results[solver] = '{0:.1f}'.format(math.sqrt(var_result))
        # std_dev_results[solver] = '{0:.1f}'.format(numpy.std(w_times_np))

        data_results[solver] = [times_np, weights_np, None, None, quantiles]

    interval = (max_val - min_val) / 20.
    bins = numpy.arange(min_val, (max_val + interval), interval)
    for s in SOLVERS:
        hist = numpy.histogram(data_results.get(s)[0], bins=bins,
                               weights=data_results.get(s)[1])
        data_results.get(s)[2] = hist[0]
        data_results.get(s)[3] = hist[1]

    return avg_results, median_results, v_results, s_d_results, data_results


def process_perf_entries(rows, entries, caption_prefix):
    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    lists = (list(), list(), list(), list(), list())

    for entry in entries:
        if 'is_blank' in entry and entry.get('is_blank'):
            results = (blank_row, blank_row, blank_row, blank_row)
        else:
            log.debug('Getting %s - %s', caption_prefix, entry.get('Selection'))
            results = get_perf_metrics(rows,
                                       mc_time=entry.get('mc_time_branch'),
                                       acc_time=entry.get('acc_time'),
                                       pred_time=entry.get('pred_time_branch'),
                                       op_time=entry.get('op_time'),
                                       input_type=entry.get('input_type'),
                                       alphabet=entry.get('alphabet'),
                                       length=entry.get('length'),
                                       operation=entry.get('operation'),
                                       exclusive_op=entry.get('exclusive_op'),
                                       op_arg_type=entry.get('op_arg_type'),
                                       predicate=entry.get('predicate'),
                                       pred_arg_type=entry.get('pred_arg_type'))
            results[0]['Selection'] = entry.get('Selection')
            results[1]['Selection'] = entry.get('Selection')
            results[2]['Selection'] = entry.get('Selection')
            results[3]['Selection'] = entry.get('Selection')
            lists[4].append((results[4],
                             ['boxplot', 'histogram'],
                             caption_prefix + ' - ' + entry.get('Selection'),
                             entry.get('label')))
        lists[0].append(results[0])
        lists[1].append(results[1])
        lists[2].append(results[2])
        lists[3].append(results[3])

    return lists


def get_acc_vs_perf_files(acc_rows, perf_rows, entries, caption_prefix):

    # initialize file list
    files = list()

    for entry in entries:
        log.debug('Getting %s - %s', caption_prefix, entry.get('Selection'))
        table = get_per_diffs(acc_rows,
                              raw=True,
                              disagree=entry.get('disagree'),
                              branch=entry.get('branch'),
                              input_type=entry.get('input_type'),
                              alphabet=entry.get('alphabet'),
                              length=entry.get('length'),
                              operation=entry.get('operation'),
                              exclusive_op=entry.get('exclusive_op'),
                              op_arg_type=entry.get('op_arg_type'),
                              predicate=entry.get('predicate'),
                              pred_arg_type=entry.get('pred_arg_type'))

        results = get_perf_metrics(perf_rows,
                                   mc_time=entry.get('mc_time_branch'),
                                   acc_time=entry.get('acc_time'),
                                   pred_time=entry.get('pred_time_branch'),
                                   op_time=entry.get('op_time'),
                                   input_type=entry.get('input_type'),
                                   alphabet=entry.get('alphabet'),
                                   length=entry.get('length'),
                                   operation=entry.get('operation'),
                                   exclusive_op=entry.get('exclusive_op'),
                                   op_arg_type=entry.get('op_arg_type'),
                                   predicate=entry.get('predicate'),
                                   pred_arg_type=entry.get('pred_arg_type'))
        values = dict()
        for solver in SOLVERS:
            values[solver] = (table.pop(solver, None),
                              results[4].get(solver)[0],
                              results[4].get(solver)[1])

        files.append((values,
                      'scatter',
                      caption_prefix + ' - ' + entry.get('Selection'),
                      entry.get('label')))

    return files


def get_per_diff_tables(mc_rows):
    tables = list()

    log.debug('Calculating Model Count Accuracy')

    for entry in GLOB.get('entries').get('per-diff'):
        log.debug('Processing %s', entry.get('caption'))
        table = get_per_diffs(mc_rows,
                              disagree=entry.get('disagree'),
                              branch=entry.get('branch'),
                              input_type=entry.get('input_type'),
                              alphabet=entry.get('alphabet'),
                              length=entry.get('length'),
                              operation=entry.get('operation'),
                              exclusive_op=entry.get('exclusive_op'),
                              op_arg_type=entry.get('op_arg_type'),
                              predicate=entry.get('predicate'),
                              pred_arg_type=entry.get('pred_arg_type'))
        tables.append((table, entry.get('caption'), entry.get('label')))

    return tables


def get_agree_tables(rows):
    tables = list()

    # agreement
    agree_results = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    for entry in GLOB.get('entries').get('agree'):
        if 'is_blank' in entry and entry.get('is_blank'):
            agree_results.append(blank_row)
        else:
            log.debug('Processing Agreement Percent for %s',
                      entry.get('Selection'))
            row = get_agreement(rows,
                                input_type=entry.get('input_type'),
                                alphabet=entry.get('alphabet'),
                                length=entry.get('length'),
                                operation=entry.get('operation'),
                                exclusive_op=entry.get('exclusive_op'),
                                op_arg_type=entry.get('op_arg_type'),
                                predicate=entry.get('predicate'),
                                pred_arg_type=entry.get('pred_arg_type'))
            row['Selection'] = entry.get('Selection')
            agree_results.append(row)

    tables.append((agree_results,
                   'Frequency of Branch Selection Agreement for '
                   'Constraints',
                   'acc_agree'))

    return tables


def get_mc_time_tables_and_files(rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Model Count Performance')

    results = process_perf_entries(rows,
                                   GLOB.get('entries').get('mc-time'),
                                   'Model Counting Times')

    tables.append((results[0],
                   'Average Model Counting Times',
                   'mc_perf_avg'))
    tables.append((results[1],
                   'Median Model Counting Times',
                   'mc_perf_median'))
    tables.append((results[2],
                   'Model Counting Time Variance',
                   'mc_perf_var'))
    tables.append((results[3],
                   'Standard Deviation for Model Counting Times',
                   'mc_perf_std_dev'))

    files.extend(results[4])

    return tables, files


def get_solve_time_tables_and_files(rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Constraint Solving Performance')

    results = process_perf_entries(rows,
                                   GLOB.get('entries').get('solve-time'),
                                   'Constraint Solving Times')

    tables.append((results[0],
                   'Average Constraint Solving Times',
                   'solve_perf_acc_avg'))
    tables.append((results[1],
                   'Median Constraint Solving Times',
                   'solve_perf_acc_median'))
    tables.append((results[2],
                   'Constraint Solving Time Variance',
                   'solve_perf_acc_var'))
    tables.append((results[3],
                   'Standard Deviation for Constraint Solving Times',
                   'solve_perf_acc_std_dev'))

    files.extend(results[4])

    return tables, files


def get_op_tables_and_files(rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Operation and Predicate Performance')

    results = process_perf_entries(rows,
                                   GLOB.get('entries').get('op-time'),
                                   'Operation and Predicate Times')

    tables.append((results[0],
                   'Average Operation and Predicate Times',
                   'solve_perf_op_avg'))
    tables.append((results[1],
                   'Median Operation and Predicate Times',
                   'solve_perf_op_median'))
    tables.append((results[2],
                   'Operation and Predicate Time Variance',
                   'solve_perf_op_var'))
    tables.append((results[3],
                   'Standard Deviation for Operation and Predicate Times',
                   'solve_perf_op_std_dev'))

    files.extend(results[4])

    return tables, files


def get_comb_time_tables_and_files(rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Combined Model Counting and Solver Performance')

    results = process_perf_entries(rows,
                                   GLOB.get('entries').get('comb-time'),
                                   'Combined Model Counting and Constraint'
                                   ' Solving Times')

    tables.append((results[0],
                   'Average Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_avg'))
    tables.append((results[1],
                   'Median Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_median'))
    tables.append((results[2],
                   'Combined Model Counting and Constraint Solving Time'
                   ' Variance',
                   'comb_perf_var'))
    tables.append((results[3],
                   'Standard Deviation for Combined Model Counting and '
                   'Constraint Solving Times',
                   'comb_perf_std_dev'))

    files.extend(results[4])

    return tables, files


def get_p_values(rows,
                 branch=None,
                 input_type=None,
                 length=None,
                 alphabet=None,
                 operation=None,
                 exclusive_op=None,
                 op_arg_type=None,
                 predicate=None,
                 pred_arg_type=None):
    results = dict()

    def row_filter(row):
        return row.get('Op 1') != '' \
               and filter_input_type(row, input_type) \
               and filter_alphabet(row, alphabet) \
               and filter_length(row, length) \
               and filter_operation(row, operation, exclusive_op, op_arg_type) \
               and filter_predicate(row, predicate, pred_arg_type)

    filtered = filter(row_filter, rows)
    weights_np = numpy.asarray(map(lambda r: r.get('Norm'), filtered))
    per_keys = {'C-T': 0, 'C-F': 1, 'U-T': 2, 'U-F': 3, 'B-T': 4, 'B-F': 5,
                'A-T': 6, 'A-F': 7, 'W-T': 8, 'W-F': 9}
    mc_per_map = dict()

    for solver in SOLVERS:
        if branch is None or branch:
            temp = map(lambda r: compute_mc_per(r, solver[0], branch_sel=True),
                       filtered)

        if branch is None or not branch:
            temp = map(lambda r: compute_mc_per(r, solver[0], branch_sel=True),
                       filtered)

    return results


def get_mc_test_tables(rows):
    # initialize tables list
    tables = list()

    log.debug('Computing p-values for Model Count Percentage')

    for entry in GLOB.get('entries').get('per-diff'):
        log.debug('Processing %s', entry.get('caption'))
        table = get_p_values(rows,
                             branch=entry.get('branch'),
                             input_type=entry.get('input_type'),
                             alphabet=entry.get('alphabet'),
                             length=entry.get('length'),
                             operation=entry.get('operation'),
                             exclusive_op=entry.get('exclusive_op'),
                             op_arg_type=entry.get('op_arg_type'),
                             predicate=entry.get('predicate'),
                             pred_arg_type=entry.get('pred_arg_type'))
        tables.append((table, entry.get('caption'), entry.get('label')))

    return tables


def analyze_accuracy(mc_rows):
    # initialize tables list
    tables = list()

    if 'mc-stat' in GLOB['entries']:
        test_tables = get_mc_test_tables(mc_rows)
        tables.extend(test_tables)

    if 'per-diff' in GLOB['entries']:
        per_diff_tables = get_per_diff_tables(mc_rows)
        tables.extend(per_diff_tables)

    if 'agree' in GLOB['entries']:
        agree_tables = get_agree_tables(mc_rows)
        tables.extend(agree_tables)

    return tables


def analyze_perf(mc_time_rows, op_time_rows):
    tables = list()
    files = list()

    if 'mc-time' in GLOB.get('entries'):
        mc_perf_tables, mc_perf_files = \
            get_mc_time_tables_and_files(mc_time_rows)
        tables.extend(mc_perf_tables)
        files.extend(mc_perf_files)

    if 'solve-time' in GLOB.get('entries'):
        solve_perf_tables, solve_perf_files = \
            get_solve_time_tables_and_files(mc_time_rows)
        tables.extend(solve_perf_tables)
        files.extend(solve_perf_files)

    if 'op-time' in GLOB.get('entries'):
        op_perf_tables, op_perf_files = get_op_tables_and_files(op_time_rows)
        tables.extend(op_perf_tables)
        files.extend(op_perf_files)

    if 'comb-time' in GLOB.get('entries'):
        comb_perf_tables, comb_perf_files = \
            get_comb_time_tables_and_files(mc_time_rows)
        tables.extend(comb_perf_tables)
        files.extend(comb_perf_files)

    return tables, files


def analyze_acc_vs_perf(mc_rows, mc_time_rows):
    files = list()

    if 'per-diff-vs-solve-time' in GLOB.get('entries'):
        files.extend(get_acc_vs_perf_files(mc_rows, mc_time_rows,
                                           GLOB.get('entries').get(
                                             'per-diff-vs-solve-time'),
                                           'Plot of Percent Difference vs '
                                           'Constraint Solving Time'))

    if 'per-diff-vs-mc-time' in GLOB.get('entries'):
        files.extend(get_acc_vs_perf_files(mc_rows, mc_time_rows,
                                           GLOB.get('entries').get(
                                             'per-diff-vs-mc-time'),
                                           'Plot of Percent Difference vs Model'
                                           ' Counting Time'))

    if 'per-diff-vs-comb-time' in GLOB.get('entries'):
        files.extend(get_acc_vs_perf_files(mc_rows, mc_time_rows,
                                           GLOB.get('entries').get(
                                             'per-diff-vs-comb-time'),
                                           'Plot of Percent Difference vs '
                                           'Combined Model Counting and '
                                           'Constraint Solving Time'))

    return files


def perform_analysis(mc_rows, mc_time_rows, op_time_rows):
    # create lists
    tables = list()
    files = list()

    acc_tables = analyze_accuracy(mc_rows)
    tables.extend(acc_tables)

    perf_tables, perf_files = analyze_perf(mc_time_rows, op_time_rows)
    tables.extend(perf_tables)
    files.extend(perf_files)

    comp_files = analyze_acc_vs_perf(mc_rows, mc_time_rows)
    files.extend(comp_files)

    return tables, files


def output_results(tables, files):
    output_plot_files(files)
    output_latex(tables, files)


def main(arguments):
    # set options from args
    set_options(arguments)

    # read data
    mc_data, mc_time_data, op_time_data = get_data()

    # get analysis entries
    get_entries()

    # perform analysis
    tables, files = perform_analysis(mc_data, mc_time_data, op_time_data)

    # output results
    output_results(tables, files)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
