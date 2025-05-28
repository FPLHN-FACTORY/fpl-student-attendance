# Deployment Configuration

This directory contains deployment configuration files for the application.

## Files
- `Dockerfile`: Frontend Docker configuration
- `docker-compose.yml`: Docker Compose configuration
- `nginx/`: Nginx configuration files

## Usage
1. Copy these files to your VPS
2. Run `docker-compose up -d`
3. Access the application at https://fplstudentattendance.site

## Notes
- These files are not tracked in git
- Keep them secure as they contain sensitive configuration
- Update them when making changes to deployment 