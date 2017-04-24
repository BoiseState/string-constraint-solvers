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


# Enums
class Branches:
    BOTH = 'both'
    TRUE = 'true'
    FALSE = 'false'

    def __init__(self):
        pass


# globals
GLOB = dict()
GLOB['len-match'] = dict()
GLOB['len-match'][1] = re.compile('\w+-A[BCDE]-01(-\d{2})?.csv')
GLOB['len-match'][2] = re.compile('\w+-A[BCDE]-02(-\d{2})?.csv')
GLOB['len-match'][3] = re.compile('\w+-A[BCDE]-03(-\d{2})?.csv')
GLOB['len-match'][4] = re.compile('\w+-A[BCDE]-04(-\d{2})?.csv')
GLOB['alphabet-match'] = dict()
GLOB['alphabet-match']['AB'] = re.compile('\w+-AB-\d{2}(-\d{2})?.csv')
GLOB['alphabet-match']['AC'] = re.compile('\w+-AC-\d{2}(-\d{2})?.csv')
GLOB['alphabet-match']['AD'] = re.compile('\w+-AD-\d{2}(-\d{2})?.csv')
GLOB['alphabet-match']['AE'] = re.compile('\w+-AE-\d{2}(-\d{2})?.csv')

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
            'AE': {1: 1, 2: 1, 3: 1, 4: 1}
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

PER_DIFF_ENTRIES = (
    {
        'caption': 'Frequency of Accuracy Difference for All Constraints',
        'label': 'acc_diff_all'
    },
    {
        'branch': True,
        'caption': 'Frequency of Accuracy Difference for \\textit{true} '
                   'Branch Constraints',
        'label': 'acc_diff_true'
    },
    {
        'branch': False,
        'caption': 'Frequency of Accuracy Difference for \\textit{false} '
                   'Branch Constraints',
        'label': 'acc_diff_false'
    },
    {
        'alphabet': 'AB',
        'caption': 'Frequency of Accuracy Difference for Constraints Where'
                   ' $|\\Sigma| = 2$',
        'label': 'acc_diff_alph_AB'
    },
    {
        'alphabet': 'AC',
        'caption': 'Frequency of Accuracy Difference for Constraints Where'
                   ' $|\\Sigma| = 3$',
        'label': 'acc_diff_alph_AC'
    },
    {
        'alphabet': 'AD',
        'caption': 'Frequency of Accuracy Difference for Constraints Where'
                   ' $|\\Sigma| = 4$',
        'label': 'acc_diff_alph_AD'
    },
    # {
    #     'alphabet': 'AE',
    #     'caption': 'Frequency of Accuracy Difference for Constraints Where'
    #                ' $|\\Sigma| = 5$',
    #     'label': 'acc_diff_alph_AE'
    # },
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
    # {
    #     'length': 4,
    #     'caption': 'Frequency of Accuracy Difference for Constraints Following'
    #                ' an Input String of Length 3',
    #     'label': 'acc_diff_len_4'
    # },
    {
        'input_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a \\textit{Simple} Input String',
        'label': 'acc_diff_simple'
    },
    {
        'input_type': 'Even',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a \\textit{Even} Unknown Input String',
        'label': 'acc_diff_even'
    },
    {
        'input_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Following'
                   ' a \\textit{Uneven} Unknown Input String',
        'label': 'acc_diff_branch'
    },
    {
        'operation': 'concat',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{concat}$ Operations',
        'label': 'acc_diff_incl_concat_all'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{concat}(Simple)$ Operations',
        'label': 'acc_diff_incl_concat_simp'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Even',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{concat}(Even)$ Operations',
        'label': 'acc_diff_incl_concat_even'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{concat}(Uneven)$ Operations',
        'label': 'acc_diff_incl_concat_uneven'
    },
    {
        'operation': 'delete',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{delete}$ Operations',
        'label': 'acc_diff_incl_delete_all'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'same',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{delete}(same)$ Operations',
        'label': 'acc_diff_incl_delete_same'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'diff',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{delete}(diff)$ Operations',
        'label': 'acc_diff_incl_delete_diff'
    },
    {
        'operation': 'replace',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{replace}$ Operations',
        'label': 'acc_diff_incl_replace_all'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'same',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{replace}(same)$ Operations',
        'label': 'acc_diff_incl_replace_same'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'diff',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{replace}(diff)$ Operations',
        'label': 'acc_diff_incl_replace_diff'
    },
    {
        'operation': 'reverse',
        'caption': 'Frequency of Accuracy Difference for Constraints Including'
                   ' $\\mathtt{reverse}$ Operations',
        'label': 'acc_diff_incl_reverse'
    },
    {
        'operation': 'concat',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{concat}$ Operations',
        'label': 'acc_diff_excl_concat_all'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{concat}(Simple)$ Operations',
        'label': 'acc_diff_excl_concat_con'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{concat}(Even)$ Operations',
        'label': 'acc_diff_excl_concat_simp'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{concat}(Uneven)$ Operations',
        'label': 'acc_diff_excl_concat_branch'
    },
    {
        'operation': 'delete',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{delete}$ Operations',
        'label': 'acc_diff_excl_delete_all'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{delete}(same)$ Operations',
        'label': 'acc_diff_excl_delete_same'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{delete}(diff)$ Operations',
        'label': 'acc_diff_excl_delete_diff'
    },
    {
        'operation': 'replace',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{replace}$ Operations',
        'label': 'acc_diff_excl_replace_all'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{replace}(same)$ Operations',
        'label': 'acc_diff_excl_replace_same'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{replace}(diff)$ Operations',
        'label': 'acc_diff_excl_replace_diff'
    },
    {
        'operation': 'reverse',
        'exclusive_op': True,
        'caption': 'Frequency of Accuracy Difference for Constraints Only'
                   ' $\\mathtt{reverse}$ Operations',
        'label': 'acc_diff_excl_reverse'
    },
    {
        'predicate': 'contains',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}$ Predicates',
        'label': 'acc_diff_contains_all'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}(Simple)$ Predicates',
        'label': 'acc_diff_contains_con'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}(Even)$ Predicates',
        'label': 'acc_diff_contains_simp'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{contains}(Uneven)$ Predicates',
        'label': 'acc_diff_contains_branch'
    },
    {
        'predicate': 'equals',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}$ Predicates',
        'label': 'acc_diff_equals_all'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}(Simple)$ Predicates',
        'label': 'acc_diff_equals_con'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}(Even)$ Predicates',
        'label': 'acc_diff_equals_simp'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'caption': 'Frequency of Accuracy Difference for Constraints Ending in'
                   ' $\\mathtt{equals}(Uneven)$ Predicates',
        'label': 'acc_diff_equals_branch'
    }
)

