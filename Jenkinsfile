pipeline { 
    agent any
    
    environment {
        AWS_PROFILE = 'jenkins_profile'
        AWS_DEFAULT_REGION = "ap-northeast-2"
        GITHUB_REPO = 'https://github.com/low-cost-chill-guy/cgv-was.git'
        
        // 브랜치에 따라 환경을 동적으로 결정
        ENV = "${env.BRANCH_NAME == 'main' ? 'prod' : env.BRANCH_NAME}"
        
        // 환경에 따라 ECR 리포지토리 이름 설정
        IMAGE_REPO_NAME = "${ENV}/lowcostchillguy${ENV}"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        
        // ECR 리포지토리 URI 가져오기
        REPOSITORY_URI = sh(
            script: "aws ecr describe-repositories --repository-names ${IMAGE_REPO_NAME} --query 'repositories[0].repositoryUri' --output text --profile ${AWS_PROFILE}",
            returnStdout: true
        ).trim()
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
        success {
            slackSend (
                channel: '#ci', 
                color: 'good',
                message: """
                    :white_check_mark: 파이프라인 빌드 성공
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Branch: ${env.BRANCH_NAME}
                    Environment: ${ENV}
                    빌드 URL: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
        failure {
            slackSend (
                channel: '#ci', 
                color: 'danger',
                message: """
                    :x: 파이프라인 빌드 실패
                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Branch: ${env.BRANCH_NAME}
                    Environment: ${ENV}
                    빌드 URL: ${env.BUILD_URL}
                """.stripIndent()
            )
        }
    }

    stages {
        stage('Checkout') { 
            steps {
                checkout scmGit(branches: [[name: "*/${env.BRANCH_NAME}"]], 
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
                        
                        # 해당 환경에 대해 latest 태그도 적용
                        docker tag ${IMAGE_REPO_NAME}:${IMAGE_TAG} ${REPOSITORY_URI}:latest
                        docker push ${REPOSITORY_URI}:latest
                    """
                }
            }
        }
        
        stage('Update Kubernetes Manifests') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts
                            chmod 600 "${SSH_KEY}"
                            export GIT_SSH_COMMAND="ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no"
                            
                            rm -rf k8s-manifests
                            git clone git@github.com:low-cost-chill-guy/k8s-manifests.git
                            cd k8s-manifests/\${ENV}
                            
                            # 이미지 태그 업데이트
                            sed -i "s|image: .*:\\(.*\\)|image: ${REPOSITORY_URI}:${IMAGE_TAG}|" deployment.yaml
                            
                            # 변경사항 확인
                            git diff
                            
                            git config user.email "jenkins@example.com"
                            git config user.name "Jenkins CI"
                            git add deployment.yaml
                            git commit -m "Update \${ENV} environment to image ${IMAGE_TAG}"
                            git push origin main
                        """
                    }
                }
            }
        }
    }
}