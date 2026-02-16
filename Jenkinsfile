pipeline {
    agent any

    environment {
        REGISTRY = "localhost:5001"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/pdrohrosario/springboot-avaliator'
            }
        }

        stage('Build Catalog Service') {
            agent {
                docker {
                    image 'maven:3.9.7-eclipse-temurin-21-alpine'
                }
            }
            steps {
                dir('catalogservice') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Feedback Service') {
            steps {
                dir('feedbackservice') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        // stage('Build Docker Images') {
        //     steps {
        //         sh 'docker build -t $REGISTRY/catalogservice:latest catalogservice'
        //         sh 'docker build -t $REGISTRY/feedbackservice:latest feedbackservice'
        //     }
        // }

        // stage('Push Images') {
        //     steps {
        //         sh 'docker push $REGISTRY/catalogservice:latest'
        //         sh 'docker push $REGISTRY/feedbackservice:latest'
        //     }
        // }

        // stage('Deploy with Docker Compose') {
        //     steps {
        //         sh 'docker compose down'
        //         sh 'docker compose up -d --build'
        //     }
        // }

        // Se quiser Kubernetes em vez de compose
        /*
        stage('Deploy Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/'
            }
        }
        */

    }
}
