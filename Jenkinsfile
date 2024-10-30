def createDir(String dirPath) {
    sh """
        install -d -o jenkins -g jenkins ${dirPath}
    """
}

def removeUnusedImage() {
    sh "docker rmi ${ecr}/${repo}:${tag} && docker rmi ${repo}:${tag}"
}

def removeUnusedNewTagImage() {
    sh "docker rmi ${ecr}/${repo}:${tag}-${buildVersion} && docker rmi ${repo}:${tag}"
}

def pushImage() {
    sh """
        aws ecr-public get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecr}
        docker tag ${repo}:${tag} ${ecr}/${repo}:${tag}
        docker push ${ecr}/${repo}:${tag}
    """
}

def pushNewTagImage() {
    sh """
        aws ecr-public get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecr}
        docker tag ${repo}:${tag} ${ecr}/${repo}:${tag}-${buildVersion}
        docker push ${ecr}/${repo}:${tag}-${buildVersion}
    """
}

pipeline {
    agent any
    options {
        ansiColor('gnome-terminal')
    }
    parameters {
        gitParameter(branchFilter: 'origin/(.*)',
                defaultValue: 'dev',
                name: 'branch',
                type: 'PT_BRANCH',
                selectedValue: 'DEFAULT')
        choice(name: 'project',
                choices: ['PME', 'Immunotec'],
                description: 'Target project to build testlum docker image')
        booleanParam(name: 'pushToECR',
                defaultValue: false,
                description: 'Push image to ECR for Main Version?')
        booleanParam(name: 'pushToECRWithNewTag',
                defaultValue: false,
                description: 'Push image to ECR New Version?')
        string(name: 'buildVersion',
                defaultValue: '1.0.0',
                description: 'Put image version if needed')
        string(name: 'cromeDriverVersion',
                defaultValue: '123.0.6312.58',
                description: 'Target CHROME DRIVER VERSION for testlum')
    }

    environment {
        accountId = '044415721934' // AWS account ID
        awsRoleArn = 'deploy' // AWS role for deployment
        credentialsId = 'aws_general_jenkins_user' // AWS credentials
        gitCredenatialsId = 'jenkins_ci_demo_id_rsa' // Git credentials for bucket
        repo = 'testlum' // AWS ECR repo for service
        ecr = "public.ecr.aws/f1r2a3f3" // AWS ECR url
        region = 'us-east-1' // AWS region
        appBucketUrl = "ssh://git@bitbucket.knubisoft.com:7999/cott/testlum.git" // App bucket url
        tag = "${project}" // docker tag
    }

    stages {
        stage('Check Parameters') {
            steps {
                script {
                    def mainTag = params.pushToECR
                    def newTag = params.pushToECRWithNewTag

                    if (mainTag && newTag) {
                        currentBuild.result = 'ABORTED'
                        error('Select only one PUSH option!')
                    }
                }
            }
        }
        stage('Create Directory') {
            steps {
                createDir("testlum")
                dir("testlum") {
                    git branch: "${branch}", credentialsId: "${gitCredenatialsId}", url: "${appBucketUrl}"
                }
                sh 'ls -la'
            }
        }
        stage('Build image') {
            steps {
                dir("testlum") {
                    script {
                        withCredentials([[$class           : 'AmazonWebServicesCredentialsBinding',
                                          credentialsId    : "$credentialsId",
                                          accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                          secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                            dockerImage = docker.build("${repo}:${tag}", "--build-arg CHROME_DRIVER_VERSION=${cromeDriverVersion} -f Dockerfile .")
                        }
                    }
                }
            }
        }
        stage('Push to ECR with Main Tag') {
            when {
                expression { params.pushToECR.toBoolean() && !params.pushToECRWithNewTag.toBoolean() }
            }
            steps {
                script {
                    withCredentials([[$class           : 'AmazonWebServicesCredentialsBinding',
                                      credentialsId    : "$credentialsId",
                                      accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                      secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                        pushImage()
                        removeUnusedImage()
                        currentBuild.displayName = "${BUILD_NUMBER}-${repo}:${tag}"
                        println("Image ${ecr}/${repo}:${tag} successfuly builded and pushed to ECR")
                    }
                }
            }
        }
        stage('Push to ECR with New Tag') {
            when {
                expression { params.pushToECRWithNewTag.toBoolean() && !params.pushToECR.toBoolean() }
            }
            steps {
                script {
                    withCredentials([[$class           : 'AmazonWebServicesCredentialsBinding',
                                      credentialsId    : "$credentialsId",
                                      accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                      secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                        pushNewTagImage()
                        removeUnusedNewTagImage()
                        currentBuild.displayName = "${BUILD_NUMBER}-${repo}:${tag}-${buildVersion}"
                        println("Image ${ecr}/${repo}:${tag}-${buildVersion} successfuly builded and pushed to ECR")
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                cleanWs()
            }
        }
    }
}
