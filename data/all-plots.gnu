set loadpath "data/plot-data"
set style fill solid 0.35 noborder
set style boxplot nooutliers
set style data boxplot
set datafile separator "\t"
unset key
set border 2
set xtics ("Unbouned" 1, "Bounded" 2, "Aggregate" 3, "Weighted" 4)
set ylabel "Time in Microseconds"
set xtics nomirror
set ytics nomirror
set logscale y 2
set term png size 900, 600
set output "data/plots/comb_time_all_boxplot.png"
plot "comb_time_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_alpha_AB_boxplot.png"
plot "comb_time_alpha_AB_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_alpha_AC_boxplot.png"
plot "comb_time_alpha_AC_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_alpha_AD_boxplot.png"
plot "comb_time_alpha_AD_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_alpha_AE_boxplot.png"
plot "comb_time_alpha_AE_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_contains_all_boxplot.png"
plot "comb_time_contains_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_contains_even_boxplot.png"
plot "comb_time_contains_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_contains_simple_boxplot.png"
plot "comb_time_contains_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_contains_uneven_boxplot.png"
plot "comb_time_contains_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_equals_all_boxplot.png"
plot "comb_time_equals_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_equals_even_boxplot.png"
plot "comb_time_equals_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_equals_simple_boxplot.png"
plot "comb_time_equals_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_equals_uneven_boxplot.png"
plot "comb_time_equals_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_even_boxplot.png"
plot "comb_time_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_concat_all_boxplot.png"
plot "comb_time_excl_concat_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_concat_even_boxplot.png"
plot "comb_time_excl_concat_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_concat_simple_boxplot.png"
plot "comb_time_excl_concat_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_concat_uneven_boxplot.png"
plot "comb_time_excl_concat_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_delete_all_boxplot.png"
plot "comb_time_excl_delete_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_delete_diff_boxplot.png"
plot "comb_time_excl_delete_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_delete_same_boxplot.png"
plot "comb_time_excl_delete_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_replace_all_boxplot.png"
plot "comb_time_excl_replace_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_replace_diff_boxplot.png"
plot "comb_time_excl_replace_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_replace_same_boxplot.png"
plot "comb_time_excl_replace_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_excl_reverse_boxplot.png"
plot "comb_time_excl_reverse_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_false_boxplot.png"
plot "comb_time_false_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_concat_all_boxplot.png"
plot "comb_time_incl_concat_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_concat_even_boxplot.png"
plot "comb_time_incl_concat_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_concat_simple_boxplot.png"
plot "comb_time_incl_concat_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_concat_uneven_boxplot.png"
plot "comb_time_incl_concat_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_delete_all_boxplot.png"
plot "comb_time_incl_delete_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_delete_diff_boxplot.png"
plot "comb_time_incl_delete_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_delete_same_boxplot.png"
plot "comb_time_incl_delete_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_replace_all_boxplot.png"
plot "comb_time_incl_replace_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_replace_diff_boxplot.png"
plot "comb_time_incl_replace_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_replace_same_boxplot.png"
plot "comb_time_incl_replace_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_incl_reverse_boxplot.png"
plot "comb_time_incl_reverse_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_len_1_boxplot.png"
plot "comb_time_len_1_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_len_2_boxplot.png"
plot "comb_time_len_2_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_len_3_boxplot.png"
plot "comb_time_len_3_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_len_4_boxplot.png"
plot "comb_time_len_4_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_simple_boxplot.png"
plot "comb_time_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_true_boxplot.png"
plot "comb_time_true_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/comb_time_uneven_boxplot.png"
plot "comb_time_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col

set output "data/plots/mc_time_all_boxplot.png"
plot "mc_time_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_alpha_AB_boxplot.png"
plot "mc_time_alpha_AB_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_alpha_AC_boxplot.png"
plot "mc_time_alpha_AC_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_alpha_AD_boxplot.png"
plot "mc_time_alpha_AD_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_alpha_AE_boxplot.png"
plot "mc_time_alpha_AE_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_false_boxplot.png"
plot "mc_time_false_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_len_1_boxplot.png"
plot "mc_time_len_1_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_len_2_boxplot.png"
plot "mc_time_len_2_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_len_3_boxplot.png"
plot "mc_time_len_3_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_len_4_boxplot.png"
plot "mc_time_len_4_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/mc_time_true_boxplot.png"
plot "mc_time_true_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col

