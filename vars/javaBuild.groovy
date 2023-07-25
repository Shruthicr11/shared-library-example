def call() {
    pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: shruthicr/jenkinsmavenagent:3
    imagePullSecretName: shruthidocker
    command:
    - cat
    tty: true
'''
    }
}

stages {
        stage('get_commit_msg') {
          steps {
            script {
              env.GIT_COMMIT_MSG = sh (script: 'git log -1 --pretty=%B ${GIT_COMMIT}', returnStdout: true).trim()
              env.GIT_COMMIT_USER = sh (script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()
            }
          }
        }
        stage('Build') {
            steps {
                container('maven') {
                    catchError(buildResult: 'UNSTABLE', catchInterruptions: false, message: 'Stage build failed', stageResult: 'FAILURE') {
                        sh 'mvn -B -DskipTests clean package'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                container('maven') {
                  sh 'mvn test'
                  //sh 'mvn -Dmaven.test.failure.ignore=true test'
                  //sh
                }
            }
        post {
                always {
                    container('maven') {
                      junit 'target/surefire-reports/*.xml'
                      step([
                          $class           : 'JacocoPublisher',
                          execPattern      : 'target/jacoco.exec',
                          classPattern     : 'build/classes/main',
                          sourcePattern    : 'src/main/java',
                          exclusionPattern : '**/*Test.class'
                      ])
                    }
                }
            }
        }
        stage('Archving test report') { 
            steps {
                 archiveArtifacts 'target/surefire-reports/*.xml'
            }
        }
        stage('Deliver') {
            steps {
                container('maven') {
                  sh 'chmod 777 ./jenkins/scripts/deliver.sh'
                  sh './jenkins/scripts/deliver.sh'
                }
            }
        }
    }
    post {
                /*success {
                    office365ConnectorSend webhookUrl: 'https://oneharman.webhook.office.com/webhookb2/60732272-9e90-47a6-a371-fb9dc203be8e@f66b6bd3-ebc2-4f54-8769-d22858de97c5/JenkinsCI/e69769375a0146f39c97040dc9445f0d/d7b8529f-150d-4516-95cd-d753bf8429c3',
                    message: "Build is completed sucessfully :- Jobname: ${env.JOB_NAME}, Branch: ${env.GIT_BRANCH}, Build number: ${env.BUILD_NUMBER}, lastcommitid: ${env.GIT_COMMIT}, lastcommitteduser: ${env.GIT_COMMIT_USER} and  lastcommittedmesage: ${env.GIT_COMMIT_MSG}",
                    status: 'Success'
                }
                unstable {
                    office365ConnectorSend webhookUrl: 'https://oneharman.webhook.office.com/webhookb2/60732272-9e90-47a6-a371-fb9dc203be8e@f66b6bd3-ebc2-4f54-8769-d22858de97c5/JenkinsCI/e69769375a0146f39c97040dc9445f0d/d7b8529f-150d-4516-95cd-d753bf8429c3',
                    message: "Build is unstable:- Jobname: ${env.JOB_NAME}, Branch: ${env.GIT_BRANCH}, Build number: ${env.BUILD_NUMBER}, lastcommitteduser: ${env.GIT_COMMIT_USER}, lastcommitid: ${env.GIT_COMMIT}, and  lastcommittedmesage: ${env.GIT_COMMIT_MSG}",
                    status: 'Unstable'
                }
                failure {
                    office365ConnectorSend webhookUrl: 'https://oneharman.webhook.office.com/webhookb2/60732272-9e90-47a6-a371-fb9dc203be8e@f66b6bd3-ebc2-4f54-8769-d22858de97c5/JenkinsCI/e69769375a0146f39c97040dc9445f0d/d7b8529f-150d-4516-95cd-d753bf8429c3',
                    message: "Build failed :- Jobname: ${env.JOB_NAME}, Branch: ${env.GIT_BRANCH}, Build number: ${env.BUILD_NUMBER}, lastcommitteduser: ${env.GIT_COMMIT_USER}, lastcommitid: ${env.GIT_COMMIT}, and  lastcommittedmesage: ${env.GIT_COMMIT_MSG}",
                    status: 'Failure'
                }*/
                always {
                  xunit (thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0')], tools: [[$class: 'JUnitType', pattern: 'target/surefire-reports/*.xml']])
                  notifyBitbuckets(currentBuild.result)
                }
              
                changed {
                    //echo 'This will run only if the state of the Pipeline has changed'
                    office365ConnectorSend webhookUrl: 'https://oneharman.webhook.office.com/webhookb2/60732272-9e90-47a6-a371-fb9dc203be8e@f66b6bd3-ebc2-4f54-8769-d22858de97c5/JenkinsCI/e69769375a0146f39c97040dc9445f0d/d7b8529f-150d-4516-95cd-d753bf8429c3',
                    message: "Build details :- Jobname: ${env.JOB_NAME}, Branch: ${env.GIT_BRANCH}, Build number: ${env.BUILD_NUMBER}, lastcommitteduser: ${env.GIT_COMMIT_USER}, lastcommitid: ${env.GIT_COMMIT}, and  lastcommittedmesage: ${env.GIT_COMMIT_MSG}",
                    status: "${currentBuild.currentResult}"
                }
                
         }
    }

    
}
def notifyBitbuckets(String state) {
 
    if('SUCCESS' == state || 'FAILED' == state) {
    // Set result of currentBuild !Important!
        currentBuild.result = state
    }
 
    notifyBitbucket considerUnstableAsSuccess: true, credentialsId: 'bitbucketuser', disableInprogressNotification: false, ignoreUnverifiedSSLPeer: true, 
    includeBuildNumberInKey: true, prependParentProjectKey: false, projectKey: '', stashServerBaseUrl: 'https://bitbucket.harman.com/'
 
}