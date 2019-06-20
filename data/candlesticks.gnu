set style fill solid 0.5 border -1
set boxwidth 0.2
set border 2
set xtics nomirror rotate by -45
set ytics nomirror
set xrange [0:21]
set logscale y 2
set term 'png' size 1000, 600
set datafile separator "\t"
set termoption noenhanced

getdataname(x, y) = stringcolumn(x).", ".stringcolumn(y)

set output "comb_time_alpha_candlestick.png"
# whisker plot: x : box_min : whisker_min : whisker_high : box_high
plot "candlestick.csv" using 0:4:3:7:6:xticlabels(getdataname(1,2)) with candlesticks lt 3 lw 2 title 'Quartiles' whiskerbars, \
                    "" using 0:5:5:5:5 with candlesticks lt -1 lw 2 notitle