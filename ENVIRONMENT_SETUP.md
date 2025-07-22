# Environment Setup Guide for Student Attendance System

## Overview
This guide provides comprehensive instructions for setting up the Student Attendance System using Docker Compose.

## Prerequisites
- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB RAM available
- At least 10GB free disk space

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd fpl-student-attendance
```

### 2. Create Environment File
Create a `.env` file in the root directory with the following content:

```env
# SERVER
SERVER_PORT=8765
SERVER_ALLOWED_ORIGIN=http://localhost:8765,http://localhost:5173

# SETTINGS
APP_CONFIG_APP_NAME=Student Attendance Fpoly
APP_CONFIG_DISABLED_CHECK_EMAIL_FPT=true
APP_CONFIG_ALLOWS_ONE_TEACHER_TO_TEACH_MULTIPLE_CLASESS=false
APP_CONFIG_SHIFT_MIN_DIFF=30
APP_CONFIG_SHIFT_MAX_LATE_ARRIVAL=90

# SCHEDULE
APP_CONFIG_EMAIL_STATISTICS_ENABLED=true
APP_CONFIG_CRON_STATISTICS_EMAIL=0 30 8 * * *

# LOG
LOG_PATH=/var/log/student_attendance
LOG_FILE=app.log
LOG_PATTERN=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n

# DATABASE INFORMATION
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=student_attendance
MYSQL_USER=root
MYSQL_PASSWORD=123456aA@
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# REDIS
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_TTL=259200
REDIS_CACHE=true

# GOOGLE OAUTH INFORMATION
GOOGLE_CLIENT_ID=297963940212-0805h66ooagrpa4mon9e34kdigetu4cs.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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
GOOGLE_SCOPE=email,profile

# AUTHENTICATION SECRET KEY
AUTHENTICATION_SECRET_KEY=0805h66ooagrpa4mon9e34kdigetu4cs0805h66ooagrpa4mon9e34kdigetu4cs

# SMTP MAILER
MAILER_USERNAME=attendancestudent86@gmail.com
MAILER_PASSWORD=cvmfghyxeujepcah
MAILER_HOST=smtp.gmail.com
MAILER_PORT=587

# ADMIN_GEN
DB_GENERATOR=true
DB_USER_NAME=Nguyễn Quốc Anh
DB_USER_CODE=TH01267
DB_USER_EMAIL=quocanhn352@gmail.com
DB_FACILITY_NAME=Hà nội,Hồ chí minh
```

### 3. Create Required Directories
```bash
mkdir -p logs mysql/logs redis/logs nginx/logs nginx/ssl uploads
```

### 4. Start the Services
```bash
docker-compose up -d
```

### 5. Check Service Status
```bash
docker-compose ps
```

## Service Details

### Available Services

| Service | Port | Description | Health Check |
|---------|------|-------------|--------------|
| Frontend | 80, 443 | Vue.js application with Nginx | http://localhost/health |
| Backend | 8765 | Spring Boot API | http://localhost:8765/actuator/health |
| Database | 3306 | MySQL 8.0 | mysqladmin ping |
| Redis | 6379 | Redis Cache | redis-cli ping |
| phpMyAdmin | 8080 | Database management | http://localhost:8080 |

### Service Dependencies
```
Frontend → Backend → Database
Frontend → Backend → Redis
phpMyAdmin → Database
```

## Configuration Details

### Database Configuration
- **Database**: `student_attendance`
- **User**: `root`
- **Password**: `123456aA@`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`

### Redis Configuration
- **Memory Limit**: 256MB
- **Eviction Policy**: `allkeys-lru`
- **TTL**: 259200 seconds (3 days)

### Application Settings
- **App Name**: Student Attendance Fpoly
- **Email Check Disabled**: true
- **Max Late Arrival**: 90 minutes
- **Statistics Email**: Enabled (8:30 AM daily)
- **Face Recognition Threshold**: 0.6 (checkin), 0.7 (register)

## Environment Variables

### Required Variables
All variables have default values, but you can override them in your `.env` file:

#### Server Configuration
- `SERVER_PORT`: Backend server port (default: 8765)
- `SERVER_ALLOWED_ORIGIN`: CORS allowed origins

#### Database Configuration
- `MYSQL_USER`: Database username (default: root)
- `MYSQL_PASSWORD`: Database password (default: 123456aA@)

#### Application Settings
- `APP_CONFIG_APP_NAME`: Application name
- `APP_CONFIG_DISABLED_CHECK_EMAIL_FPT`: Disable FPT email validation
- `APP_CONFIG_SHIFT_MAX_LATE_ARRIVAL`: Maximum late arrival time (minutes)
- `APP_CONFIG_EMAIL_STATISTICS_ENABLED`: Enable email statistics
- `APP_CONFIG_CRON_STATISTICS_EMAIL`: Cron expression for statistics email

