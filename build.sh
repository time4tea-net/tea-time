#!/bin/bash

BASE_VERSION=0.1
BUILD_VERSION="1-SNAPSHOT"
RELEASE=no
ADDITIONAL_TASKS=""

# codeshop magic beans
if [ ! -z "$CI_BUILD_NUMBER" ]
then
    BUILD_VERSION=$CI_BUILD_NUMBER
    RELEASE="yes"
fi

VERSION=${BASE_VERSION}.${BUILD_VERSION}

if [ $RELEASE = "yes" ]
then
    ADDITIONAL_TASKS="uploadArchives closeAndPromoteRepository"
fi

$(dirname $0)/gradlew -PVERSION=${VERSION} test ${ADDITIONAL_TASKS}



