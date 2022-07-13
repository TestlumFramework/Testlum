pipeline {
    agent any
     options {
        ansiColor('vga')
    }
    parameters {
        gitParameter(branchFilter: 'origin/(.*)',
                     defaultValue: 'master',
                     name: 'BRANCH',
                     type: 'PT_BRANCH',
                     useRepository: '.*e2e-testing-tool.git')
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
        TAG = "latest"
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
        }
    }
    stage('checkout ci tool') {
      steps {
        dir("tool") {
            git branch: "${params.BRANCH}", url: URL_TESTING_TOOL, credentialsId: GIT_CREDENTIALS_ID
            sh 'mkdir -p e2e-testing-scenarios'
            sh 'mkdir -p ${SITE}'
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
                sh "docker build -t ${SERVICE}:${TAG} ."
                // sh "mvn clean install  -DskipTests"
            }

        }
    }
    stage('run test tool') {
        steps {
            dir("tool") {
                sh 'docker run --rm --network=host --mount type=bind,source="$(pwd)"/e2e-testing-scenarios,target=/e2e-testing-scenarios ${SERVICE}:latest -c=config-jenkins.xml -p=/e2e-testing-scenarios/JENKINS_resources'
                // sh "java -jar ./target/e2e-testing-tool.jar -c=config-jenkins.xml -p=./e2e-testing-scenarios/JENKINS_resources"
            }
        }
    }
    stage('docker cleanup') {
        steps {
            sh "docker rmi ${SERVICE}:${TAG}"
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
}