AGREE_ENTRIES = (
    {
        'Selection': 'All'
    },
    {'is_blank': True},
    {
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    {
        'alphabet': 'AD',
        'Selection': '$|\\Sigma| = 4$'
    },
    # {
    #     'alphabet': 'AE',
    #     'Selection': '$|\\Sigma| = 5$'
    # },
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
    # {
    #     'length': 4,
    #     'Selection': 'Length 4'
    # },
    {'is_blank': True},
    {
        'input_type': 'Simple',
        'Selection': '\\textit{Simple}'
    },
    {
        'input_type': 'Even',
        'Selection': '\\textit{Even}'
    },
    {
        'input_type': 'Uneven',
        'Selection': '\\textit{Uneven}'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'operation': 'delete',
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'operation': 'replace',
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'operation': 'reverse',
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{concat}$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{concat}(Simple)$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{concat}(Even)$'
    },
    {
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{concat}(Uneven)$'
    },
    {
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{delete}$'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{delete}(same)$'
    },
    {
        'operation': 'delete',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{delete}(diff)$'
    },
    {
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{replace}$'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{replace}(same)$'
    },
    {
        'operation': 'replace',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{replace}(diff)$'
    },
    {
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'predicate': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'predicate': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    }
)

MC_TIME_ENTRIES = (
    {
        'mc_time_branch': Branches.BOTH,
        'Selection': 'All Constraints'
    },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.TRUE,
        'Selection': '\\textit{true} Branches'
    },
    {
        'mc_time_branch': Branches.FALSE,
        'Selection': '\\textit{false} Branches'
    },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AE',
        'Selection': '$|\\Sigma| = 5$'
    },
    # {
    #     'mc_time_branch': Branches.BOTH,
    #     'alphabet': 'AB',
    #     'Selection': '$|\\Sigma| = 4$'
    # },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'length': 3,
        'Selection': 'Length 3'
    },
    # {
    #     'mc_time_branch': Branches.BOTH,
    #     'length': 4,
    #     'Selection': 'Length 4'
    # }
)

SOLVE_TIME_ENTRIES = (
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'Selection': 'All'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.TRUE,
        'Selection': '\\textit{true} Branches'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.FALSE,
        'Selection': '\\textit{false} Branches'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    # {
    #     'acc_time': True,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AD',
    #     'Selection': '$|\\Sigma| = 4$'
    # },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AE',
        'Selection': '$|\\Sigma| = 5$'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 3,
        'Selection': 'Length 3'
    },
    # {
    #     'acc_time': True,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 4,
    #     'Selection': 'Length 4'
    # },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Simple',
        'Selection': '\\textit{Simple}'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Even',
        'Selection': '\\textit{Even}'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Uneven',
        'Selection': '\\textit{Uneven}'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{delete}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{delete}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{replace}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Only $\\mathtt{replace}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    }
)

