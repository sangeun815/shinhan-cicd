pipeline {
    agent any

    environment {
        // 환경 변수 설정
        IMAGE_NAME = 'lsb8375/esthete-user-service'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub_jenkins')
        JOB_NAME = 'esthete-user-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'pwd' // 현재 디렉토리 확인
            }
        }

        stage('Gradle Build') {
            steps {
                sh 'cd /var/jenkins_home/workspace/${JOB_NAME} && ./gradlew clean build -x test'
            }
        }

        stage('DockerHub Login') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin' // docker hub 로그인
            }
        }

        stage('Determine Docker Image Version') {
            steps {
                script {
                    def lastImageVersion
                    def lastImageTag = readFile("../${JOB_NAME}-image-tag.txt").trim()

                    if (lastImageTag) {
                        lastImageVersion = lastImageTag.tokenize('.')
                        def majorVersion = lastImageVersion[0] as int
                        def minorVersion = lastImageVersion[1] as int

                        // 이미지 버전 증가
                        minorVersion += 1
                        if (minorVersion >= 10) {
                            majorVersion += 1
                            minorVersion = 0
                        }

                        env.IMAGE_TAG = "${majorVersion}.${minorVersion}"
                        currentBuild.description = "Docker 이미지 버전: ${env.IMAGE_TAG}"
                    } else {
                        error("이전 이미지 버전 정보를 읽어올 수 없습니다.")
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} --platform linux/amd64 -f /var/jenkins_home/workspace/${JOB_NAME}/Dockerfile /var/jenkins_home/workspace/${JOB_NAME}"
            }
        }

        stage('Docker Push to Docker Hub') {
            steps {
                sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Docker Clean Up') {
            steps {
                script {
                    sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG}" // 이미지 삭제
                    sh "docker image prune -f" // 사용하지 않는 이미지 삭제
                }
            }
        }
        stage('Update Image Version File') {
            steps {
                script {
                    writeFile file: "../${JOB_NAME}-image-tag.txt", text: env.IMAGE_TAG
                }
            }
        }
    }
}
