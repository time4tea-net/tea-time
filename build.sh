#!/bin/bash

$(dirname $0)/gradlew test uploadArchives closeAndPromoteRepository