OP_TIME_ENTRIES = (
    {
        'op_time': True,
        'operation': 'concat',
        'Selection': '$\\mathtt{concat}$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{concat}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{concat}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{concat}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{concat}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'concat',
        'length': 1,
        'Selection': '$\\mathtt{concat}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'length': 2,
        'Selection': '$\\mathtt{concat}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'length': 3,
        'Selection': '$\\mathtt{concat}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'length': 4,
    #     'Selection': '$\\mathtt{concat}$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': '$\\mathtt{concat}(Simple)$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{concat}(Simple)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{concat}(Simple)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{concat}(Simple)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Simple',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{concat}(Simple)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'length': 1,
        'Selection': '$\\mathtt{concat}(Simple)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'length': 2,
        'Selection': '$\\mathtt{concat}(Simple)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'length': 3,
        'Selection': '$\\mathtt{concat}(Simple)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Simple',
    #     'length': 4,
    #     'Selection': '$\\mathtt{concat}(Simple)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': '$\\mathtt{concat}(Even)$'
    },
    {
        '(Even)op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{concat}(Even)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{concat}(Even)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{concat}(Even)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Even',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{concat}(Even)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'length': 1,
        'Selection': '$\\mathtt{concat}(Even)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'length': 2,
        'Selection': '$\\mathtt{concat}(Even)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'length': 3,
        'Selection': '$\\mathtt{concat}(Even)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Even',
    #     'length': 4,
    #     'Selection': '$\\mathtt{concat}(Even)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': '$\\mathtt{concat}(Uneven)$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{concat}(Uneven)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{concat}(Uneven)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{concat}(Uneven)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Uneven',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{concat}(Uneven)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'length': 1,
        'Selection': '$\\mathtt{concat}(Uneven)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'length': 2,
        'Selection': '$\\mathtt{concat}(Uneven)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'length': 3,
        'Selection': '$\\mathtt{concat}(Uneven)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'concat',
    #     'op_arg_type': 'Uneven',
    #     'length': 4,
    #     'Selection': '$\\mathtt{concat}(Uneven)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'delete',
        'Selection': '$\\mathtt{delete}$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{delete}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{delete}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{delete}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{delete}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'delete',
        'length': 1,
        'Selection': '$\\mathtt{delete}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'length': 2,
        'Selection': '$\\mathtt{delete}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'length': 3,
        'Selection': '$\\mathtt{delete}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'length': 4,
    #     'Selection': '$\\mathtt{delete}$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': '$\\mathtt{delete}(same)$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{delete}(same)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{delete}(same)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{delete}(same)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'op_arg_type': 'same',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{delete}(same)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'length': 1,
        'Selection': '$\\mathtt{delete}(same)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'length': 2,
        'Selection': '$\\mathtt{delete}(same)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'same',
        'length': 3,
        'Selection': '$\\mathtt{delete}(same)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'op_arg_type': 'same',
    #     'length': 4,
    #     'Selection': '$\\mathtt{delete}(same)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': '$\\mathtt{delete}(diff)$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{delete}(diff)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{delete}(diff)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{delete}(diff)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'op_arg_type': 'diff',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{delete}(diff)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'length': 1,
        'Selection': '$\\mathtt{delete}(diff)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'length': 2,
        'Selection': '$\\mathtt{delete}(diff)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'length': 3,
        'Selection': '$\\mathtt{delete}(diff)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'delete',
    #     'op_arg_type': 'diff',
    #     'length': 4,
    #     'Selection': '$\\mathtt{delete}(diff)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'replace',
        'Selection': '$\\mathtt{replace}$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{replace}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{replace}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{replace}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{replace}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'replace',
        'length': 1,
        'Selection': '$\\mathtt{replace}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'length': 2,
        'Selection': '$\\mathtt{replace}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'length': 3,
        'Selection': '$\\mathtt{replace}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'length': 4,
    #     'Selection': '$\\mathtt{replace}$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': '$\\mathtt{replace}(same)$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{replace}(same)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{replace}(same)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{replace}(same)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'op_arg_type': 'same',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{replace}(same)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'length': 1,
        'Selection': '$\\mathtt{replace}(same)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'length': 2,
        'Selection': '$\\mathtt{replace}(same)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'same',
        'length': 3,
        'Selection': '$\\mathtt{replace}(same)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'op_arg_type': 'same',
    #     'length': 4,
    #     'Selection': '$\\mathtt{replace}(same)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': '$\\mathtt{replace}(diff)$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{replace}(diff)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{replace}(diff)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{replace}(diff)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'op_arg_type': 'diff',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{replace}(diff)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'length': 1,
        'Selection': '$\\mathtt{replace}(diff)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'length': 2,
        'Selection': '$\\mathtt{replace}(diff)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'length': 3,
        'Selection': '$\\mathtt{replace}(diff)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'replace',
    #     'op_arg_type': 'diff',
    #     'length': 4,
    #     'Selection': '$\\mathtt{replace}(diff)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'reverse',
        'Selection': '$\\mathtt{reverse}$'
    },
    {
        'op_time': True,
        'operation': 'reverse',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{reverse}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'reverse',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{reverse}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'reverse',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{reverse}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'reverse',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{reverse}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'reverse',
        'length': 1,
        'Selection': '$\\mathtt{reverse}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'reverse',
        'length': 2,
        'Selection': '$\\mathtt{reverse}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'reverse',
        'length': 3,
        'Selection': '$\\mathtt{reverse}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'reverse',
    #     'length': 4,
    #     'Selection': '$\\mathtt{reverse}$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{contains}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{contains}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{contains}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{contains}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'contains',
        'length': 1,
        'Selection': '$\\mathtt{contains}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'length': 2,
        'Selection': '$\\mathtt{contains}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'length': 3,
        'Selection': '$\\mathtt{contains}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'length': 4,
    #     'Selection': '$\\mathtt{contains}$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{contains}(Simple)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{contains}(Simple)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{contains}(Simple)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Simple',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{contains}(Simple)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'length': 1,
        'Selection': '$\\mathtt{contains}(Simple)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'length': 2,
        'Selection': '$\\mathtt{contains}(Simple)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Simple',
        'length': 3,
        'Selection': '$\\mathtt{contains}(Simple)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Simple',
    #     'length': 4,
    #     'Selection': '$\\mathtt{contains}(Simple)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{contains}(Even)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{contains}(Even)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{contains}(Even)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Even',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{contains}(Even)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'length': 1,
        'Selection': '$\\mathtt{contains}(Even)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'length': 2,
        'Selection': '$\\mathtt{contains}(Even)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Even',
        'length': 3,
        'Selection': '$\\mathtt{contains}(Even)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Even',
    #     'length': 4,
    #     'Selection': '$\\mathtt{contains}(Even)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{contains}(Uneven)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{contains}(Uneven)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{contains}(Uneven)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Uneven',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{contains}(Uneven)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'length': 1,
        'Selection': '$\\mathtt{contains}(Uneven)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'length': 2,
        'Selection': '$\\mathtt{contains}(Uneven)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'contains',
        'op_arg_type': 'Uneven',
        'length': 3,
        'Selection': '$\\mathtt{contains}(Uneven)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'contains',
    #     'op_arg_type': 'Uneven',
    #     'length': 4,
    #     'Selection': '$\\mathtt{contains}(Uneven)$ for Length 4'
    # },
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{equals}$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{equals}$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{equals}$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{equals}$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'equals',
        'length': 1,
        'Selection': '$\\mathtt{equals}$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'length': 2,
        'Selection': '$\\mathtt{equals}$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'length': 3,
        'Selection': '$\\mathtt{equals}$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'length': 4,
    #     'Selection': '$\\mathtt{equals}$ for Length 4'
    # }
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{equals}(Simple)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{equals}(Simple)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{equals}(Simple)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Simple',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{equals}(Simple)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'length': 1,
        'Selection': '$\\mathtt{equals}(Simple)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'length': 2,
        'Selection': '$\\mathtt{equals}(Simple)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Simple',
        'length': 3,
        'Selection': '$\\mathtt{equals}(Simple)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Simple',
    #     'length': 4,
    #     'Selection': '$\\mathtt{equals}(Simple)$ for Length 4'
    # }
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{equals}(Even)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{equals}(Even)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{equals}(Even)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Even',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{equals}(Even)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'length': 1,
        'Selection': '$\\mathtt{equals}(Even)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'length': 2,
        'Selection': '$\\mathtt{equals}(Even)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Even',
        'length': 3,
        'Selection': '$\\mathtt{equals}(Even)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Even',
    #     'length': 4,
    #     'Selection': '$\\mathtt{equals}(Even)$ for Length 4'
    # }
    {'is_blank': True},
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'alphabet': 'AB',
        'Selection': '$\\mathtt{equals}(Uneven)$ where $|\\Sigma| = 2$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'alphabet': 'AC',
        'Selection': '$\\mathtt{equals}(Uneven)$ where $|\\Sigma| = 3$'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'alphabet': 'AD',
        'Selection': '$\\mathtt{equals}(Uneven)$ where $|\\Sigma| = 4$'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Uneven',
    #     'alphabet': 'AE',
    #     'Selection': '$\\mathtt{equals}(Uneven)$ where $|\\Sigma| = 5$'
    # },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'length': 1,
        'Selection': '$\\mathtt{equals}(Uneven)$ for Length 1'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'length': 2,
        'Selection': '$\\mathtt{equals}(Uneven)$ for Length 2'
    },
    {
        'op_time': True,
        'operation': 'equals',
        'op_arg_type': 'Uneven',
        'length': 3,
        'Selection': '$\\mathtt{equals}(Uneven)$ for Length 3'
    },
    # {
    #     'op_time': True,
    #     'operation': 'equals',
    #     'op_arg_type': 'Uneven',
    #     'length': 4,
    #     'Selection': '$\\mathtt{equals}(Uneven)$ for Length 4'
    # }
)

