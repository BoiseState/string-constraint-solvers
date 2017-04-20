#! /usr/bin/env python
import logging

import argparse
import csv
import fnmatch
import numpy
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
GLOB['len-match'][1] = re.compile('.*-01(-\d{2})?.csv')
GLOB['len-match'][2] = re.compile('.*-02(-\d{2})?.csv')
GLOB['len-match'][3] = re.compile('.*-03(-\d{2})?.csv')
GLOB['alphabet-match'] = dict()
GLOB['alphabet-match']['AB'] = re.compile('.*-AB-\d{2}(-\d{2})?.csv')
GLOB['alphabet-match']['AC'] = re.compile('.*-AC-\d{2}(-\d{2})?.csv')
GLOB['alphabet-match']['AD'] = re.compile('.*-AD-\d{2}(-\d{2})?.csv')

SOLVERS = (
    'Concrete',
    'Unbounded',
    'Bounded',
    'Aggregate',
    'Weighted'
)

OP_NORMS = {
    'concat': {
        'Simple': {
            'AB': {1: 6, 2: 6, 3: 6},
            'AC': {1: 4, 2: 4, 3: 4},
            'AD': {1: 3, 2: 3, 3: 3}
        },
        'Even': {
            'AB': {1: 12, 2: 12, 3: 12},
            'AC': {1: 12, 2: 12, 3: 12},
            'AD': {1: 12, 2: 12, 3: 12}
        },
        'Uneven': {
            'AB': {1: 12, 2: 12, 3: 12},
            'AC': {1: 12, 2: 12, 3: 12},
            'AD': {1: 12, 2: 12, 3: 12}
        }
    },
    'delete': {
        'same': {
            'AB': {1: 6, 2: 4, 3: 3},
            'AC': {1: 6, 2: 4, 3: 3},
            'AD': {1: 6, 2: 4, 3: 3}
        },
        'different': {
            'AB': {1: 12, 2: 4, 3: 2},
            'AC': {1: 12, 2: 4, 3: 2},
            'AD': {1: 12, 2: 4, 3: 2}
        }
    },
    'replace': {
        'same': {
            'AB': {1: 6, 2: 6, 3: 6},
            'AC': {1: 4, 2: 4, 3: 4},
            'AD': {1: 3, 2: 3, 3: 3}
        },
        'different': {
            'AB': {1: 6, 2: 6, 3: 6},
            'AC': {1: 2, 2: 2, 3: 2},
            'AD': {1: 1, 2: 1, 3: 1}
        }
    },
    'reverse': {
        'AB': {1: 12, 2: 12, 3: 12},
        'AC': {1: 12, 2: 12, 3: 12},
        'AD': {1: 12, 2: 12, 3: 12}
    }
}

