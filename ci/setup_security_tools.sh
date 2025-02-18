# #!/bin/bash
# set -e

# # 로그 함수 정의
# log() {
#   echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
# }

# # 현재 디렉토리 저장
# CURRENT_DIR=$(pwd)
# TOOLS_DIR="$CURRENT_DIR/security-tools"
# mkdir -p $TOOLS_DIR

# # SonarQube 설치 및 실행
# install_sonarqube() {
#   log "SonarQube 설치 시작"
  
#   # Docker가 설치되어 있는지 확인
#   if ! command -v docker &> /dev/null; then
#     log "Docker가 설치되어 있지 않습니다. Docker를 먼저 설치해주세요."
#     exit 1
#   fi
  
#   # SonarQube 컨테이너가 이미 실행 중인지 확인
#   if docker ps | grep -q sonarqube; then
#     log "SonarQube가 이미 실행 중입니다."
#   else
#     # 필요한 디렉토리 생성
#     mkdir -p $TOOLS_DIR/sonarqube/data
#     mkdir -p $TOOLS_DIR/sonarqube/logs
#     mkdir -p $TOOLS_DIR/sonarqube/extensions
    
#     # SonarQube 실행
#     log "SonarQube 컨테이너 시작"
#     docker run -d --name sonarqube \
#       -p 9000:9000 \
#       -v $TOOLS_DIR/sonarqube/data:/opt/sonarqube/data \
#       -v $TOOLS_DIR/sonarqube/logs:/opt/sonarqube/logs \
#       -v $TOOLS_DIR/sonarqube/extensions:/opt/sonarqube/extensions \
#       -e SONAR_JDBC_URL=jdbc:h2:$TOOLS_DIR/sonarqube/data/sonarqube \
#       sonarqube:latest
    
#     # SonarQube 시작 대기
#     log "SonarQube 시작 대기 중..."
#     sleep 30
    
#     # 토큰 생성 (처음 실행 시에만)
#     if [ ! -f $TOOLS_DIR/sonarqube/admin_token.txt ]; then
#       log "SonarQube 관리자 토큰 생성 중..."
#       ADMIN_TOKEN=$(docker exec -it sonarqube /opt/sonarqube/bin/sonar-scanner-cli.sh -Dsonar.login=admin -Dsonar.password=admin -Dsonar.host.url=http://localhost:9000 -Dsonar.projectKey=test | grep -o "SONAR_TOKEN=[a-zA-Z0-9]*" | cut -d= -f2)
#       echo $ADMIN_TOKEN > $TOOLS_DIR/sonarqube/admin_token.txt
#       log "SonarQube 관리자 토큰이 저장되었습니다: $TOOLS_DIR/sonarqube/admin_token.txt"
#     fi
#   fi
  
#   log "SonarQube 설치 완료"
# }

# # Trivy 설치
# install_trivy() {
#   log "Trivy 설치 시작"
  
#   if command -v trivy &> /dev/null; then
#     log "Trivy가 이미 설치되어 있습니다."
#   else
#     # 운영 체제 확인
#     if [ -f /etc/os-release ]; then
#       . /etc/os-release
#       OS=$ID
#     else
#       OS=$(uname -s)
#     fi
    
#     case $OS in
#       ubuntu|debian)
#         log "Debian/Ubuntu 계열 운영 체제 감지"
#         sudo apt-get update
#         sudo apt-get install -y wget apt-transport-https gnupg lsb-release
#         wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo apt-key add -
#         echo "deb https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
#         sudo apt-get update
#         sudo apt-get install -y trivy
#         ;;
#       centos|rhel|fedora)
#         log "RHEL/CentOS 계열 운영 체제 감지"
#         sudo rpm -ivh https://github.com/aquasecurity/trivy/releases/download/v0.43.1/trivy_0.43.1_Linux-64bit.rpm
#         ;;
#       *)
#         log "지원되지 않는 운영 체제입니다. 직접 바이너리를 다운로드합니다."
#         wget -q https://github.com/aquasecurity/trivy/releases/download/v0.43.1/trivy_0.43.1_Linux-64bit.tar.gz
#         tar -zxvf trivy_0.43.1_Linux-64bit.tar.gz
#         sudo mv trivy /usr/local/bin/
#         ;;
#     esac
#   fi
  
#   # 버전 확인
#   TRIVY_VERSION=$(trivy --version | head -n 1)
#   log "Trivy 설치 완료: $TRIVY_VERSION"
# }

