#!/usr/bin/env python
import math

model = ['U', 'B', 'A', 'W']

cat_a = [
    'All',
    '$|\\Sigma| = 2$',
    '$|\\Sigma| = 3$',
    '$|\\Sigma| = 4$',
    '$|\\Sigma| = 5$',
    '$k = 1$',
    '$k = 2$',
    '$k = 3$',
    '$k = 4$',
    '\\textit{Literal}',
    '\\textit{Simple}',
    '\\textit{Complex}',
    '$\\exists\\ \mathtt{concat}$',
    '$\\exists\\ \mathtt{concat}(L)$',
    '$\\exists\\ \mathtt{concat}(S)$',
    '$\\exists\\ \mathtt{concat}(C)$',
    '$\\exists\\ \mathtt{delete}$',
    '$\\exists\\ \mathtt{delete}(s)$',
    '$\\exists\\ \mathtt{delete}(d)$',
    '$\\exists\\ \mathtt{replace}$',
    '$\\exists\\ \mathtt{replace}(s)$',
    '$\\exists\\ \mathtt{replace}(d)$',
    '$\\exists\\ \mathtt{reverse}$',
    '$\\forall\\ \mathtt{concat}$',
    '$\\forall\\ \mathtt{concat}(L)$',
    '$\\forall\\ \mathtt{concat}(S)$',
    '$\\forall\\ \mathtt{concat}(C)$',
    '$\\forall\\ \mathtt{delete}$',
    '$\\forall\\ \mathtt{delete}(s)$',
    '$\\forall\\ \mathtt{delete}(d)$',
    '$\\forall\\ \mathtt{replace}$',
    '$\\forall\\ \mathtt{replace}(s)$',
    '$\\forall\\ \mathtt{replace}(d)$',
    '$\\forall\\ \mathtt{reverse}$',
    '$\mathtt{contains}$',
    '$\mathtt{contains}(L)$',
    '$\mathtt{contains}(S)$',
    '$\mathtt{contains}(C)$',
    '$\mathtt{equals}$',
    '$\mathtt{equals}(L)$',
    '$\mathtt{equals}(S)$',
    '$\mathtt{equals}(C)$'
]

order_a_cat = [
    'All',
    'hline',
    '$|\\Sigma| = 2$',
    '$|\\Sigma| = 3$',
    '$|\\Sigma| = 4$',
    '$|\\Sigma| = 5$',
    'hline',
    '$k = 1$',
    '$k = 2$',
    '$k = 3$',
    '$k = 4$',
    'hline',
    '\\textit{Literal}',
    '\\textit{Simple}',
    '\\textit{Complex}',
    'hline',
    '$\\exists\\ \mathtt{concat}$',
    '$\\exists\\ \mathtt{concat}(L)$',
    '$\\exists\\ \mathtt{concat}(S)$',
    '$\\exists\\ \mathtt{concat}(C)$',
    '$\\forall\\ \mathtt{concat}$',
    '$\\forall\\ \mathtt{concat}(L)$',
    '$\\forall\\ \mathtt{concat}(S)$',
    '$\\forall\\ \mathtt{concat}(C)$',
    'hline',
    '$\\exists\\ \mathtt{delete}$',
    '$\\exists\\ \mathtt{delete}(s)$',
    '$\\exists\\ \mathtt{delete}(d)$',
    '$\\forall\\ \mathtt{delete}$',
    '$\\forall\\ \mathtt{delete}(s)$',
    '$\\forall\\ \mathtt{delete}(d)$',
    'hline',
    '$\\exists\\ \mathtt{replace}$',
    '$\\exists\\ \mathtt{replace}(s)$',
    '$\\exists\\ \mathtt{replace}(d)$',
    '$\\forall\\ \mathtt{replace}$',
    '$\\forall\\ \mathtt{replace}(s)$',
    '$\\forall\\ \mathtt{replace}(d)$',
    'hline',
    '$\\exists\\ \mathtt{reverse}$',
    '$\\forall\\ \mathtt{reverse}$',
    'hline',
    '$\mathtt{contains}$',
    '$\mathtt{contains}(L)$',
    '$\mathtt{contains}(S)$',
    '$\mathtt{contains}(C)$',
    'hline',
    '$\mathtt{equals}$',
    '$\mathtt{equals}(L)$',
    '$\mathtt{equals}(S)$',
    '$\mathtt{equals}(C)$',
]

