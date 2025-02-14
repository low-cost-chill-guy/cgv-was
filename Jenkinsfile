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
   
    stages {
        stage('Checkout') { 
            steps {
                checkout scmGit(branches: [[name: '*/main']], 
                    userRemoteConfigs: [[url: "${GITHUB_REPO}"]])
            }
        }
    
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
        
        stage('Building image') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_REPO_NAME}:${IMAGE_TAG}")
                }
            }
        }
        
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