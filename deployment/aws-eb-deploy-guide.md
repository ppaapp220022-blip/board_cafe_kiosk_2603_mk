# AWS Elastic Beanstalk Deployment Guide

## 1. Architecture

- Deploy the Spring Boot app to Elastic Beanstalk.
- Run MariaDB and PGVector on a separate EC2 instance with Docker Compose.
- Keep Elastic Beanstalk and the DB EC2 instance in the same VPC.

## 2. EC2 Docker DB setup

### Install Docker on Amazon Linux 2023

```bash
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user
```

Reconnect with SSH and verify:

```bash
docker ps
docker compose version
```

### Copy the DB files

Create a working directory on the EC2 instance:

```bash
mkdir -p ~/boardwave-db/mariadb-init
```

Copy these files from the repository:

- `deployment/docker-db/docker-compose.yml`
- `deployment/docker-db/.env.example` -> rename to `.env`
- `src/main/resources/sql/MariaDB/01_init.sql`
- `src/main/resources/sql/MariaDB/02_dummy.sql`

Place the SQL files in:

```text
~/boardwave-db/mariadb-init/01_init.sql
~/boardwave-db/mariadb-init/02_dummy.sql
```

### Start the DB containers

```bash
cd ~/boardwave-db
cp .env.example .env
docker compose up -d
docker ps
```

### Enable the PGVector extension

```bash
docker exec -it boardwave-pgvector psql -U postgres -d board_cafe_kiosk_2603
```

Then run:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## 3. Security groups

Use two security groups.

### Elastic Beanstalk instance security group

- Inbound `80` from `0.0.0.0/0`
- Inbound `443` from `0.0.0.0/0` if using HTTPS

### DB EC2 security group

- Inbound `22` from your public IP
- Inbound `3306` from the Elastic Beanstalk security group
- Inbound `5432` from the Elastic Beanstalk security group

Do not open `3306` or `5432` to `0.0.0.0/0`.

## 4. Elastic Beanstalk environment properties

Replace `172.31.12.34` with the private IP of the DB EC2 instance.

```text
MARIADB_URL=jdbc:mariadb://172.31.12.34:3306/board_cafe_kiosk_2603
MARIADB_USERNAME=boardwave
MARIADB_PASSWORD=change-app-password

PGVECTOR_URL=jdbc:postgresql://172.31.12.34:5432/board_cafe_kiosk_2603
PGVECTOR_USERNAME=postgres
PGVECTOR_PASSWORD=change-postgres-password

OPENAI_API_KEY=replace-me
SPRING_MAIL_USERNAME=replace-me
SPRING_MAIL_PASSWORD=replace-me
MYAPP_MAIL_FROM=replace-me
MYAPP_MAIL_FROM_NAME=BOARD_WAVE_SYSTEM

TOSS_PAYMENTS_SECRET_KEY=replace-me
TOSS_PAYMENTS_CLIENT_KEY=replace-me

PORTFOLIO_SUPER_KEY_ID=super
PORTFOLIO_SUPER_KEY_OTP=369369
PORTFOLIO_SUPER_KEY_TEMP_PASSWD=abcd1234!

SPRING_BATCH_JDBC_INITIALIZE_SCHEMA=never
SPRING_SERVLET_MULTIPART_LOCATION=/var/app/current/upload
MY_UPLOAD_PATH=/var/app/current/upload
```

## 5. GitHub repository secrets

Set these repository secrets before running the deployment workflow:

```text
AWS_REGION
AWS_ROLE_ARN
EB_APPLICATION_NAME
EB_ENVIRONMENT_NAME
```

## 6. Deployment workflow

The repository includes:

- `.github/workflows/deploy-eb.yml`
- `Procfile`
- `.platform/hooks/predeploy/01-create-upload-dir.sh`

The workflow:

1. Builds the app with Gradle
2. Runs tests
3. Creates `deploy.zip`
4. Uses GitHub OIDC to assume the AWS role
5. Deploys to Elastic Beanstalk

## 7. Notes

- Elastic Beanstalk should not use `localhost` for DB connections.
- The DB host must be reachable from the Elastic Beanstalk instances.
- Keep the Docker volumes attached so DB data survives container restarts.
- If the environment fails on startup, inspect the Elastic Beanstalk logs first.