#### Authentication
- `GOOGLE_CLIENT_ID`: Google OAuth client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth client secret
- `AUTHENTICATION_SECRET_KEY`: JWT secret key

#### SMTP Configuration
- `MAILER_USERNAME`: SMTP username
- `MAILER_PASSWORD`: SMTP password

#### Admin Generator
- `DB_GENERATOR`: Enable admin user generation
- `DB_USER_NAME`: Admin user name
- `DB_USER_CODE`: Admin user code
- `DB_USER_EMAIL`: Admin user email
- `DB_FACILITY_NAME`: Facility names

## Security Features

### Container Security
- `no-new-privileges`: Prevents privilege escalation
- Resource limits: CPU and memory limits for each service
- Health checks: Automatic service health monitoring

### Database Security
- `MYSQL_DISABLE_SHOW_DATABASE`: Hides database information
- `MYSQL_SKIP_NAME_RESOLVE`: Disables DNS lookups
- Root password protection

### Network Security
- Internal network: `app-network` (172.20.0.0/16)
- Port binding: Services bound to localhost where appropriate

## Monitoring and Logging

### Log Management
- **Log Rotation**: 10MB max size, 3 files max
- **Log Locations**:
  - Application: `./logs/`
  - MySQL: `./mysql/logs/`
  - Redis: `./redis/logs/`
  - Nginx: `./nginx/logs/`

### Health Checks
- **Backend**: `/actuator/health` endpoint
- **Frontend**: `/health` endpoint
- **Database**: `mysqladmin ping`
- **Redis**: `redis-cli ping`

## Troubleshooting

### Common Issues

#### 1. Port Conflicts
```bash
# Check if ports are in use
netstat -tulpn | grep :8765
netstat -tulpn | grep :3306
netstat -tulpn | grep :6379
```

#### 2. Database Connection Issues
```bash
# Check database logs
docker-compose logs attendance-db

# Connect to database
docker-compose exec attendance-db mysql -u root -p
```

#### 3. Backend Startup Issues
```bash
# Check backend logs
docker-compose logs attendance-be

# Check backend health
curl http://localhost:8765/actuator/health
```

#### 4. Memory Issues
```bash
# Check container resource usage
docker stats

# Restart services if needed
docker-compose restart
```

### Service Recovery
```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart attendance-be

# Rebuild and restart
docker-compose up -d --build
```

## Performance Optimization

### Resource Allocation
- **Backend**: 1 CPU, 1.5GB RAM
- **Database**: 1 CPU, 1GB RAM
- **Frontend**: 0.5 CPU, 512MB RAM
- **Redis**: 0.5 CPU, 256MB RAM

### Java Optimization
- **Heap Size**: 512MB-1024MB
- **GC**: G1GC with optimizations
- **Container Support**: Enabled

### Database Optimization
- **Buffer Pool**: 256MB
- **Log File Size**: 64MB
- **Flush Method**: O_DIRECT

## Backup and Recovery

### Database Backup
```bash
# Create backup
docker-compose exec attendance-db mysqldump -u root -p student_attendance > backup.sql

# Restore backup
docker-compose exec -T attendance-db mysql -u root -p student_attendance < backup.sql
```

### Volume Backup
```bash
# Backup volumes
docker run --rm -v attendance_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz -C /data .
docker run --rm -v redis_data:/data -v $(pwd):/backup alpine tar czf /backup/redis_backup.tar.gz -C /data .
```

## Production Deployment

### Security Checklist
- [ ] Change default passwords
- [ ] Configure SSL certificates
- [ ] Set up firewall rules
- [ ] Enable log monitoring
- [ ] Configure backup schedules
- [ ] Set up monitoring alerts

### SSL Configuration
1. Place SSL certificates in `./nginx/ssl/`
2. Update Nginx configuration
3. Enable HTTPS in frontend environment

### Scaling Considerations
- Use external database for high availability
- Implement Redis clustering for cache scaling
- Set up load balancer for multiple instances
- Configure proper monitoring and alerting

## Maintenance

### Regular Tasks
- Monitor log files for errors
- Check disk space usage
- Review resource utilization
- Update security patches
- Backup database regularly

### Updates
```bash
# Update images
docker-compose pull

# Restart with new images
docker-compose up -d
```

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review service logs
3. Verify environment configuration
4. Check resource availability
5. Contact system administrator

## Version Information
- **Docker Compose**: 3.8
- **MySQL**: 8.0
- **Redis**: 7.2-alpine
- **Backend**: Spring Boot (custom image)
- **Frontend**: Vue.js with Nginx (custom image)
- **phpMyAdmin**: Latest 