count = [
    542592,
    49536,
    85824,
    150336,
    256896,
    91152,
    112752,
    145584,
    193104,
    180864,
    180864,
    180864,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    542592,
    271296,
    90432,
    63381,
    117483,
    271296,
    90432,
    63974,
    116890
]

a_freq = {
    'U': [84.6, 82.6, 84.5, 85.8, 85.7, 84.2, 84.7, 84.7, 84.9, 82.7, 86.5,
          84.7, 75.5, 75.0, 76.2, 74.4, 91.7, 88.9, 95.5, 87.6, 89.0, 87.5,
          89.0, 72.1, 70.7, 74.5, 72.2, 99.5, 99.5, 99.5, 97.6, 99.8, 96.4,
          99.9, 98.9, 97.6, 100.0, 99.1, 70.4, 98.7, 45.3, 63.0],
    'B': [99.3, 98.9, 99.4, 99.4, 99.6, 98.8, 99.3, 99.5, 99.7, 99.6, 99.0,
          99.4, 99.6, 99.5, 99.7, 99.6, 99.3, 99.6, 99.1, 98.9, 99.5, 98.1,
          99.5, 99.6, 100.0, 99.5, 98.8, 99.6, 99.8, 99.5, 97.9, 99.8, 96.9,
          99.9, 99.2, 98.0, 100.0, 99.6, 99.5, 98.6, 100.0, 99.8],
    'A': [99.3, 98.8, 99.3, 99.5, 99.6, 98.9, 99.3, 99.5, 99.6, 99.6, 99.0,
          99.4, 99.6, 99.5, 99.7, 99.7, 99.3, 99.6, 99.0, 98.9, 99.5, 98.1,
          99.4, 99.9, 100.0, 99.5, 99.7, 99.5, 99.8, 99.5, 97.9, 99.8, 96.9,
          99.9, 99.2, 98.1, 100.0, 99.7, 99.4, 98.5, 100.0, 99.8],
    'W': [99.8, 99.6, 99.9, 99.8, 99.9, 99.6, 99.7, 99.9, 100.0, 99.6, 99.9,
          99.8, 99.9, 99.9, 99.9, 99.9, 99.7, 99.8, 99.7, 99.7, 99.8, 99.6,
          99.8, 99.9, 100.0, 99.5, 99.7, 99.8, 99.8, 99.8, 99.6, 99.8, 99.4,
          99.9, 99.6, 99.0, 100.0, 99.9, 100.0, 100.0, 100.0, 100.0]
}
a_delta = {
    'U': [(x - a_freq['U'][0]) for x in a_freq['U']],
    'B': [(x - a_freq['B'][0]) for x in a_freq['B']],
    'A': [(x - a_freq['A'][0]) for x in a_freq['A']],
    'W': [(x - a_freq['W'][0]) for x in a_freq['W']]
}

a_count = {
    'U': [(count[i] * (a_freq['U'][i] / 100)) for i in range(len(a_freq['U']))],
    'B': [(count[i] * (a_freq['B'][i] / 100)) for i in range(len(a_freq['B']))],
    'A': [(count[i] * (a_freq['A'][i] / 100)) for i in range(len(a_freq['A']))],
    'W': [(count[i] * (a_freq['W'][i] / 100)) for i in range(len(a_freq['W']))]
}

a_effect = {
    'U': [],
    'B': [],
    'A': [],
    'W': []
}

for m in model:
    for i in range(len(a_count[m])):
        p = (a_count[m][i] + a_count[m][0]) / (count[i] + count[0])
        z_n = a_delta[m][i] / 100
        z_d = math.sqrt(p * (1 - p) * ((1 / count[i]) + (1 / count[0])))
        z = z_n / z_d
        es = abs(z / math.sqrt(count[i] + count[0]))
        a_effect[m].append(es)

