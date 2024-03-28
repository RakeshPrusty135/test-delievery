Build a Jenkins pipeline with the following information:
If the branch is from feature branch then during PR merge to main branch, below git PR checks will be conducted 
source code repository https://github.com/spring-projects/spring-petclinic
a. Jfrog third party license check 
b. Jfrog third party vaulnerability scan
c. Make sure all dependencies are resolved from JCenter
d. No secret keys passed 
e. sonarqube code coverage 

If above tests are passed then next steps will be executed

feature branch PR will be merged to main branch 
1. checkout the code from branch main https://github.com/spring-projects/spring-petclinic 
2. Compile the code using maven
3. Run the tests using maven 
4. Package the project as a runnable Docker image
5. Image will be uploaded to JFrog Artifactory


The directory structure contains
1. ci/pipeline/build_pipeline : jenkins pipeline groovy code 
2. ci/tasks/* : contains all executable file 
   a. codecoverage.sh
   b. jfrog-push-artifact.sh
   c. jfrog-vulnerability-scan.sh
   d. secret-scan.sh
   e. version-update.sh
   f. dockerbuild.sh
   g. jfrog-license-check.sh
   h. jfrog-push-image.sh
   i. mavenbuild.sh
   j. unittest.sh
3. ci/settings: contains the maven settings file with Jcenter as dependency resolver site
4. bin/dockerfile: for the docker image build 

Triggers: The pipeline triggers only for pull requests from feature branches.
Pull Request Checks (Feature Branch):
This stage runs only for feature branches.
1. It retrieves GIT, JFrog Artifactory details from environment variables 
2. Security Jfrog scans includes license check and vulnerability scan.
3. It searches for secret keys in properties files 
4. Runs the unit test for jacoco report & upload to SonarQube Scanner plugin for code coverage analysis 

Merge feature branch to Main and Build (if checks pass):
This stage executes only if the previous stage succeeds (checks pass) and the target branch is "main".
It merges the feature branch to main and then performs the build and deployment steps as outlined previously 

1. git checkout
2. run test
3. run build includes version settings
4. create docker image
5. push artifacts like .jar to jfrog 
6. push docker image to jfrog
7. semver version update
8. email success/failure notification

Post: The pipeline cleans the workspace after each run.

Note: The pipeline assumes all the credentials has been set either in git secret store or any secret storage like vault/credhub

