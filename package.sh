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
	if [ ! -d $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX ]; then
		echo Error - Not converted!
		exit 1
	fi
	echo UNDO - Moving java files
	mv $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX/*.java $ROOT/src/com/jtschohl/androidfirewall/
	echo UNDO - Fixing package name on java files
	sed -i "s/package com.jtschohl.androidfirewall.$SUFFIX;/package com.jtschohl.androidfirewall;/" $ROOT/src/com/jtschohl/androidfirewall/*.java || exit
	sed -i "s/import com.jtschohl.androidfirewall.$SUFFIX/import com.jtschohl.androidfirewall/" $ROOT/src/com/jtschohl/androidfirewall/*.java || exit
	echo UNDO - Fixing package name on AndroidManifest.xml
	sed -i "s/com.jtschohl.androidfirewall.$SUFFIX/com.jtschohl.androidfirewall/" $ROOT/AndroidManifest.xml || exit
	rmdir $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX
	echo UNDO - Done!
	exit 0
fi

# Convert
if [ -d $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX ]; then
	echo Error - Already converted!
	exit 1
fi
mkdir $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX || exit
echo Moving java files
mv $ROOT/src/com/jtschohl/androidfirewall/*.java $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX/
echo Fixing package name on java files
sed -i "s/package com.jtschohl.androidfirewall;/package com.jtschohl.androidfirewall.$SUFFIX;/" $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX/*.java || exit
sed -i "s/import com.jtschohl.androidfirewall/import com.jtschohl.androidfirewall.$SUFFIX/" $ROOT/src/com/jtschohl/androidfirewall/$SUFFIX/*.java || exit
echo Fixing package name on AndroidManifest.xml
sed -i "s/package=\"com.jtschohl.androidfirewall\"/package=\"com.jtschohl.androidfirewall.$SUFFIX\"/" $ROOT/AndroidManifest.xml || exit
echo Done!