COMB_TIME_ENTRIES = (
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'Selection': 'All'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.TRUE,
        'pred_time_branch': Branches.TRUE,
        'Selection': '\\textit{true} Branches'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.FALSE,
        'pred_time_branch': Branches.FALSE,
        'Selection': '\\textit{false} Branches'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AD',
        'Selection': '$|\\Sigma| = 4$'
    },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AE',
    #     'Selection': '$|\\Sigma| = 5$'
    # },
    {'is_blank': True},
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'length': 3,
        'Selection': 'Length 3'
    },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 4,
    #     'Selection': 'Length 4'
    # },
    {'is_blank': True},
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Simple',
        'Selection': '\\textit{Simple}'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Even',
        'Selection': '\\textit{Even}'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Uneven',
        'Selection': '\\textit{Uneven}'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    }
)

PER_DIFF_VS_MC_TIME_ENTRIES = (
    {
        'mc_time_branch': Branches.BOTH,
        'Selection': 'All'
    },
    {
        'mc_time_branch': Branches.TRUE,
        'Selection': '\\textit{true} Branches'
    },
    {
        'mc_time_branch': Branches.FALSE,
        'Selection': '\\textit{false} Branches'
    },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'alphabet': 'AD',
        'Selection': '$|\\Sigma| = 4$'
    },
    # {
    #     'mc_time_branch': Branches.BOTH,
    #     'alphabet': 'AE',
    #     'Selection': '$|\\Sigma| = 5$'
    # },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'length': 3,
        'Selection': 'Length 3'
    },
    # {
    #     'mc_time_branch': Branches.BOTH,
    #     'length': 4,
    #     'Selection': 'Length 4'
    # },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'input_type': 'Simple',
        'Selection': '\\textit{Simple}'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'input_type': 'Even',
        'Selection': '\\textit{Even}'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'input_type': 'Uneven',
        'Selection': '\\textit{Uneven}'
    },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'delete',
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'replace',
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'mc_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    }
)