set output "data/plots/solve_time_all_boxplot.png"
plot "solve_time_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_alpha_AB_boxplot.png"
plot "solve_time_alpha_AB_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_alpha_AC_boxplot.png"
plot "solve_time_alpha_AC_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_alpha_AD_boxplot.png"
plot "solve_time_alpha_AD_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_alpha_AE_boxplot.png"
plot "solve_time_alpha_AE_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_contains_all_boxplot.png"
plot "solve_time_contains_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_contains_even_boxplot.png"
plot "solve_time_contains_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_contains_simple_boxplot.png"
plot "solve_time_contains_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_contains_uneven_boxplot.png"
plot "solve_time_contains_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_equals_all_boxplot.png"
plot "solve_time_equals_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_equals_even_boxplot.png"
plot "solve_time_equals_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_equals_simple_boxplot.png"
plot "solve_time_equals_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_equals_uneven_boxplot.png"
plot "solve_time_equals_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_even_boxplot.png"
plot "solve_time_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_concat_all_boxplot.png"
plot "solve_time_excl_concat_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_concat_even_boxplot.png"
plot "solve_time_excl_concat_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_concat_simple_boxplot.png"
plot "solve_time_excl_concat_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_concat_uneven_boxplot.png"
plot "solve_time_excl_concat_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_delete_all_boxplot.png"
plot "solve_time_excl_delete_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_delete_diff_boxplot.png"
plot "solve_time_excl_delete_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_delete_same_boxplot.png"
plot "solve_time_excl_delete_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_replace_all_boxplot.png"
plot "solve_time_excl_replace_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_replace_diff_boxplot.png"
plot "solve_time_excl_replace_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_replace_same_boxplot.png"
plot "solve_time_excl_replace_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_excl_reverse_boxplot.png"
plot "solve_time_excl_reverse_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_false_boxplot.png"
plot "solve_time_false_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_concat_all_boxplot.png"
plot "solve_time_incl_concat_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_concat_even_boxplot.png"
plot "solve_time_incl_concat_even_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_concat_simple_boxplot.png"
plot "solve_time_incl_concat_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_concat_uneven_boxplot.png"
plot "solve_time_incl_concat_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_delete_all_boxplot.png"
plot "solve_time_incl_delete_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_delete_diff_boxplot.png"
plot "solve_time_incl_delete_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_delete_same_boxplot.png"
plot "solve_time_incl_delete_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_replace_all_boxplot.png"
plot "solve_time_incl_replace_all_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_replace_diff_boxplot.png"
plot "solve_time_incl_replace_diff_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_replace_same_boxplot.png"
plot "solve_time_incl_replace_same_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_incl_reverse_boxplot.png"
plot "solve_time_incl_reverse_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_len_1_boxplot.png"
plot "solve_time_len_1_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_len_2_boxplot.png"
plot "solve_time_len_2_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_len_3_boxplot.png"
plot "solve_time_len_3_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_len_4_boxplot.png"
plot "solve_time_len_4_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_simple_boxplot.png"
plot "solve_time_simple_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_true_boxplot.png"
plot "solve_time_true_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col
set output "data/plots/solve_time_uneven_boxplot.png"
plot "solve_time_uneven_boxplot.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col