cat_pd = [
    'All',
    '\\textit{true} Branches',
    '\\textit{false} Branches',
    '$|\\Sigma| = 2$',
    '$|\\Sigma| = 3$',
    '$|\\Sigma| = 4$',
    '$|\\Sigma| = 5$',
    '$k = 1$',
    '$k = 2$',
    '$k = 3$',
    '$k = 4$',
    '\\textit{Literal}',
    '\\textit{Simple}',
    '\\textit{Complex}',
    '$\\exists\\ \mathtt{concat}$',
    '$\\exists\\ \mathtt{concat}(L)$',
    '$\\exists\\ \mathtt{concat}(S)$',
    '$\\exists\\ \mathtt{concat}(C)$',
    '$\\exists\\ \mathtt{delete}$',
    '$\\exists\\ \mathtt{delete}(s)$',
    '$\\exists\\ \mathtt{delete}(d)$',
    '$\\exists\\ \mathtt{replace}$',
    '$\\exists\\ \mathtt{replace}(s)$',
    '$\\exists\\ \mathtt{replace}(d)$',
    '$\\exists\\ \mathtt{reverse}$',
    '$\\forall\\ \mathtt{concat}$',
    '$\\forall\\ \mathtt{concat}(L)$',
    '$\\forall\\ \mathtt{concat}(S)$',
    '$\\forall\\ \mathtt{concat}(C)$',
    '$\\forall\\ \mathtt{delete}$',
    '$\\forall\\ \mathtt{delete}(s)$',
    '$\\forall\\ \mathtt{delete}(d)$',
    '$\\forall\\ \mathtt{replace}$',
    '$\\forall\\ \mathtt{replace}(s)$',
    '$\\forall\\ \mathtt{replace}(d)$',
    '$\\forall\\ \mathtt{reverse}$',
    '$\mathtt{contains}$',
    '$\mathtt{contains}(L)$',
    '$\mathtt{contains}(S)$',
    '$\mathtt{contains}(C)$',
    '$\mathtt{equals}$',
    '$\mathtt{equals}(L)$',
    '$\mathtt{equals}(S)$',
    '$\mathtt{equals}(C)$',
    'A. All',
    'A. \\textit{true} Branches',
    'A. \\textit{false} Branches',
    'A. $|\\Sigma| = 2$',
    'A. $|\\Sigma| = 3$',
    'A. $|\\Sigma| = 4$',
    'A. $|\\Sigma| = 5$',
    'A. $k = 1$',
    'A. $k = 2$',
    'A. $k = 3$',
    'A. $k = 4$',
    'A. \\textit{Literal}',
    'A. \\textit{Simple}',
    'A. \\textit{Complex}',
    'A. $\\exists\\ \mathtt{concat}$',
    'A. $\\exists\\ \mathtt{concat}(L)$',
    'A. $\\exists\\ \mathtt{concat}(S)$',
    'A. $\\exists\\ \mathtt{concat}(C)$',
    'A. $\\exists\\ \mathtt{delete}$',
    'A. $\\exists\\ \mathtt{delete}(s)$',
    'A. $\\exists\\ \mathtt{delete}(d)$',
    'A. $\\exists\\ \mathtt{replace}$',
    'A. $\\exists\\ \mathtt{replace}(s)$',
    'A. $\\exists\\ \mathtt{replace}(d)$',
    'A. $\\exists\\ \mathtt{reverse}$',
    'A. $\\forall\\ \mathtt{concat}$',
    'A. $\\forall\\ \mathtt{concat}(L)$',
    'A. $\\forall\\ \mathtt{concat}(S)$',
    'A. $\\forall\\ \mathtt{concat}(C)$',
    'A. $\\forall\\ \mathtt{delete}$',
    'A. $\\forall\\ \mathtt{delete}(s)$',
    'A. $\\forall\\ \mathtt{delete}(d)$',
    'A. $\\forall\\ \mathtt{replace}$',
    'A. $\\forall\\ \mathtt{replace}(s)$',
    'A. $\\forall\\ \mathtt{replace}(d)$',
    'A. $\\forall\\ \mathtt{reverse}$',
    'A. $\mathtt{contains}$',
    'A. $\mathtt{contains}(L)$',
    'A. $\mathtt{contains}(S)$',
    'A. $\mathtt{contains}(C)$',
    'A. $\mathtt{equals}$',
    'A. $\mathtt{equals}(L)$',
    'A. $\mathtt{equals}(S)$',
    'A. $\mathtt{equals}(C)$'
]

