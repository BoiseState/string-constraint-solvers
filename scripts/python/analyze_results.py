#! /usr/bin/env python

# import standard libraries
import argparse
import csv
import fnmatch
import json
import logging
import os
import sys

# import third party libraries
import numpy
import scipy.stats

# import constants
from constants import ANALYSIS_LIST
from constants import MEASUREMENTS
from constants import SOLVERS
from constants import D_TYPES
from constants import OUT_FORMATS
from constants import DATA_SETS
from constants import ALPHABETS
from constants import LENGTHS
from constants import OPS_AND_PREDS
from constants import IN_AND_ARG_TYPE
from constants import OP_NORMS
from constants import ORDER_COLUMNS

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

CONVERTERS = {
    'const': {
        'Alphabet': lambda r: ALPHABETS.index(r.get('File')[4:6]),
        'Length': lambda r: int(r.get('File')[7:9]),
        'U_Agree': lambda r: compute_agreement(r, 'U'),
        'B_Agree': lambda r: compute_agreement(r, 'B'),
        'A_Agree': lambda r: compute_agreement(r, 'A'),
        'W_Agree': lambda r: compute_agreement(r, 'W'),
        'C_T_Per_Diff': lambda r: compute_per_diff(r, 'C'),
        'U_T_Per_Diff': lambda r: compute_per_diff(r, 'U'),
        'B_T_Per_Diff': lambda r: compute_per_diff(r, 'B'),
        'A_T_Per_Diff': lambda r: compute_per_diff(r, 'A'),
        'W_T_Per_Diff': lambda r: compute_per_diff(r, 'W'),
        'C_F_Per_Diff': lambda r: compute_per_diff(r, 'C', branch_sel=False),
        'U_F_Per_Diff': lambda r: compute_per_diff(r, 'U', branch_sel=False),
        'B_F_Per_Diff': lambda r: compute_per_diff(r, 'B', branch_sel=False),
        'A_F_Per_Diff': lambda r: compute_per_diff(r, 'A', branch_sel=False),
        'W_F_Per_Diff': lambda r: compute_per_diff(r, 'W', branch_sel=False),
        'C_T_Solve_Time': lambda r: (int(r.get('C Acc Time')) +
                                     int(r.get('C T Pred Time'))),
        'U_T_Solve_Time': lambda r: (int(r.get('U Acc Time')) +
                                     int(r.get('U T Pred Time'))),
        'B_T_Solve_Time': lambda r: (int(r.get('B Acc Time')) +
                                     int(r.get('B T Pred Time'))),
        'A_T_Solve_Time': lambda r: (int(r.get('A Acc Time')) +
                                     int(r.get('A T Pred Time'))),
        'W_T_Solve_Time': lambda r: (int(r.get('W Acc Time')) +
                                     int(r.get('W T Pred Time'))),
        'C_F_Solve_Time': lambda r: (int(r.get('C Acc Time')) +
                                     int(r.get('C F Pred Time'))),
        'U_F_Solve_Time': lambda r: (int(r.get('U Acc Time')) +
                                     int(r.get('U F Pred Time'))),
        'B_F_Solve_Time': lambda r: (int(r.get('B Acc Time')) +
                                     int(r.get('B F Pred Time'))),
        'A_F_Solve_Time': lambda r: (int(r.get('A Acc Time')) +
                                     int(r.get('A F Pred Time'))),
        'W_F_Solve_Time': lambda r: (int(r.get('W Acc Time')) +
                                     int(r.get('W F Pred Time'))),
        'Input_Type': lambda r: IN_AND_ARG_TYPE.index(r.get('Input Type')),
        'Op_1': lambda r: OPS_AND_PREDS.index(r.get('Op 1')),
        'Op_1_Arg': lambda r: IN_AND_ARG_TYPE.index(r.get('Op 1 Arg')),
        'Op_2': lambda r: OPS_AND_PREDS.index(r.get('Op 2')),
        'Op_2_Arg': lambda r: IN_AND_ARG_TYPE.index(r.get('Op 2 Arg')),
        'Pred': lambda r: OPS_AND_PREDS.index(r.get('Pred')),
        'Pred_Arg': lambda r: IN_AND_ARG_TYPE.index(r.get('Pred Arg')),
        'Norm': lambda r: get_norm(r)
    },
    'op': {
        'Alphabet': lambda r: ALPHABETS.index(r.get('File')[4:6]),
        'Length': lambda r: int(r.get('File')[7:9]),
        'Op': lambda r: OPS_AND_PREDS.index(r.get('Op')),
        'In_Type': lambda r: IN_AND_ARG_TYPE.index(r.get('In Type')),
        'Args': lambda r: IN_AND_ARG_TYPE.index(r.get('Args')),
        'Norm': lambda r: get_norm(r)
    }
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
        self.entries.append('const')
        self.entries.append('op')
        for a in ANALYSIS_LIST:
            if a in options.analysis_list:
                self.entries.append(a)

        # initialize result file pattern
        self.file_pattern = options.data_files


def get_init_max_len(f_name):
    for i in LENGTHS:
        if GLOB.get('len-match').get(i).match(f_name):
            return i
    return -1


def get_alphabet(f_name):
    for a in ALPHABETS:
        if GLOB.get('alphabet-match').get(a).match(f_name):
            return a
    return ''


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


def get_entries():
    GLOB['entries'] = dict()

    for e in GLOB['Settings'].entries:
        entry_file_path = os.path.join(project_dir,
                                       'data',
                                       'data-analysis-entries',
                                       '{0}-entries.json'.format(e))
        with open(entry_file_path, 'r') as entry_file:
            entries = json.load(entry_file)
            GLOB['entries'][e] = entries


def get_norm(row):
    op_column = 'Op 1'
    op_arg_column = 'Op 1 Arg'
    if row.get(op_column) is None:
        op_column = 'Op'
        op_arg_column = 'Args'
    if row.get(op_column) and row.get('Op 2'):
        norm_1 = OP_NORMS.get(row.get('Op 1'))\
                         .get(row.get('Op 1 Arg'))\
                         .get(row.get('File')[4:6])\
                         .get(int(row.get('File')[7:9]))
        norm_2 = OP_NORMS.get(row.get('Op 2'))\
                         .get(row.get('Op 2 Arg'))\
                         .get(row.get('File')[4:6])\
                         .get(int(row.get('File')[7:9]))
        return norm_1 * norm_2
    elif row.get(op_column):
        return OP_NORMS.get(row.get(op_column)) \
                       .get(row.get(op_arg_column)) \
                       .get(row.get('File')[4:6]) \
                       .get(int(row.get('File')[7:9]))
    else:
        return 0


def normalize_row(row):
    f_name = row.get('File')
    for a in ALPHABETS:
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


def read_data_files(file_pattern, id_field_names):
    # initialize return list
    data_rows = dict()

    # check for matching files and read csv data
    for f in os.listdir(data_dir):
        test_path = os.path.join(data_dir, f)
        if os.path.isfile(test_path) and fnmatch.fnmatch(f, file_pattern):
            log.debug('Reading in data from %s', test_path)
            with open(test_path, 'r') as csv_file:
                reader = csv.DictReader(csv_file,
                                        delimiter='\t',
                                        quoting=csv.QUOTE_NONE,
                                        quotechar='|',
                                        lineterminator='\n')
                for row in reader:
                    id_fields = list()
                    for id_field_name in id_field_names:
                        id_fields.append(row.get(id_field_name))
                    data_rows[tuple(id_fields)] = row

    return data_rows


def rows_to_numpy_array(rows, key):
    # create return data array from data types
    return_data = numpy.zeros(len(rows), dtype=D_TYPES.get(key))

    log.debug('Converting data list into byte arrays')
    converters = CONVERTERS.get(key)
    for col, dt in D_TYPES.get(key):
        converter = converters.get(col)
        if converter is not None:
            return_data[col] = [converter(r) for r in rows]
        else:
            return_data[col] = [r.get(col.replace('_', ' ')) for r in rows]

    return return_data


def get_data():
    # initialize return data variables
    const_data = None
    op_data = None

    if 'const' in GLOB.get('entries').keys():
        # check if result file already exists
        const_file_path = os.path.join(project_dir, 'data', 'constraints.npy')
        if os.path.isfile(const_file_path):
            log.debug('Constraint data file exists, reading data.')

            const_data = numpy.load(const_file_path)
        else:
            log.debug('Constraint data file not found,'
                      ' creating file from collected results')

            # get lists of data files
            const_count_data = read_data_files('const-count-' +
                                               GLOB['Settings'].file_pattern,
                                               ['File', 'Id'])
            const_time_data = read_data_files('const-time-' +
                                              GLOB['Settings'].file_pattern,
                                              ['File', 'Id'])

            const_rows = list()
            for key in const_count_data.keys():
                count_row = const_count_data.get(key)
                time_row = const_time_data.get(key)
                for missing_keys in filter(lambda k: k not in count_row.keys(),
                                           time_row.keys()):
                    count_row[missing_keys] = time_row.get(missing_keys)
                const_rows.append(count_row)

            # convert rows to numpy array
            const_data = rows_to_numpy_array(const_rows, 'const')

            # write data to file
            numpy.save(const_file_path[:-4], const_data)

    if 'op' in GLOB.get('entries').keys():
        # check if result file already exists
        op_file_path = os.path.join(project_dir, 'data', 'ops-and-preds.npy')
        if os.path.isfile(op_file_path):
            log.debug('Operation and Predicate data file exists, reading data.')

            op_data = numpy.load(op_file_path)
        else:
            log.debug('Constraint data file not found,'
                      ' creating file from collected results')

            # get op time data rows
            op_time_data = read_data_files('op-time-' +
                                           GLOB['Settings'].file_pattern,
                                           ['File', 'Op Id'])

            # convert rows to numpy array
            op_data = rows_to_numpy_array(list(op_time_data.values()), 'op')

            # write data to file
            numpy.save(op_file_path[:-4], op_data)

    # return data
    return const_data, op_data


def order_columns(column):
    try:
        return ORDER_COLUMNS.index(column)
    except ValueError:
        return 32767


def get_latex_table(table, caption, label):
    lines = list()
    keys = table[0].keys()
    if 'multicolumn' in keys:
        keys = table[1].keys()
    columns = sorted(keys, key=order_columns)

    # create before table
    before_table = list()
    before_table.append('\\begin{table}[h!]\n')
    before_table.append(' ' * 4 + '\\centering\n')
    before_table.append(' ' * 4 + '\\footnotesize\n')

    begin_tabu = ' ' * 4 + '\\begin{tabu}{|'
    for i in range(0, len(columns)):
        begin_tabu += ' c |'
    begin_tabu += '}\n'
    before_table.append(begin_tabu)
    before_table.append(' ' * 8 + '\\hline\n')

    # create multicolumn header
    if 'multicolumn' in table[0].keys():
        multi_i_start = table[0]['multicolumn'][0]
        multi_i_end = table[0]['multicolumn'][1]
        multi_i_text = table[0]['multicolumn'][2]
        multi_size = multi_i_end - multi_i_start + 1
        multi_header = ' ' * 8
        for i in range(1, multi_i_start):
            if i != 1:
                multi_header += '& '

        multi_header += '& \\multicolumn{' + str(multi_size) + \
                        '}{c|}{\\textbf{' + multi_i_text + '}} '

        for i in range(multi_i_end, len(columns)):
            multi_header += '& '

        multi_header += '\\\\\n'

        before_table.append(multi_header)
        before_table.append(' ' * 8 + '\\hline\n')
        table = table[1:]

    # create column headers
    headers = ' ' * 8
    for i, column in enumerate(columns):
        if i != 0:
            headers += '& '
        headers += '\\textbf{' + column + '} '
    headers += '\\\\\n'
    before_table.append(headers)
    before_table.append(' ' * 8 + '\\hline\n')

    # create after table
    after_table = list()
    after_table.append(' ' * 4 + '\\end{tabu}\n')
    after_table.append(' ' * 4 + '\\caption{' + caption + '}\n')
    after_table.append(' ' * 4 + '\\label{tab:' + label + '}\n')
    after_table.append('\\end{table}\n')

    lines.extend(before_table)

    # create columns
    for i, row in enumerate(table):
        out_row = ' ' * 8
        for j, column in enumerate(columns):
            if j != 0:
                out_row += '& '
            col_val = row.get(column)
            if isinstance(col_val, tuple):
                if col_val[1]:
                    col_val = col_val[0]
                else:
                    col_val = '\\cellcolor[gray]{0.8} ' + col_val[0]
            elif col_val == 'BLANK':
                col_val = '\\cellcolor{black}'
            out_row += '{0} '.format(col_val)
        out_row += '\\\\\n'
        lines.append(out_row)
        lines.append(' ' * 8 + '\\hline\n')
        if (i + 1) % 55 == 0:
            lines.extend(after_table)
            lines.append('\n\\clearpage\n')
            lines.extend(before_table)

    lines.extend(after_table)

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
    before_lines.append('\\usepackage{hyperref}\n')
    before_lines.append('\\usepackage[justification=centering]{caption}\n')
    before_lines.append('\\usepackage{tabu}\n')
    before_lines.append('\\usepackage[table]{xcolor}\n')
    before_lines.append('\n')
    before_lines.append('\\graphicspath{{../plot-data/}}\n')
    before_lines.append('\n')
    before_lines.append('\\begin{document}\n')
    before_lines.append('\n')
    before_lines.append('\\listoffigures\n')
    before_lines.append('\n')
    before_lines.append('\\listoftables\n')
    before_lines.append('\n')

    table_list = list()
    num_lines = 0
    for table, caption, label in tables:
        new_num_lines = num_lines + len(table) + 7
        if new_num_lines > 55:
            table_list.append(['\n\\clearpage\n'])
            new_num_lines = 0
        table_list.append(get_latex_table(table, caption, label))
        num_lines = new_num_lines

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
            num_rows = data.get(SOLVERS[0])[plot.get('columns')[0][0]].size
            for i in range(num_rows):
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
        out_file.write('set style data dots\n')
        out_file.write('set logscale y 2\n')
        out_file.write('set term png size 1000, 1000\n')
        out_file.writelines(scatter_lines)


def get_script_lines(plot_types, label):
    boxplot_lines = list()
    hist_lines = list()
    scatter_lines = list()

    if 'boxplot' in plot_types:
        ind = ' ' * (len(label) + 17)
        boxplot_lines.append('set output "' + label + '_boxplot.png"\n')
        boxplot_lines.append('plot "' + label + '_boxplot.csv" '
                             'using (1):(column(1)) ti col, \\\n' + ind +
                             '"" using (2):(column(2)) ti col, \\\n' + ind +
                             '"" using (3):(column(3)) ti col, \\\n' + ind +
                             '"" using (4):(column(4)) ti col\n')
    if 'histogram' in plot_types:
        ind = ' ' * (len(label) + 19)
        hist_lines.append('set output "' + label + '_histogram.png"\n')
        hist_lines.append('plot "' + label + '_histogram.csv" using '
                                             '2:xtic(1) ti col, \\\n' + ind +
                                             '"" using 3 ti col, \\\n' + ind +
                                             '"" using 4 ti col, \\\n' + ind +
                                             '"" using 5 ti col\n')
    if 'scatter' in plot_types:
        ind = ' ' * (len(label) + 17)
        scatter_lines.append('set output "' + label + '_scatter.png"\n')
        scatter_lines.append('plot "' + label + '_scatter.csv" '
                             'using "U Times":"U Per Diffs" ti '
                             '"Unbounded", \\\n' + ind +
                             '"" using "B Times":"B Per Diffs" ti '
                             '"Bounded", \\\n' + ind +
                             '"" using "A Times":"A Per Diffs" ti '
                             '"Aggregate", \\\n' + ind +
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

        new_lines = get_script_lines(plot_types, label)
        script_lines['boxplot'].extend(new_lines[0])
        script_lines['histogram'].extend(new_lines[1])
        script_lines['scatter'].extend(new_lines[2])

    # output_plot_script(script_lines.get('boxplot'),
    #                    script_lines.get('histogram'),
    #                    script_lines.get('scatter'))


def filter_input_type(rows, input_type=None):
    return numpy.full(rows.size, True) if input_type is None else \
        rows['Input_Type'] == IN_AND_ARG_TYPE.index(input_type)


def filter_length(rows, length=None):
    return numpy.full(rows.size, True) if length is None else \
        rows['Length'] == length


def filter_alphabet(rows, alphabet=None):
    return numpy.full(rows.size, True) if alphabet is None else \
        rows['Alphabet'] == ALPHABETS.index(alphabet)


def filter_operation(rows, operation=None, exclusive=False, arg_type=None, ):
    return numpy.full(rows.size, True) if operation is None else \
        (numpy.full(rows.size, True) if 'Op' not in rows.dtype.names else
         (rows['Op'] == OPS_AND_PREDS.index(operation))
         & (numpy.full(rows.size, True) if arg_type is None else
            rows['Args'] == IN_AND_ARG_TYPE.index(arg_type))) \
        | (numpy.full(rows.size, True) if not exclusive else
           (numpy.full(rows.size, True) if 'Op_1' not in rows.dtype.names else
            (((rows['Op_1'] == OPS_AND_PREDS.index(operation))
             & (rows['Op_2'] == OPS_AND_PREDS.index(operation))
             & (numpy.full(rows.size, True) if arg_type is None else
                ((rows['Op_1_Arg'] == IN_AND_ARG_TYPE.index(arg_type))
                 & (rows['Op_2_Arg'] == IN_AND_ARG_TYPE.index(arg_type)))))
            | ((rows['Op_1'] == OPS_AND_PREDS.index(operation))
               & (rows['Op_2'] == 0)
               & (numpy.full(rows.size, True) if arg_type is None else
                  rows['Op_1_Arg'] == IN_AND_ARG_TYPE.index(arg_type)))))) \
        | (numpy.full(rows.size, True) if exclusive else
           (numpy.full(rows.size, True) if 'Op_1' not in rows.dtype.names else
            (((rows['Op_1'] == OPS_AND_PREDS.index(operation))
             & (numpy.full(rows.size, True) if arg_type is None else
                rows['Op_1_Arg'] == IN_AND_ARG_TYPE.index(arg_type)))
            | ((rows['Op_2'] == OPS_AND_PREDS.index(operation))
               & (numpy.full(rows.size, True) if arg_type is None else
                  rows['Op_2_Arg'] == IN_AND_ARG_TYPE.index(arg_type))))))


def filter_predicate(rows, predicate, arg_type=None):
    return numpy.full(rows.size, True) if predicate is None else \
        ((rows['Pred'] == OPS_AND_PREDS.index(predicate))
         & (numpy.full(rows.size, True) if arg_type is None else
            rows['Pred_Arg'] == IN_AND_ARG_TYPE.index(arg_type)))


def filter_rows(rows, alphabet=None, length=None, input_type=None,
                operation=None, exclusive_op=None, op_arg_type=None,
                predicate=None, pred_arg_type=None):
    filter_array = (numpy.full(rows.size, True)
                    if 'Op_1' not in rows.dtype.names else rows['Op_1'] != 0) \
                   & filter_input_type(rows, input_type) \
                   & filter_alphabet(rows, alphabet) \
                   & filter_length(rows, length) \
                   & filter_operation(rows, operation, exclusive_op,
                                      op_arg_type) \
                   & filter_predicate(rows, predicate, pred_arg_type)
    return rows[filter_array]


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


def weighted_quantile(a, q, weights=None,
                      values_sorted=False, old_style=False):
    """ 
    Compute the qth percentile of the data along the specified axis.

    Returns the qth percentile(s) of the array elements.

    Parameters
    ----------
    a : array_like
        Array containing data to be averaged. If `a` is not an array, a
        conversion is attempted.
    q : float in range of [0,1] (or sequence of floats)
        Percentile to compute, which must be between 0 and 100 inclusive.
    weights : array_like, optional
        An array of weights associated with the values in `a`. Each value in
        `a` contributes to the average according to its associated weight.
        The weights array can either be 1-D (in which case its length must be
        the size of `a` along the given axis) or of the same shape as `a`.
        If `weights=None`, then all data in `a` are assumed to have a
        weight equal to one.
    values_sorted: bool, optional
        if True, then will avoid sorting of initial array
    old_style: bool, optional
        if True, will correct output to be consistent with numpy.percentile.


    Returns
    -------
    percentile : scalar or ndarray
        If `q` is a single percentile and `axis=None`, then the result
        is a scalar. If multiple percentiles are given, first axis of
        the result corresponds to the percentiles. The other axes are
        the axes that remain after the reduction of `a`. If the input
        contains integers or floats smaller than ``float64``, the output
        data-type is ``float64``. Otherwise, the output data-type is the
        same as that of the input. If `out` is specified, that array is
        returned instead.
    """
    a = numpy.array(a)
    q = numpy.array(q)
    if weights is None:
        weights = numpy.ones(len(a))
    weights = numpy.array(weights)
    assert numpy.all(q >= 0) and numpy.all(
        q <= 1), 'quantiles should be in [0, 1]'

    if not values_sorted:
        sorter = numpy.argsort(a)
        a = a[sorter]
        weights = weights[sorter]

    weighted_quantiles = numpy.cumsum(weights) - 0.5 * weights
    if old_style:
        # To be convenient with numpy.percentile
        weighted_quantiles -= weighted_quantiles[0]
        weighted_quantiles /= weighted_quantiles[-1]
    else:
        weighted_quantiles /= numpy.sum(weights)
    return numpy.interp(q, weighted_quantiles, a)


def get_histogram_data(values,
                       bins=None):

    # initialize bins
    if bins is None:
        min_val = numpy.nanmin(values[list(SOLVERS)].copy().view('f8'))
        max_val = numpy.nanmax(values[list(SOLVERS)].copy().view('f8'))

        if min_val > 1.0:
            min_log = numpy.log10(min_val)
            floor = numpy.floor_divide(min_val, min_log) * 10**min_log
        else:
            floor = min_val

        if max_val > 1.0:
            max_log = numpy.log10(max_val)
            ceiling = (numpy.floor_divide(min_val, max_log) + 1) * 10**max_log
        else:
            ceiling = max_val

        interval = numpy.true_divide(ceiling - floor, 20)
        bins_np = numpy.arange(floor, ceiling + interval, interval)
    else:
        bins_np = numpy.asarray(bins)

    # initialize result array
    histogram_dtypes = list()
    histogram_dtypes.append(('Bins', 'f8'))
    for solver in SOLVERS:
        histogram_dtypes.append((solver, 'f8'))
    results = numpy.empty(bins_np.size - 1, dtype=histogram_dtypes)
    results['Bins'] = bins_np[1:]

    for solver in SOLVERS:
        s_values = values[solver]
        s_weights = values[solver + '_Norm'][
            numpy.logical_not(numpy.isnan(s_values))]
        s_values = s_values[numpy.logical_not(numpy.isnan(s_values))]

        counts, bins = numpy.histogram(s_values,
                                       bins=bins_np,
                                       weights=s_weights)
        results[solver] = counts

    return results


def get_agreement(values):
    # initialize result array
    agree_dtypes = list()
    for solver in SOLVERS:
        agree_dtypes.append((solver, 'f8'))
    results = numpy.zeros(1, dtype=agree_dtypes)

    for solver in SOLVERS:
        agree_weights = values[values[solver]][solver + '_Norm']
        all_weights = values[solver]
        agree_count = sum(agree_weights)
        all_count = sum(all_weights)
        results[solver] = numpy.true_divide(agree_count, all_count)

    return results


def get_desc_stats(values):
    # initialize result array
    result_dtypes = list()
    for solver in SOLVERS:
        result_dtypes.append((solver, 'f8'))
    avg_results = numpy.zeros(1, dtype=result_dtypes)
    median_results = numpy.zeros(1, dtype=result_dtypes)
    std_dev_results = numpy.zeros(1, dtype=result_dtypes)

    for solver in SOLVERS:
        s_values = values[solver]
        s_weights = values[solver + '_Norm'][
            numpy.logical_not(numpy.isnan(s_values))]
        s_values = s_values[numpy.logical_not(numpy.isnan(s_values))]

        # get average for solver
        avg_results[solver] = numpy.average(s_values, weights=s_weights)

        # get median for solver
        median_results[solver] = weighted_quantile(s_values, 0.5,
                                                   weights=s_weights)

        # get std dev for solver
        std_dev_results[solver] = compute_std_dev(s_values, weights=s_weights)

    return avg_results, median_results, std_dev_results


def get_quantiles(values, start=0.0, stop=1.0, step=0.01):
    # initialize quantiles array
    quantiles = numpy.arange(start, stop + step, step)

    # initialize result array
    result_dtypes = list()
    for solver in SOLVERS:
        result_dtypes.append((solver, 'f8'))
    results = numpy.zeros(quantiles.size, dtype=result_dtypes)

    for solver in SOLVERS:
        s_values = values[solver]
        s_weights = values[solver + '_Norm'][
            numpy.logical_not(numpy.isnan(s_values))]
        s_values = s_values[numpy.logical_not(numpy.isnan(s_values))]
        results[solver] = weighted_quantile(s_values,
                                            quantiles,
                                            weights=s_weights)

    return results


def get_values(rows, measurement, branch=None, only_agree=False):
    s_values = dict()
    s_weights = dict()

    num_vals = rows.size
    if measurement in ['per_diff', 'solve_time', 'mc_time', 'comb_time'] \
            and branch is None:
        num_vals = num_vals * 2

    for solver in SOLVERS:
        s_weights[solver] = rows['Norm']
        if measurement == 'per_diff':
            filtered = rows
            if only_agree:
                filtered = rows[rows[solver[0] + '_Agree']]
                s_weights[solver] = filtered['Norm']

            if branch is None:
                s_weights[solver] = numpy.append(s_weights[solver],
                                                 s_weights[solver])

            per_diff_np = numpy.empty(0)
            if branch is None or branch:
                per_diff_np = numpy.append(per_diff_np,
                                           filtered[solver[0] + '_T_Per_Diff'])
            if branch is None or not branch:
                per_diff_np = numpy.append(per_diff_np,
                                           filtered[solver[0] + '_F_Per_Diff'])

            if per_diff_np.size < num_vals:
                size_diff = num_vals - per_diff_np.size
                diff_np = numpy.full(size_diff, numpy.nan)
                per_diff_np = numpy.append(per_diff_np, diff_np)
                s_weights[solver] = numpy.append(s_weights[solver], diff_np)

            s_values[solver] = per_diff_np

        elif measurement == 'agree':
            s_values[solver] = rows[solver[0] + '_Agree']

        elif measurement == 'op_time':
            # get op times
            s_values[solver] = rows[solver[0] + '_Op_Time']
        else:
            if branch is None:
                s_weights[solver] = numpy.append(s_weights[solver],
                                                 s_weights[solver])

            vals_np = numpy.empty([0, num_vals])

            # get mc times
            if measurement == 'mc_time' or measurement == 'comb_time':
                mc_times_np = numpy.empty(0)
                if branch is None or branch:
                    mc_times_np = numpy.append(mc_times_np,
                                               rows[solver[0] + '_T_MC_Time'])
                if branch is None or not branch:
                    mc_times_np = numpy.append(mc_times_np,
                                               rows[solver[0] + '_F_MC_Time'])

                vals_np = numpy.append(vals_np, [mc_times_np], axis=0)

            # get solve times
            if measurement == 'solve_time' or measurement == 'comb_time':
                solve_times_np = numpy.empty(0)
                if branch is None or branch:
                    solve_times_np = numpy.append(solve_times_np,
                                                  rows[solver[0] +
                                                       '_T_Solve_Time'])
                if branch is None or not branch:
                    solve_times_np = numpy.append(solve_times_np,
                                                  rows[solver[0] +
                                                       '_F_Solve_Time'])

                vals_np = numpy.append(vals_np, [solve_times_np], axis=0)

            # sum times
            if len(vals_np.shape) == 2 and vals_np.shape[0] > 1:
                s_values[solver] = numpy.sum(vals_np, axis=0)
            else:
                s_values[solver] = vals_np[0]

    val_d_type = s_values.get(SOLVERS[0]).dtype
    d_types = list()
    for solver in SOLVERS:
        d_types.append((solver, val_d_type))
        d_types.append((solver + '_Norm', 'u8'))
    values = numpy.zeros(num_vals, dtype=d_types)
    for solver in SOLVERS:
        values[solver] = s_values.get(solver)
        values[solver + '_Norm'] = s_weights.get(solver)

    return values


def compute_variance(a, weights=None, axis=None):
    """
    Compute the variance along the specified axis.

    Returns the variance of the array elements, a measure of the spread of a
    distribution.  The variance is computed for the flattened array by
    default, otherwise over the specified axis.

    Parameters
    ----------
    a : array_like
        Array containing numbers whose variance is desired.  If `a` is not an
        array, a conversion is attempted.
    weights : array_like, optional
        An array of weights associated with the values in `a`. Each value in
        `a` contributes to the average according to its associated weight.
        The weights array can either be 1-D (in which case its length must be
        the size of `a` along the given axis) or of the same shape as `a`.
        If `weights=None`, then all data in `a` are assumed to have a
        weight equal to one.
    axis : None or int or tuple of ints, optional
        Axis or axes along which the variance is computed.  The default is to
        compute the variance of the flattened array.

    Returns
    -------
    variance : ndarray, see dtype parameter above
        If ``out=None``, returns a new array containing the variance;
        otherwise, a reference to the output array is returned.
    """
    if weights is None:
        return numpy.var(a, axis=axis)

    # get mean
    mean = numpy.average(a, axis=axis, weights=weights)

    # compute squared difference along axis
    apply_axis = 0 if axis is None else axis
    sq_d = numpy.apply_along_axis(lambda x: (x - mean) ** 2, apply_axis, a)

    # average squared difference to get variance
    return numpy.average(sq_d, axis=axis, weights=weights)


def compute_std_dev(a, weights=None, axis=None):
    """
    Compute the standard deviation along the specified axis.

    Returns the standard deviation, a measure of the spread of a distribution,
    of the array elements. The standard deviation is computed for the
    flattened array by default, otherwise over the specified axis.

    Parameters
    ----------
    a : array_like
        Calculate the standard deviation of these values.
    weights : array_like, optional
        An array of weights associated with the values in `a`. Each value in
        `a` contributes to the average according to its associated weight.
        The weights array can either be 1-D (in which case its length must be
        the size of `a` along the given axis) or of the same shape as `a`.
        If `weights=None`, then all data in `a` are assumed to have a
        weight equal to one.
    axis : None or int or tuple of ints, optional
        Axis or axes along which the standard deviation is computed. The
        default is to compute the standard deviation of the flattened array.

    Returns
    -------
    standard_deviation : ndarray
        Return a new array containing the standard deviation, otherwise return
        a reference to the output array.
    """
    if weights is None:
        return numpy.std(a, axis=axis)

    # get mean
    mean = numpy.average(a, axis=axis, weights=weights)

    # compute squared difference along axis
    apply_axis = 0 if axis is None else axis
    sq_d = numpy.apply_along_axis(lambda x: (x - mean) ** 2, apply_axis, a)

    # average squared difference to get variance
    variance = numpy.average(sq_d, axis=axis, weights=weights)

    # get standard deviation by computing square root of variance
    return numpy.sqrt(variance)


def two_sample_z_test(mean_1, sd_1, n_1, mean_2, sd_2, n_2, mu_diff=0):
    squared_se_1 = sd_1**2 / n_1
    squared_se_2 = sd_2**2 / n_2
    pooled_se = numpy.sqrt(squared_se_1 + squared_se_2)
    avg_diff = mean_1 - mean_2 - mu_diff
    z = numpy.true_divide(avg_diff, pooled_se)
    p_val = 2 * (scipy.stats.norm.sf(numpy.absolute(z)))
    return numpy.round(z, 3), numpy.round(p_val, 4)


def perform_test(a1, a2, w1=None, w2=None, fisher=False):

    if fisher:
        # stack values and weights together
        a_w_1 = numpy.column_stack([a1, w1])
        a_w_2 = numpy.column_stack([a2, w2])

        # get rows for true and false
        true_1 = numpy.where(a_w_1[:, 0],
                             a_w_1[:, 1],
                             numpy.zeros(a_w_1.shape[0]))
        false_1 = numpy.where(numpy.logical_not(a_w_1[:, 0]),
                              a_w_1[:, 1],
                              numpy.zeros(a_w_1.shape[0]))
        true_2 = numpy.where(a_w_2[:, 0],
                             a_w_2[:, 1],
                             numpy.zeros(a_w_2.shape[0]))
        false_2 = numpy.where(numpy.logical_not(a_w_2[:, 0]),
                              a_w_2[:, 1],
                              numpy.zeros(a_w_2.shape[0]))

        a1_true_count = numpy.sum(true_1)
        a1_false_count = numpy.sum(false_1)
        a2_true_count = numpy.sum(true_2)
        a2_false_count = numpy.sum(false_2)

        # contingency_table = [
        #     [a1_true_count, a1_false_count],
        #     [a2_true_count, a2_false_count]
        # ]
        # stat_val, p_val, dof, expected =\
        #     scipy.stats.chi2_contingency(contingency_table)

        o_t_freq = numpy.true_divide(a1_true_count,
                                     (a1_true_count + a1_false_count))
        o_f_freq = numpy.true_divide(a1_false_count,
                                     (a1_true_count + a1_false_count))
        e_t_freq = numpy.true_divide(a2_true_count,
                                     (a2_true_count + a2_false_count))
        e_f_freq = numpy.true_divide(a2_false_count,
                                     (a2_true_count + a2_false_count))

        o_freq = numpy.asarray([o_t_freq, o_f_freq])
        e_freq = numpy.asarray([e_t_freq, e_f_freq])

        stat_val, p_val = scipy.stats.chisquare(o_freq, e_freq)

    else:  # paired Z-test
        mean_1 = numpy.average(a1, weights=w1)
        std_1 = compute_std_dev(a1, weights=w1)
        mean_2 = numpy.average(a2, weights=w2)
        std_2 = compute_std_dev(a2, weights=w2)
        obs_1 = numpy.sum(w1)
        obs_2 = numpy.sum(w2)
        stat_val, p_val = two_sample_z_test(mean_1,
                                            std_1,
                                            obs_1,
                                            mean_2,
                                            std_2,
                                            obs_2)

    return stat_val, p_val


def get_stat_tests(f_values, u_values, fisher=False):
    # initialize result arrays
    result_dtypes = list()
    result_dtypes.append(('Test_Stat_Ind_Var', 'f8'))
    result_dtypes.append(('P_Val_Ind_Var', 'f8'))
    for solver in SOLVERS:
        result_dtypes.append(('Test_Stat_vs_' + solver, 'f8'))
        result_dtypes.append(('P_Val_vs_' + solver, 'f8'))
    results = numpy.zeros(len(SOLVERS), dtype=result_dtypes)

    # get test statistics
    for i, s1 in enumerate(SOLVERS):
        s1_values = f_values[s1]
        s1_weights = f_values[s1 + '_Norm'][
            numpy.logical_not(numpy.isnan(s1_values))]
        s1_values = s1_values[numpy.logical_not(numpy.isnan(s1_values))]

        # compare solvers
        for s2 in SOLVERS:
            s2_values = f_values[s2]
            s2_weights = f_values[s2 + '_Norm'][
                numpy.logical_not(numpy.isnan(s2_values))]
            s2_values = s2_values[numpy.logical_not(numpy.isnan(s2_values))]

            stat_val, p_val = perform_test(s1_values,
                                           s2_values,
                                           w1=s1_weights,
                                           w2=s2_weights,
                                           fisher=fisher)
            results['Test_Stat_vs_' + solver][i] = stat_val
            results['P_Val_vs_' + solver][i] = p_val

        # compare filtered for ind var vs unfiltered
        s_u_values = u_values[s1]
        s_u_weights = u_values[s1 + '_Norm'][
            numpy.logical_not(numpy.isnan(s_u_values))]
        s_u_values = s_u_values[numpy.logical_not(numpy.isnan(s_u_values))]

        stat_val, p_val = perform_test(s1_values,
                                       s_u_values,
                                       w1=s1_weights,
                                       w2=s_u_weights,
                                       fisher=fisher)
        results['Test_Stat_Ind_Var'][i] = stat_val
        results['P_Val_Ind_Var'][i] = p_val

    return results


def calculate_stats(values, u_values, stat_rows, agree=False, test=0):
    if agree:
        # get mean, median, and std deviation
        agreements = get_agreement(values)
        stat_rows['Avg/Freq'] = agreements.view('f8').reshape(stat_rows.size)

        # get test an p values
        stat_test_results = get_stat_tests(values, u_values, fisher=True)
    else:
        # get mean, median, and std deviation
        avg_np, median_np, std_dev_np = get_desc_stats(values)
        stat_rows['Avg/Freq'] = avg_np.view('f8').reshape(stat_rows.size)
        stat_rows['Median'] = median_np.view('f8').reshape(stat_rows.size)
        stat_rows['Std_Dev'] = std_dev_np.view('f8').reshape(stat_rows.size)

        # get test an p values
        stat_test_results = get_stat_tests(values, u_values)

    for col in stat_test_results.dtype.names:
        stat_rows[col] = stat_test_results[col]


def analyze_data(const_data, entries):
    # create return structures
    stat_rows = numpy.empty(0, dtype=D_TYPES.get('stat'))
    box_plot_data = dict()
    histogram_data = dict()
    unfiltered = filter_rows(const_data)

    for entry in entries:
        log.debug('Processing Data Set Entry: %s', entry.get('data_set'))
        filtered = filter_rows(const_data,
                               alphabet=entry.get('alphabet'),
                               length=entry.get('length'),
                               input_type=entry.get('input_type'),
                               operation=entry.get('operation'),
                               exclusive_op=entry.get('exclusive_op'),
                               op_arg_type=entry.get('op_arg_type'),
                               predicate=entry.get('predicate'),
                               pred_arg_type=entry.get('pred_arg_type'))

        measurements = set()
        if entry.get('stats') is not None:
            measurements.update(entry.get('stats'))
        if entry.get('box_plots') is not None:
            measurements.update(entry.get('box_plots'))
        if entry.get('histograms') is not None:
            measurements.update(entry.get('histograms'))

        for measurement in measurements:
            log.debug('Getting Values for Data Set and Measurement: %s, %s',
                      entry.get('data_set'),
                      measurement)

            # get filtered measurements
            values = get_values(filtered,
                                measurement,
                                branch=entry.get('branch'),
                                only_agree=entry.get('only_agree'))

            # get unfiltered measurements
            u_values = get_values(unfiltered,
                                  measurement,
                                  branch=entry.get('branch'),
                                  only_agree=entry.get('only_agree'))

            m_val = MEASUREMENTS.index(measurement)
            ds_val = DATA_SETS.index(entry.get('data_set'))

            # get stats for measurement
            if entry.get('stats') is not None \
                    and measurement in entry.get('stats'):
                log.debug('Getting Stats for Data Set and Measurement: %s, %s',
                          entry.get('data_set'),
                          measurement)

                # create new stat rows array
                new_stat_rows = numpy.zeros(len(SOLVERS),
                                            dtype=D_TYPES.get('stat'))
                new_stat_rows['Measurement'] = numpy.full(new_stat_rows.size,
                                                          m_val)
                solver_indices = [SOLVERS.index(s) for s in SOLVERS]
                new_stat_rows['Solver'] = numpy.asarray(solver_indices)
                new_stat_rows['Data_Set'] = numpy.full(new_stat_rows.size,
                                                       ds_val)
                new_stat_rows['Count'] = numpy.full(new_stat_rows.size,
                                                    filtered.size)

                calculate_stats(values, u_values, new_stat_rows,
                                agree=(measurement == 'agree'))
                stat_rows = numpy.append(stat_rows, new_stat_rows)

            if entry.get('box_plots') is not None \
                    and measurement in entry.get('box_plots'):
                log.debug('Getting Box Plots for Data Set and Measurement:'
                          ' %s, %s',
                          entry.get('data_set'),
                          measurement)

                box_plot_data[(m_val, ds_val)] = get_quantiles(values)

            if entry.get('histograms') is not None \
                    and measurement in entry.get('histograms'):
                log.debug('Getting Histograms for Data Set and Measurement:'
                          ' %s, %s',
                          entry.get('data_set'),
                          measurement)

                histogram_data[(m_val, ds_val)] = get_histogram_data(values)

    return stat_rows, box_plot_data, histogram_data


def perform_analysis(const_data, op_data):
    # create return structures
    stat_rows = numpy.empty(0, dtype=D_TYPES.get('stat'))
    box_columns = dict()
    hist_columns = dict()

    const_results = analyze_data(const_data, GLOB.get('entries').get('const'))
    stat_rows = numpy.append(stat_rows, const_results[0])
    box_columns.update(const_results[1])
    hist_columns.update(const_results[2])

    op_results = analyze_data(op_data, GLOB.get('entries').get('op'))
    stat_rows = numpy.append(stat_rows, op_results[0])
    box_columns.update(op_results[1])
    hist_columns.update(op_results[2])

    return stat_rows, box_columns, hist_columns


def output_results(stats_results, box_results, hist_results):

    log.debug('Saving Statistic Results')
    stats_file_path = os.path.join(project_dir,
                                   'data',
                                   'plot-data',
                                   'stats.csv')
    output_csv_file(stats_file_path, stats_results, 'stat')

    # get plot data dir
    plot_data_dir = os.path.join(project_dir, 'data', 'plot-data')
    if not os.path.isdir(plot_data_dir):
        os.makedir(plot_data_dir)

    log.debug('Writing Box Plot Data')
    for m_num, ds_num in box_results.keys():
        box_values = box_results.get((m_num, ds_num))
        box_file_name = '{0}_{1}_box.csv'.format(MEASUREMENTS[m_num],
                                                 DATA_SETS[ds_num])
        box_file_name = box_file_name.replace('-', '_')
        box_file_path = os.path.join(plot_data_dir, box_file_name)
        output_csv_file(box_file_path, box_values, 'box')

    log.debug('Writing Histogram Data')
    for m_num, ds_num in hist_results.keys():
        hist_values = hist_results.get((m_num, ds_num))
        hist_file_name = '{0}_{1}_hist.csv'.format(MEASUREMENTS[m_num],
                                                   DATA_SETS[ds_num])
        hist_file_name = hist_file_name.replace('-', '_')
        hist_file_path = os.path.join(plot_data_dir, hist_file_name)
        output_csv_file(hist_file_path, hist_values, 'hist')


def output_data(const_data, op_data):
    # get plot data dir
    plot_data_dir = os.path.join(project_dir, 'data', 'plot-data')
    if not os.path.isdir(plot_data_dir):
        os.makedir(plot_data_dir)

    log.debug('Writing Constraint Data')
    const_file_path = os.path.join(plot_data_dir, 'const.csv')
    if not os.path.isfile(const_file_path):
        output_csv_file(const_file_path, const_data, 'const')

    log.debug('Writing Operation and Predicate Data')
    op_file_path = os.path.join(plot_data_dir, 'op.csv')
    if not os.path.isfile(op_file_path):
        output_csv_file(op_file_path, op_data, 'op')


def output_csv_file(file_path, np_array, format_id):
    # get column headers
    columns = '\t'.join(np_array.dtype.names)
    formats = OUT_FORMATS.get(format_id)

    with open(file_path, 'w') as out_file:
        # write header
        out_file.write(columns)
        out_file.write('\n')

        for row in np_array.flat:
            for i, col in enumerate(np_array.dtype.names):
                if i > 0:
                    out_file.write('\t')
                col_val = row[col]
                out_val = formats[i].format(col_val)
                out_file.write(out_val)
            out_file.write('\n')


def main(arguments):
    # set options from args
    set_options(arguments)

    # get analysis entries
    get_entries()

    # read data
    const_data, op_data = get_data()

    # output const and op data
    output_data(const_data, op_data)

    # perform analysis
    results = perform_analysis(const_data, op_data)

    # output results
    output_results(*results)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
