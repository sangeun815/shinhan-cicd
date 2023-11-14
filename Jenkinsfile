pipeline {
    agent any

    environment {
        // 환경 변수 설정
        IMAGE_NAME = 'lsb8375/shinhan-cicd'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub_jenkins')
        JOB_NAME = 'shinhan-cicd'
        GITHUB_TOKEN = credentials('ghp_write_repo')
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
        stage('Update values.yaml on GitHub') {
            steps {
                script {
                    def githubToken = env.GITHUB_TOKEN

                    def githubRepo = 'bin-pro/nks-gitops'

                    def filePath = 'shinhan-charts/shinhan-chart/values.yaml'

                    def newContents = """
# Default values for esthete-user-chart.
# This is a YAML-formatted file.
# Declare variables to be passed into your templates.

# esthete-deployment-chart/values.yaml

replicaCount: 1

image:
  repository: lsb8375/esthete-user
  tag: ${env.IMAGE_TAG}

containerPort: 8080

ingress:
  enabled: true

controller:
  ## Argo controller commandline flags
  args:
    appResyncPeriod: \"60\"
"""
                    def shaOutput = sh(script: """
curl -s -X GET \\
-H "Accept: application/vnd.github+json" \\
-H "X-GitHub-Api-Version: 2022-11-28" \\
-H 'Authorization: Bearer ${githubToken}' https://api.github.com/repos/${githubRepo}/contents/${filePath}?ref=deployment | jq -r '.sha'
""", returnStdout: true)

                    def sha = shaOutput.trim() // 가져온 출력의 앞뒤 공백을 제거하고 저장
                    println("sha: ${sha}")

                    // newContents를 파일에 저장
                    def newContentsFile = writeFile file: "temp-new-contents.yaml", text: newContents

                    // 파일을 base64로 인코딩
                    def base64Contents = sh(script: "cat temp-new-contents.yaml | base64 | tr -d '\\n'", returnStdout: true)


                    println("base64Contents: ${base64Contents}")
                    // 파일 삭제
                    sh "rm temp-new-contents.yaml"

                    def response = sh(script: """
curl -X PUT -H "Accept: application/vnd.github+json" -H "Authorization: Bearer ${githubToken}" -H "X-GitHub-Api-Version: 2022-11-28" https://api.github.com/repos/${githubRepo}/contents/${filePath} -d '{"message": "Chore: Update values.yaml by Jenkins","content": "${base64Contents}","branch": "deployment","sha": "$sha"}'
""", returnStatus: true)

                    if (response == 0) {
                        currentBuild.result = 'SUCCESS'
                    } else {
                        currentBuild.result = 'FAILURE'
                        error("GitHub의 values.yaml를 업데이트하지 못했습니다.")
                    }
                }
            }
        }
    }
}