PER_DIFF_ENTRIES = (
    {
        'caption': 'Frequency of Accuracy Difference for All Constraints',
        'label': 'acc_diff_all'
    },
    {
        'branch': True,
        'caption': 'Frequency of Accuracy Difference for True Branch '
                   'Constraints',
        'label': 'acc_diff_true'
    },
    {
        'branch': False,
        'caption': 'Frequency of Accuracy Difference for False Branch '
                   'Constraints',
        'label': 'acc_diff_false'
    },
    {
        'input_type': 'Concrete',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a Concrete Input String',
        'label': 'acc_diff_concrete'
    },
    {
        'input_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a Simple Unknown Input String',
        'label': 'acc_diff_simple'
    },
    {
        'input_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a Uneven Unknown Input String',
        'label': 'acc_diff_branch'
    },
    {
        'alphabet-size': 2,
        'caption': 'Frequency of Accuracy Difference for Constraints With a 2'
                   ' Character Alphabet',
        'label': 'acc_diff_alph_2'
    },
    {
        'alphabet-size': 3,
        'caption': 'Frequency of Accuracy Difference for Constraints With a 3'
                   ' Character Alphabet',
        'label': 'acc_diff_alph_3'
    },
    {
        'alphabet-size': 4,
        'caption': 'Frequency of Accuracy Difference for Constraints With a 4'
                   ' Character Alphabet',
        'label': 'acc_diff_alph_4'
    },
    {
        'length': 1,
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' an Input String of Length 1',
        'label': 'acc_diff_len_1'
    },
    {
        'length': 2,
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' an Input String of Length 2',
        'label': 'acc_diff_len_2'
    },
    {
        'length': 3,
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' an Input String of Length 3',
        'label': 'acc_diff_len_3'
    },
    {
        'operation': 'concat',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}$ Operations',
        'label': 'acc_diff_incl_concat_all'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Concrete \\rangle$ Operations',
        'label': 'acc_diff_incl_concat_con'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Simple \\rangle$ Operations',
        'label': 'acc_diff_incl_concat_simp'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Uneven \\rangle$ Operations',
        'label': 'acc_diff_incl_concat_branch'
    },
    {
        'operation': 'delete',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{delete}$ Operations',
        'label': 'acc_diff_incl_delete'
    },
    {
        'operation': 'replace',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{replace}$ Operations',
        'label': 'acc_diff_incl_replace'
    },
    {
        'operation': 'reverse',
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{reverse}$ Operations',
        'label': 'acc_diff_incl_reverse'
    },
    {
        'operation': 'concat',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}$ Operations',
        'label': 'acc_diff_excl_concat_all'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Concrete \\rangle$ Operations',
        'label': 'acc_diff_excl_concat_con'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Simple \\rangle$ Operations',
        'label': 'acc_diff_excl_concat_simp'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{concat}\\langle Uneven \\rangle$ Operations',
        'label': 'acc_diff_excl_concat_branch'
    },
    {
        'operation': 'delete',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{delete}$ Operations',
        'label': 'acc_diff_excl_delete'
    },
    {
        'operation': 'replace',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{replace}$ Operations',
        'label': 'acc_diff_excl_replace'
    },
    {
        'operation': 'reverse',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints $\\forall'
                   ' \\mathtt{reverse}$ Operations',
        'label': 'acc_diff_excl_reverse'
    },
    {
        'predicate': 'contains',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' \\texttt{contains} Predicates',
        'label': 'acc_diff_contains_all'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Concrete',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}\\langle Concrete \\rangle$ Predicates',
        'label': 'acc_diff_contains_con'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}\\langle Simple \\rangle$ Predicates',
        'label': 'acc_diff_contains_simp'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}\\langle Uneven \\rangle$ Predicates',
        'label': 'acc_diff_contains_branch'
    },
    {
        'predicate': 'equals',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' \\texttt{equals} Predicates',
        'label': 'acc_diff_equals_all'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Concrete',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}\\langle Concrete \\rangle$ Predicates',
        'label': 'acc_diff_equals_con'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}\\langle Simple \\rangle$ Predicates',
        'label': 'acc_diff_equals_simp'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}\\langle Uneven \\rangle$ Predicates',
        'label': 'acc_diff_equals_branch'
    }
)