PER_DIFF_VS_SOLVE_TIME_ENTRIES = (
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'Selection': 'All'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.TRUE,
        'Selection': '\\textit{true} Branches'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.FALSE,
        'Selection': '\\textit{false} Branches'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AB',
        'Selection': '$|\\Sigma| = 2$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AC',
        'Selection': '$|\\Sigma| = 3$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'alphabet': 'AD',
        'Selection': '$|\\Sigma| = 4$'
    },
    # {
    #     'acc_time': True,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AE',
    #     'Selection': '$|\\Sigma| = 5$'
    # },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 1,
        'Selection': 'Length 1'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 2,
        'Selection': 'Length 2'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'length': 3,
        'Selection': 'Length 3'
    },
    # {
    #     'acc_time': True,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 4,
    #     'Selection': 'Length 4'
    # },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Simple',
        'Selection': '\\textit{Simple}'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Even',
        'Selection': '\\textit{Even}'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'input_type': 'Uneven',
        'Selection': '\\textit{Uneven}'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{delete}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{delete}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'same',
        'Selection': 'Includes $\\mathtt{replace}(same)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'op_arg_type': 'diff',
        'Selection': 'Includes $\\mathtt{replace}(diff)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {'is_blank': True},
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Simple',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Even',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'concat',
        'op_arg_type': 'Uneven',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'delete',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{delete}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'replace',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{replace}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'operation': 'reverse',
        'exclusive_op': True,
        'Selection': 'Includes $\\mathtt{reverse}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'Selection': '$\\mathtt{contains}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{contains}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{contains}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'contains',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{contains}(Uneven)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'Selection': '$\\mathtt{equals}$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Simple',
        'Selection': '$\\mathtt{equals}(Simple)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Even',
        'Selection': '$\\mathtt{equals}(Even)$'
    },
    {
        'acc_time': True,
        'pred_time_branch': Branches.BOTH,
        'predicate': 'equals',
        'pred_arg_type': 'Uneven',
        'Selection': '$\\mathtt{equals}(Uneven)$'
    }
)

PER_DIFF_VS_COMB_TIME_ENTRIES = (
    {
        'acc_time': True,
        'mc_time_branch': Branches.BOTH,
        'pred_time_branch': Branches.BOTH,
        'Selection': 'All'
    },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.TRUE,
    #     'pred_time_branch': Branches.TRUE,
    #     'Selection': '\\textit{true} Branches'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.FALSE,
    #     'pred_time_branch': Branches.FALSE,
    #     'Selection': '\\textit{false} Branches'
    # },
    # {'is_blank': True},
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AB',
    #     'Selection': '$|\\Sigma| = 2$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AC',
    #     'Selection': '$|\\Sigma| = 3$'
    # },
    # # {
    # #     'acc_time': True,
    # #     'mc_time_branch': Branches.BOTH,
    # #     'pred_time_branch': Branches.BOTH,
    # #     'alphabet': 'AD',
    # #     'Selection': '$|\\Sigma| = 4$'
    # # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'alphabet': 'AE',
    #     'Selection': '$|\\Sigma| = 5$'
    # },
    # {'is_blank': True},
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 1,
    #     'Selection': 'Length 1'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 2,
    #     'Selection': 'Length 2'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'length': 3,
    #     'Selection': 'Length 3'
    # },
    # # {
    # #     'acc_time': True,
    # #     'mc_time_branch': Branches.BOTH,
    # #     'pred_time_branch': Branches.BOTH,
    # #     'length': 4,
    # #     'Selection': 'Length 4'
    # # },
    # {'is_blank': True},
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'input_type': 'Simple',
    #     'Selection': '\\textit{Simple}'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'input_type': 'Even',
    #     'Selection': '\\textit{Even}'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'input_type': 'Uneven',
    #     'Selection': '\\textit{Uneven}'
    # },
    # {'is_blank': True},
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'Selection': 'Includes $\\mathtt{concat}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Simple',
    #     'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Even',
    #     'Selection': 'Includes $\\mathtt{concat}(Even)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Uneven',
    #     'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'delete',
    #     'Selection': 'Includes $\\mathtt{delete}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'delete',
    #     'op_arg_type': 'same',
    #     'Selection': 'Includes $\\mathtt{delete}(same)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'delete',
    #     'op_arg_type': 'diff',
    #     'Selection': 'Includes $\\mathtt{delete}(diff)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'replace',
    #     'Selection': 'Includes $\\mathtt{replace}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'replace',
    #     'op_arg_type': 'same',
    #     'Selection': 'Includes $\\mathtt{replace}(same)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'replace',
    #     'op_arg_type': 'diff',
    #     'Selection': 'Includes $\\mathtt{replace}(diff)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'reverse',
    #     'Selection': 'Includes $\\mathtt{reverse}$'
    # },
    # {'is_blank': True},
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{concat}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Simple',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{concat}(Simple)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Even',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{concat}(Even)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'concat',
    #     'op_arg_type': 'Uneven',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{concat}(Uneven)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'delete',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{delete}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'replace',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{replace}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'operation': 'reverse',
    #     'exclusive_op': True,
    #     'Selection': 'Includes $\\mathtt{reverse}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'contains',
    #     'Selection': '$\\mathtt{contains}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'contains',
    #     'pred_arg_type': 'Simple',
    #     'Selection': '$\\mathtt{contains}(Simple)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'contains',
    #     'pred_arg_type': 'Even',
    #     'Selection': '$\\mathtt{contains}(Even)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'contains',
    #     'pred_arg_type': 'Uneven',
    #     'Selection': '$\\mathtt{contains}(Uneven)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'equals',
    #     'Selection': '$\\mathtt{equals}$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'equals',
    #     'pred_arg_type': 'Simple',
    #     'Selection': '$\\mathtt{equals}(Simple)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'equals',
    #     'pred_arg_type': 'Even',
    #     'Selection': '$\\mathtt{equals}(Even)$'
    # },
    # {
    #     'acc_time': True,
    #     'mc_time_branch': Branches.BOTH,
    #     'pred_time_branch': Branches.BOTH,
    #     'predicate': 'equals',
    #     'pred_arg_type': 'Uneven',
    #     'Selection': '$\\mathtt{equals}(Uneven)$'
    # }
)

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


