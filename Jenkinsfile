pipeline {
    agent any

    environment {
        JAVA17_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'

        DOCKER_IMAGE = 'avah777/aws-jenkins-kubernetes'

        AWS_REGION = 'us-east-1'
        EKS_CLUSTER = 'devops-demo'

        K8S_NAMESPACE = 'devops-demo'
        K8S_DEPLOYMENT = 'aws-java-app'
        CONTAINER_NAME = 'aws-java-app'

        IMAGE_TAG = "${BUILD_NUMBER}"
        IMAGE_URI = "${DOCKER_IMAGE}:${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh '''
                    export JAVA_HOME="$JAVA17_HOME"
                    export PATH="$JAVA_HOME/bin:$PATH"

                    echo "===== MAVEN JAVA ====="
                    java -version

                    echo "===== MAVEN ====="
                    mvn -version

                    echo "===== RUNNING TESTS ====="
                    mvn clean test
                '''
            }
        }

        stage('Build') {
            steps {
                sh '''
                    export JAVA_HOME="$JAVA17_HOME"
                    export PATH="$JAVA_HOME/bin:$PATH"

                    echo "===== MAVEN JAVA ====="
                    java -version

                    echo "===== MAVEN ====="
                    mvn -version

                    echo "===== BUILDING APPLICATION ====="
                    mvn clean package -DskipTests
                '''
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
                sh '''
                    echo "===== DOCKER BUILD ====="

                    docker build \
                        -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                        -t ${DOCKER_IMAGE}:latest \
                        .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "===== DOCKER HUB LOGIN ====="

                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        echo "===== PUSH BUILD IMAGE ====="

                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}

                        echo "===== PUSH LATEST IMAGE ====="

                        docker push ${DOCKER_IMAGE}:latest

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                        set -e

                        echo "===== AWS IDENTITY ====="
                        aws sts get-caller-identity

                        echo "===== CONFIGURE EKS ====="
                        aws eks update-kubeconfig \
                            --region "$AWS_REGION" \
                            --name "$EKS_CLUSTER"

                        echo "===== KUBERNETES CLUSTER ====="
                        kubectl cluster-info

                        echo "===== KUBERNETES NODES ====="
                        kubectl get nodes

                        echo "===== CREATE NAMESPACE ====="
                        kubectl apply -f namespace.yaml

                        echo "===== DEPLOY APPLICATION ====="
                        kubectl apply -f deployment.yaml

                        echo "===== CREATE SERVICE ====="
                        kubectl apply -f service.yaml

                        echo "===== UPDATE IMAGE ====="
                        kubectl set image deployment/$K8S_DEPLOYMENT \
                            $CONTAINER_NAME=${IMAGE_URI} \
                            -n $K8S_NAMESPACE

                        echo "===== WAIT FOR ROLLOUT ====="
                        kubectl rollout status \
                            deployment/$K8S_DEPLOYMENT \
                            -n $K8S_NAMESPACE \
                            --timeout=180s

                        echo "===== PODS ====="
                        kubectl get pods -n $K8S_NAMESPACE

                        echo "===== SERVICE ====="
                        kubectl get svc -n $K8S_NAMESPACE
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'CI/CD pipeline failed.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}


