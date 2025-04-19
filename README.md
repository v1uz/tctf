# Как запустить приложение в докере

`docker compose up -d`

При запуске в Production поменяйте все ключи!

# Как запустить приложение не в контейнере

1. `docker compose up -d db` запустит базу данных Postgres
2. Если у вас IDE, запустите `ru.capybarovsk.overhaul.Application` с флагом `-Dspring.profiles.active=local`.
3. Если у вас не IDE, запустите `./gradlew bootRun --args='--spring.profiles.active=local'` (Linux / Mac OS)
   или `gradlew.bat bootRun --args='--spring.profiles.active=local'` (Windows)
