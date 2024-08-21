pipeline {
    agent any
    
    tools {
        jdk 'jdk17'
        nodejs 'node16'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }
        
        stage('Checkout from Git') {
            steps {
                git branch: 'main', url: 'https://github.com/abdraheem98/a-youtube-clone-app.git'
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh "npm install"
            }
        }
        
        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
            }
        }
        stage("Docker Build & Push"){
             steps{
                 script{
                   withDockerRegistry(credentialsId: 'dockerhub', toolName: 'docker'){   
                      sh "docker build -t youtube-clone ."
                      sh "docker tag youtube-clone abdraheem98/youtube-clone:latest"
                      sh "docker push abdraheem98/youtube-clone:latest"
                    }
                }
            }
        }
        stage("TRIVY Image Scan"){
            steps{
                sh "trivy image abdraheem98/youtube-clone:latest > trivyimage.txt" 
            }
        }
        stage('Deploy to Kubernets'){
            steps{
                script{
                    dir('Kubernetes') {
                      withKubeConfig(caCertificate: '', clusterName: '', contextName: '', credentialsId: '', namespace: '', restrictKubeConfigAccess: false, serverUrl: '') {
                      sh 'kubectl delete --all pods'
                      sh 'kubectl apply -f deployment.yml'
                      sh 'kubectl apply -f service.yml'
                      }   
                    }
                }
            }
        }
    } 
}
