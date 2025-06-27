# MY-WORK-BE

## 1. 프로젝트 실행 방법

- 프로젝트 루트를 기준으로 한 명령어 입니다.

```bash
# 1. docker compose 설정
docker-compose -f ./docker/docker-compose.yml up -d

# 2. 프로제트 빌드
./gradlew clean build

# 3. 프로젝트 실행
java -jar ./build/libs/mywork-be-0.0.1-SNAPSHOT.jar
```

# 성준님 PR