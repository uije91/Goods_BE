FROM openjdk:17-jdk

# wait-for-it.sh 스크립트를 컨테이너에 추가
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# 애플리케이션 파일 추가
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 시작 명령어 수정
ENTRYPOINT ["/wait-for-it.sh", "redis:6379", "--", "/wait-for-it.sh", "es-container:9200", "--", "-Dspring.profiles.active=prod", "-jar", "app.jar"]