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
    }
}
 