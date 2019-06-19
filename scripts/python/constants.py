#! /usr/bin/env python

ANALYSIS_LIST = (
    # 'const',
    # 'op',
    'agree',
    'agree-stat',
    'comb-time',
    'comb-time-stat',
    'mc-time',
    'mc-time-stat',
    'op-time',
    'op-time-stat',
    'per-diff',
    'per-diff-stat',
    'per-diff-vs-solve-time',
    'per-diff-vs-mc-time',
    'per-diff-vs-comb-time',
    'solve-time',
    'solve-time-stat'
)

MEASUREMENTS = (
    'per_diff',
    'agree',
    'solve_time',
    'mc_time',
    'comb_time',
    'op_time'
)

SOLVERS = (
    'Unbounded',
    'Bounded',
    'Aggregate',
    'Weighted'
)

D_TYPES = {
    'const': [
        ('Alphabet', 'u1'),
        ('Length', 'u1'),
        ('Id', 'u8'),
        ('U_Agree', 'b1'),
        ('B_Agree', 'b1'),
        ('A_Agree', 'b1'),
        ('W_Agree', 'b1'),
        ('C_T_Per_Diff', 'f8'),
        ('U_T_Per_Diff', 'f8'),
        ('B_T_Per_Diff', 'f8'),
        ('A_T_Per_Diff', 'f8'),
        ('W_T_Per_Diff', 'f8'),
        ('C_F_Per_Diff', 'f8'),
        ('U_F_Per_Diff', 'f8'),
        ('B_F_Per_Diff', 'f8'),
        ('A_F_Per_Diff', 'f8'),
        ('W_F_Per_Diff', 'f8'),
        ('C_T_MC_Time', 'u8'),
        ('U_T_MC_Time', 'u8'),
        ('B_T_MC_Time', 'u8'),
        ('A_T_MC_Time', 'u8'),
        ('W_T_MC_Time', 'u8'),
        ('C_F_MC_Time', 'u8'),
        ('U_F_MC_Time', 'u8'),
        ('B_F_MC_Time', 'u8'),
        ('A_F_MC_Time', 'u8'),
        ('W_F_MC_Time', 'u8'),
        ('C_T_Solve_Time', 'u8'),
        ('U_T_Solve_Time', 'u8'),
        ('B_T_Solve_Time', 'u8'),
        ('A_T_Solve_Time', 'u8'),
        ('W_T_Solve_Time', 'u8'),
        ('C_F_Solve_Time', 'u8'),
        ('U_F_Solve_Time', 'u8'),
        ('B_F_Solve_Time', 'u8'),
        ('A_F_Solve_Time', 'u8'),
        ('W_F_Solve_Time', 'u8'),
        ('Input_Type', 'u1'),
        ('Op_1', 'u1'),
        ('Op_1_Arg', 'u1'),
        ('Op_2', 'u1'),
        ('Op_2_Arg', 'u1'),
        ('Pred', 'u1'),
        ('Pred_Arg', 'u1'),
        ('Norm', 'u8')
    ],
    'op': [
        ('Alphabet', 'u1'),
        ('Length', 'u1'),
        ('Op_Id', 'u8'),
        ('Op', 'u1'),
        ('In_Type', 'u1'),
        ('Args', 'u1'),
        ('Base_Id', 'u8'),
        ('C_Op_Time', 'u8'),
        ('U_Op_Time', 'u8'),
        ('B_Op_Time', 'u8'),
        ('A_Op_Time', 'u8'),
        ('W_Op_Time', 'u8'),
        ('Norm', 'u8')
    ],
    'stat': [
        ('Measurement', 'u1'),
        ('Solver', 'u1'),
        ('Data_Set', 'u1'),
        ('Avg/Freq', 'f8'),
        ('Median', 'f8'),
        ('Std_Dev', 'f8'),
        ('Test_Stat_vs_Unbounded', 'f8'),
        ('P_Val_vs_Unbounded', 'f8'),
        ('Test_Stat_vs_Bounded', 'f8'),
        ('P_Val_vs_Bounded', 'f8'),
        ('Test_Stat_vs_Aggregate', 'f8'),
        ('P_Val_vs_Aggregate', 'f8'),
        ('Test_Stat_vs_Weighted', 'f8'),
        ('P_Val_vs_Weighted', 'f8'),
        ('Test_Stat_Ind_Var', 'f8'),
        ('P_Val_Ind_Var', 'f8')
    ]
}

