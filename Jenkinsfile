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
        
        // 보안 도구 설정 경로
        SECURITY_TOOLS_DIR = "${env.WORKSPACE}/security-tools"
        SONAR_HOME = "${SECURITY_TOOLS_DIR}/sonarqube"
        
        // SonarQube 환경 변수
        SONAR_PROJECT_KEY = "lowcostchillguy-${ENV}"
        SONAR_SERVER_URL = "http://localhost:9000"
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
                
                // 빌드 아티팩트 정리
                sh 'docker system prune -f'
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
                    SonarQube 분석 결과: ${SONAR_SERVER_URL}/dashboard?id=${SONAR_PROJECT_KEY}
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
        
        stage('Setup Security Tools') {
            steps {
                script {
                    // 스크립트 파일 실행 권한 부여
                    sh 'chmod +x ./ci/setup_security_tools.sh'
                    
                    // 스크립트 실행
                    sh './ci/setup_security_tools.sh'
                    
                    // OWASP Dependency Check 환경 설정 적용
                    if (fileExists("${SECURITY_TOOLS_DIR}/dependency-check/env.sh")) {
                        sh '''
                        #!/bin/bash  
                            "source ${SECURITY_TOOLS_DIR}/dependency-check/env.sh"
                        '''
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    // SonarQube 토큰 읽기
                    def sonarToken = ""
                    if (fileExists("${SONAR_HOME}/admin_token.txt")) {
                        sonarToken = readFile("${SONAR_HOME}/admin_token.txt").trim()
                    } else {
                        echo "SonarQube 토큰을 찾을 수 없습니다. 기본 admin 계정을 사용합니다."
                        sonarToken = "admin"
                    }
                    
                    // Gradle 프로젝트인 경우
                    if (fileExists("gradlew")) {
                        sh """
                            ./gradlew sonarqube \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="LowCostChillGuy ${ENV}" \
                                -Dsonar.host.url=${SONAR_SERVER_URL} \
                                -Dsonar.login=${sonarToken}
                        """
                    } 
                    // Maven 프로젝트인 경우
                    else if (fileExists("mvnw")) {
                        sh """
                            ./mvnw sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="LowCostChillGuy ${ENV}" \
                                -Dsonar.host.url=${SONAR_SERVER_URL} \
                                -Dsonar.login=${sonarToken}
                        """
                    }
                    // 빌드 도구가 없는 경우 sonar-scanner 직접 사용
                    else {
                        sh """
                            docker run --rm \
                              -e SONAR_HOST_URL=${SONAR_SERVER_URL} \
                              -e SONAR_LOGIN=${sonarToken} \
                              -v "${env.WORKSPACE}:/usr/src" \
                              sonarsource/sonar-scanner-cli:latest \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.projectName="LowCostChillGuy ${ENV}"
                        """
                    }
                }
            }
        }
        
        stage('Security Scanning') {
            parallel {
                stage('OWASP Dependency Check') {
                    steps {
                        script {
                            // Gradle 프로젝트인 경우
                            if (fileExists("gradlew") && fileExists("build.gradle")) {
                                sh './gradlew dependencyCheckAnalyze'
                                dependencyCheckPublisher pattern: 'build/reports/dependency-check-report.xml'
                            } 
                            // 그 외의 경우 직접 OWASP Dependency Check 실행
                            else {
                                sh """
                                    ${SECURITY_TOOLS_DIR}/dependency-check/bin/dependency-check.sh \
                                      --project "LowCostChillGuy ${ENV}" \
                                      --scan ${env.WORKSPACE} \
                                      --format "XML" \
                                      --format "HTML" \
                                      --out ${env.WORKSPACE}/dependency-check-reports
                                """
                                // 빌드된 리포트를 Jenkins에 발행
                                dependencyCheckPublisher pattern: 'dependency-check-reports/dependency-check-report.xml'
                            }
                        }
                    }
                }
                
                stage('Trivy Scan') {
                    steps {
                        script {
                            // 소스 코드 스캔
                            sh "trivy fs --format table --output trivy-fs-report.txt ."
                        }
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