order_pd_cat = [
    'All',
    'A. All',
    'hline',
    '\\textit{true} Branches',
    '\\textit{false} Branches',
    # 'A. \\textit{true} Branches',
    # 'A. \\textit{false} Branches',
    'hline',
    '$|\\Sigma| = 2$',
    '$|\\Sigma| = 3$',
    '$|\\Sigma| = 4$',
    '$|\\Sigma| = 5$',
    # 'A. $|\\Sigma| = 2$',
    # 'A. $|\\Sigma| = 3$',
    # 'A. $|\\Sigma| = 4$',
    # 'A. $|\\Sigma| = 5$',
    'hline',
    '$k = 1$',
    '$k = 2$',
    '$k = 3$',
    '$k = 4$',
    # 'A. $k = 1$',
    # 'A. $k = 2$',
    # 'A. $k = 3$',
    # 'A. $k = 4$',
    'hline',
    '\\textit{Literal}',
    '\\textit{Simple}',
    '\\textit{Complex}',
    # 'A. \\textit{Literal}',
    # 'A. \\textit{Simple}',
    # 'A. \\textit{Complex}',
    'hline',
    '$\\exists\\ \mathtt{concat}$',
    '$\\exists\\ \mathtt{concat}(L)$',
    '$\\exists\\ \mathtt{concat}(S)$',
    '$\\exists\\ \mathtt{concat}(C)$',
    '$\\forall\\ \mathtt{concat}$',
    '$\\forall\\ \mathtt{concat}(L)$',
    '$\\forall\\ \mathtt{concat}(S)$',
    '$\\forall\\ \mathtt{concat}(C)$',
    # 'A. $\\exists\\ \mathtt{concat}$',
    # 'A. $\\exists\\ \mathtt{concat}(L)$',
    # 'A. $\\exists\\ \mathtt{concat}(S)$',
    # 'A. $\\exists\\ \mathtt{concat}(C)$',
    # 'A. $\\forall\\ \mathtt{concat}$',
    # 'A. $\\forall\\ \mathtt{concat}(L)$',
    # 'A. $\\forall\\ \mathtt{concat}(S)$',
    # 'A. $\\forall\\ \mathtt{concat}(C)$',
    'hline',
    '$\\exists\\ \mathtt{delete}$',
    '$\\exists\\ \mathtt{delete}(s)$',
    '$\\exists\\ \mathtt{delete}(d)$',
    '$\\forall\\ \mathtt{delete}$',
    '$\\forall\\ \mathtt{delete}(s)$',
    '$\\forall\\ \mathtt{delete}(d)$',
    # 'A. $\\exists\\ \mathtt{delete}$',
    # 'A. $\\exists\\ \mathtt{delete}(s)$',
    # 'A. $\\exists\\ \mathtt{delete}(d)$',
    # 'A. $\\forall\\ \mathtt{delete}$',
    # 'A. $\\forall\\ \mathtt{delete}(s)$',
    # 'A. $\\forall\\ \mathtt{delete}(d)$',
    'hline',
    '$\\exists\\ \mathtt{replace}$',
    '$\\exists\\ \mathtt{replace}(s)$',
    '$\\exists\\ \mathtt{replace}(d)$',
    '$\\forall\\ \mathtt{replace}$',
    '$\\forall\\ \mathtt{replace}(s)$',
    '$\\forall\\ \mathtt{replace}(d)$',
    # 'A. $\\exists\\ \mathtt{replace}$',
    # 'A. $\\exists\\ \mathtt{replace}(s)$',
    # 'A. $\\exists\\ \mathtt{replace}(d)$',
    # 'A. $\\forall\\ \mathtt{replace}$',
    # 'A. $\\forall\\ \mathtt{replace}(s)$',
    # 'A. $\\forall\\ \mathtt{replace}(d)$',
    'hline',
    '$\\exists\\ \mathtt{reverse}$',
    '$\\forall\\ \mathtt{reverse}$',
    # 'A. $\\exists\\ \mathtt{reverse}$',
    # 'A. $\\forall\\ \mathtt{reverse}$',
    'hline',
    '$\mathtt{contains}$',
    '$\mathtt{contains}(L)$',
    '$\mathtt{contains}(S)$',
    '$\mathtt{contains}(C)$',
    # 'A. $\mathtt{contains}$',
    # 'A. $\mathtt{contains}(L)$',
    # 'A. $\mathtt{contains}(S)$',
    # 'A. $\mathtt{contains}(C)$',
    'hline',
    '$\mathtt{equals}$',
    '$\mathtt{equals}(L)$',
    '$\mathtt{equals}(S)$',
    '$\mathtt{equals}(C)$',
    # 'A. $\mathtt{equals}$',
    # 'A. $\mathtt{equals}(L)$',
    # 'A. $\mathtt{equals}(S)$',
    # 'A. $\mathtt{equals}(C)$'
]

