pipeline {
    agent any
    options {
        ansiColor('gnome-terminal')
        disableConcurrentBuilds()
    }
    parameters {
        gitParameter(branchFilter: 'origin/(.*)',
                     defaultValue: 'master',
                     name: 'BRANCH_SCENARIOS',
                     type: 'PT_BRANCH',
                     useRepository: '.*cott-test-resources.git') 
        gitParameter(branchFilter: 'origin/(.*)',
                     defaultValue: 'master',
                     name: 'BRANCH_SITE',
                     type: 'PT_BRANCH',
                     useRepository: '.*site-sample.git')    
    }
    environment {
        SERVICE = "testing-tool"
        SITE = "site-sample"
        TAG = "${GIT_COMMIT}"
        SITE_URL = "ssh://git@bitbucket.knubisoft.com:7999/cott/site-sample.git"
        URL_TESTING_TOOL = "ssh://git@bitbucket.knubisoft.com:7999/cott/cost-optimization-testing-tool.git"
        URL_TESTING_TOOL_SCENARIOS = "ssh://git@bitbucket.knubisoft.com:7999/cott/cott-test-resources.git"
        GIT_CREDENTIALS_ID = "bitbucket"
        HOST='jenkins@192.168.0.7'
        PORT='22'
        HOST_DIR='/data/e2e-testing-tool'
        DOCKER_USERNAME='dex'
        DOCKER_PASSWORD='UbJhHCr94cvH6nRDvkhW'
    }
  stages {
    stage('Print job envs') {
        steps {
            echo "Print env"
            cleanWs()
            sh "env"
            sh "mkdir tool site"
            sh "curl -s -X POST https://api.telegram.org/bot1846108211:AAH7Qm_y__ARQXh4q_fXiLEjnMhJyQ-eeok/sendMessage -d chat_id=-1001593036618 -d text='Build Started %F0%9F%98%80 : ${env.JOB_NAME} \n PR: ${env.BRANCH_NAME} \n Parameters: \n BRANCH_TOOL: ${CHANGE_BRANCH} \n BRANCH_SITE: ${env.BRANCH_SITE} \n BRANCH_SCENARIOS: ${env.BRANCH_SCENARIOS} \n BUILD_URL: ${env.BUILD_URL}'"
        }
    }
    stage('checkout ci tool') {
      steps {
        dir("tool") {
            git branch: CHANGE_BRANCH, url: URL_TESTING_TOOL, credentialsId: GIT_CREDENTIALS_ID
            sh 'mkdir -p cott-test-resources'
        }
      }
    }
    stage('checkout ci tool scenarios') {
        steps {
            dir("tool/cott-test-resources") {
                git branch: "${params.BRANCH_SCENARIOS}", url: URL_TESTING_TOOL_SCENARIOS, credentialsId: GIT_CREDENTIALS_ID
            }
        }
    }
    stage('checkout site') {
        steps {
            dir("site") {
                git branch: "${params.BRANCH_SITE}", url: SITE_URL, credentialsId: GIT_CREDENTIALS_ID
            }
        }
    }
    stage('start site') {
        steps {
            dir("site") {
                sh "docker-compose -f docker-compose.yaml up -d --force-recreate && docker-compose -f docker-compose-selenium-grid.yaml up -d --force-recreate"
            }
        }
    }
    stage('build test tool') {
        steps {
            dir("tool") {
                sh "docker build -f Dockerfile.jenkins -t ${SERVICE}:${TAG} ."
                // sh "mvn clean install  -DskipTests"
            }

        }
    }
    stage('run test tool') {
        steps {
            dir("tool") {
                sh 'docker run -u $(id -u):$(id -g) --rm --network=host -v "$(pwd)"/cott-test-resources:/cott/cott-test-resources ${SERVICE}:${TAG} -c=config-jenkins.xml -p=/cott/cott-test-resources/JENKINS_resources'
                sh "cat cott-test-resources/JENKINS_resources/scenarios_execution_result.txt | awk '/successfully/{ exit 0 }/failed/{ exit 1 }'"
                // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./cott-test-resources/JENKINS_resources"
            }
        }
    }
    // stage('down site') {
    //     steps {
    //         dir("site") {
    //             sh "docker-compose down"
    //         }
    //     }
    // }
  }
  post {
    always {
        script {
            sh "docker rmi ${SERVICE}:${TAG}"
            sh 'docker rmi $(docker images -f "dangling=true" -q) || true'
        }
    }
    success {
        sh "curl -s -X POST https://api.telegram.org/bot1846108211:AAH7Qm_y__ARQXh4q_fXiLEjnMhJyQ-eeok/sendMessage -d chat_id=-1001593036618 -d text='*SUCCESS* %E2%9C%94 \n PR: ${env.BRANCH_NAME} \n Parameters: \n BRANCH_TOOL: ${CHANGE_BRANCH} \n BRANCH_SITE: ${env.BRANCH_SITE} \n BRANCH_SCENARIOS: ${env.BRANCH_SCENARIOS} \n BUILD_URL: ${env.BUILD_URL}'"
    }
    failure {
        sh "curl -s -X POST https://api.telegram.org/bot1846108211:AAH7Qm_y__ARQXh4q_fXiLEjnMhJyQ-eeok/sendMessage -d chat_id=-1001593036618 -d text='*FAILURE* %E2%9C%96 \n PR: ${env.BRANCH_NAME} \n Parameters: \n BRANCH_TOOL: ${CHANGE_BRANCH} \n BRANCH_SITE: ${env.BRANCH_SITE} \n BRANCH_SCENARIOS: ${env.BRANCH_SCENARIOS} \n BUILD_URL: ${env.BUILD_URL}'"
    }
  }
}
