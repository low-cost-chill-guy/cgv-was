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
        LOC_FILE = credentials('application-local-yaml')
        SONAR_TOKEN = credentials('sonar-token')
    }

    tools {
        'dependency-check' 'Dependency-Check'
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
        stage('Prepare local File') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'application-local-yaml', variable: 'LOC_FILE')]) {
                        sh """
                            mkdir -p src/main/resources
                            chmod -R 755 src/main/resources
                            cp ${LOC_FILE} src/main/resources/application-local.yaml
                        """
                    }
                    sh 'cat src/main/resources/application-local.yaml'
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

        // stage('Dependency Check') {
        //     steps {
        //         withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
        //             sh 'mkdir -p dependency-check-reports'

        //             dependencyCheck additionalArguments: """
        //                 --scan ./
        //                 --format "HTML"
        //                 --format "XML"
        //                 --out ./dependency-check-reports
        //                 --data /var/jenkins_home/dependency-check-data
        //                 --nvdApiKey ${NVD_API_KEY}
        //             """, odcInstallation: 'Dependency-Check'

        //             dependencyCheckPublisher pattern: 'dependency-check-reports/dependency-check-report.xml'
        //         }
        //     }
        // }

        stage('Build & Test') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }

        // stage('SonarQube Analysis') {
        //     steps {
        //         withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
        //             sh """
        //                 ./gradlew sonar \
        //                     -Dsonar.projectKey=jenkinssonaqube \
        //                     -Dsonar.host.url=http://khp-sonarqube-1:9000 \
        //                     -Dsonar.login=${SONAR_TOKEN}
        //             """
        //         }
        //     }
        // }

        stage('Building image') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_REPO_NAME}:${IMAGE_TAG}")
                }
            }
        }

        stage('Trivy Security Scan') {
            steps {
                script {
                    // 결과 저장할 디렉토리 생성
                    sh 'mkdir -p reports/trivy'
                    sh 'chmod -R 777 reports/trivy'
                    
                    // HTML 템플릿 다운로드
                    sh '''
                        rm -rf /tmp/html.tpl
                        curl -o /tmp/html.tpl https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl
                        ls -la /tmp/html.tpl
                    '''
                    
                    // Trivy 스캔 실행
                    sh """
                        docker run --rm \
                            -v /var/run/docker.sock:/var/run/docker.sock \
                            -v /tmp:/tmp \
                            -v ${WORKSPACE}/reports/trivy:/report \
                            aquasec/trivy:latest image \
                            --severity HIGH,CRITICAL \
                            --format template \
                            --template '@/tmp/html.tpl' \
                            --output /report/trivy-scan-report-${env.BUILD_NUMBER}.html \
                            ${IMAGE_REPO_NAME}:${IMAGE_TAG}
                    """
                    
                    // 결과 파일 권한 설정
                    sh 'chmod -R 777 reports/trivy'
                }
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'reports/trivy',
                        reportFiles: "trivy-scan-report-${env.BUILD_NUMBER}.html",
                        reportName: 'Trivy Scan Report'
                    ])
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
}