AGREE_ENTRIES = (
    {
        'Selection': 'All'
    },
    {'is_blank': True},
    {
        'input_type': 'Concrete',
        'Selection': 'Concrete'
    },
    {
        'input_type': 'Simple',
        'Selection': 'Simple'
    },
    {
        'input_type': 'Uneven',
        'Selection': 'Uneven'
    },
    {'is_blank': True},
    {
        'alphabet-size': 2,
        'Selection': '2 Character Alphabet'
    },
    {
        'alphabet-size': 3,
        'Selection': '3 Character Alphabet'
    },
    {
        'alphabet-size': 4,
        'Selection': '4 Character Alphabet'
    },
    {'is_blank': True},
    {
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'length': 3,
        'Selection': 'Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'Selection': '\\exists \\texttt{concat}'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'Selection': '$\\exists \\texttt{concat}\\langle Concrete \\rangle$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': '\\exists \\texttt{concat} for Simple Unknown Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': '\\exists \\texttt{concat} for Uneven Unknown Args'
    },
    {
        'operation': 'delete',
        'Selection': '\\exists \\texttt{delete}'
    },
    {
        'operation': 'replace',
        'Selection': '\\exists \\texttt{replace}'
    },
    {
        'operation': 'reverse',
        'Selection': '\\exists \\texttt{reverse}'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{concat}'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{concat} for Concrete Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{concat} for Simple Unknown Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{concat} for Uneven Unknown Args'
    },
    {
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{delete}'
    },
    {
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{replace}'
    },
    {
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': '\\forall \\texttt{reverse}'
    },
    {'is_blank': True},
    {
        'predicate': 'contains',
        'Selection': '\\texttt{contains}'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Concrete',
        'Selection': '\\texttt{contains} for Concrete Args'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '\\texttt{contains} for Simple Args'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '\\texttt{contains} for Uneven Args'
    },
    {
        'predicate': 'equals',
        'Selection': '\\texttt{equals}'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Concrete',
        'Selection': '\\texttt{equals} for Concrete Args'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '\\texttt{equals} for Simple Args'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '\\texttt{equals} for Uneven Args'
    }
)

MC_TIME_ENTRIES = (
    {
        'columns': ['T MC Time', 'F MC Time'],
        'Selection': 'All Constraints'
    },
    {'is_blank': True},
    {
        'columns': ['T MC Time'],
        'Selection': 'True Branch Constraints'
    },
    {
        'columns': ['F MC Time'],
        'Selection': 'False Branch Constraints'
    },
    {'is_blank': True},
    {
        'columns': ['T MC Time', 'F MC Time'],
        'alphabet-size': 2,
        'Selection': 'Constraints for 2 Character Alphabet'
    },
    {
        'columns': ['T MC Time', 'F MC Time'],
        'alphabet-size': 3,
        'Selection': 'Constraints for 3 Character Alphabet'
    },
    {
        'columns': ['T MC Time', 'F MC Time'],
        'alphabet-size': 4,
        'Selection': 'Constraints for 4 Character Alphabet'
    },
    {'is_blank': True},
    {
        'columns': ['T MC Time', 'F MC Time'],
        'length': 1,
        'Selection': 'Constraints for Input Strings of Length 1'
    },
    {
        'columns': ['T MC Time', 'F MC Time'],
        'length': 2,
        'Selection': 'Constraints for Input Strings of Length 2'
    },
    {
        'columns': ['T MC Time', 'F MC Time'],
        'length': 3,
        'Selection': 'Constraints for Input Strings of Length 3'
    }
)

SOLVE_TIME_ENTRIES = (
    {
        'Selection': 'All Constraints'
    },
    {'is_blank': True},
    {
        'input_type': 'Concrete',
        'Selection': 'Concrete Input Strings'
    },
    {
        'input_type': 'Simple',
        'Selection': 'Simple Unknown Input Strings'
    },
    {
        'input_type': 'Uneven',
        'Selection': 'Uneven Unknown Input Strings'
    },
    {'is_blank': True},
    {
        'length': 1,
        'Selection': 'Input Strings of Length 1'
    },
    {
        'length': 2,
        'Selection': 'Input Strings of Length 2'
    },
    {
        'length': 3,
        'Selection': 'Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'Selection': '\\exists \\texttt{concat}'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'Selection': '\\exists \\texttt{concat} for Concrete Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': '\\exists \\texttt{concat} for Simple Unknown Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': '\\exists \\texttt{concat} for Uneven Unknown Args'
    },
    {
        'operation': 'delete',
        'Selection': '\\exists \\texttt{delete}'
    },
    {
        'operation': 'replace',
        'Selection': '\\exists \\texttt{replace}'
    },
    {
        'operation': 'reverse',
        'Selection': '\\exists \\texttt{reverse}'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{concat}'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Concrete',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{concat} for Concrete Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{concat} for Simple Unknown Args'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{concat} for Uneven Unknown Args'
    },
    {
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{delete}'
    },
    {
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{replace}'
    },
    {
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': '\\exists \\texttt{reverse}'
    },
    {
        'predicate': 'contains',
        'Selection': '\\texttt{contains}'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Concrete',
        'Selection': '\\texttt{contains} for Concrete Args'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '\\texttt{contains} for Simple Unknown Args'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '\\texttt{contains} for Uneven Unknown Args'
    },
    {
        'predicate': 'equals',
        'Selection': '\\texttt{equals}'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Concrete',
        'Selection': '\\texttt{equals} for Concrete Args'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '\\texttt{equals} for Simple Unknown Args'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '\\texttt{equals} for Uneven Unknown Args'
    },
)