pd_count = {
    'U': count.copy(),
    'B': count.copy(),
    'A': count.copy(),
    'W': count.copy(),
}

for m in model:
    # true branch
    pd_count[m].insert(1, count[0])
    # false branch
    pd_count[m].insert(2, count[0])
    # agree all
    pd_count[m].append(a_count[m][0])
    # agree true branch
    pd_count[m].append(a_count[m][0])
    # agree false branch
    pd_count[m].append(a_count[m][0])
    # all other independent variable categories
    for i in range(1, len(a_count)):
        pd_count[m].append(a_count[m][i])

per_diff = {
    'U': [9.93, 16.28, 3.57, 10.80, 10.01, 9.55, 9.34, 12.95, 8.70, 8.93, 9.11,
          10.12, 9.30, 10.33, 14.94, 14.38, 15.51, 16.04, 5.66, 7.23, 3.46,
          8.27, 7.54, 8.48, 7.51, 17.98, 16.94, 17.53, 17.92, 0.95, 1.27, 0.73,
          3.82, 2.17, 4.64, 2.25, 3.27, 2.19, 1.04, 5.49, 16.58, 1.04, 24.52,
          24.25, 9.93, 16.28, 3.57, 10.80, 10.01, 9.55, 9.34, 12.95, 8.70, 8.93,
          9.11, 10.12, 9.30, 10.33, 14.94, 14.38, 15.51, 16.04, 5.66, 7.23,
          3.46, 8.27, 7.54, 8.48, 7.51, 17.98, 16.94, 17.53, 17.92, 0.95, 1.27,
          0.73, 3.82, 2.17, 4.64, 2.25, 3.27, 2.19, 1.04, 5.49, 16.58, 1.04,
          24.52, 24.25],
    'B': [1.02, 0.99, 1.06, 1.82, 0.87, 0.79, 0.59, 1.41, 1.18, 0.83, 0.67,
          0.42, 1.50, 1.14, 0.94, 0.82, 1.24, 0.82, 0.95, 0.67, 1.22, 1.37,
          0.80, 2.07, 0.81, 0.79, 0.52, 1.35, 0.67, 0.70, 0.29, 0.73, 1.88,
          0.25, 2.66, 0.24, 1.26, 1.92, 1.04, 0.87, 0.78, 0.91, 0.61, 0.78,
          1.02, 0.99, 1.06, 1.82, 0.87, 0.79, 0.59, 1.41, 1.18, 0.83, 0.67,
          0.42, 1.50, 1.14, 0.94, 0.82, 1.24, 0.82, 0.95, 0.67, 1.22, 1.37,
          0.80, 2.07, 0.81, 0.79, 0.52, 1.35, 0.67, 0.70, 0.29, 0.73, 1.88,
          0.25, 2.66, 0.24, 1.26, 1.92, 1.04, 0.87, 0.78, 0.91, 0.61, 0.78],
    'A': [0.95, 0.88, 1.02, 1.72, 0.82, 0.71, 0.56, 1.26, 1.10, 0.79, 0.66,
          0.41, 1.31, 1.12, 0.81, 0.85, 0.86, 0.72, 0.91, 0.59, 1.24, 1.36,
          0.71, 2.14, 0.72, 0.66, 0.52, 0.79, 0.48, 0.71, 0.29, 0.75, 1.88,
          0.25, 2.66, 0.24, 1.19, 1.81, 1.04, 0.79, 0.71, 0.93, 0.45, 0.69,
          0.95, 0.88, 1.02, 1.72, 0.82, 0.71, 0.56, 1.26, 1.10, 0.79, 0.66,
          0.41, 1.31, 1.12, 0.81, 0.85, 0.86, 0.72, 0.91, 0.59, 1.24, 1.36,
          0.71, 2.14, 0.72, 0.66, 0.52, 0.79, 0.48, 0.71, 0.29, 0.75, 1.88,
          0.25, 2.66, 0.24, 1.19, 1.81, 1.04, 0.79, 0.71, 0.93, 0.45, 0.69],
    'W': [0.40, 0.05, 0.75, 0.97, 0.27, 0.25, 0.12, 0.91, 0.44, 0.14, 0.11,
          0.37, 0.40, 0.42, 0.39, 0.41, 0.48, 0.41, 0.42, 0.33, 0.51, 0.36,
          0.32, 0.40, 0.35, 0.60, 0.52, 0.73, 0.44, 0.35, 0.29, 0.36, 0.41,
          0.25, 0.51, 0.24, 0.75, 1.18, 1.04, 0.22, 0.05, 0.09, 0.03, 0.04,
          0.40, 0.05, 0.75, 0.97, 0.27, 0.25, 0.12, 0.91, 0.44, 0.14, 0.11,
          0.37, 0.40, 0.42, 0.39, 0.41, 0.48, 0.41, 0.42, 0.33, 0.51, 0.36,
          0.32, 0.40, 0.35, 0.60, 0.52, 0.73, 0.44, 0.35, 0.29, 0.36, 0.41,
          0.25, 0.51, 0.24, 0.75, 1.18, 1.04, 0.22, 0.05, 0.09, 0.03, 0.04]
}

