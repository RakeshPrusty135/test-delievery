#!/bin/bash

set -ue
export ROOT_FOLDER=$( pwd )
export M2_LOCAL_REPO="${ROOT_FOLDER}/.m2"

sudo -u builder mvn clean test -s /test-delivery/ci/settings.xml
build_profile="default"

# finding the version for the artifacts, This follows semver versioing 

VERSION=$(cat version)
IFS='.'
read -ra arr <<< "$(cat version)" 
MAJOR_VERSION=${arr[0]}
MINOR_VERSION=${arr[1]}
PATCH_VERSION=${arr[2]}
unset IFS
NEW_VERSION=${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}
echo $NEW_VERSION

# setting the version
mvn versions:set -DnewVersion=$NEW_VERSION -s /test-delivery/ci/settings.xml -P ${build_profile} 

echo "mvn clean install -DskipTests  -s /test-delivery/ci/settings.xml -P ${build_profile}"
mvn clean install -DskipTests  -s /test-delivery/ci/settings.xml -P ${build_profile}

# The built artifacts being collected from the created folder and being saved in a destination folder for upload to kfrog as artifacts & image
mkdir -p build-package-output/${artifactory_path}
cp -r `ls -d ${ROOT_FOLDER}/.m2/repository/$artifactory_path/**  | egrep -i $artifactory_directory` ../build-package-output/${artifactory_path}
