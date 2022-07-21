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
                     useRepository: '.*e2e-testing-scenarios.git') 
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
        SITE_URL = "ssh://git@bitbucket.knubisoft.com:7999/ee/site-sample.git"
        URL_TESTING_TOOL = "ssh://git@bitbucket.knubisoft.com:7999/ee/e2e-testing-tool.git"
        URL_TESTING_TOOL_SCENARIOS = "ssh://git@bitbucket.knubisoft.com:7999/ee/e2e-testing-scenarios.git"
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
            slackSend color: "warning", message: "Build Started: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
        }
    }
    stage('checkout ci tool') {
      steps {
        dir("tool") {
            git branch: CHANGE_BRANCH, url: URL_TESTING_TOOL, credentialsId: GIT_CREDENTIALS_ID
            sh 'mkdir -p e2e-testing-scenarios'
        }
      }
    }
    stage('checkout ci tool scenarios') {
        steps {
            dir("tool/e2e-testing-scenarios") {
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
                sh 'docker run -u $(id -u):$(id -g) --rm --network=host --mount type=bind,source="$(pwd)"/e2e-testing-scenarios,target=/e2e-testing-scenarios ${SERVICE}:${TAG} -c=config-jenkins.xml -p=/e2e-testing-scenarios/JENKINS_resources'
                // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./e2e-testing-scenarios/JENKINS_resources"
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
            currentBuild.result = currentBuild.result ?: 'SUCCESS'
            notifyBitbucket()
        }
    }
    success {
        slackSend color: "good", message: "*SUCCESS* \n Job name: ${env.JOB_NAME} \n Build number: ${env.BUILD_NUMBER} \n Build url: (<${env.BUILD_URL}|Open>)"
    }
    failure {
        slackSend color: "danger", message: "*FAILURE* \n Job name: ${env.JOB_NAME} \n Build number: ${env.BUILD_NUMBER} \n Build url: (<${env.BUILD_URL}|Open>)"
    }
  }
}