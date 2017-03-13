#!/bin/bash

RELEASE=no
ADDITIONAL_TASKS=""
WHAT="test"

# codeship magic beans
if [ ! -z "$CI_BUILD_NUMBER" ]
then
    VERSION="0.0.$CI_BUILD_NUMBER"
    WHAT="-PVERSION=$VERSION -PbintrayUser=$BINTRAY_USER -PbintrayApiKey=$BINTRAY_API_KEY test bintrayUpload"
fi

$(dirname $0)/gradlew $WHAT



