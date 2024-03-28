#!/bin/bash

set -ue
export ROOT_FOLDER=$( pwd )
export M2_LOCAL_REPO="${ROOT_FOLDER}/.m2"

#unit test run
sudo -u builder mvn clean test -s /test-delivery/ci/settings.xml