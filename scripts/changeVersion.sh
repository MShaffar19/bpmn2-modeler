#!/bin/bash

# this script can be used to change the version recursively throughout the project
# Initial version from lhein, 2014

# check parameter count and provide help if count doesn't match
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    echo "Please specify the old and the new version like this:"
    echo "	changeVersion.sh <oldVersionString> <newVersionString>"
    echo "Example:"
    echo "	changeVersion.sh 7.2.0-SNAPSHOT 7.2.0"
    exit 0
fi

CMD_1=$1
CMD_2=$2

# cut away the snapshot from the source version
if [[ ${CMD_1} == *-SNAPSHOT ]]
then
  SOURCE_VERSION=`echo $CMD_1| cut -d'-' -f 1`
  SOURCE_VERSION_PATTERN_MVN="${SOURCE_VERSION}-SNAPSHOT"
  SOURCE_VERSION_PATTERN_OSGI="${SOURCE_VERSION}.qualifier"
else
  SOURCE_VERSION=${CMD_1}
  SOURCE_VERSION_PATTERN_MVN="${SOURCE_VERSION}"
  SOURCE_VERSION_PATTERN_OSGI="${SOURCE_VERSION}.qualifier"
fi

# cut away the snapshot from the target version
if [[ ${CMD_2} == *-SNAPSHOT ]]
then
  TARGET_VERSION=`echo $CMD_2| cut -d'-' -f 1`
  MVN_TARGET_VERSION=$CMD_2
  OSGI_TARGET_VERSION="$TARGET_VERSION.qualifier"
else
  TARGET_VERSION="${CMD_2}"
  MVN_TARGET_VERSION=$CMD_2
  OSGI_TARGET_VERSION=$TARGET_VERSION
fi

echo "Using the following settings:"
echo "	Source Version:		$CMD_1"
echo "	Target Version Maven:	$MVN_TARGET_VERSION"
echo "	Target Version OSGi:	$OSGI_TARGET_VERSION"
echo "	Maven Replace Pattern:	$SOURCE_VERSION_PATTERN_MVN"
echo "	OSGi Replace Pattern:	$SOURCE_VERSION_PATTERN_OSGI"
echo "Replacing now..."

###################
## REPLACE LOGIC ##
###################

# replace regular maven versions
find * -name 'pom.xml' | xargs perl -pi -e "s/<version>$SOURCE_VERSION_PATTERN_MVN<\/version>/<version>$MVN_TARGET_VERSION<\/version>/g"

# replace OSGi versions
find * -name 'pom.xml' | xargs perl -pi -e "s/<version>$SOURCE_VERSION_PATTERN_OSGI<\/version>/<version>$OSGI_TARGET_VERSION<\/version>/g"

# replace manifest versions
find * -name 'MANIFEST.MF' | xargs perl -pi -e "s/$SOURCE_VERSION_PATTERN_OSGI/$OSGI_TARGET_VERSION/g"

# feature.xml files
find * -name 'feature.xml' | xargs perl -pi -e "s/$SOURCE_VERSION_PATTERN_OSGI/$OSGI_TARGET_VERSION/g"

echo "DONE!"

