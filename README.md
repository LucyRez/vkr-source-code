# vkr-source-code
Исходный код серверной части, разработанной в рамках ВКР.  
Резуник Людмила  
Группа БПИ192  

# Структура репозитория

- container: содержит файлы сборки контейнера для запуска проекта
- sklif-security-management: содержит исходный код сервиса авторизации
- sklif-organization-management: содержит исходный код сервиса управления организациями
- sklif-scans-provider: содержит исходный код сервиса для взаимодействия с S3-хранилищем
- ai-provider: содержит исходный код сервиса взаимодействия с triton inference server для разметки

# Контейнер

В директории container содержутся файлы для сборки проекта с помощью Docker.  
Для того, чтобы собрать проект необходимо иметь заранее установленный Docker.  
В директории inference-server содержутся файлы для запуска ИИ-сервиса.  

## Порядок сборки

**Примечание:** Если нет необходимости использования ИИ-сервиса, можно пропустить шаги 1-2. Не выполняя их, будет доступна вся функциональность, кроме фугкциональности разметки. (Сервисы системы не зависят от внешенго сервиса ИИ)  

1. Через терминал переходим в папку container  
2. Вводим команду  
```
docker run --rm -p8000:8000 -v /inference-server/deploy:/models nvcr.io/nvidia/tritonserver:22.07-py3 tritonserver --model-repository=/models
```  
    Данная команда необходима для инициализации docker image сервиса распознавания.  
3. После скачивания docker image и инициализации сервиса c шага 2, находясь в той же папке в терминале вводим команду docker-compose up. Таким образом, все сервисы, которые были реализованы в рамках ВКР, будут собраны в один контейнер.  
4. Для продолжения работы и тестирования сервиса, необходимо заполнить БД. Для этого в приложении Docker можно выбрать запущенный сервис backend-postgres и зайти в его консоль.  

### Команды для заполнения БД:

1. Создание глобального администратора (логин - admin@hse.ru, пароль - test):  
```
 insert into app_user values (1, 'admin@hse.ru', TRUE, 'test', 'test', FALSE, 'HSE', '$2a$12$jW0tS3C1zY5m6Nj5E9dTde.ZLOWAEK5uOUc2bHIOS/zBM9M/KGtBe', 'test', 'test', 'ADMIN_GLOBAL', '12345'); 
 ```
2. Создание локального администратора (логин - admin_local@hse.ru, пароль - test):  
```
 insert into app_user values (2, 'admin_local@hse.ru', TRUE, 'test', 'test', FALSE, 'HSE', '$2a$12$g4cW6fy45lZ6ubpCba1bP.EW2R4Gr8RHG/NlRjLlPim2UpeItVx9e', 'test', 'test', 'ADMIN_LOCAL', '12345');  
 ```
3. Создание клиента (необходимо по протоколу OAuth2):  
```
 insert into client values (1, 'client_secret_basic', 'client', '$2a$12$.eUgn7RDCYBcPVjHe3XuveK.nC.t0lthC9J9WPA6yzl8ENdduBHhy', 'authorization_code', 'http://127.0.0.1:3000/redirect', 'openid'); 
 ``` 
4. Создание организации:  
```
 insert into organization values (1,  'ул. Покровка, д. 1', 'test test test', 'admin_local@hse.ru', 'HSE', '1234567');  
 ```

