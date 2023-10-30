pipeline {
    agent any

    environment {
        // 환경 변수 설정
        IMAGE_NAME = 'lsb8375/esthete-user-service'
        IMAGE_TAG = 'latest'
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

        stage('DockerHub Login'){
            steps{
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin' // docker hub 로그인
            }
        }
        stage('Docker Build') {
            steps {
                sh '''
                   docker build -t ${IMAGE_NAME}:${IMAGE_TAG} --platform linux/amd64 -f /var/jenkins_home/workspace/${JOB_NAME}/Dockerfile /var/jenkins_home/workspace/${JOB_NAME}
                   '''
                //-f {Dockerfile경로} {Build할 디렉토리경로}
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
    }
}

