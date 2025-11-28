pipeline {
    agent any

    environment {
        APP_REPO_URL = "https://github.com/kookyungmin/cicd-study"
        GITHUB_CREDENTIAL_ID = "github-access-token"
        DOCKERHUB_CREDENTIALS = credentials("dockerhub-token")
        DOCKERHUB_REPOSITORY = "rudals4549/cicd-study"
        TARGET_HOST = "ec2-43-201-8-169.ap-northeast-2.compute.amazonaws.com"

        NGINX_CONF_PATH = "/etc/nginx/conf.d"
        NGINX_CONTAINER = "ubuntu-api-gateway-1"
    }

    stages {
        //공통 함수 정의
        stage("Init Functions") {
            steps {
                script {

                    /* --- SSH 명령 실행 Function --- */
                    sshRun = { String body ->
                        sshagent(credentials: ['jenkins-controller']) {
                            sh """
                                ssh -o StrictHostKeyChecking=no ubuntu@${TARGET_HOST} '
                                    ${body}
                                '
                            """
                        }
                    }

                    /* --- Nginx Upstream 변경 --- */
                    changeUpstream = { String confFile ->
                        sshRun("""
                            docker exec ${NGINX_CONTAINER} cp ${NGINX_CONF_PATH}/${confFile} ${NGINX_CONF_PATH}/cicd.conf
                            RELOAD_RESULT=\$(docker exec ${NGINX_CONTAINER} service nginx reload)
                            echo "nginx reload: \$RELOAD_RESULT"
                        """)
                    }

                    /* --- 특정 서비스 컨테이너 재시작 --- */
                    restartService = { String serviceName ->
                        sshRun("""
                            docker compose -f docker-compose-app.yml stop ${serviceName}
                            docker compose -f docker-compose-app.yml rm -f ${serviceName}
                            docker pull ${DOCKERHUB_REPOSITORY}:latest
                            docker compose -f docker-compose-app.yml up -d ${serviceName}
                        """)
                    }

                    /* --- Health Check --- */
                    healthCheck = { String url ->
                        sshRun("""
                            for retry_count in \$(seq 10); do
                                echo "Health checking... (\$retry_count/10)"

                                if curl -sf ${url} > /dev/null ; then
                                    echo "Health OK"
                                    exit 0
                                fi

                                if [ \$retry_count -eq 10 ]; then
                                    echo "Health CHECK FAILED"
                                    exit 1
                                fi

                                sleep 5
                            done
                        """)
                    }

                    /* --- Blue/Green 배포 공통 함수 --- */
                    deploy = { String color, String confFile, String url ->
                        echo "===== Deploying ${color} ====="
                        changeUpstream(confFile)
                        restartService("app-${color}")
                        healthCheck(url)
                    }
                }
            }
        }

        /* --------------------------------------------------
         * Git Checkout
         * -------------------------------------------------- */
        stage("Checkout & Build") {
            steps {
                dir('boot-app') {
                    sh "chmod +x gradlew"
                    sh "./gradlew clean test bootJar"
                }
            }
        }

        /* --------------------------------------------------
         * Docker Build & Push
         * -------------------------------------------------- */
        stage("Docker Build & Push") {
            steps {
                sh "docker build --no-cache --platform linux/amd64 -t ${DOCKERHUB_REPOSITORY}:latest ."
                sh "echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin"
                sh "docker push ${DOCKERHUB_REPOSITORY}:latest"
                sh "docker rmi ${DOCKERHUB_REPOSITORY}:latest"
            }
        }

        /* --------------------------------------------------
         * Blue / Green Rolling Deploy
         * -------------------------------------------------- */

        stage("Deploy to GREEN") {
            steps {
                script {
                    deploy(
                            "green",                // color
                            "green-shutdown.conf", // upstream conf
                            "http://localhost:8080/api/v1/health" // health URL
                    )
                }
            }
        }

        stage("Deploy to BLUE") {
            steps {
                script {
                    deploy(
                            "blue",
                            "blue-shutdown.conf",
                            "http://localhost:8081/api/v1/health"
                    )
                }
            }
        }

        stage("Nginx All-UP") {
            steps {
                script {
                    changeUpstream("all-up.conf")
                }
            }
        }
    }
}