pd_sd = {
    'U': [27.12, 33.75, 15.84, 27.72, 27.38, 26.54, 26.78, 30.16, 24.85, 26.07,
          26.86, 29.27, 24.15, 27.63, 32.67, 32.66, 32.39, 34.18, 20.12, 23.50,
          14.26, 24.30, 23.67, 23.97, 23.65, 36.53, 36.13, 34.88, 36.21, 6.64,
          9.66, 4.89, 14.84, 13.35, 15.71, 13.82, 14.53, 8.96, 8.51, 19.77,
          34.22, 3.99, 40.58, 38.99, 27.12, 33.75, 15.84, 27.72, 27.38, 26.54,
          26.78, 30.16, 24.85, 26.07, 26.86, 29.27, 24.15, 27.63, 32.67, 32.66,
          32.39, 34.18, 20.12, 23.50, 14.26, 24.30, 23.67, 23.97, 23.65, 36.53,
          36.13, 34.88, 36.21, 6.64, 9.66, 4.89, 14.84, 13.35, 15.71, 13.82,
          14.53, 8.96, 8.51, 19.77, 34.22, 3.99, 40.58, 38.99],
    'B': [5.54, 3.77, 6.86, 8.51, 4.37, 4.29, 3.40, 8.23, 5.16, 3.97, 3.46,
          5.42, 4.98, 6.05, 5.32, 6.12, 4.91, 5.24, 5.34, 4.72, 5.88, 5.97,
          4.89, 7.04, 5.03, 6.10, 6.67, 5.52, 5.01, 4.78, 3.52, 4.89, 7.05,
          3.26, 8.50, 3.03, 7.02, 8.59, 8.51, 3.90, 3.45, 3.84, 2.99, 3.37,
          5.54, 3.77, 6.86, 8.51, 4.37, 4.29, 3.40, 8.23, 5.16, 3.97, 3.46,
          5.42, 4.98, 6.05, 5.32, 6.12, 4.91, 5.24, 5.34, 4.72, 5.88, 5.97,
          4.89, 7.04, 5.03, 6.10, 6.67, 5.52, 5.01, 4.78, 3.52, 4.89, 7.05,
          3.26, 8.50, 3.03, 7.02, 8.59, 8.51, 3.90, 3.45, 3.84, 2.99, 3.37],
    'A': [5.57, 3.85, 6.87, 8.53, 4.43, 4.32, 3.47, 8.19, 5.18, 4.09, 3.62,
          5.42, 5.05, 6.11, 5.34, 6.23, 4.78, 5.25, 5.42, 4.68, 6.03, 6.04,
          4.86, 7.17, 5.00, 6.09, 6.67, 5.47, 4.98, 4.80, 3.52, 4.91, 7.05,
          3.26, 8.50, 3.03, 7.04, 8.59, 8.51, 3.97, 3.52, 4.11, 2.80, 3.39,
          5.57, 3.85, 6.87, 8.53, 4.43, 4.32, 3.47, 8.19, 5.18, 4.09, 3.62,
          5.42, 5.05, 6.11, 5.34, 6.23, 4.78, 5.25, 5.42, 4.68, 6.03, 6.04,
          4.86, 7.17, 5.00, 6.09, 6.67, 5.47, 4.98, 4.80, 3.52, 4.91, 7.05,
          3.26, 8.50, 3.03, 7.04, 8.59, 8.51, 3.97, 3.52, 4.11, 2.80, 3.39],
    'W': [4.76, 0.86, 6.66, 7.73, 3.61, 3.61, 2.02, 7.96, 4.24, 2.16, 1.96,
          5.37, 3.62, 5.08, 4.71, 5.54, 4.24, 4.88, 4.62, 4.31, 4.87, 4.66,
          4.25, 5.19, 4.43, 6.08, 6.67, 5.47, 4.98, 4.11, 3.52, 4.17, 5.40,
          3.26, 6.23, 3.03, 6.66, 8.33, 8.51, 2.60, 0.85, 1.16, 0.67, 0.63,
          4.76, 0.86, 6.66, 7.73, 3.61, 3.61, 2.02, 7.96, 4.24, 2.16, 1.96,
          5.37, 3.62, 5.08, 4.71, 5.54, 4.24, 4.88, 4.62, 4.31, 4.87, 4.66,
          4.25, 5.19, 4.43, 6.08, 6.67, 5.47, 4.98, 4.11, 3.52, 4.17, 5.40,
          3.26, 6.23, 3.03, 6.66, 8.33, 8.51, 2.60, 0.85, 1.16, 0.67, 0.63]
}

