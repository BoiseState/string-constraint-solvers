set loadpath "plot-data"
set style fill solid 0.25 border -1
set style boxplot nooutliers
set style data boxplot
set linetype 1 lc rgb "dark-violet"
set linetype 2 lc rgb "dark-violet"
set linetype 3 lc rgb "#009e73"
set linetype 4 lc rgb "#009e73"
set linetype 5 lc rgb "#56b4e9"
set linetype 6 lc rgb "#56b4e9"
set linetype 7 lc rgb "#e69f00"
set linetype 8 lc rgb "#e69f00"

unset key
set border 2
set xtics ("U MC" 1, "U S" 2, "B MC" 3, "B S" 4, "A MC" 5, "A S" 6, "W MC" 7, "W S" 8)
set xtics nomirror
set ytics nomirror

set logscale y 2

set term 'png'

set output "mc_vs_solve_time_all_boxplot.png"
plot "mc_vs_solve_time_all_box.csv" using (1):(column(1)) ti col, \
									 "" using (2):(column(5)) ti col, \
									 "" using (3):(column(2)) ti col, \
									 "" using (4):(column(6)) ti col, \
									 "" using (5):(column(3)) ti col, \
									 "" using (6):(column(7)) ti col, \
									 "" using (7):(column(4)) ti col, \
									 "" using (8):(column(8)) ti col