OP_TIME_ENTRIES = (
    {
        'operation': 'concat',
        'Selection': '\\texttt{concat}'
    },
    {
        'operation': 'concat',
        'length': 1,
        'Selection': '\\texttt{concat} for Input Strings of Length 1'
    },
    {
        'operation': 'concat',
        'length': 2,
        'Selection': '\\texttt{concat} for Input Strings of Length 2'
    },
    {
        'operation': 'concat',
        'length': 3,
        'Selection': '\\texttt{concat} for Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'delete',
        'Selection': '\\texttt{delete}'
    },
    {
        'operation': 'delete',
        'length': 1,
        'Selection': '\\texttt{delete} for Input Strings of Length 1'
    },
    {
        'operation': 'delete',
        'length': 2,
        'Selection': '\\texttt{delete} for Input Strings of Length 2'
    },
    {
        'operation': 'delete',
        'length': 3,
        'Selection': '\\texttt{delete} for Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'replace',
        'Selection': '\\texttt{replace}'
    },
    {
        'operation': 'replace',
        'length': 1,
        'Selection': '\\texttt{replace} for Input Strings of Length 1'
    },
    {
        'operation': 'replace',
        'length': 2,
        'Selection': '\\texttt{replace} for Input Strings of Length 2'
    },
    {
        'operation': 'replace',
        'length': 3,
        'Selection': '\\texttt{replace} for Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'reverse',
        'Selection': '\\texttt{reverse}'
    },
    {
        'operation': 'reverse',
        'length': 1,
        'Selection': '\\texttt{reverse} for Input Strings of Length 1'
    },
    {
        'operation': 'reverse',
        'length': 2,
        'Selection': '\\texttt{reverse} for Input Strings of Length 2'
    },
    {
        'operation': 'reverse',
        'length': 3,
        'Selection': '\\texttt{reverse} for Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'contains',
        'Selection': '\\texttt{contains}'
    },
    {
        'operation': 'contains',
        'length': 1,
        'Selection': '\\texttt{contains} for Input Strings of Length 1'
    },
    {
        'operation': 'contains',
        'length': 2,
        'Selection': '\\texttt{contains} for Input Strings of Length 2'
    },
    {
        'operation': 'contains',
        'length': 3,
        'Selection': '\\texttt{contains} for Input Strings of Length 3'
    },
    {'is_blank': True},
    {
        'operation': 'equals',
        'Selection': '\\texttt{equals}'
    },
    {
        'operation': 'equals',
        'length': 1,
        'Selection': '\\texttt{equals} for Input Strings of Length 1'
    },
    {
        'operation': 'equals',
        'length': 2,
        'Selection': '\\texttt{equals} for Input Strings of Length 2'
    },
    {
        'operation': 'equals',
        'length': 3,
        'Selection': '\\texttt{equals} for Input Strings of Length 3'
    }
)

PER_DIFF_VS_MC_TIME_ENTRIES = (
)

