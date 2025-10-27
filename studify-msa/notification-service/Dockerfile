# Java 17이 설치된 리눅스
FROM openjdk:17-jdk-slim

# 빌드된 .jar 파일이 있는 경로
ARG JAR_FILE=build/libs/*.jar

# .jar 파일을 컨테이너 안으로 app.jar 로 복사
COPY ${JAR_FILE} app.jar

# 컨테이너가 시작 시 명령어
ENTRYPOINT ["java","-jar","/app.jar"]