set style data histogram
set style histogram clustered
set key on
unset xtics
set xtics nomirror rotate by -45
set logscale y 2
set output "data/plots/comb_time_all_histogram.png"
plot "comb_time_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_alpha_AB_histogram.png"
plot "comb_time_alpha_AB_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_alpha_AC_histogram.png"
plot "comb_time_alpha_AC_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_alpha_AD_histogram.png"
plot "comb_time_alpha_AD_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_alpha_AE_histogram.png"
plot "comb_time_alpha_AE_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_contains_all_histogram.png"
plot "comb_time_contains_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_contains_even_histogram.png"
plot "comb_time_contains_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_contains_simple_histogram.png"
plot "comb_time_contains_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_contains_uneven_histogram.png"
plot "comb_time_contains_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_equals_all_histogram.png"
plot "comb_time_equals_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_equals_even_histogram.png"
plot "comb_time_equals_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_equals_simple_histogram.png"
plot "comb_time_equals_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_equals_uneven_histogram.png"
plot "comb_time_equals_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_even_histogram.png"
plot "comb_time_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_concat_all_histogram.png"
plot "comb_time_excl_concat_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_concat_even_histogram.png"
plot "comb_time_excl_concat_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_concat_simple_histogram.png"
plot "comb_time_excl_concat_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_concat_uneven_histogram.png"
plot "comb_time_excl_concat_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_delete_all_histogram.png"
plot "comb_time_excl_delete_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_delete_diff_histogram.png"
plot "comb_time_excl_delete_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_delete_same_histogram.png"
plot "comb_time_excl_delete_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_replace_all_histogram.png"
plot "comb_time_excl_replace_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_replace_diff_histogram.png"
plot "comb_time_excl_replace_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_replace_same_histogram.png"
plot "comb_time_excl_replace_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_excl_reverse_histogram.png"
plot "comb_time_excl_reverse_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_false_histogram.png"
plot "comb_time_false_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_concat_all_histogram.png"
plot "comb_time_incl_concat_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_concat_even_histogram.png"
plot "comb_time_incl_concat_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_concat_simple_histogram.png"
plot "comb_time_incl_concat_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_concat_uneven_histogram.png"
plot "comb_time_incl_concat_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_delete_all_histogram.png"
plot "comb_time_incl_delete_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_delete_diff_histogram.png"
plot "comb_time_incl_delete_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_delete_same_histogram.png"
plot "comb_time_incl_delete_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_replace_all_histogram.png"
plot "comb_time_incl_replace_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_replace_diff_histogram.png"
plot "comb_time_incl_replace_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_replace_same_histogram.png"
plot "comb_time_incl_replace_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_incl_reverse_histogram.png"
plot "comb_time_incl_reverse_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_len_1_histogram.png"
plot "comb_time_len_1_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_len_2_histogram.png"
plot "comb_time_len_2_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_len_3_histogram.png"
plot "comb_time_len_3_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_len_4_histogram.png"
plot "comb_time_len_4_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_simple_histogram.png"
plot "comb_time_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_true_histogram.png"
plot "comb_time_true_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/comb_time_uneven_histogram.png"
plot "comb_time_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col

set output "data/plots/mc_time_all_histogram.png"
plot "mc_time_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_alpha_AB_histogram.png"
plot "mc_time_alpha_AB_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_alpha_AC_histogram.png"
plot "mc_time_alpha_AC_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_alpha_AD_histogram.png"
plot "mc_time_alpha_AD_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_alpha_AE_histogram.png"
plot "mc_time_alpha_AE_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_false_histogram.png"
plot "mc_time_false_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_len_1_histogram.png"
plot "mc_time_len_1_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_len_2_histogram.png"
plot "mc_time_len_2_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_len_3_histogram.png"
plot "mc_time_len_3_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_len_4_histogram.png"
plot "mc_time_len_4_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/mc_time_true_histogram.png"
plot "mc_time_true_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col

set output "data/plots/solve_time_all_histogram.png"
plot "solve_time_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_alpha_AB_histogram.png"
plot "solve_time_alpha_AB_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_alpha_AC_histogram.png"
plot "solve_time_alpha_AC_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_alpha_AD_histogram.png"
plot "solve_time_alpha_AD_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_alpha_AE_histogram.png"
plot "solve_time_alpha_AE_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_contains_all_histogram.png"
plot "solve_time_contains_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_contains_even_histogram.png"
plot "solve_time_contains_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_contains_simple_histogram.png"
plot "solve_time_contains_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_contains_uneven_histogram.png"
plot "solve_time_contains_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_equals_all_histogram.png"
plot "solve_time_equals_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_equals_even_histogram.png"
plot "solve_time_equals_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_equals_simple_histogram.png"
plot "solve_time_equals_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_equals_uneven_histogram.png"
plot "solve_time_equals_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_even_histogram.png"
plot "solve_time_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_concat_all_histogram.png"
plot "solve_time_excl_concat_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_concat_even_histogram.png"
plot "solve_time_excl_concat_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_concat_simple_histogram.png"
plot "solve_time_excl_concat_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_concat_uneven_histogram.png"
plot "solve_time_excl_concat_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_delete_all_histogram.png"
plot "solve_time_excl_delete_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_delete_diff_histogram.png"
plot "solve_time_excl_delete_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_delete_same_histogram.png"
plot "solve_time_excl_delete_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_replace_all_histogram.png"
plot "solve_time_excl_replace_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_replace_diff_histogram.png"
plot "solve_time_excl_replace_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_replace_same_histogram.png"
plot "solve_time_excl_replace_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_excl_reverse_histogram.png"
plot "solve_time_excl_reverse_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_false_histogram.png"
plot "solve_time_false_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_concat_all_histogram.png"
plot "solve_time_incl_concat_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_concat_even_histogram.png"
plot "solve_time_incl_concat_even_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_concat_simple_histogram.png"
plot "solve_time_incl_concat_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_concat_uneven_histogram.png"
plot "solve_time_incl_concat_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_delete_all_histogram.png"
plot "solve_time_incl_delete_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_delete_diff_histogram.png"
plot "solve_time_incl_delete_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_delete_same_histogram.png"
plot "solve_time_incl_delete_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_replace_all_histogram.png"
plot "solve_time_incl_replace_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_replace_diff_histogram.png"
plot "solve_time_incl_replace_diff_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_replace_same_histogram.png"
plot "solve_time_incl_replace_same_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_incl_reverse_histogram.png"
plot "solve_time_incl_reverse_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_len_1_histogram.png"
plot "solve_time_len_1_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_len_2_histogram.png"
plot "solve_time_len_2_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_len_3_histogram.png"
plot "solve_time_len_3_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_len_4_histogram.png"
plot "solve_time_len_4_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_simple_histogram.png"
plot "solve_time_simple_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_true_histogram.png"
plot "solve_time_true_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col
set output "data/plots/solve_time_uneven_histogram.png"
plot "solve_time_uneven_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col

