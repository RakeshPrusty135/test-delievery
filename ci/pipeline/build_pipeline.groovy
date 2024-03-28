pipeline {
    agent any

    environment {
        // JFrog Artifactory details (replace with your values)
        ARTIFACTORY_SERVER = 'your-artifactory-server-id'
        ARTIFACTORY_USER = 'your-artifactory-username'  // stored in GITHUb secret store or Use credentials plugin for secure storage (eg vault or credhub)
        ARTIFACTORY_PASSWORD = 'your-artifactory-password'  // stored in GITHUb secret store or Use credentials plugin for secure storage (eg vault or credhub)

    triggers {
        githubPullRequest {
            branchFilter = 'feature.*' // Only for feature branches
           # serverName = 'github' // Assuming GitHub server configured in Jenkins
            credentialsId: 'your-git-credentials-id', // Replace with your credential ID
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
                    result('FAILURE', message: 'secret scan failure')
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

                    // JFrog License Check (replace with actual command)
                    sh "jfrog rt rt [JFrog License Check command]"

                    // JFrog Vulnerability Scan (replace with actual command)
                    sh "jfrog rt rt [JFrog Vulnerability Scan command]"

                    // Check for secret keys (replace with your tool and command)
                    sh "find . -name '*.properties' -exec grep -HnE 'api\\.key|secret' {} \\;"

                    // SonarQube Code Coverage (replace with SonarQube Scanner plugin configuration)
                    // ... (SonarQube Scanner plugin steps)
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

                // Build and deploy steps from previous example (replace with your specific logic)
                // ... (previous pipeline stages from checkout to Artifactory upload)
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh 'docker build -t spring-petclinic ./'
                }
            }
        }

        stage('Push Image to Artifactory') {
            steps {
                script {
                    def username = 'your-artifactory-username'
                    def password = 'your-artifactory-password'
                    withCredentials([usernamePassword(credentialsId: 'your-artifactory-credentials-id', usernameVariable: 'username', passwordVariable: 'password')]) {
                        sh "docker login -u ${username} -p ${password} JFrog_URL" // Replace with your JFrog Artifactory URL
                        sh 'docker push JFrog_URL/spring-petclinic:latest'
                    }
                }
            }
        }



    }

    post {
        always {
            // Clean workspace after each run
            cleanWs()
        }
    }
}