def normalize_row(row):
    f_name = row.get('File')
    for a in ('AB', 'AC', 'AD', 'AE'):
        if GLOB.get('alphabet-match').get(a).match(f_name):
            for i in range(1, 4):
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


def output_plot_data_file(rows, label):

    field_names = list(rows[0].keys())

    data_file_path = os.path.join(project_dir, 'data', label + '.csv')
    with open(data_file_path, 'w') as csv_file:
        # create csv writer with field names
        writer = csv.DictWriter(csv_file,
                                field_names)
        # output header
        writer.writeheader()
        # output rows
        writer.writerows(rows)


def output_plot_script(rows, plot_type, label):
    lines = list()

    out_path = os.path.join(project_dir, 'data', label + '.gnu')
    with open(out_path, 'w') as out_file:
        out_file.writelines(lines)


def output_plot_files(files):
    for rows, plot_type, c, label in files:
        # output_plot_script(rows, plot_type, label)
        output_plot_data_file(rows, label)


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


def get_per_diffs(rows,
                  disagree=True,
                  bins=None,
                  branch=None,
                  raw=None,
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
        bins = [0.0, 0.01, 0.03, 0.05, 1.0]
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
                    and (disagree or compute_agreement(row, prefix))

        return per_diff_filter

    for solver in SOLVERS:
        diffs = list()
        weights = list()
        filtered = filter(get_filter(solver[0]), rows)
        if branch is None or branch:
            diffs.extend(map(lambda x: compute_per_diff(x, solver[0]), filtered))
            weights.extend(map(lambda x: x.get('Norm'), filtered))

        if branch is None or not branch:
            diffs.extend(map(lambda x: compute_per_diff(x, solver[0], branch_sel=False), filtered))
            weights.extend(map(lambda x: x.get('Norm'), filtered))

        diffs_np = numpy.asarray(diffs)
        weights_np = numpy.asarray(weights)

        if raw is not None:
            weights_np = weights_np + raw
            per_diff_map[solver] = numpy.repeat(diffs_np, weights_np)
        else:
            per_diff_map[solver] = numpy.histogram(diffs_np, bins=bins, weights=weights_np)

    if raw is not None:
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


def analyze_accuracy(mc_rows):
    # initialize tables list
    tables = list()

    log.debug('Calculating Model Count Accuracy')

    for entry in PER_DIFF_ENTRIES:
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
            log.debug('Processing Agreement Percent for %s', entry.get('Selection'))
            row = get_agreement(mc_rows,
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
                   'Frequency of Branch Selection Agreement for Constraints',
                   'acc_agree'))

    return tables


