pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'avah777/aws-jenkins-kubernetes'
        K8S_NAMESPACE = 'devops-demo'
        K8S_DEPLOYMENT = 'aws-java-app'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Check Files') {
            steps {
                sh '''
                    echo "===== CURRENT DIRECTORY ====="
                    pwd

                    echo "===== PROJECT FILES ====="
                    ls -la

                    echo "===== TARGET ====="
                    ls -la target/ || true

                    echo "===== DOCKERFILE ====="
                    ls -la Dockerfile || true
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                    docker build \
                    -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                    -t ${DOCKER_IMAGE}:latest .
                """
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                        -u "$DOCKER_USERNAME" \
                        --password-stdin

                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKER_IMAGE}:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    kubectl set image deployment/${K8S_DEPLOYMENT} \
                    ${K8S_DEPLOYMENT}=${DOCKER_IMAGE}:${BUILD_NUMBER} \
                    -n ${K8S_NAMESPACE}

                    kubectl rollout status \
                    deployment/${K8S_DEPLOYMENT} \
                    -n ${K8S_NAMESPACE}
                """
            }
        }
    }

    post {
        success {
            echo 'CI/CD deployment successful!'
        }

        failure {
            echo 'CI/CD pipeline failed.'
        }
    }
}
