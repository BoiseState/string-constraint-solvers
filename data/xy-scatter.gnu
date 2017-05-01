set style fill transparent solid 0.35 noborder
set style data dot

set border 2
set xtics nomirror rotate by -45
set ytics nomirror
set yrange [0:1]

#set logscale x 2

set term 'png' size 1000, 600

set output "per_diff_vs_comb_time_all_scatter.png"
plot "per_diff_vs_comb_time_all_scatter.csv" using "Unbounded-Time_Values":"Unbounded Percent Differences" ti "Unbounded", "" using "Bounded_Time_Values":"Bounded Percent Differences" ti "Bounded", "" using "Aggregate_Time_Values":"Aggregate Percent Differences" ti "Aggregate", "" using "Weighted_Time_Values":"Weighted Percent Differences" ti "Weighted"