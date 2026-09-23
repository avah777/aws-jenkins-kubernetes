pipeline {
    agent any

    environment {
	JAVA17_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'       
	 DOCKER_IMAGE = 'avah777/aws-jenkins-kubernetes'
        K8S_NAMESPACE = 'devops-demo'
        K8S_DEPLOYMENT = 'aws-java-app'
	EKS_CLUSTER = 'devops-demo'
    	AWS_REGION = 'us-east-1'
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
		 export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
           	 export PATH=$JAVA_HOME/bin:$PATH

           	 echo "Maven Java:"
           	 java -version

           	 echo "Maven:"
           	 mvn -version

           	 mvn clean test
     	       '''
            }
        }

        stage('Build') {
            steps {
                sh '''
         	   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
         	   export PATH=$JAVA_HOME/bin:$PATH

         	   echo "Maven Java:"
         	   java -version

         	   echo "Maven:"
         	   mvn -version

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
                sh """
                    docker build \
                    -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                    -t ${DOCKER_IMAGE}:latest .
                """
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
                    --region us-east-1 \
                    --name devops-demo

                echo "===== KUBERNETES CLUSTER ====="
                kubectl cluster-info

                echo "===== NODES ====="
                kubectl get nodes

                echo "===== CREATE NAMESPACE ====="
                kubectl apply -f namespace.yaml

                echo "===== DEPLOY APPLICATION ====="
                kubectl apply -f deployment.yaml

                echo "===== CREATE SERVICE ====="
                kubectl apply -f service.yaml

                echo "===== UPDATE IMAGE ====="
                kubectl set image deployment/aws-java-app \
                    aws-java-app=${IMAGE_URI} \
                    -n devops-demo

                echo "===== WAIT FOR ROLLOUT ====="
                kubectl rollout status \
                    deployment/aws-java-app \
                    -n devops-demo \
                    --timeout=180s

                echo "===== PODS ====="
                kubectl get pods -n devops-demo

                echo "===== SERVICE ====="
                kubectl get svc -n devops-demo
            '''
        }
    }
}
