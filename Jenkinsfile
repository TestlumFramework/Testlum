def startBrowserStackLocal() {
    sh "sudo /var/lib/jenkins/BrowserStackLocal --key ${STACKLOCAL_KEY} &"
}

def stopBrowserStackLocal() {
    sh "sudo lsof -i:${STACKLOCAL_PORT} | grep BrowserSt | awk '{ print \$2 }' | xargs sudo kill -9"
}

pipeline {
    agent any
    options {
        ansiColor('gnome-terminal')
        disableConcurrentBuilds()
    }

    parameters {
        gitParameter(branchFilter: 'origin/(.*)',
                     defaultValue: 'dev',
                     name: 'BRANCH',
                     type: 'PT_BRANCH',
                     useRepository: '.*Testlum.git')
                     
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
        SERVICE                    = "testing-tool"
        TEST_SITE                  = "mega-test-site"
        TEST_API                   = "mega-test-api"
        TAG                        = "${GIT_COMMIT}"

        SITE_URL                   = "ssh://git@bitbucket.knubisoft.com:7999/cott/mega-test-app.git"
        URL_TESTING_TOOL           = "git@github.com:TestlumFramework/Testlum.git"
        URL_TESTING_TOOL_SCENARIOS = "ssh://git@bitbucket.knubisoft.com:7999/cott/testlum-test-resources.git"

        BITBUCKET_CREDENTIALS_ID   = credentials('jenkins_ssh_for_git')
        GITHUB_CREDENTIALS_ID      = credentials('github-jenkins-ssh')
        
        HOST                       = 'jenkins@192.168.0.7'
        PORT                       = '22'
        HOST_DIR                   = '/data/e2e-testing-tool'

        STACKLOCAL_PORT            = '45454'
        STACKLOCAL_KEY             = credentials('browserstack-local-key') //change in creds
}




  stages {
    stage('Print job envs') {
        steps {
            echo "Print env"
            cleanWs()
            sh "env"
            sh "mkdir tool site"
            sh "curl -s -X POST https://api.telegram.org/bot1846108211:AAH7Qm_y__ARQXh4q_fXiLEjnMhJyQ-eeok/sendMessage -d chat_id=-1001593036618 -d text='Build Started %F0%9F%98%80 : ${env.JOB_NAME} \n PR: ${env.BRANCH_NAME} \n Parameters: \n BRANCH_TOOL: ${BRANCH} \n BRANCH_SITE: ${params.BRANCH_SITE} \n BRANCH_SCENARIOS: ${env.BRANCH_SCENARIOS} \n BUILD_URL: ${env.BUILD_URL}'"
        }
    }
    stage('checkout ci testlum') {
      steps {
        dir("tool") {
            git branch: "${params.BRANCH}", url: URL_TESTING_TOOL, credentialsId: GITHUB_CREDENTIALS_ID
            sh 'mkdir -p testlum-test-resources'
        }
      }
    }
    stage('checkout ci scenarios') {
        steps {
            dir("tool/testlum-test-resources") {
                git branch: "${params.BRANCH_SCENARIOS}", url: URL_TESTING_TOOL_SCENARIOS, credentialsId: BITBUCKET_CREDENTIALS_ID
            }
        }
    }
    stage('checkout test app') {
        steps {
            dir("site") {
                git branch: "${params.BRANCH_SITE}", url: SITE_URL, credentialsId: BITBUCKET_CREDENTIALS_ID
            }
        }
    }
    stage('build test api') {
        steps {
            dir("site") {
                sh "docker build -f Dockerfile.jenkins -t ${TEST_API} ."
            }
        }
    }
    stage('build test site') {
        steps {
            dir("site/TEST-UI") {
                sh "docker build -f Dockerfile.jenkins -t ${TEST_SITE} ."
            }
        }
    }
    stage('running BrowserStackLocal') {
        steps {
            script{
                startBrowserStackLocal()
            }
        }
    }
    stage('start test api') {
        steps {
            dir("site") {
                sh "docker-compose -f docker/docker-compose-jenkins.yaml up -d --force-recreate"
                sh "docker exec -t vault-e2e sh /tmp/init_vault.sh"
                sh 'sleep 5'
                sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -e TZ=Europe/Kiev -p 8080:8080 -d ${TEST_API} --name ${TEST_API} -e SPRING_PROFILES_ACTIVE=jenkins'
//                 sh 'java -jar TEST-API/target/mega-test-api.jar --spring.profiles.active=jenkins --spring.config.location="TEST-API/src/main/resources/" &'
            }
        }
    }
    stage('start test site') {
        steps {
            dir("site") {
                sh "docker-compose -f docker/docker-compose-selenium-grid.yaml up -d --force-recreate"
                sh "docker-compose -f docker/docker-compose-app.yaml up -d --force-recreate"
//                 sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -e TZ=Europe/Kiev -p 3000:3000 -d ${TEST_SITE} --name ${TEST_SITE}'
            }
        }
    }
    // professional subscription
    stage('build testlum-professional') {
        steps {
            dir("tool") {
                sh """docker build -f Dockerfile.jenkins -t ${SERVICE}:professional --build-arg="PROFILE=professional" ."""
            }
        }
    }
    stage('run regression tests professional') {
        steps {
            dir("tool") {
                sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -e TZ=Europe/Kiev -v "$(pwd)"/testlum-test-resources:/testlum-test-resources ${SERVICE}:professional -c=config-professional-testlum.xml -p=/testlum-test-resources/REGRESSION_TESTS_resources'
                sh "cat testlum-test-resources/REGRESSION_TESTS_resources/scenarios_execution_result.txt | awk '/successfully/{ exit 0 }/failed/{ exit 1 }'"
                sh "docker rmi ${SERVICE}:professional || true"
                // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./testlum-test-resources/JENKINS_resources"
            }
        }
    }
//CHECK FILE FOR BITBUCKET in jenkins ubrat
    // free subscription
    // stage('build testlum-free') {
    //     steps {
    //         dir("tool") {
    //             sh """docker build -f Dockerfile.jenkins -t ${SERVICE}:free --build-arg="PROFILE=free" ."""
    //         }
    //     }
    // }
    // stage('run regression tests free') {
    //     steps {
    //         dir("tool") {
    //             sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -e TZ=Europe/Kiev -v "$(pwd)"/testlum-test-resources:/testlum-test-resources ${SERVICE}:free -c=config-free-testlum.xml -p=/testlum-test-resources/REGRESSION_TESTS_resources'
    //             sh "cat testlum-test-resources/REGRESSION_TESTS_resources/scenarios_execution_result.txt | awk '/successfully/{ exit 0 }/failed/{ exit 1 }'"
    //             sh "docker rmi ${SERVICE}:free || true"
    //             // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./testlum-test-resources/JENKINS_resources"
    //         }
    //     }
    // }
    // // standard subscription
    // stage('build testlum-standard') {
    //     steps {
    //         dir("tool") {
    //             sh """docker build -f Dockerfile.jenkins -t ${SERVICE}:standard --build-arg="PROFILE=standard" ."""
    //         }
    //     }
    // }
    // stage('run regression tests standard') {
    //     steps {
    //         dir("tool") {
    //             sh 'docker run -u $(id -u):$(id -g) --rm --network=e2e_network -e TZ=Europe/Kiev -v "$(pwd)"/testlum-test-resources:/testlum-test-resources ${SERVICE}:standard -c=config-standard-testlum.xml -p=/testlum-test-resources/REGRESSION_TESTS_resources'
    //             sh "cat testlum-test-resources/REGRESSION_TESTS_resources/scenarios_execution_result.txt | awk '/successfully/{ exit 0 }/failed/{ exit 1 }'"
    //             sh "docker rmi ${SERVICE}:standard || true"
    //             // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./testlum-test-resources/JENKINS_resources"
    //         }
    //     }
    // }
  }
  post {
    always {
        dir("site") {
            sh "docker-compose -f docker/docker-compose-jenkins.yaml down || true"
            sh "docker-compose -f docker/docker-compose-selenium-grid.yaml down || true"
            sh "docker-compose -f docker/docker-compose-app.yaml down || true"
        }
        script {
            sh "docker rm -f -v \$(docker ps -q) || true"
            //sh "docker rmi ${SERVICE}:${TAG} || true"
            sh "docker rmi -f ${TEST_API} || true"
            sh "docker rmi -f ${TEST_SITE} || true"
            sh 'docker rmi -f $(docker images -f "dangling=true" -q) || true'
            sh 'docker volume ls -qf "dangling=true" | xargs docker volume rm || true'
            stopBrowserStackLocal()
        }

        script {

            if (env.GIT_URL?.contains('github.com')) {
                step([
                    $class: 'GitHubCommitStatusSetter',
                    reposSource: [$class: "ManuallyEnteredRepositorySource", url: env.GIT_URL],
                    commitShaSource: [$class: "ManuallyEnteredShaSource", sha: env.GIT_COMMIT],
                    contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/e2e-tests"],
                    errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
                    statusResultSource: [
                        $class: "ConditionalStatusResultSource",
                        results: [
                            [$class: "AnyBuildResult", state: currentBuild.currentResult == 'SUCCESS' ? 'SUCCESS' : 'FAILURE', message: "Jenkins E2E: ${currentBuild.currentResult}"]
                        ]
                    ]
                ])
            }
     }
   }
 }
}
