FROM eclipse-temurin:21-jdk
LABEL maintainer="rudals4549"

# jar 파일 위치를 변수로 설정
ARG JARFILE=boot-app/build/libs/*.jar

# 환경변수 설정
ENV CUSTOM_NAME default

# jar 파일을 컨테이너 내부로 복사
COPY ${JARFILE} app.jar

# 외부 호스 8080 포트로 노출
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

