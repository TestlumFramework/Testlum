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
                     useRepository: '.*testlum-test-resources.git')
        gitParameter(branchFilter: 'origin/(.*)',
                     defaultValue: 'master',
                     name: 'BRANCH_SITE',
                     type: 'PT_BRANCH',
                     useRepository: '.*mega-test-app.git')
    }
    environment {
        SERVICE = "testing-tool"
        SITE = "site-sample"
        TEST_API = "mega-test-api"
        TAG = "${GIT_COMMIT}"
        SITE_URL = "ssh://git@bitbucket.knubisoft.com:7999/cott/mega-test-app.git"
        URL_TESTING_TOOL = "ssh://git@bitbucket.knubisoft.com:7999/cott/testlum.git"
        URL_TESTING_TOOL_SCENARIOS = "ssh://git@bitbucket.knubisoft.com:7999/cott/testlum-test-resources.git"
        GIT_CREDENTIALS_ID = "954f8583-fdd1-4359-b45c-3ccb24df6ba4"
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
            sh 'mkdir -p testlum-test-resources'
        }
      }
    }
    stage('checkout ci tool scenarios') {
        steps {
            dir("tool/testlum-test-resources") {
                git branch: "${params.BRANCH_SCENARIOS}", url: URL_TESTING_TOOL_SCENARIOS, credentialsId: GIT_CREDENTIALS_ID
            }
        }
    }
    stage('checkout test app') {
        steps {
            dir("site") {
                git branch: "${params.BRANCH_SITE}", url: SITE_URL, credentialsId: GIT_CREDENTIALS_ID
            }
        }
    }
    stage('build test api') {
        steps {
            dir("site") {
                sh "mvn clean package -DskipTests"
//                 sh "docker build -f Dockerfile.jenkins -t ${TEST_API} ."
            }
        }
    }
    stage('start test api') {
        steps {
            dir("site") {
                sh "docker-compose -f docker-compose-jenkins.yaml up -d --force-recreate "
//                  && docker-compose -f docker-compose-selenium-grid.yaml up -d --force-recreate"
            }
        }
    }
    stage('start test app') {
        steps {
            dir("site") {
                sh 'sleep 20 && java -jar TEST-API/target/mega-test-api.jar --spring.profiles.active=jenkins --spring.config.location="TEST-API/src/main/resources'
//                 sh "sleep 20 && docker-compose -f docker-compose-api.yaml up -d --force-recreate "
            }
        }
    }
    stage('build test tool') {
        steps {
            dir("tool") {
                sh "docker build -f Dockerfile.jenkins -t ${SERVICE}:${TAG} ."
            }
        }
    }
    stage('run test tool') {
        steps {
            dir("tool") {
                sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -v "$(pwd)"/testlum-test-resources:/testlum/testlum-test-resources ${SERVICE}:${TAG} -c=config-jenkins.xml -p=/testlum/testlum-test-resources/REGRESSION_TESTS_resources'
                sh "cat testlum-test-resources/REGRESSION_TESTS_resources/scenarios_execution_result.txt | awk '/successfully/{ exit 0 }/failed/{ exit 1 }'"
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
//         dir("site") {
//             sh "docker-compose -f docker-compose-api.yaml down"
//         }
        script {
            sh "docker rmi ${SERVICE}:${TAG}"
//             sh "docker rmi -f ${TEST_API}"
            sh 'docker rmi -f $(docker images -f "dangling=true" -q) || true'
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
