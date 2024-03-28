pipeline {
    agent any   // The Linux (can be windows also) node should be loaded with Jfrog CLI, Maven, Sonar Client CLI, Docker binaries. This also can be acheived by containers

    environment {
        // JFrog Artifactory details (replace with your values)
        ARTIFACTORY_SERVER = 'your-artifactory-server-id'
        ARTIFACTORY_USER = 'your-artifactory-username'  // stored in GITHUb secret store or Use credentials plugin for secure storage (eg vault or credhub)
        ARTIFACTORY_PASSWORD = 'your-artifactory-password'  // stored in GITHUb secret store or Use credentials plugin for secure storage (eg vault or credhub)

    triggers {
        githubPullRequest {
            branchFilter = 'feature.*' // Only for feature branches
            serverName = 'github' // Assuming GitHub server configured in Jenkins
            credentialsId: 'git token', // stored in GITHUb secret store or Use credentials plugin for secure storage (eg vault or credhub)
            url: 'https://github.com/spring-projects/spring-petclinic.git'
            traits {
                // Enforce checks before merge
                disableAutomaticMerges()
                // Check for specific failures (replace with your needs)
                failureTriggers {
                    // JFrog license check failure
                    result('FAILURE', message: 'JFrog License Check Failed')
                    // JFrog vulnerability scan failure
                    result('FAILURE', message: 'JFrog Vulnerability Scan Failed')
                    result('FAILURE', message: 'Secret scan Failed')
                    result('FAILURE', message: 'Sonar CodeCoverage Failed')  // For running this test we need to run maven unit test so that we will get the results 
                    
                }
            }
        }
    }

    stages {

        stage('Pull Request Checks (Feature Branch)') {
            when {
                expression { return branch =~ /feature.*/ } // Only for feature branches
            }
            steps {
                script {
                    // Configure JFrog details from environment variables
                    def server = Artifactory.server "${ARTIFACTORY_SERVER}"
                    def username = "${ARTIFACTORY_USER}"
                    def password = "${ARTIFACTORY_PASSWORD}"

                    // Define paths (replace with your structure)
                    def buildArtifactPath = 'com/example/petclinic/petclinic'
   
                    // JFrog License Check 
                    sh 'ci/tasks/jfrog-license-check.sh <project-key> spring-petclinic'
                    

                    // JFrog Vulnerability Scan 
                    sh 'ci/tasks/jfrog-vulnerability-scan.sh spring-petclinic'
                    

                    // Check for secret keys 
                    sh 'ci/tasks/secret-scan.sh spring-petclinic'
                    

                    //Jacoco code coverage will be collected after running the UNIT test & pushed to SONARQUBE for the quality gate check 
                    sh 'ci/tasks/codecoverage.sh'
                    
                } 
            }
        }


      // Following the feature branch test results below stages will be carried out & performed on main branch
        stage('Merge to Main and Checkout (if checks pass)') {
            when {
                // Only for successful previous stage and targeting main branch
                expression { return branch == 'main' && !currentBuild.previousBuildResult || previousResult == SUCCESS }
            }
            steps {
                   git branch: 'main',
                   credentialsId: 'your-git-credentials-id', // Replace with your credential ID
                   url: 'https://github.com/spring-projects/spring-petclinic.git'
         
                }
        }


        stage('Run Unit/Component/Integration Tests') {
            steps {
                sh 'ci/tasks/unittest.sh'
            }
        }


        stage('Run Build') {
            steps {
                sh 'ci/tasks/mavenbuild.sh'
            }
        }


        stage('Build Docker Image') {
            steps {
                sh 'ci/tasks/dockerbuild.sh'
            }
        }

        stage('Push build artifact to Artifactory') {
            steps {
                script {
                    def jfrog_server = Artifactory.server "${ARTIFACTORY_SERVER}"
                    def jfrog_username = 'artifactory-username'
                    def jfrog_password = 'artifactory-password'
                    sh 'ci/tasks/jfrog-push-artifact.sh jfrog_username jfrog_password jfrog_server'                   
                    }
                }
            }
        }

        stage('Push Image to Artifactory') {
            steps {
                script {
                    def jfrog_server = Artifactory.server "${ARTIFACTORY_SERVER}"
                    def jfrog_username = 'artifactory-username'
                    def jfrog_password = 'artifactory-password'
                    sh 'ci/tasks/jfrog-push-image.sh jfrog_username jfrog_password jfrog_server'  
                    }
                }
            }
        }

         stage('Version update') {
            steps {
                sh 'ci/tasks/version-update.sh'
            }
        }

        stage('Email Notification') {
            steps {
            emailext to: 'rakesh.prusty@xyz.com',
            recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
            subject: "jenkins build:${currentBuild.currentResult}: ${env.JOB_NAME}",
            body: "${currentBuild.currentResult}: Job ${env.JOB_NAME}\nMore Info can be found here: ${env.BUILD_URL}"
      }



    }

    post {
        

        always {            
            cleanWs()
        }
    }
}
