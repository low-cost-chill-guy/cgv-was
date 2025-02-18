pipeline {
    agent any
    
    environment {
        AWS_PROFILE = 'jenkins_profile'
        AWS_DEFAULT_REGION = "ap-northeast-2"
        GITHUB_REPO = 'https://github.com/low-cost-chill-guy/cgv-was.git'
        ENV = "${env.BRANCH_NAME == 'main' ? 'prod' : env.BRANCH_NAME}"
        IMAGE_REPO_NAME = "chillguy/wastest"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        REPOSITORY_URI = sh(
            script: "aws ecr describe-repositories --repository-names ${IMAGE_REPO_NAME} --query 'repositories[0].repositoryUri' --output text --profile ${AWS_PROFILE}",
            returnStdout: true
        ).trim()
        SECURITY_TOOLS_DIR = "${env.WORKSPACE}/security-tools"
        SONAR_HOME = "${SECURITY_TOOLS_DIR}/sonarqube"
        SONAR_PROJECT_KEY = "lowcostchillguy-${ENV}"
        SONAR_SERVER_URL = "http://localhost:9000"
    }
   
    options {
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                script {
                    // 워크스페이스 초기화
                    cleanWs()
                    // .git 디렉토리 삭제
                    sh 'rm -rf .git'
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    // Git 저장소 새로 클론
                    checkout([$class: 'GitSCM',
                        branches: [[name: "*/${env.BRANCH_NAME}"]],
                        extensions: [[$class: 'CleanBeforeCheckout']],
                        userRemoteConfigs: [[url: "${GITHUB_REPO}"]]
                    ])
                }
            }
        }
        
        stage('Setup Security Tools') {
            steps {
                script {
                    // security-tools 디렉토리 생성
                    sh 'mkdir -p ${SECURITY_TOOLS_DIR}'
                    
                    // env.sh 파일 생성
                    writeFile file: "${SECURITY_TOOLS_DIR}/dependency-check/env.sh", text: '''
                        #!/bin/bash
                        export DEPENDENCY_CHECK_HOME="${SECURITY_TOOLS_DIR}/dependency-check"
                        export PATH="$DEPENDENCY_CHECK_HOME/bin:$PATH"
                    '''
                    
                    // 실행 권한 부여
                    sh """
                        mkdir -p ${SECURITY_TOOLS_DIR}/dependency-check/bin
                        chmod +x ${SECURITY_TOOLS_DIR}/dependency-check/env.sh
                        
                        # setup_security_tools.sh가 존재하는 경우에만 실행
                        if [ -f ./ci/setup_security_tools.sh ]; then
                            chmod +x ./ci/setup_security_tools.sh
                            ./ci/setup_security_tools.sh
                        else
                            echo "setup_security_tools.sh not found. Installing minimal security tools..."
                            # 최소한의 보안 도구 설치
                            curl -sSfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
                        fi
                    """
                }
            }
        }
        
        stage('Security Scanning') {
            steps {
                script {
                    try {
                        // OWASP Dependency Check 실행
                        if (fileExists("build.gradle")) {
                            sh '''
                                # Gradle 프로젝트용 Dependency Check 설정
                                echo "plugins { id 'org.owasp.dependencycheck' version '8.2.1' }" >> build.gradle
                                ./gradlew dependencyCheckAnalyze || true
                            '''
                        } else {
                            echo "Skipping Dependency Check - no Gradle project found"
                        }
                        
                        // Trivy 스캔
                        sh 'trivy fs --format table --output trivy-fs-report.txt . || true'
                    } catch (Exception e) {
                        echo "Security scanning failed but continuing: ${e.message}"
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
                    dockerImage = docker.build("${IMAGE_REPO_NAME}:${IMAGE_TAG}")
                }
            }
        }
        
        stage('Container Security Scan') {
            steps {
                script {
                    // Trivy로 빌드된 이미지 스캔
                    sh "trivy image --format table --output trivy-image-report.txt ${IMAGE_REPO_NAME}:${IMAGE_TAG}"
                    
                    // 취약점이 심각(CRITICAL)할 경우 파이프라인 실패 처리 (선택적)
                    def trivyExitCode = sh(script: "trivy image --exit-code 1 --severity CRITICAL ${IMAGE_REPO_NAME}:${IMAGE_TAG}", returnStatus: true)
                    if (trivyExitCode == 1) {
                        input message: '심각한 취약점이 발견되었습니다. 그래도 계속 진행하시겠습니까?', ok: '진행'
                    }
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
        
        // main 환경은 argocd로 배포
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
post {
    always {
       script {
            if (currentBuild.result == null) {
                currentBuild.result = 'SUCCESS'
            }
                
            // 빌드 아티팩트 정리
            sh 'docker system prune -f || true'
            cleanWs(patterns: [[pattern: 'security-tools', type: 'EXCLUDE']])
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