set style fill solid 0.25 border -1
set style data histogram
set style histogram clustered
set datafile separator "\t"

set border 2
set xtics nomirror rotate by -45
set ytics nomirror

set logscale y 2

set term 'png' size 1000,500

set output "comb_time_all_histogram.png"
plot "comb_time_all_histogram.csv" using 2:xtic(1) ti col, "" using 3 ti col, "" using 4 ti col, "" using 5 ti col