def get_perf_metrics(rows,
                     mc_time_branch=None,
                     acc_time=False,
                     pred_time_branch=None,
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
    variance_results = dict()
    std_dev_results = dict()
    w_times = dict()

    def perf_metric_filter(row):
        return row.get('Op 1') != '' \
               and filter_input_type(row, input_type) \
               and filter_alphabet(row, alphabet) \
               and filter_length(row, length)\
               and filter_operation(row, operation, exclusive_op, op_arg_type)\
               and filter_predicate(row, predicate, pred_arg_type)

    filtered = filter(perf_metric_filter, rows)

    log.debug('Getting Weights')

    # get weights
    weights = map(lambda r: int(r.get('Norm')), filtered)
    if mc_time_branch == Branches.BOTH or pred_time_branch == Branches.BOTH:
        weights = reduce(lambda l, v: l + [v, v], weights, list())

    for solver in SOLVERS:
        times = list()
        s = solver[0].upper()

        log.debug('Getting Performance Times for %s', solver)

        # get mc times
        if mc_time_branch is not None:
            mc_times = list()
            if mc_time_branch == Branches.BOTH:
                for row in filtered:
                    mc_times.append(int(row.get(s + ' T MC Time')))
                    mc_times.append(int(row.get(s + ' F MC Time')))
            else:
                if mc_time_branch == Branches.TRUE:
                    mc_times = map(lambda r: int(r.get(s + ' T MC Time')), filtered)
                elif mc_time_branch == Branches.FALSE:
                    mc_times = map(lambda r: int(r.get(s + ' F MC Time')), filtered)

                if pred_time_branch == Branches.BOTH:
                    mc_times = reduce(lambda l, v: l + [v, v], mc_times, list())

            times.append(mc_times)

        # get acc times
        if acc_time:
            acc_times = map(lambda r: int(r.get(s + ' Acc Time')), filtered)

            if mc_time_branch == Branches.BOTH or pred_time_branch == Branches.BOTH:
                acc_times = reduce(lambda l, v: l + [v, v], acc_times, list())

            times.append(acc_times)

        # get pred times
        if pred_time_branch is not None:
            pred_times = list()
            if pred_time_branch == Branches.BOTH:
                for row in filtered:
                    pred_times.append(int(row.get(s + ' T Pred Time')))
                    pred_times.append(int(row.get(s + ' F Pred Time')))
            else:
                if pred_time_branch == Branches.TRUE:
                    pred_times = map(lambda r: int(r.get(s + ' T Pred Time')), filtered)
                elif pred_time_branch == Branches.FALSE:
                    pred_times = map(lambda r: int(r.get(s + ' F Pred Time')), filtered)

                if mc_time_branch == Branches.BOTH:
                    pred_times = reduce(lambda l, v: l + [v, v], pred_times, list())

            times.append(pred_times)

        # get op times
        if op_time:
            times.append(map(lambda r: int(r.get(s + ' Op Time')), filtered))

        log.debug('Summing Times')

        # sum times
        if len(times) > 1:
            times = map(lambda x: sum(x), zip(*times))
        else:
            times = times[0]

        log.debug('Converting times to numpy arrays')
        times_np = numpy.asarray(times)
        weights_np = numpy.asarray(weights)
        w_times_np = numpy.repeat(times_np, weights_np)
        w_times[solver] = w_times_np

        log.debug('Calculating mean')
        avg_results[solver] = '{0:.1f}'.format(numpy.mean(w_times_np))

        log.debug('Calculating median')
        median_results[solver] = '{0:.1f}'.format(numpy.median(w_times_np))

        log.debug('Calculating variance')
        variance_results[solver] = '{0:.1f}'.format(numpy.var(w_times_np))

        log.debug('Calculating standard deviation')
        std_dev_results[solver] = '{0:.1f}'.format(numpy.std(w_times_np))

    # get weighted times as list of dictionaries
    log.debug('Transforming Weighted Times')
    w_t_keys = w_times.keys()
    w_times = map(lambda r: {
                                w_t_keys[0]: r[0],
                                w_t_keys[1]: r[1],
                                w_t_keys[2]: r[2],
                                w_t_keys[3]: r[3]
                            }, w_times.values())

    return avg_results, median_results, variance_results, std_dev_results, w_times


def process_perf_entries(rows,
                         entries,
                         perf_type,
                         label):
    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    lists = (list(), list(), list(), list(), list())

    for i, entry in enumerate(entries):
        if 'is_blank' in entry and entry.get('is_blank'):
            results = (blank_row, blank_row, blank_row, blank_row)
        else:
            log.debug('Getting Performance Metrics - ' + entry.get('Selection'))
            results = get_perf_metrics(rows,
                                       mc_time_branch=entry.get('mc_time_branch'),
                                       acc_time=entry.get('acc_time'),
                                       pred_time_branch=entry.get('pred_time_branch'),
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
                             'boxplot',
                             'Box Plot for {0} - {1}'.format(perf_type, entry.get('Selection')),
                             '{0}_{1:02d}'.format(label, i)))
        lists[0].append(results[0])
        lists[1].append(results[1])
        lists[2].append(results[2])
        lists[3].append(results[3])

    return lists