set style fill transparent solid 0.5 noborder
set style data point
set key outside
set xtic nomirror
unset logscale y
set logscale x 2
set xrange [4000:500000]
set yrange [0:100]
set ylabel "Model Count Percentage Difference"
set xlabel "Time in Microseconds"
set term png size 1200,800
set output "data/plots/per_diff_vs_comb_time_all_scatter.png"
plot "per_diff_vs_comb_time_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_alpha_AB_scatter.png"
# plot "per_diff_vs_comb_time_alpha_AB_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_alpha_AC_scatter.png"
# plot "per_diff_vs_comb_time_alpha_AC_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_alpha_AD_scatter.png"
# plot "per_diff_vs_comb_time_alpha_AD_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_alpha_AE_scatter.png"
# plot "per_diff_vs_comb_time_alpha_AE_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_contains_all_scatter.png"
# plot "per_diff_vs_comb_time_contains_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_contains_even_scatter.png"
# plot "per_diff_vs_comb_time_contains_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_contains_simple_scatter.png"
# plot "per_diff_vs_comb_time_contains_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_contains_uneven_scatter.png"
# plot "per_diff_vs_comb_time_contains_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_equals_all_scatter.png"
# plot "per_diff_vs_comb_time_equals_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_equals_even_scatter.png"
# plot "per_diff_vs_comb_time_equals_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_equals_simple_scatter.png"
# plot "per_diff_vs_comb_time_equals_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_equals_uneven_scatter.png"
# plot "per_diff_vs_comb_time_equals_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_even_scatter.png"
# plot "per_diff_vs_comb_time_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_excl_concat_all_scatter.png"
# plot "per_diff_vs_comb_time_excl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_excl_concat_even_scatter.png"
# plot "per_diff_vs_comb_time_excl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_excl_concat_simple_scatter.png"
# plot "per_diff_vs_comb_time_excl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_excl_concat_uneven_scatter.png"
# plot "per_diff_vs_comb_time_excl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_excl_reverse_scatter.png"
# plot "per_diff_vs_comb_time_excl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_false_scatter.png"
# plot "per_diff_vs_comb_time_false_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_concat_all_scatter.png"
# plot "per_diff_vs_comb_time_incl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_concat_even_scatter.png"
# plot "per_diff_vs_comb_time_incl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_concat_simple_scatter.png"
# plot "per_diff_vs_comb_time_incl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_concat_uneven_scatter.png"
# plot "per_diff_vs_comb_time_incl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_delete_all_scatter.png"
# plot "per_diff_vs_comb_time_incl_delete_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_delete_diff_scatter.png"
# plot "per_diff_vs_comb_time_incl_delete_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_delete_same_scatter.png"
# plot "per_diff_vs_comb_time_incl_delete_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_replace_all_scatter.png"
# plot "per_diff_vs_comb_time_incl_replace_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_replace_diff_scatter.png"
# plot "per_diff_vs_comb_time_incl_replace_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_replace_same_scatter.png"
# plot "per_diff_vs_comb_time_incl_replace_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_incl_reverse_scatter.png"
# plot "per_diff_vs_comb_time_incl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_len_1_scatter.png"
# plot "per_diff_vs_comb_time_len_1_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_len_2_scatter.png"
# plot "per_diff_vs_comb_time_len_2_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_len_3_scatter.png"
# plot "per_diff_vs_comb_time_len_3_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_len_4_scatter.png"
# plot "per_diff_vs_comb_time_len_4_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_simple_scatter.png"
# plot "per_diff_vs_comb_time_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_true_scatter.png"
# plot "per_diff_vs_comb_time_true_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_comb_time_uneven_scatter.png"
# plot "per_diff_vs_comb_time_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"

