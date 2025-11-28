pipeline {
    agent any

    environment {
        APP_REPO_URL = "https://github.com/kookyungmin/cicd-study"
        GITHUB_CREDENTIAL_ID = "github-access-token"
        DOCKERHUB_CREDENTIALS = credentials("dockerhub-token")
        DOCKERHUB_REPOSITORY = "rudals4549/cicd-study"
        TARGET_HOST = "ec2-43-201-8-169.ap-northeast-2.compute.amazonaws.com"
    }

    stages {
        stage("Checkout Source As Tagname") {
            steps {
                script {
                    try {
                        if ("${env.TAG_NAME}" == "origin/main") {
                            print("selected origin/main")
                            throw new Exception("Tag selection is required")
                        }
                    } catch (err) {
                        echo "Caught: ${err}"
                        currentBuild.result = "FAILURE"
                    }
                }

                checkout([
                        $class           : 'GitSCM',
                        branches         : [[name: "refs/tags/${params.TAG_NAME}"]],
                        userRemoteConfigs: [[url: "${env.APP_REPO_URL}", credentialsId: "${env.GITHUB_CREDENTIAL_ID}"]],
                        extensions       : [
                                [$class: 'CloneOption', noTags: false, shallow: false]
                        ]
                ])
            }
        }

        stage("Build") {
            steps {
                dir('boot-app') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean test bootJar"
                }
            }
        }

        stage("Create new Docker Image") {
            steps {
                sh "docker build --no-cache --platform linux/amd64 -t $DOCKERHUB_REPOSITORY:latest ."
            }
        }

        stage("Login Docker Hub") {
            steps {
                sh "echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin"
            }
        }

        stage("Upload Docker Image") {
            steps {
                sh "docker push $DOCKERHUB_REPOSITORY:latest"
            }
        }

        stage("Prune Docker") {
            steps {
                sh "docker rmi $DOCKERHUB_REPOSITORY:latest"
                sh "docker system prune"
            }
        }

        stage("Deploy to EC2") {
            steps {
                sshagent(credentials: ['jenkins-controller']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${TARGET_HOST} '
                            pwd
                            docker compose down
                            docker rmi ${DOCKERHUB_REPOSITORY}:latest
                            docker pull ${DOCKERHUB_REPOSITORY}:latest
                            docker compose up -d
                        '
                    """
                }
            }
        }
    }
}