def analyze_mc_performance(mc_time_rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Model Count Performance')

    results = process_perf_entries(mc_time_rows,
                                   MC_TIME_ENTRIES,
                                   'Model Counting Times',
                                   'mc_perf')

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


def analyze_solve_performance(mc_time_rows, op_time_rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Constraint Solving Performance')

    results = process_perf_entries(mc_time_rows,
                                   SOLVE_TIME_ENTRIES,
                                   'Constraint Solving Times',
                                   'solve_perf_acc')

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

    # Operation Times
    log.debug('Calculating Operation and Predicate Performance')

    results = process_perf_entries(op_time_rows,
                                   OP_TIME_ENTRIES,
                                   'Operation and Predicate Times',
                                   'solve_perf_op')

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


def analyze_comb_perf(mc_time_rows):
    # initialize tables list
    tables = list()
    files = list()

    log.debug('Calculating Combined Model Counting and Solver Performance')

    blank_row = {
        'Selection': '',
        'Unbounded': '',
        'Bounded': '',
        'Aggregate': '',
        'Weighted': ''
    }

    lists = (list(), list(), list(), list(), list())

    for i, entry in enumerate(COMB_TIME_ENTRIES):
        if 'is_blank' in entry and entry.get('is_blank'):
            results = (blank_row, blank_row, blank_row, blank_row)
        else:
            log.debug('Getting Combined Performance - ' + entry.get('Selection'))
            results = get_perf_metrics(mc_time_rows,
                                       mc_time_branch=entry.get('mc_time_branch'),
                                       acc_time=entry.get('acc_time'),
                                       pred_time_branch=entry.get('pred_time_branch'),
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
        lists[0].append(results[0])
        lists[1].append(results[1])
        lists[2].append(results[2])
        lists[3].append(results[3])
        files.append((results[4],
                      'boxplot',
                      'Box Plot of Combined Model Counting and Constraint Solving Times - ' + entry.get('Selection'),
                      'comb_perf_plot_{0}'.format(i)))

    tables.append((lists[0],
                   'Average Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_avg'))
    tables.append((lists[1],
                   'Median Combined Model Counting and Constraint Solving'
                   ' Times',
                   'comb_perf_median'))
    tables.append((lists[2],
                   'Combined Model Counting and Constraint Solving Time Variance',
                   'comb_perf_var'))
    tables.append((lists[3],
                   'Standard Deviation for Combined Model Counting and '
                   'Constraint Solving Times',
                   'comb_perf_std_dev'))

    return tables, files


def analyze_acc_vs_mc_perf(mc_rows, mc_time_rows):
    # initialize file list
    files = list()

    log.debug('Gathering Model Count Accuracy vs Model Count Performance')

    for i, entry in enumerate(PER_DIFF_VS_MC_TIME_ENTRIES):
        repeat_per_diffs = 1
        if entry.get('mc_time_branch') is None \
                or entry.get('pred_time_branch') is None:
            repeat_per_diffs = 2
        table = get_per_diffs(mc_rows,
                              raw=repeat_per_diffs,
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

        results = get_perf_metrics(mc_time_rows,
                                   mc_time_branch=entry.get('mc_time_branch'),
                                   acc_time=entry.get('acc_time'),
                                   pred_time_branch=entry.get('pred_time_branch'),
                                   op_time=entry.get('op_time'),
                                   input_type=entry.get('input_type'),
                                   alphabet=entry.get('alphabet'),
                                   length=entry.get('length'),
                                   operation=entry.get('operation'),
                                   exclusive_op=entry.get('exclusive_op'),
                                   op_arg_type=entry.get('op_arg_type'),
                                   predicate=entry.get('predicate'),
                                   pred_arg_type=entry.get('pred_arg_type'))

        for solver in SOLVERS:
            table[solver[0] + '_Per_Diff'] = table.pop(solver, None)
            table[solver[0] + '_MC_Times'] = results[4]

        files.append((table,
                      'scatter',
                      'Plot of Percent Difference vs Model Counting Time - ' + entry.get('Selection'),
                      'acc_vs_mc_perf_plot_{0}'.format(i)))

    return files


def analyze_acc_vs_solve_perf(mc_rows, mc_time_rows):
    # initialize file list
    files = list()

    log.debug('Gathering Model Count Accuracy vs Constraint Solving Performance')

    for i, entry in enumerate(PER_DIFF_VS_SOLVE_TIME_ENTRIES):
        table = get_per_diffs(mc_rows,
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

        results = get_perf_metrics(mc_time_rows,
                                   mc_time_branch=entry.get('mc_time_branch'),
                                   acc_time=entry.get('acc_time'),
                                   pred_time_branch=entry.get('pred_time_branch'),
                                   op_time=entry.get('op_time'),
                                   input_type=entry.get('input_type'),
                                   alphabet=entry.get('alphabet'),
                                   length=entry.get('length'),
                                   operation=entry.get('operation'),
                                   exclusive_op=entry.get('exclusive_op'),
                                   op_arg_type=entry.get('op_arg_type'),
                                   predicate=entry.get('predicate'),
                                   pred_arg_type=entry.get('pred_arg_type'))

        for solver in SOLVERS:
            table[solver[0] + '_Per_Diff'] = table.pop(solver, None)
            table[solver[0] + '_Solve_Times'] = results[4]

        files.append((results[4],
                      'scatter',
                      'Plot of Percent Difference vs Constraint Solving Time - ' + entry.get('Selection'),
                      'acc_vs_solve_perf_plot_{0}'.format(i)))

    return files


def analyze_acc_vs_comb_perf(mc_rows, mc_time_rows):
    # initialize file list
    files = list()

    log.debug('Gathering Model Count Accuracy vs Combined Model Counting and '
              'Constraint Solving Performance')

    for i, entry in enumerate(PER_DIFF_VS_COMB_TIME_ENTRIES):
        table = get_per_diffs(mc_rows,
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

        results = get_perf_metrics(mc_time_rows,
                                   mc_time_branch=entry.get('mc_time_branch'),
                                   acc_time=entry.get('acc_time'),
                                   pred_time_branch=entry.get('pred_time_branch'),
                                   op_time=entry.get('op_time'),
                                   input_type=entry.get('input_type'),
                                   alphabet=entry.get('alphabet'),
                                   length=entry.get('length'),
                                   operation=entry.get('operation'),
                                   exclusive_op=entry.get('exclusive_op'),
                                   op_arg_type=entry.get('op_arg_type'),
                                   predicate=entry.get('predicate'),
                                   pred_arg_type=entry.get('pred_arg_type'))

        for solver in SOLVERS:
            table[solver[0] + '_Per_Diff'] = table.pop(solver, None)
            table[solver[0] + '_Comb_Times'] = results[4]

        files.append((results[4],
                      'scatter',
                      'Plot of Percent Difference vs Combined Model Counting and Constraint Solving Time - ' + entry.get('Selection'),
                      'acc_vs_solve_perf_plot_{0}'.format(i)))

    return files


def perform_analysis(mc_rows, mc_time_rows, op_time_rows):
    # create lists
    tables = list()
    figures = list()

    # acc_tables = analyze_accuracy(mc_rows)
    # tables.extend(acc_tables)

    # mc_perf_tables, mc_perf_files = analyze_mc_performance(mc_time_rows)
    # tables.extend(mc_perf_tables)
    # figures.extend(mc_perf_files)

    solve_perf_tables, solve_perf_files = analyze_solve_performance(mc_time_rows, op_time_rows)
    tables.extend(solve_perf_tables)
    figures.extend(solve_perf_files)

    # comb_perf_tables, comb_perf_files = analyze_comb_perf(mc_time_rows)
    # tables.extend(comb_perf_tables)
    # figures.extend(comb_perf_files)

    # figures.extend(analyze_acc_vs_mc_perf(mc_rows, mc_time_rows))

    # figures.extend(analyze_acc_vs_solve_perf(mc_rows, mc_time_rows))

    # figures.extend(analyze_acc_vs_comb_perf(mc_rows, mc_time_rows))

    output_plot_files(figures)

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