OUT_FORMATS = {
    'const': [
        '{:d}',
        '{:d}',
        '{:d}',
        '{:b}',
        '{:b}',
        '{:b}',
        '{:b}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:.3f}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}'
    ],
    'op': [
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}',
        '{:d}'
    ],
    'stat': [
        '{:d}',
        '{:d}',
        '{:d}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}',
        '{:.4f}'
    ],
    'box': [
        '{:f}',
        '{:f}',
        '{:f}',
        '{:f}'
    ],
    'hist': [
        '{:f}',
        '{:f}',
        '{:f}',
        '{:f}',
        '{:f}'
    ]
}

DATA_SETS = (
    'all',  # 0
    'true',
    'false',
    'alpha_AB',
    'alpha_AC',
    'alpha_AD',
    'alpha_AE',
    'len_1',
    'len_2',
    'len_3',
    'len_4',  # 10
    'literal',
    'simple',
    'complex',
    'incl_concat_all',
    'incl_concat_literal',
    'incl_concat_simple',
    'incl_concat_complex',
    'incl_delete_all',
    'incl_delete_same',
    'incl_delete_diff',  # 20
    'incl_replace_all',
    'incl_replace_same',
    'incl_replace_diff',
    'incl_reverse',
    'excl_concat_all',
    'excl_concat_literal',
    'excl_concat_simple',
    'excl_concat_complex',
    'excl_delete_all',
    'excl_delete_same',  # 30
    'excl_delete_diff',
    'excl_replace_all',
    'excl_replace_same',
    'excl_replace_diff',
    'excl_reverse',
    'contains_all',
    'contains_literal',
    'contains_simple',
    'contains_complex',
    'equals_all',  # 40
    'equals_literal',
    'equals_simple',
    'equals_complex',
    'agree_all',
    'agree_true',
    'agree_false',
    'agree_alpha_AB',
    'agree_alpha_AC',
    'agree_alpha_AD',
    'agree_alpha_AE',  # 50
    'agree_len_1',
    'agree_len_2',
    'agree_len_3',
    'agree_len_4',
    'agree_literal',
    'agree_simple',
    'agree_complex',
    'agree_incl_concat_all',
    'agree_incl_concat_literal',
    'agree_incl_concat_simple',  # 60
    'agree_incl_concat_complex',
    'agree_incl_delete_all',
    'agree_incl_delete_same',
    'agree_incl_delete_diff',
    'agree_incl_replace_all',
    'agree_incl_replace_same',
    'agree_incl_replace_diff',
    'agree_incl_reverse',
    'agree_excl_concat_all',
    'agree_excl_concat_literal',  # 70
    'agree_excl_concat_simple',
    'agree_excl_concat_complex',
    'agree_excl_delete_all',
    'agree_excl_delete_same',
    'agree_excl_delete_diff',
    'agree_excl_replace_all',
    'agree_excl_replace_same',
    'agree_excl_replace_diff',
    'agree_excl_reverse',
    'agree_contains_all',  # 80
    'agree_contains_literal',
    'agree_contains_simple',
    'agree_contains_complex',
    'agree_equals_all',
    'agree_equals_literal',
    'agree_equals_simple',
    'agree_equals_complex',
    'concat_all',
    'concat_alpha_AB',
    'concat_alpha_AC',  # 90
    'concat_alpha_AD',
    'concat_alpha_AE',
    'concat_len_1',
    'concat_len_2',
    'concat_len_3',
    'concat_len_4',
    'concat_literal_all',
    'concat_literal_alpha_AB',
    'concat_literal_alpha_AC',
    'concat_literal_alpha_AD',  # 100
    'concat_literal_alpha_AE',
    'concat_literal_len_1',
    'concat_literal_len_2',
    'concat_literal_len_3',
    'concat_literal_len_4',
    'concat_simple_all',
    'concat_simple_alpha_AB',
    'concat_simple_alpha_AC',
    'concat_simple_alpha_AD',
    'concat_simple_alpha_AE',  # 110
    'concat_simple_len_1',
    'concat_simple_len_2',
    'concat_simple_len_3',
    'concat_simple_len_4',
    'concat_complex_all',
    'concat_complex_alpha_AB',
    'concat_complex_alpha_AC',
    'concat_complex_alpha_AD',
    'concat_complex_alpha_AE',
    'concat_complex_len_1',  # 120
    'concat_complex_len_2',
    'concat_complex_len_3',
    'concat_complex_len_4',
    'delete_all',
    'delete_alpha_AB',
    'delete_alpha_AC',
    'delete_alpha_AD',
    'delete_alpha_AE',
    'delete_len_1',
    'delete_len_2',  # 130
    'delete_len_3',
    'delete_len_4',
    'delete_same_all',
    'delete_same_alpha_AB',
    'delete_same_alpha_AC',
    'delete_same_alpha_AD',
    'delete_same_alpha_AE',
    'delete_same_len_1',
    'delete_same_len_2',
    'delete_same_len_3',  # 140
    'delete_same_len_4',
    'delete_diff_all',
    'delete_diff_alpha_AB',
    'delete_diff_alpha_AC',
    'delete_diff_alpha_AD',
    'delete_diff_alpha_AE',
    'delete_diff_len_1',
    'delete_diff_len_2',
    'delete_diff_len_3',
    'delete_diff_len_4',  # 150
    'replace_all',
    'replace_alpha_AB',
    'replace_alpha_AC',
    'replace_alpha_AD',
    'replace_alpha_AE',
    'replace_len_1',
    'replace_len_2',
    'replace_len_3',
    'replace_len_4',
    'replace_same_all',  # 160
    'replace_same_alpha_AB',
    'replace_same_alpha_AC',
    'replace_same_alpha_AD',
    'replace_same_alpha_AE',
    'replace_same_len_1',
    'replace_same_len_2',
    'replace_same_len_3',
    'replace_same_len_4',
    'replace_diff_all',
    'replace_diff_alpha_AB',  # 170
    'replace_diff_alpha_AC',
    'replace_diff_alpha_AD',
    'replace_diff_alpha_AE',
    'replace_diff_len_1',
    'replace_diff_len_2',
    'replace_diff_len_3',
    'replace_diff_len_4',
    'reverse_all',
    'reverse_alpha_AB',
    'reverse_alpha_AC',  # 180
    'reverse_alpha_AD',
    'reverse_alpha_AE',
    'reverse_len_1',
    'reverse_len_2',
    'reverse_len_3',
    'reverse_len_4',
    'contains_all',
    'contains_alpha_AB',
    'contains_alpha_AC',
    'contains_alpha_AD',  # 190
    'contains_alpha_AE',
    'contains_len_1',
    'contains_len_2',
    'contains_len_3',
    'contains_len_4',
    'contains_literal_all',
    'contains_literal_alpha_AB',
    'contains_literal_alpha_AC',
    'contains_literal_alpha_AD',
    'contains_literal_alpha_AE',  # 200
    'contains_literal_len_1',
    'contains_literal_len_2',
    'contains_literal_len_3',
    'contains_literal_len_4',
    'contains_Simple_all',
    'contains_Simple_alpha_AB',
    'contains_Simple_alpha_AC',
    'contains_Simple_alpha_AD',
    'contains_Simple_alpha_AE',
    'contains_Simple_len_1',  # 210
    'contains_Simple_len_2',
    'contains_Simple_len_3',
    'contains_Simple_len_4',
    'contains_complex_all',
    'contains_complex_alpha_AB',
    'contains_complex_alpha_AC',
    'contains_complex_alpha_AD',
    'contains_complex_alpha_AE',
    'contains_complex_len_1',
    'contains_complex_len_2',  # 220
    'contains_complex_len_3',
    'contains_complex_len_4',
    'equals_all',
    'equals_alpha_AB',
    'equals_alpha_AC',
    'equals_alpha_AD',
    'equals_alpha_AE',
    'equals_len_1',
    'equals_len_2',
    'equals_len_3',  # 230
    'equals_len_4',
    'equals_literal_all',
    'equals_literal_alpha_AB',
    'equals_literal_alpha_AC',
    'equals_literal_alpha_AD',
    'equals_literal_alpha_AE',
    'equals_literal_len_1',
    'equals_literal_len_2',
    'equals_literal_len_3',
    'equals_literal_len_4',  # 240
    'equals_simple_all',
    'equals_simple_alpha_AB',
    'equals_simple_alpha_AC',
    'equals_simple_alpha_AD',
    'equals_simple_alpha_AE',
    'equals_simple_len_1',
    'equals_simple_len_2',
    'equals_simple_len_3',
    'equals_simple_len_4',
    'equals_complex_all',  # 250
    'equals_complex_alpha_AB',
    'equals_complex_alpha_AC',
    'equals_complex_alpha_AD',
    'equals_complex_alpha_AE',
    'equals_complex_len_1',
    'equals_complex_len_2',
    'equals_complex_len_3',
    'equals_complex_len_4'
)

