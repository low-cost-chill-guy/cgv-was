pipeline {
    agent any
    
    environment {
        AWS_PROFILE = 'jenkins_profile'
        AWS_DEFAULT_REGION = "ap-northeast-2"
        GITHUB_REPO = 'https://github.com/low-cost-chill-guy/cgv-was.git'
        ENV = "${env.BRANCH_NAME == 'main' ? 'prod' : env.BRANCH_NAME}"
        IMAGE_REPO_NAME = "${ENV}/lowcostchillguy${ENV}"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        REPOSITORY_URI = sh(
            script: "aws ecr describe-repositories --repository-names ${IMAGE_REPO_NAME} --query 'repositories[0].repositoryUri' --output text --profile ${AWS_PROFILE}",
            returnStdout: true
        ).trim()
    }
   
    options {
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                script {
                    cleanWs()
                    sh 'rm -rf .git'
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM',
                        branches: [[name: "*/${env.BRANCH_NAME}"]],
                        extensions: [[$class: 'CleanBeforeCheckout']],
                        userRemoteConfigs: [[url: "${GITHUB_REPO}"]]
                    ])
                }
            }
        }
        
        stage('Setup Gradle') {
            steps {
                script {
                    sh 'chmod +x ./gradlew'
                    
                    if (fileExists('build.gradle')) {
                        def buildGradleContent = readFile('build.gradle')
                        if (!buildGradleContent.contains('org.owasp.dependencycheck')) {
                            writeFile file: 'build.gradle', text: """
                            ${buildGradleContent}

                            plugins {
                                id 'org.owasp.dependencycheck' version '8.2.1'
                            }

                            dependencyCheck {
                                formats = ['HTML', 'XML']
                                suppressionFile = 'dependency-check-suppressions.xml'
                                failBuildOnCVSS = 7
                            }
                            """
                        }
                    } else {
                        writeFile file: 'build.gradle', text: '''
                        plugins {
                            id 'org.owasp.dependencycheck' version '8.2.1'
                        }

                        dependencyCheck {
                            formats = ['HTML', 'XML']
                            suppressionFile = 'dependency-check-suppressions.xml'
                            failBuildOnCVSS = 7
                        }
                        '''
                    }
                    
                    if (!fileExists('gradlew')) {
                        sh '''
                            wget https://services.gradle.org/distributions/gradle-7.6.1-bin.zip
                            unzip gradle-7.6.1-bin.zip
                            gradle-7.6.1/bin/gradle wrapper
                            chmod +x gradlew
                            rm -rf gradle-7.6.1
                            rm gradle-7.6.1-bin.zip
                        '''
                    }
                }
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
                    writeFile file: 'Dockerfile', text: '''
                    FROM openjdk:17-jdk-slim

                    WORKDIR /app

                    COPY build/libs/*.jar app.jar

                    EXPOSE 8080

                    ENTRYPOINT ["java", "-jar", "app.jar"]
                    '''
                    
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
                            
                            sed -i "s|image: .*:\\(.*\\)|image: ${REPOSITORY_URI}:${IMAGE_TAG}|" deployment.yaml
                            
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
    
    post {
        always {
            script {
                if (currentBuild.result == null) {
                    currentBuild.result = 'SUCCESS'
                }
                
                sh 'docker system prune -f || true'
                cleanWs()
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
}