#!/bin/sh
#
# Convert the project package name, in order to publish on Android Market
#

ROOT=$(dirname $0)
SUFFIX=$1

if [ "$SUFFIX" != "free" -a "$SUFFIX" != "donate" ]; then
	echo Error - Invalid package suffix: $SUFFIX
	exit 1
fi

if [ "$2" = "-u" ]; then
	# UNDO THE CONVERSION
	if [ ! -d $ROOT/src/com/googlecode/droidwall/$SUFFIX ]; then
		echo Error - Not converted!
		exit 1
	fi
	echo UNDO - Moving java files
	mv $ROOT/src/com/googlecode/droidwall/$SUFFIX/*.java $ROOT/src/com/googlecode/droidwall/
	echo UNDO - Fixing package name on java files
	sed -i "s/package com.googlecode.droidwall.$SUFFIX;/package com.googlecode.droidwall;/" $ROOT/src/com/googlecode/droidwall/*.java || exit
	sed -i "s/import com.googlecode.droidwall.$SUFFIX/import com.googlecode.droidwall/" $ROOT/src/com/googlecode/droidwall/*.java || exit
	echo UNDO - Fixing package name on AndroidManifest.xml
	sed -i "s/com.googlecode.droidwall.$SUFFIX/com.googlecode.droidwall/" $ROOT/AndroidManifest.xml || exit
	rmdir $ROOT/src/com/googlecode/droidwall/$SUFFIX
	echo UNDO - Done!
	exit 0
fi

# Convert
if [ -d $ROOT/src/com/googlecode/droidwall/$SUFFIX ]; then
	echo Error - Already converted!
	exit 1
fi
mkdir $ROOT/src/com/googlecode/droidwall/$SUFFIX || exit
echo Moving java files
mv $ROOT/src/com/googlecode/droidwall/*.java $ROOT/src/com/googlecode/droidwall/$SUFFIX/
echo Fixing package name on java files
sed -i "s/package com.googlecode.droidwall;/package com.googlecode.droidwall.$SUFFIX;/" $ROOT/src/com/googlecode/droidwall/$SUFFIX/*.java || exit
sed -i "s/import com.googlecode.droidwall/import com.googlecode.droidwall.$SUFFIX/" $ROOT/src/com/googlecode/droidwall/$SUFFIX/*.java || exit
echo Fixing package name on AndroidManifest.xml
sed -i "s/package=\"com.googlecode.droidwall\"/package=\"com.googlecode.droidwall.$SUFFIX\"/" $ROOT/AndroidManifest.xml || exit
echo Done!
