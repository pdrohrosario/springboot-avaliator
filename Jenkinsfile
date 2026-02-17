pipeline {
    agent any

    environment {
        REGISTRY = "localhost:5001"
        CATALOG_IMAGE = "${REGISTRY}/catalogservice:latest"
        FEEDBACK_IMAGE = "${REGISTRY}/feedbackservice:latest"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/pdrohrosario/springboot-avaliator'
            }
        }

        stage('Build & Test') {
            parallel {
                stage('Catalog Service') {
                    agent {
                        docker {
                            image 'maven:3.9.7-eclipse-temurin-21-alpine'
                            args '-v $HOME/.m2:/root/.m2'
                        }
                    }
                    steps {
                        dir('catalogservice') {
                            sh 'mvn clean verify -DskipTests=false'
                            stash name: 'catalog-jar', includes: 'target/*.jar'
                        }
                    }
                    post {
                        always {
                            junit testResults: 'catalogservice/target/surefire-reports/*.xml', allowEmptyResults: true
                        }
                    }
                }

                stage('Feedback Service') {
                    agent {
                        docker {
                            image 'maven:3.9.7-eclipse-temurin-21-alpine'
                            args '-v $HOME/.m2:/root/.m2'
                        }
                    }
                    steps {
                        dir('feedbackservice') {
                            sh 'mvn clean verify -DskipTests=false'
                            stash name: 'feedback-jar', includes: 'target/*.jar'
                        }
                    }
                    post {
                        always {
                            junit testResults: 'feedbackservice/target/surefire-reports/*.xml', allowEmptyResults: true
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            agent any 
            steps {
                script {
                    dir('catalogservice') { unstash 'catalog-jar' }
                    dir('feedbackservice') { unstash 'feedback-jar' }
                    
                    sh "docker build -t ${CATALOG_IMAGE} catalogservice"
                    sh "docker build -t ${FEEDBACK_IMAGE} feedbackservice"
                }
            }
        }

        stage('Push Images') {
            agent any
            when {
                branch 'master'
            }
            steps {
                script {
                    sh "docker push ${CATALOG_IMAGE}"
                    sh "docker push ${FEEDBACK_IMAGE}"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo "Pipeline falhou! Verifique os logs."
        }
        success {
            echo "Pipeline executada com sucesso!"
        }
    }
}