ALPHABETS = (
    'AB',
    'AC',
    'AD',
    'AE'
)

LENGTHS = (
    1,
    2,
    3,
    4
)

OPS_AND_PREDS = (
    '',
    'concat',
    'delete',
    'replace',
    'reverse',
    'contains',
    'equals',
    'init'
)

IN_AND_ARG_TYPE = (
    '',
    'none',
    'Literal',
    'Simple',
    'Complex',
    'same',
    'diff'
)

OP_NORMS = {
    'concat': {
        'Literal': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 20, 2: 20, 3: 20, 4: 20},
            'AD': {1: 15, 2: 15, 3: 15, 4: 15},
            'AE': {1: 12, 2: 12, 3: 12, 4: 12}
        },
        'Simple': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        },
        'Complex': {
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
    },
    'contains': {
        'Literal': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 20, 2: 20, 3: 20, 4: 20},
            'AD': {1: 15, 2: 15, 3: 15, 4: 15},
            'AE': {1: 12, 2: 12, 3: 12, 4: 12}
        },
        'Simple': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        },
        'Complex': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        }
    },
    'equals': {
        'Literal': {
            'AB': {1: 30, 2: 30, 3: 30, 4: 30},
            'AC': {1: 20, 2: 20, 3: 20, 4: 20},
            'AD': {1: 15, 2: 15, 3: 15, 4: 15},
            'AE': {1: 12, 2: 12, 3: 12, 4: 12}
        },
        'Simple': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        },
        'Complex': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        }
    },
    'init': {
        'none': {
            'AB': {1: 60, 2: 60, 3: 60, 4: 60},
            'AC': {1: 60, 2: 60, 3: 60, 4: 60},
            'AD': {1: 60, 2: 60, 3: 60, 4: 60},
            'AE': {1: 60, 2: 60, 3: 60, 4: 60}
        }
    }
}

ORDER_COLUMNS = (
    'Bin',
    'Selection',
    'First Solver',
    'Solver',
    'Statistic',
    '\\textit{p}-value',
    'Concrete',
    'Unbounded',
    'U Stat',
    'U \\textit{p}-value',
    'Bounded',
    'B Stat',
    'B \\textit{p}-value',
    'Aggregate',
    'A Stat',
    'A \\textit{p}-value',
    'Weighted',
    'W Stat',
    'W \\textit{p}-value'
)