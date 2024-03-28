#!/bin/bash
set -ue

jfrog_username=$1
jfrog_password=$2
jfrog_server=$3

docker login -u ${jfrog_username} -p ${jfrog_password} ${jfrog_server} 

docker push ${jfrog_server}/<image-name>:v$NEW_VERSION'

jfrog rt u *.jar libs-release/
