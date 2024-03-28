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

PATCH_VERSION=$(expr $PATCH_VERSION= + 1)

NEW_VERSION=${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}

# store into git version file. This is a complete github action where the new version will be "git push"  

