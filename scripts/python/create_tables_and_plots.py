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
from constants import MEASUREMENTS
from constants import SOLVERS
from constants import D_TYPES
from constants import OUT_FORMATS
from constants import DATA_SETS
from constants import ALPHABETS
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


# Classes
class Settings:
    def __init__(self, options):
        self.tables = options.tables
        self.box_plots = options.box_plots
        self.histograms = options.histograms
        self.candlesticks = options.candlesticks
        self.scatter_plots = options.scatter_plots

        # set debug
        self.debug = options.debug
        if self.debug:
            log.setLevel(logging.DEBUG)
            ch.setLevel(logging.DEBUG)
            log.debug('Args: %s', options)


def set_options(arguments):
    # process command line args
    arg_desc = 'Create result tables and data plots.'
    create_parser = argparse.ArgumentParser(prog=__doc__, description=arg_desc)

    create_parser.add_argument('-d',
                               '--debug',
                               help='Display debug messages for this script.',
                               action='store_true')

    tables_help = 'List of tables to create, all are created if no argument ' \
                  'is provided.'
    create_parser.add_argument('-t',
                               '--tables',
                               nargs='+',
                               default=list(),
                               help=tables_help)

    box_plots_help = 'List of tables to create, all are created if no ' \
                     'argument is provided.'
    create_parser.add_argument('-b',
                               '--box-plots',
                               nargs='+',
                               default=list(),
                               help=box_plots_help)

    histograms_help = 'List of tables to create, all are created if no ' \
                      'argument is provided.'
    create_parser.add_argument('-h',
                               '--histograms',
                               nargs='+',
                               default=list(),
                               help=histograms_help)

    candlesticks_help = 'List of candlestick to create, all are created if ' \
                        'no argument is provided.'
    create_parser.add_argument('-c',
                               '--candlesticks',
                               nargs='+',
                               default=list(),
                               help=candlesticks_help)

    scatter_plots_help = 'List of scatter plots to create, all are created ' \
                         'if no argument is provided.'
    create_parser.add_argument('-s',
                               '--scatter-plots',
                               nargs='+',
                               default=list(),
                               help=scatter_plots_help)

    return Settings(create_parser.parse_args(arguments))


def order_columns(column):
    try:
        return ORDER_COLUMNS.index(column)
    except ValueError:
        return 32767  # 2 ^31


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
    before_lines.append('\\usepackage{fullpage}\n')
    before_lines.append('\\usepackage{hyperref}\n')
    before_lines.append('\\usepackage[justification=centering]{caption}\n')
    before_lines.append('\\usepackage{tabu}\n')
    before_lines.append('\\usepackage[table]{xcolor}\n')
    before_lines.append('\n')
    before_lines.append('\\begin{document}\n')
    before_lines.append('\n')
    before_lines.append('\\tablesofcontents\n')
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


def read_tables(options):
    tables = list()

    return tables


def write_tables(tables, options):
    pass


def create_box_plots(options):
    pass


def create_histograms(options):
    pass


def create_candlesticks(options):
    pass


def create_scatter_plots(options):
    pass


def main(arguments):
    # set options from args
    options = set_options(arguments)

    # output tables
    if options.tables:
        tables = read_tables(options)
        write_tables(tables, options)

    # create box plots
    if options.box_plots:
        create_box_plots(options)

    # create histograms
    if options.histograms:
        create_histograms(options)

    # create candlesticks
    if options.candlesticks:
        create_candlesticks(options)

    # create scatter plots
    if options.scatter_plots:
        create_scatter_plots(options)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