set output "data/plots/per_diff_vs_solve_time_all_scatter.png"
plot "per_diff_vs_solve_time_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_alpha_AB_scatter.png"
# plot "per_diff_vs_solve_time_alpha_AB_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_alpha_AC_scatter.png"
# plot "per_diff_vs_solve_time_alpha_AC_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_alpha_AD_scatter.png"
# plot "per_diff_vs_solve_time_alpha_AD_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_alpha_AE_scatter.png"
# plot "per_diff_vs_solve_time_alpha_AE_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_contains_all_scatter.png"
# plot "per_diff_vs_solve_time_contains_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_contains_even_scatter.png"
# plot "per_diff_vs_solve_time_contains_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_contains_simple_scatter.png"
# plot "per_diff_vs_solve_time_contains_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_contains_uneven_scatter.png"
# plot "per_diff_vs_solve_time_contains_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_equals_all_scatter.png"
# plot "per_diff_vs_solve_time_equals_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_equals_even_scatter.png"
# plot "per_diff_vs_solve_time_equals_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_equals_simple_scatter.png"
# plot "per_diff_vs_solve_time_equals_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_equals_uneven_scatter.png"
# plot "per_diff_vs_solve_time_equals_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_even_scatter.png"
# plot "per_diff_vs_solve_time_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_concat_all_scatter.png"
# plot "per_diff_vs_solve_time_excl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_concat_even_scatter.png"
# plot "per_diff_vs_solve_time_excl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_concat_simple_scatter.png"
# plot "per_diff_vs_solve_time_excl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_concat_uneven_scatter.png"
# plot "per_diff_vs_solve_time_excl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_delete_all_scatter.png"
# plot "per_diff_vs_solve_time_excl_delete_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_delete_diff_scatter.png"
# plot "per_diff_vs_solve_time_excl_delete_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_delete_same_scatter.png"
# plot "per_diff_vs_solve_time_excl_delete_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_replace_all_scatter.png"
# plot "per_diff_vs_solve_time_excl_replace_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_replace_diff_scatter.png"
# plot "per_diff_vs_solve_time_excl_replace_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_replace_same_scatter.png"
# plot "per_diff_vs_solve_time_excl_replace_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_excl_reverse_scatter.png"
# plot "per_diff_vs_solve_time_excl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_false_scatter.png"
# plot "per_diff_vs_solve_time_false_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_concat_all_scatter.png"
# plot "per_diff_vs_solve_time_incl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_concat_even_scatter.png"
# plot "per_diff_vs_solve_time_incl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_concat_simple_scatter.png"
# plot "per_diff_vs_solve_time_incl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_concat_uneven_scatter.png"
# plot "per_diff_vs_solve_time_incl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_delete_all_scatter.png"
# plot "per_diff_vs_solve_time_incl_delete_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_delete_diff_scatter.png"
# plot "per_diff_vs_solve_time_incl_delete_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_delete_same_scatter.png"
# plot "per_diff_vs_solve_time_incl_delete_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_replace_all_scatter.png"
# plot "per_diff_vs_solve_time_incl_replace_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_replace_diff_scatter.png"
# plot "per_diff_vs_solve_time_incl_replace_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_replace_same_scatter.png"
# plot "per_diff_vs_solve_time_incl_replace_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_incl_reverse_scatter.png"
# plot "per_diff_vs_solve_time_incl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_len_1_scatter.png"
# plot "per_diff_vs_solve_time_len_1_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_len_2_scatter.png"
# plot "per_diff_vs_solve_time_len_2_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_len_3_scatter.png"
# plot "per_diff_vs_solve_time_len_3_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_len_4_scatter.png"
# plot "per_diff_vs_solve_time_len_4_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_simple_scatter.png"
# plot "per_diff_vs_solve_time_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_true_scatter.png"
# plot "per_diff_vs_solve_time_true_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_solve_time_uneven_scatter.png"
# plot "per_diff_vs_solve_time_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"

