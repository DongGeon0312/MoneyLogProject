// 도전(F-16): Jenkins 파이프라인 예시.
// 실행하려면 Jenkins에 Docker/kubectl이 설치돼 있어야 하고,
// Jenkins Credentials에 'dockerhub-credentials'(Username/Password)를 등록해야 한다.
pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        IMAGE_NAME = "${DOCKERHUB_CREDENTIALS_USR}/moneylog"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                dir('backend') {
                    sh 'gradle build'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh "docker push ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

        stage('Deploy to Kubernetes (rolling update)') {
            steps {
                sh "kubectl set image deployment/moneylog-app moneylog-app=${IMAGE_NAME}:${BUILD_NUMBER} --record"
                sh 'kubectl rollout status deployment/moneylog-app'
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