# # OWASP Dependency Check 설치
# install_owasp_dependency_check() {
#   log "OWASP Dependency Check 설치 시작"
  
#   OWASP_DC_DIR="$TOOLS_DIR/dependency-check"
#   OWASP_DC_VERSION="7.4.4"
  
#   if [ -d "$OWASP_DC_DIR" ]; then
#     log "OWASP Dependency Check가 이미 설치되어 있습니다."
#   else
#     mkdir -p $OWASP_DC_DIR
#     cd $OWASP_DC_DIR
    
#     # 다운로드 및 압축 해제
#     log "OWASP Dependency Check 다운로드 중..."
#     wget -q https://github.com/jeremylong/DependencyCheck/releases/download/v$OWASP_DC_VERSION/dependency-check-$OWASP_DC_VERSION-release.zip
#     unzip -q dependency-check-$OWASP_DC_VERSION-release.zip
#     rm dependency-check-$OWASP_DC_VERSION-release.zip
    
#     # 실행 권한 부여
#     chmod +x $OWASP_DC_DIR/dependency-check/bin/dependency-check.sh
    
#     # 환경 변수 설정을 위한 스크립트 생성
#     echo "export DEPENDENCY_CHECK_HOME=$OWASP_DC_DIR/dependency-check" > $OWASP_DC_DIR/env.sh
#     echo 'export PATH=$DEPENDENCY_CHECK_HOME/bin:$PATH' >> $OWASP_DC_DIR/env.sh
#     chmod +x $OWASP_DC_DIR/env.sh
    
#     # 초기 업데이트
#     log "OWASP Dependency Check 데이터베이스 업데이트 중..."
#     $OWASP_DC_DIR/dependency-check/bin/dependency-check.sh --updateonly
#   fi
  
#   cd $CURRENT_DIR
#   log "OWASP Dependency Check 설치 완료"
# }

# # Gradle 프로젝트에 OWASP Dependency Check 설정 추가
# setup_gradle_dependency_check() {
#   log "Gradle 프로젝트에 OWASP Dependency Check 설정 추가 시작"
  
#   if [ ! -f build.gradle ]; then
#     log "build.gradle 파일이 현재 디렉토리에 없습니다. 스킵합니다."
#     return
#   fi
  
#   # 이미 추가되어 있는지 확인
#   if grep -q "org.owasp.dependencycheck" build.gradle; then
#     log "OWASP Dependency Check가 이미 build.gradle에 설정되어 있습니다."
#   else
#     # 플러그인 블록에 추가
#     if grep -q "plugins {" build.gradle; then
#       sed -i '/plugins {/a\    id "org.owasp.dependencycheck" version "7.4.4"' build.gradle
#     else
#       echo '
# plugins {
#     id "org.owasp.dependencycheck" version "7.4.4"
# }' >> build.gradle
#     fi
    
#     # 설정 블록 추가
#     echo '
# dependencyCheck {
#     formats = ["HTML", "XML"]
#     failBuildOnCVSS = 7
#     suppressionFile = file("dependency-check-suppressions.xml")
# }' >> build.gradle
    
#     # 샘플 억제 파일 생성
#     echo '<?xml version="1.0" encoding="UTF-8"?>
# <suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
# </suppressions>' > dependency-check-suppressions.xml
    
#     log "OWASP Dependency Check 설정이 build.gradle에 추가되었습니다."
#   fi
  
#   log "Gradle 프로젝트에 OWASP Dependency Check 설정 추가 완료"
# }

# # 메인 함수
# main() {
#   log "보안 도구 설치 및 설정 시작"
  
#   # 각 도구 설치
#   install_sonarqube
#   install_trivy
#   install_owasp_dependency_check
  
#   # Gradle 프로젝트 설정 (프로젝트 루트 디렉토리에서 실행되었을 경우)
#   setup_gradle_dependency_check
  
#   log "모든 보안 도구 설치 및 설정 완료"
#   log "SonarQube는 http://localhost:9000 에서 접근할 수 있습니다. (기본 계정: admin/admin)"
#   log "Trivy는 'trivy' 명령으로 실행할 수 있습니다."
#   log "OWASP Dependency Check는 '$TOOLS_DIR/dependency-check/bin/dependency-check.sh' 또는 Gradle 프로젝트에서 './gradlew dependencyCheckAnalyze' 명령으로 실행할 수 있습니다."
# }

# # 스크립트 실행
# main