PER_DIFF_VS_SOLVE_TIME_ENTRIES = (
)

PER_DIFF_VS_COMB_TIME_ENTRIES = (
)

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


def normalize_data(rows):
    for row in rows:
        f_name = row.get('File')
        for a in ('AB', 'AC', 'AD'):
            if GLOB.get('alphabet-match').get(a).match(f_name):
                for i in range(1, 4):
                    if GLOB.get('len-match').get(i).match(f_name):
                        op_1 = row.get('Op 1')
                        op_2 = row.get('Op 2')
                        if op_1 and op_2:
                            norm_1 = OP_NORMS.get(op_1).get(a).get(i)
                            norm_2 = OP_NORMS.get(op_2).get(a).get(i)
                            norm = norm_1 * norm_2
                        elif op_1:
                            norm = OP_NORMS.get(op_1).get(a).get(i)
                        else:
                            norm = 1
                        row['Norm'] = norm
                        break


def get_data():
    # get lists of data files
    mc_data = read_data_files('mc-' + GLOB['Settings'].file_pattern)
    mc_time_data = read_data_files('mc-time-' + GLOB['Settings'].file_pattern)
    op_time_data = read_data_files('op-time-' + GLOB['Settings'].file_pattern)

    # normalize data across operations
    normalize_data(mc_data)
    normalize_data(mc_time_data)
    normalize_data(op_time_data)

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


def get_latex_plot_figure(fig):
    pass


def output_latex(tables, plots):

    before_lines = list()
    before_lines.append('\\documentclass [11pt]{article}\n')
    before_lines.append('\n')
    before_lines.append('\\usepackage[utf8]{inputenc}\n')
    before_lines.append('\\usepackage{fullpage}\n')
    before_lines.append('\\usepackage[justification=centering]{caption}\n')
    before_lines.append('\\usepackage{tabu}\n')
    before_lines.append('\n')
    before_lines.append('\\begin{document}\n')
    before_lines.append('\n')

    table_list = list()
    for table, caption, label in tables:
        table_list.append(get_latex_table(table, caption, label))

    figure_list = list()
    for fig in plots:
        figure_list.append(get_latex_plot_figure(fig))

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


def output_plot_file(rows):
    pass


def output_plot_files(files):
    pass


def filter_disagree(row, prefix, disagree=True):
    return disagree or row.get(prefix + ' Agree')


def filter_input_type(row, input_type=None):
    return input_type is None or row.get('Input Type') == input_type


def filter_length(row, length=None):
    return length is None \
           or GLOB.get('len-match').get(length).match(row.get('File'))


def filter_alphabet_size(row, alphabet_size=None):
    return alphabet_size is None \
           or GLOB.get('alphabet-match').get(alphabet_size).match(row.get('File'))


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


def get_per_diffs(rows, disagree=True, bins=None, branch=None,
                  input_type=None, alphabet_size=None, length=None,
                  operation=None, exclusive_op=None, op_arg_type=None,
                  predicate=None, pred_arg_type=None):
    # initialize structures
    if bins is None:
        bins = [0, 10, 30, 50, 100]
    solvers = SOLVERS[1:]
    results = list()

    # get all diff values
    diffs = dict()

    def get_filter(prefix):
        def per_diff_filter(row):
            return row.get('Op 1') != '' \
                    and filter_input_type(row, input_type) \
                    and filter_alphabet_size(row, alphabet_size) \
                    and filter_length(row, length) \
                    and filter_operation(row, operation, exclusive_op,
                                         op_arg_type) \
                    and filter_predicate(row, predicate, pred_arg_type) \
                    and (disagree or compute_agreement(row, prefix))

        return per_diff_filter

    for solver in solvers:
        diffs[solver] = list()
        filtered = filter(get_filter(solver[0]), rows)
        if branch is None or branch:
            diffs[solver].extend(map(lambda x: compute_per_diff(x, solver[0]), filtered))

        if branch is None or not branch:
            diffs[solver].extend(map(lambda x: compute_per_diff(x, solver[0], branch_sel=False), filtered))

    hist_map = dict()
    for solver in solvers:
        diffs_np = numpy.asarray(diffs.get(solver))
        hist_map[solver] = numpy.histogram(diffs_np, bins=bins)

    for i, p in enumerate(bins):
        result = dict()
        result['Bin'] = '{0:d}\\%'.format(p)
        for solver in solvers:
            per_diff = 100 * (hist_map.get(solver)[0][i] / float(len(diffs)))
            result[solver] = '{0:.1f}\\%'.format(per_diff)
        results.append(result)

    return results