unset logscale x
set xrange [0:200]
set output "data/plots/per_diff_vs_mc_time_all_scatter.png"
plot "per_diff_vs_mc_time_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_alpha_AB_scatter.png"
# plot "per_diff_vs_mc_time_alpha_AB_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_alpha_AC_scatter.png"
# plot "per_diff_vs_mc_time_alpha_AC_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_alpha_AD_scatter.png"
# plot "per_diff_vs_mc_time_alpha_AD_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_alpha_AE_scatter.png"
# plot "per_diff_vs_mc_time_alpha_AE_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_contains_all_scatter.png"
# plot "per_diff_vs_mc_time_contains_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_contains_even_scatter.png"
# plot "per_diff_vs_mc_time_contains_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_contains_simple_scatter.png"
# plot "per_diff_vs_mc_time_contains_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_contains_uneven_scatter.png"
# plot "per_diff_vs_mc_time_contains_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_equals_all_scatter.png"
# plot "per_diff_vs_mc_time_equals_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_equals_even_scatter.png"
# plot "per_diff_vs_mc_time_equals_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_equals_simple_scatter.png"
# plot "per_diff_vs_mc_time_equals_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_equals_uneven_scatter.png"
# plot "per_diff_vs_mc_time_equals_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_even_scatter.png"
# plot "per_diff_vs_mc_time_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_concat_all_scatter.png"
# plot "per_diff_vs_mc_time_excl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_concat_even_scatter.png"
# plot "per_diff_vs_mc_time_excl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_concat_simple_scatter.png"
# plot "per_diff_vs_mc_time_excl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_concat_uneven_scatter.png"
# plot "per_diff_vs_mc_time_excl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_delete_all_scatter.png"
# plot "per_diff_vs_mc_time_excl_delete_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_delete_diff_scatter.png"
# plot "per_diff_vs_mc_time_excl_delete_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_delete_same_scatter.png"
# plot "per_diff_vs_mc_time_excl_delete_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_replace_all_scatter.png"
# plot "per_diff_vs_mc_time_excl_replace_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_replace_diff_scatter.png"
# plot "per_diff_vs_mc_time_excl_replace_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_replace_same_scatter.png"
# plot "per_diff_vs_mc_time_excl_replace_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_excl_reverse_scatter.png"
# plot "per_diff_vs_mc_time_excl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_false_scatter.png"
# plot "per_diff_vs_mc_time_false_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_concat_all_scatter.png"
# plot "per_diff_vs_mc_time_incl_concat_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_concat_even_scatter.png"
# plot "per_diff_vs_mc_time_incl_concat_even_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_concat_simple_scatter.png"
# plot "per_diff_vs_mc_time_incl_concat_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_concat_uneven_scatter.png"
# plot "per_diff_vs_mc_time_incl_concat_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_delete_all_scatter.png"
# plot "per_diff_vs_mc_time_incl_delete_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_delete_diff_scatter.png"
# plot "per_diff_vs_mc_time_incl_delete_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_delete_same_scatter.png"
# plot "per_diff_vs_mc_time_incl_delete_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_replace_all_scatter.png"
# plot "per_diff_vs_mc_time_incl_replace_all_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_replace_diff_scatter.png"
# plot "per_diff_vs_mc_time_incl_replace_diff_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_replace_same_scatter.png"
# plot "per_diff_vs_mc_time_incl_replace_same_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_incl_reverse_scatter.png"
# plot "per_diff_vs_mc_time_incl_reverse_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_len_1_scatter.png"
# plot "per_diff_vs_mc_time_len_1_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_len_2_scatter.png"
# plot "per_diff_vs_mc_time_len_2_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_len_3_scatter.png"
# plot "per_diff_vs_mc_time_len_3_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_len_4_scatter.png"
# plot "per_diff_vs_mc_time_len_4_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_simple_scatter.png"
# plot "per_diff_vs_mc_time_simple_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_true_scatter.png"
# plot "per_diff_vs_mc_time_true_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
# set output "data/plots/per_diff_vs_mc_time_uneven_scatter.png"
# plot "per_diff_vs_mc_time_uneven_scatter.csv" using "A Times":"A Per Diffs" with points pt 7 ti "Aggregate", "" using "W Times":"W Per Diffs" with points pt 7 ti "Weighted", "" using "U Times":"U Per Diffs" with points pt 7 ti "Unbounded", "" using "B Times":"B Per Diffs" with points pt 7 ti "Bounded"
