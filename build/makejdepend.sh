#!/bin/sh
# makejdepend.sh: generate .java-.class dependencies
# $Id$

USAGE1="usage: $0 relative_paths_to_source_directories"
USAGE2="	e.g. $0 ../src >Makefile.depend"
if [ $# -lt 1 ]
then
  echo $USAGE1
  echo $USAGE2
  exit 1
fi

echo "# Java depedencies generated by"
echo "#   $0 $*"
echo "# on `date`"
echo "# DO NOT EDIT - AUTOGENERATED"
echo

ALLCLASSES=
HERE=`pwd`
for DIR in $*
do
  DIRPARENT=`dirname $DIR`
  DIRNAME=`basename $DIR`
  cd $DIRPARENT
  JAVAS=`find $DIRNAME -name '*.java' -print`
  cd $HERE

  for FILE in $JAVAS
  do
    STEMNAME=`echo $FILE |sed 's,.java$,,'`
    CLASS=../classes/${STEMNAME}.class
    ALLCLASSES="$ALLCLASSES $CLASS"
    echo $CLASS: ${DIRPARENT}/${FILE}
    echo '	@ $(COMPILE) '${DIRPARENT}/${FILE}
    echo
  done
done

echo
echo 'classes: ' $ALLCLASSES
echo '	'
echo '# end of makejdepend.sh output'
