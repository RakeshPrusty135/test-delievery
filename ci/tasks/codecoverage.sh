#!/bin/bash

set -ue
# collecting the jacoco report after the unit test run
mkdir jacoco-collect-report
cp -rf .\target\jacoco.exec jacoco-collect-report/jacoco.exec

mvn sonar:sonar 
        -Dsonar.projectKey=<project_key> 
        -Dsonar.projectName=<Project_Name> 
        -Dsonar.projectVersion=1.0 
        -Dsonar.sources=src 
        -Dsonar.tests=test 
        -Dsonar.java.binaries=target/classes 
        -Dsonar.java.test.binaries=target/test-classes 
        -Dsonar.coverage.jacoco.xmlReportPaths=jacoco-collect-report/jacoco.exec"