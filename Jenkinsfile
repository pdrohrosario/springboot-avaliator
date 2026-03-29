pipeline {
    agent any

    environment {
        REGISTRY                      = "${env.REGISTRY ?: 'localhost:5001'}"
        MAVEN_IMAGE                   = "${env.MAVEN_IMAGE ?: 'maven:3.9.7-eclipse-temurin-21-alpine'}"
        K8S_NAMESPACE                 = "${env.K8S_NAMESPACE ?: 'avaliator'}"
        K8S_KUBECONFIG_CREDENTIALS_ID = "${env.K8S_KUBECONFIG_CREDENTIALS_ID ?: 'k8s-kubeconfig'}"
        ENV_FILE_CREDENTIALS_ID       = "${env.ENV_FILE_CREDENTIALS_ID ?: 'env-file'}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Generate Configs') {
            steps {
                sh 'apt-get update -qq && apt-get install -y -qq gettext-base > /dev/null'
                withCredentials([file(credentialsId: "${ENV_FILE_CREDENTIALS_ID}", variable: 'ENV_FILE')]) {
                    sh '''
                        set -e

                        cp "${ENV_FILE}" .env
                        set -a
                        . ./.env
                        set +a

                        envsubst < k8s/postgres/secret.yaml.tpl > k8s/postgres/secret.yaml
                        envsubst < k8s/jenkins-kubconfig.yaml.tpl > k8s/jenkins-kubconfig.yaml

                        rm -f .env

                        [ -s k8s/postgres/secret.yaml ] || { echo "ERROR: secret.yaml generation failed"; exit 1; }
                        [ -s k8s/jenkins-kubconfig.yaml ] || { echo "ERROR: jenkins-kubconfig.yaml generation failed"; exit 1; }
                    '''
                }
            }
        }

        stage('Build Services') {
            parallel {
                stage('Build Catalog') {
                    agent {
                        docker { image "${MAVEN_IMAGE}" }
                    }
                    steps {
                        dir('catalogservice') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build Feedback') {
                    agent {
                        docker { image "${MAVEN_IMAGE}" }
                    }
                    steps {
                        dir('feedbackservice') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Test Services') {
            parallel {
                stage('Test Catalog') {
                    agent {
                        docker { image "${MAVEN_IMAGE}" }
                    }
                    steps {
                        dir('catalogservice') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true,
                                testResults: 'catalogservice/**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Test Feedback') {
                    agent {
                        docker { image "${MAVEN_IMAGE}" }
                    }
                    steps {
                        dir('feedbackservice') {
                            sh 'mvn test'
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true,
                                testResults: 'feedbackservice/**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Build & Push Images') {
            parallel {
                stage('Catalog Image') {
                    steps {
                        sh """
                            docker build \
                                -t ${REGISTRY}/catalogservice:${BUILD_NUMBER} \
                                -t ${REGISTRY}/catalogservice:latest \
                                catalogservice
                            docker push ${REGISTRY}/catalogservice:${BUILD_NUMBER}
                            docker push ${REGISTRY}/catalogservice:latest
                        """
                    }
                }
                stage('Feedback Image') {
                    steps {
                        sh """
                            docker build \
                                -t ${REGISTRY}/feedbackservice:${BUILD_NUMBER} \
                                -t ${REGISTRY}/feedbackservice:latest \
                                feedbackservice
                            docker push ${REGISTRY}/feedbackservice:${BUILD_NUMBER}
                            docker push ${REGISTRY}/feedbackservice:latest
                        """
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                    sh """
                        echo '📋 Aplicando manifests K8s...'
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/postgres/
                        kubectl apply -f k8s/catalogservice/
                        kubectl apply -f k8s/feedbackservice/
                        kubectl apply -f k8s/ingress.yaml

                        echo '🔄 Atualizando imagens para build ${BUILD_NUMBER}...'
                        kubectl set image deployment/catalogservice \
                            catalogservice=${REGISTRY}/catalogservice:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/feedbackservice \
                            feedbackservice=${REGISTRY}/feedbackservice:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}

                        echo '⏳ Verificando rollout...'
                        kubectl rollout status deployment/catalogservice \
                            -n ${K8S_NAMESPACE} --timeout=180s

                        kubectl rollout status deployment/feedbackservice \
                            -n ${K8S_NAMESPACE} --timeout=180s

                        echo '✅ Deploy concluído com sucesso!'
                        kubectl get pods -n ${K8S_NAMESPACE}
                    """ 
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Success! Build #${BUILD_NUMBER}'
        }
        failure {
            echo '❌ Failure! Build #${BUILD_NUMBER}'
        }
    }
}
