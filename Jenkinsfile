pipeline { 
    agent any
    
    environment {
        AWS_PROFILE = 'jenkins_profile'
        AWS_DEFAULT_REGION="ap-northeast-2"
        IMAGE_REPO_NAME="chillguy/cicdwas"
        IMAGE_TAG="latest"
        REPOSITORY_URI = sh(
            script: "aws ecr describe-repositories --repository-names ${IMAGE_REPO_NAME} --query 'repositories[0].repositoryUri' --output text --profile ${AWS_PROFILE}",
            returnStdout: true
        ).trim()
        GITHUB_REPO = 'https://github.com/low-cost-chill-guy/cgv-was.git'
    }
   
    options {
        disableConcurrentBuilds()
    }
    
    post {
        always {
            script {
                if (currentBuild.result == null) {
                    currentBuild.result = 'SUCCESS'
                }
            }
        }
        // 빌드 성공 시 슬랙 전송
        success {
            slackSend (
                channel: '#ci', 
                color: 'good',
                message: """
                    :white_check_mark: 파이프라인 빌드 성공
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Branch: main
                    빌드 URL: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
        // 빌드 실패 시 슬랙 전송
        failure {
            slackSend (
                channel: '#ci', 
                color: 'danger',
                message: """
                    :x: 파이프라인 빌드 실패
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Branch: main
                    빌드 URL: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
    }

    stages {
        stage('Checkout') { 
            steps {
                checkout scmGit(branches: [[name: '*/main']], 
                    userRemoteConfigs: [[url: "${GITHUB_REPO}"]])
            }
        }

        // ecr에 로그인
        stage('Logging into AWS ECR') { 
            steps {
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} --profile ${AWS_PROFILE} | \
                        docker login --username AWS --password-stdin \$(aws ecr describe-repositories \
                        --repository-names ${IMAGE_REPO_NAME} \
                        --query 'repositories[0].repositoryUri' \
                        --output text \
                        --profile ${AWS_PROFILE} \
                        | cut -d'/' -f1)
                    """
                }
            }
        }
        
        // 도커를 사용하여 이미지로 생성성
        stage('Building image') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_REPO_NAME}:${IMAGE_TAG}")
                }
            }
        }
        
        // ecr에 이미지 푸쉬쉬
        stage('Pushing to ECR') {
            steps {
                script {
                    sh """
                        docker tag ${IMAGE_REPO_NAME}:${IMAGE_TAG} ${REPOSITORY_URI}:${IMAGE_TAG}
                        docker push ${REPOSITORY_URI}:${IMAGE_TAG}
                    """
                }
            }
        }
    }
}



// pipeline { 
//    agent any
   
//    environment {
//        AWS_PROFILE = 'jenkins_profile'
//        AWS_DEFAULT_REGION="ap-northeast-2"
//        IMAGE_REPO_NAME="chillguy/cicdwas"
//        IMAGE_TAG="latest"
//        REPOSITORY_URI = sh(
//            script: "aws ecr describe-repositories --repository-names ${IMAGE_REPO_NAME} --query 'repositories[0].repositoryUri' --output text --profile ${AWS_PROFILE}",
//            returnStdout: true
//        ).trim()
//        GITLAB_REPO = 'https://gitlab.com/yhoka/yhgitlab.git'
//        GITLAB_CREDENTIALS = credentials('gitlabjenkinsauth')  // 이 한 줄로 통합
//    }
  
//    options {
//        disableConcurrentBuilds()
//    }
   
//    post {
//        always {
//            script {
//                if (currentBuild.result == null) {
//                    currentBuild.result = 'SUCCESS'
//                }
//            }
//        }
//        success {
//            slackSend (
//                channel: '#젠킨스-ci-빌드-결과', 
//                color: 'good',
//                message: """
//                    :white_check_mark: 파이프라인 빌드 성공
//                    Job: ${env.JOB_NAME}
//                    Build Number: ${env.BUILD_NUMBER}
//                    Branch: main
//                    빌드 URL: ${env.BUILD_URL}
//                """.stripIndent()
//            )
//        }
//        failure {
//            slackSend (
//                channel: '#젠킨스-ci-빌드-결과', 
//                color: 'danger',
//                message: """
//                    :x: 파이프라인 빌드 실패
//                    Job: ${env.JOB_NAME}
//                    Build Number: ${env.BUILD_NUMBER}
//                    Branch: main
//                    빌드 URL: ${env.BUILD_URL}
//                """.stripIndent()
//            )
//        }
//    }

//    stages {
//        stage('Checkout from GitLab') { 
//            steps {
//                checkout([$class: 'GitSCM',
//                    branches: [[name: '*/main']],
//                    userRemoteConfigs: [[
//                        url: "${GITLAB_REPO}",
//                        credentialsId: "${GITLAB_CREDENTIALS}"  // 환경 변수 참조로 변경
//                    ]]
//                ])
//            }
//        }

//        stage('Logging into AWS ECR') { 
//            steps {
//                script {
//                    sh """
//                        aws ecr get-login-password --region ${AWS_DEFAULT_REGION} --profile ${AWS_PROFILE} | \
//                        docker login --username AWS --password-stdin \$(aws ecr describe-repositories \
//                        --repository-names ${IMAGE_REPO_NAME} \
//                        --query 'repositories[0].repositoryUri' \
//                        --output text \
//                        --profile ${AWS_PROFILE} \
//                        | cut -d'/' -f1)
//                    """
//                }
//            }
//        }
       
//        stage('Building image') {
//            steps {
//                script {
//                    dockerImage = docker.build("${IMAGE_REPO_NAME}:${IMAGE_TAG}")
//                }
//            }
//        }
       
//        stage('Pushing to ECR') {
//            steps {
//                script {
//                    sh """
//                        docker tag ${IMAGE_REPO_NAME}:${IMAGE_TAG} ${REPOSITORY_URI}:${IMAGE_TAG}
//                        docker push ${REPOSITORY_URI}:${IMAGE_TAG}
//                    """
//                }
//            }
//        }
//    }
// }