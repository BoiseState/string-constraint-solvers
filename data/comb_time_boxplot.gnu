set loadpath "plot-data"
set style fill solid 0.25 border -1
set style boxplot nooutliers
set style data boxplot

unset key
set border 2
set xtics ("Unbouned" 1, "Bounded" 2, "Aggregate" 3, "Weighted" 4)
set xtics nomirror
set ytics nomirror

set logscale y 2

set term 'png'

set output "comb_time_all_boxplot.png"
plot "comb_time_all_box.csv" using (1):(column(1)) ti col, "" using (2):(column(2)) ti col, "" using (3):(column(3)) ti col, "" using (4):(column(4)) ti col