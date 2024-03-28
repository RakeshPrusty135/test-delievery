#!/bin/bash
set -ue

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


# building the docker image with versioning
docker build -t <image-name>:v$NEW_VERSION -f bin/dockerfile .