#!/bin/sh
list="";for i in select.gif search.gif hand.gif textselect.gif actualsi.gif fitwidth.gif fitpage.gif zoomin.gif zoomout.gif firstpage.gif prevpage.gif nextpage.gif lastpage.gif searchbackdoc.gif searchback.gif searchfwd.gif searchfwddoc.gif zoomselect.gif lizardtech.gif; do j=`basename $i .gif`-90.gif;convert -rotate +90 "$i" "$j";list=`echo $list $j`; done
convert -background magenta -append `echo $list` y.pnm
convert y.pnm -rotate 270 x.pnm
cpaldjvu x.pnm ../com/lizardtech/djvubean/toolbar/toolbar.djvu
rm -f `echo $list` y.pnm x.pnm