def get_agreement(rows, input_type=None, length=None, alphabet_size=None,
                  operation=None, exclusive_op=None, op_arg_type=None,
                  predicate=None, pred_arg_type=None):
    # initialize result dictionary
    results = dict()
    solvers = SOLVERS[1:]

    def get_agree_filter(prefix, skip_agree=False):
        def agree_filter(count, row):
            return row.get('Op 1') != '' \
                    and filter_input_type(row, input_type) \
                    and filter_alphabet_size(row, alphabet_size) \
                    and filter_length(row, length) \
                    and filter_operation(row, operation, exclusive_op,
                                         op_arg_type) \
                    and filter_predicate(row, predicate, pred_arg_type) \
                    and (skip_agree or compute_agreement(count, row))

        return agree_filter

    valid_count = len(filter(get_agree_filter('Id', skip_agree=True), rows))

    for solver in solvers:
        agree_count = len(filter(get_agree_filter(solver[0]), rows))
        per = 100 * (float(agree_count) / valid_count)
        results[solver] = '{0:.1f}\\%'.format(per)

    return results


def analyze_accuracy(mc_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Model Count Accuracy')

    for entry in PER_DIFF_ENTRIES:
        table = get_per_diffs(mc_rows,
                              disagree=entry.get('disagree'),
                              branch=entry.get('branch'),
                              input_type=entry.get('input_type'),
                              alphabet_size=entry.get('alphabet-size'),
                              length=entry.get('length'),
                              operation=entry.get('operation'),
                              exclusive_op=entry.get('exclusive_op'),
                              op_arg_type=entry.get('op_arg_type'),
                              predicate=entry.get('predicate'),
                              pred_arg_type=entry.get('pred_arg_type'))
        tables.append((table, entry.get('caption'), entry.get('label')))

    # agreement
    agree_results = list()

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    for entry in AGREE_ENTRIES:
        if 'is_blank' in entry and entry.get('is_blank'):
            agree_results.append(blank_row)
        else:
            row = get_agreement(mc_rows,
                                input_type=entry.get('input_type'),
                                alphabet_size=entry.get('alphabet-size'),
                                length=entry.get('length'),
                                operation=entry.get('operation'),
                                exclusive_op=entry.get('exclusive_op'),
                                op_arg_type=entry.get('op_arg_type'),
                                predicate=entry.get('predicate'),
                                pred_arg_type=entry.get('pred_arg_type'))
            row['Selection'] = entry.get('Selection')
            agree_results.append(row)

    tables.append((agree_results,
                   'Frequency of Branch Selection Agreement for Constraints',
                   'acc_agree'))

    return tables


def get_perf_metrics(rows, column_suffixes, sum_cols=False, input_type=None,
                     length=None, alphabet_size=None, operation=None,
                     exclusive_op=None, op_arg_type=None, predicate=None,
                     pred_arg_type=None):
    avg_results = dict()
    median_results = dict()
    range_results = dict()
    std_dev_results = dict()

    solvers = SOLVERS[1:]

    def perf_metric_filter(values, row):
        return row.get('Op 1') != '' \
               and filter_input_type(row, input_type) \
               and filter_alphabet_size(row, alphabet_size) \
               and filter_length(row, length)\
               and filter_operation(row, operation, exclusive_op, op_arg_type)\
               and filter_predicate(row, predicate, pred_arg_type)

    data = dict()

    for solver in solvers:
        data[solver] = dict()
        times = list()
        for col_suffix in column_suffixes:
            filtered = filter(perf_metric_filter, rows)
            if isinstance(col_suffix, str):
                times.append(map(lambda r: int(r.get(solver[0] + ' ' + col_suffix)), filtered))
            else:
                temp = list()
                for c_suffix in col_suffix:
                    temp.append(map(lambda r: int(r.get(solver[0] + ' ' + c_suffix)), filtered))

                times.append(map(lambda *l: sum(l) / len(l), *temp))

        if sum_cols:
            map(lambda *l: sum(l), *times)
        else:
            times = [x for sublist in times for x in sublist]

        times_np = numpy.asarray(times)
        data[solver]['avg'] = numpy.mean(times_np)
        data[solver]['median'] = numpy.median(times_np)
        data[solver]['range'] = (numpy.amin(times_np), numpy.amax(times_np))
        data[solver]['std_dev'] = numpy.std(times_np)

    for solver in solvers:
        col = data.get(solver)
        avg_results[solver] = '{0:.1f}'.format(col.get('avg'))
        median_results[solver] = '{0:.1f}'.format(col.get('median'))
        range_results[solver] = '{0:.1f} - {1:.1f}'.format(col.get('range')[0], col.get('range')[1])
        std_dev_results[solver] = '{0:.1f}'.format(col.get('std_dev'))

    return avg_results, median_results, range_results, std_dev_results


def process_perf_entries(rows, entries, column_suffixes=None):
    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    lists = (list(), list(), list(), list())

    for entry in entries:
        if 'is_blank' in entry and entry.get('is_blank'):
            results = (blank_row, blank_row, blank_row, blank_row)
        else:
            log.debug('Getting Performance Metrics - '
                      + entry.get('Selection'))
            if column_suffixes is None:
                column_suffixes = entry.get(column_suffixes)
            results = get_perf_metrics(rows,
                                       column_suffixes,
                                       input_type=entry.get('input_type'),
                                       alphabet_size=entry.get('alphabet-size'),
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
        lists[0].append(results[0])
        lists[1].append(results[1])
        lists[2].append(results[2])
        lists[3].append(results[3])

    return lists


def analyze_mc_performance(mc_time_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Model Count Performance')

    results = process_perf_entries(mc_time_rows, MC_TIME_ENTRIES)

    tables.append((results[0],
                   'Average Model Counting Times',
                   'mc_perf_avg'))
    tables.append((results[1],
                   'Median Model Counting Times',
                   'mc_perf_median'))
    tables.append((results[2],
                   'Model Counting Time Ranges',
                   'mc_perf_range'))
    tables.append((results[3],
                   'Standard Deviation for Model Counting Times',
                   'mc_perf_std_dev'))

    return tables


def analyze_solve_performance(mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Constraint Solving Performance')

    results = process_perf_entries(mc_time_rows, SOLVE_TIME_ENTRIES,
                                   ['Acc Time'])

    tables.append((results[0],
                   'Average Constraint Solving Times',
                   'solve_perf_acc_avg'))
    tables.append((results[1],
                   'Median Constraint Solving Times',
                   'solve_perf_acc_median'))
    tables.append((results[2],
                   'Constraint Solving Time Ranges',
                   'solve_perf_acc_range'))
    tables.append((results[3],
                   'Standard Deviation for Constraint Solving Times',
                   'solve_perf_acc_std_dev'))

    # Operation Times
    log.debug('Calculating Operation and Predicate Performance')

    results = process_perf_entries(op_time_rows, OP_TIME_ENTRIES, ['Op Time'])

    tables.append((results[0],
                   'Average Operation and Predicate Times',
                   'solve_perf_op_avg'))
    tables.append((results[1],
                   'Median Operation and Predicate Times',
                   'solve_perf_op_median'))
    tables.append((results[2],
                   'Operation and Predicate Time Ranges',
                   'solve_perf_op_range'))
    tables.append((results[3],
                   'Standard Deviation for Operation and Predicate Times',
                   'solve_perf_op_std_dev'))

    return tables


def analyze_comb_perf(mc_time_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Combined Model Counting and Solver Performance')

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    lists = (list(), list(), list(), list())

    for entry in SOLVE_TIME_ENTRIES:
        if 'is_blank' in entry and entry.get('is_blank'):
            results = (blank_row, blank_row, blank_row, blank_row)
        else:
            log.debug('Getting Combined Performance - '
                      + entry.get('Selection'))
            results = get_perf_metrics(mc_time_rows,
                                       [['T MC Time', 'F MC Time'], 'Acc Time'],
                                       input_type=entry.get('input_type'),
                                       alphabet_size=entry.get('alphabet-size'),
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
        lists[0].append(results[0])
        lists[1].append(results[1])
        lists[2].append(results[2])
        lists[3].append(results[3])

    tables.append((lists[0],
                   'Average Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_avg'))
    tables.append((lists[1],
                   'Median Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_median'))
    tables.append((lists[2],
                   'Combined Model Counting and Constraint Solving Time Ranges',
                   'comb_perf_range'))
    tables.append((lists[3],
                   'Standard Deviation for Combined Model Counting and '
                   'Constraint Solving Times',
                   'comb_perf_std_dev'))

    return tables


def analyze_acc_vs_mc_perf(mc_rows, mc_time_rows):
    # initialize file list
    files = list()

    log.debug('Gathering Model Count Accuracy vs Model Count Performance')

    for entry in MC_TIME_ENTRIES:
        table = get_per_diffs(mc_rows,
                              disagree=entry.get('disagree'),
                              branch=entry.get('branch'),
                              input_type=entry.get('input_type'),
                              alphabet_size=entry.get('alphabet-size'),
                              length=entry.get('length'),
                              operation=entry.get('operation'),
                              exclusive_op=entry.get('exclusive_op'),
                              op_arg_type=entry.get('op_arg_type'),
                              predicate=entry.get('predicate'),
                              pred_arg_type=entry.get('pred_arg_type'))
        files.append((table, entry.get('caption'), entry.get('label')))

    # get_per_diff_and_mc_time(mc_rows, mc_time_rows)

    return files


def analyze_acc_vs_solve_perf(mc_rows, mc_time_rows, op_time_rows):
    # initialize file list
    files = list()

    return files


def analyze_acc_vs_comb_perf(mc_rows, mc_time_rows, op_time_rows):
    # initialize file list
    files = list()

    return files


def perform_analysis(mc_rows, mc_time_rows, op_time_rows):
    # create latex tables
    tables = list()

    tables.extend(analyze_accuracy(mc_rows))

    tables.extend(analyze_mc_performance(mc_time_rows))

    tables.extend(analyze_solve_performance(mc_time_rows, op_time_rows))

    tables.extend(analyze_comb_perf(mc_time_rows))

    # create gnuplot graphs
    figures = list()

    # figures.extend(analyze_acc_vs_mc_perf(mc_rows, mc_time_rows))
    #
    # figures.extend(analyze_acc_vs_solve_perf(mc_rows, mc_time_rows))
    #
    # figures.extend(analyze_acc_vs_comb_perf(mc_rows, mc_time_rows))
    #
    # output_plot_files(figures)

    output_latex(tables, figures)


def main(arguments):
    # set options from args
    set_options(arguments)

    # read data
    mc_data, mc_time_data, op_time_data = get_data()

    # perform analysis
    perform_analysis(mc_data, mc_time_data, op_time_data)


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
