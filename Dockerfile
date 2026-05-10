FROM eclipse-temurin:11-jdk

WORKDIR /app

COPY lib ./lib
COPY src ./src
COPY docker ./docker

RUN mkdir -p target/classes \
    && find src/domain -name "*.java" -print > sources.txt \
    && javac -encoding UTF-8 -cp "lib/*" -d target/classes @sources.txt

CMD ["java", "-cp", "/app/target/classes:/app/lib/*", "domain.Source"]