pd_delta = {
    'U': [(x - per_diff['U'][0]) for x in per_diff['U']],
    'B': [(x - per_diff['B'][0]) for x in per_diff['B']],
    'A': [(x - per_diff['A'][0]) for x in per_diff['A']],
    'W': [(x - per_diff['W'][0]) for x in per_diff['W']]
}

pd_effect = {
    'U': [],
    'B': [],
    'A': [],
    'W': []
}

for m in model:
    for i in range(len(cat_pd)):
        z = (per_diff[m][i] - per_diff[m][0]) / pd_sd[m][i]
        es = abs(z)
        pd_effect[m].append(es)

# prepare latex data rows
a_rows = {}
for i, cat in enumerate(cat_a):
    a_rows[cat] = [
        cat,
        # count[i],
        a_freq['U'][i],
        a_delta['U'][i],
        a_effect['U'][i],
        a_freq['B'][i],
        a_delta['B'][i],
        a_effect['B'][i],
        a_freq['A'][i],
        a_delta['A'][i],
        a_effect['A'][i],
        a_freq['W'][i],
        a_delta['W'][i],
        a_effect['W'][i],
    ]

pd_rows = {}
for i, cat in enumerate(cat_pd):
    pd_rows[cat] = [
        cat,
        per_diff['U'][i],
        pd_delta['U'][i],
        pd_effect['U'][i],
        per_diff['B'][i],
        pd_delta['B'][i],
        pd_effect['B'][i],
        per_diff['A'][i],
        pd_delta['A'][i],
        pd_effect['A'][i],
        per_diff['W'][i],
        pd_delta['W'][i],
        pd_effect['W'][i],
    ]

# agreement output strings and values
agree_line = '\t\t{0} & '
for i in range(1, 13, 3):
    # agreement frequency
    agree_line += '{' + str(i) + ':.1f}\\% & '
    # delta
    agree_line += '{' + str(i + 1) + ':.1f} & '
    # effect size
    agree_line += '{' + str(i + 2) + ':.2f} & '

# finish output string
agree_line = agree_line[:-3]
agree_line += ' \\\\\n'

pd_line = '\t\t{0} & '
for i in range(1, 13, 3):
    # percent difference
    pd_line += '{' + str(i) + ':.2f} & '
    # delta
    pd_line += '{' + str(i + 1) + ':.2f} & '
    # effect size
    pd_line += '{' + str(i + 2) + ':.2f} & '

# finish output string
pd_line = pd_line[:-3]
pd_line += ' \\\\\n'

lines = []

lines.append('===== agreement =====\n')
for cat in order_a_cat:
    if cat == 'hline':
        lines.pop()
        lines.append('\t\t\\tabucline[1pt]{-}\n')
    else:
        lines.append(agree_line.format(*a_rows[cat]))
        lines.append('\t\t\\hline\n')

lines.append('\n\n===== per diff =====\n')
for cat in order_pd_cat:
    if cat == 'hline':
        lines.pop()
        lines.append('\t\t\\tabucline[1pt]{-}\n')
    else:
        lines.append(pd_line.format(*pd_rows[cat]))
        lines.append('\t\t\\hline\n')

with open('temp.txt',
          'w',
          encoding='utf-8') as f:
    for line in lines:
        f.write(line)