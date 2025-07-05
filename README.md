# FPL Student Attendance System

## Overview

The FPL Student Attendance System is a comprehensive solution designed to manage student attendance for educational institutions, with a focus on workshop and practical sessions. The system provides different interfaces for students, teachers, staff, and administrators, enabling efficient attendance tracking, reporting, and management.

## Key Features

### Student Features
- **Face Recognition Attendance**: Students can check in/out using facial recognition technology
- **Geolocation Verification**: System verifies student location during attendance
- **Attendance History**: Students can view their attendance records and statistics
- **Schedule Management**: Students can view their class schedules and upcoming sessions

### Teacher Features
- **Workshop Management**: Teachers can manage their workshop groups and sessions
- **Student Attendance Monitoring**: View and manage student attendance records
- **Teaching Schedule**: Access and manage teaching timetables
- **Statistics and Reporting**: Generate attendance reports and statistics

### Staff Features
- **Factory/Workshop Planning**: Staff can plan and schedule workshop sessions
- **Project Management**: Create and manage projects assigned to workshops
- **Attendance Recovery Management**: Handle makeup classes and attendance recovery
- **Student Assignment**: Assign students to appropriate workshop groups

### Admin Features
- **User Management**: Manage all user accounts (admin, staff, teachers, students)
- **Facility Management**: Configure and manage different facilities and locations
- **Subject Management**: Set up and maintain course subjects
- **System Configuration**: Configure system parameters and settings
- **Statistics Dashboard**: Access comprehensive system-wide statistics

## Technical Architecture

### Backend (Spring Boot)
- **Java 17**: Modern Java language features
- **Spring Boot 3.4.2**: Core framework for the application
- **Spring Security**: Authentication and authorization with JWT
- **Spring Data JPA**: Database access and ORM
- **Spring WebSocket**: Real-time notifications
- **OAuth2**: Integration with Google for authentication
- **Redis**: Caching layer for improved performance
- **MySQL**: Primary database

#### Key Backend Libraries
- **JJWT (0.11.5)**: JSON Web Token implementation for secure authentication
- **Apache POI (5.2.3)**: Excel file generation and parsing
- **OpenPDF (1.3.30)**: PDF document generation
- **Commons-Net (3.9.0)**: Network utilities
- **Commons-Validator (1.7)**: Data validation utilities
- **SLF4J (2.0.7)**: Logging facade
- **Spring Dotenv (4.0.0)**: Environment variable management
- **Lombok**: Reduces boilerplate code with annotations

### Frontend (Vue.js)
- **Vue 3**: Progressive JavaScript framework
- **Pinia**: State management
- **Vue Router**: Client-side routing
- **Ant Design Vue**: UI component library
- **TensorFlow.js**: Client-side face recognition processing
- **Chart.js**: Data visualization
- **Axios**: HTTP client
- **Vue Leaflet**: Maps and geolocation

#### Key Frontend Libraries
- **@tensorflow/tfjs (4.22.0)**: Machine learning in the browser
- **@vladmandic/human (3.3.5)**: Advanced face detection and recognition
- **@vue-leaflet/vue-leaflet (0.10.1)**: Interactive maps
- **ant-design-vue (4.2.6)**: UI component framework
- **axios (1.7.9)**: HTTP client
- **chart.js (3.4.1)**: Data visualization
- **dayjs (1.11.13)**: Date manipulation
- **exceljs (4.4.0)**: Excel file generation
- **file-saver (2.0.5)**: Client-side file saving
- **jspdf (3.0.1)** & **jspdf-autotable (5.0.2)**: PDF generation
- **jwt-decode (4.0.0)**: JWT token parsing
- **leaflet (1.9.4)** & **leaflet-control-geocoder (3.1.0)**: Mapping and geocoding
- **vue3-toastify (0.2.8)**: Toast notifications

### Data Analytics & Reporting
- **Power BI**: Advanced data visualization and analytics dashboard
  - Custom connectors to export data from MySQL database
  - Automated report generation and distribution
  - Interactive dashboards for administrators and teachers
  - Attendance trend analysis and prediction
- **Scheduled Exports Excel/PDF**: Automated data extraction for external analysis
  - Daily/weekly/monthly attendance reports
  - Student performance correlation analysis
  - Facility utilization metrics

### DevOps & Infrastructure
- **Docker**: Containerization for consistent deployment
- **Nginx**: Web server and reverse proxy
- **Redis**: In-memory data store for caching
- **MySQL**: Relational database
- **Linux Ubuntu 2022**: VPS hosting environment
- **Cloudflare**: SSL/TLS security and CDN services
- **GitHub Actions**: CI/CD pipeline automation
- **Prometheus & Grafana**: System monitoring and alerting
- **ELK Stack**: Log aggregation and analysis

### Mobile APP
- **ReactNative** : Modern language for mobile app
- **Progressive Web App (PWA)**: Mobile-optimized web interface
- **Responsive Design**: Adapts to different screen sizes
- **Camera API Integration**: Native camera access for face recognition
- **Push Notifications**: Native mobile notifications
- **Low-bandwidth Mode**: Optimized for slower connections

## Project Structure

### Backend Structure
```
Backend/
├── src/main/
│   ├── java/udpm/hn/studentattendance/
│   │   ├── core/                           # Core business logic modules
│   │   │   ├── admin/                      # Admin module
│   │   │   │   ├── facility/               # Facility management
│   │   │   │   ├── levelproject/           # Project level management
│   │   │   │   ├── semester/               # Semester management
│   │   │   │   ├── statistics/             # Admin statistics
│   │   │   │   ├── subject/                # Subject management
│   │   │   │   └── useradmin/              # Admin user management
│   │   │   ├── authentication/             # Authentication module
│   │   │   ├── notification/               # Notification system
│   │   │   ├── staff/                      # Staff module
│   │   │   │   ├── attendancerecovery/     # Attendance recovery management
│   │   │   │   ├── factory/                # Factory management
│   │   │   │   ├── plan/                   # Planning management
│   │   │   │   ├── project/                # Project management
│   │   │   │   └── student/                # Student management by staff
│   │   │   ├── student/                    # Student module
│   │   │   │   ├── attendance/             # Student attendance
│   │   │   │   ├── historyattendance/      # Attendance history
│   │   │   │   └── schedule/               # Student schedules
│   │   │   └── teacher/                    # Teacher module
│   │   │       ├── factory/                # Factory management by teachers
│   │   │       ├── statistics/             # Teacher statistics
│   │   │       └── studentattendance/      # Student attendance management
│   │   ├── entities/                       # Database entities
│   │   │   └── base/                       # Base entity classes
│   │   ├── helpers/                        # Helper utilities
│   │   ├── infrastructure/                 # Infrastructure components
│   │   │   ├── common/                     # Common utilities
│   │   │   ├── config/                     # Configuration classes
│   │   │   ├── constants/                  # Constants and enums
│   │   │   ├── exception/                  # Exception handling
│   │   │   ├── redis/                      # Redis configuration and services
│   │   │   └── security/                   # Security configuration
│   │   ├── repositories/                   # Data repositories
│   │   └── utils/                          # Utility classes
│   └── resources/
│       ├── application.properties          # Application configuration
│       └── templates/                      # Email templates
└── build.gradle                            # Gradle build configuration
```

### Frontend Structure
```
Frontend/
├── public/                                 # Public assets
│   └── models/                             # Face recognition models
├── src/
│   ├── assets/                             # Static assets
│   │   ├── css/                            # Stylesheets
│   │   ├── images/                         # Image assets
│   │   └── js/                             # JavaScript utilities
│   ├── components/                         # Reusable Vue components
│   │   ├── charts/                         # Chart components
│   │   ├── excel/                          # Excel import/export
│   │   ├── layout/                         # Layout components
│   │   └── widgets/                        # Widget components
│   ├── config/                             # Configuration files
│   │   └── locales/                        # Localization
│   ├── constants/                          # Application constants
│   ├── router/                             # Vue Router configuration
│   ├── services/                           # API services
│   ├── stores/                             # Pinia stores
│   ├── utils/                              # Utility functions
│   ├── views/                              # Vue views/pages
│   │   ├── layout/                         # Layout templates
│   │   └── pages/                          # Application pages
│   │       ├── admin/                      # Admin pages
│   │       ├── authentication/             # Authentication pages
│   │       ├── staff/                      # Staff pages
│   │       ├── student/                    # Student pages
│   │       └── teacher/                    # Teacher pages
│   ├── App.vue                             # Root component
│   └── main.js                             # Application entry point
└── package.json                            # NPM configuration
```

### Data Analytics Structure
```
Analytics/
├── PowerBI/                                # Power BI reports and dashboards
│   ├── AttendanceAnalytics.pbix            # Main attendance analytics dashboard
│   ├── FacilityUtilization.pbix            # Facility usage analytics
└── Exports/                                # Scheduled data exports
    ├── daily/                              # Daily export scripts
    ├── weekly/                             # Weekly export scripts
    └── monthly/                            # Monthly export scripts
```

### Machine Learning Model Training Structure
```
ML/
├── face_recognition/                       # Face recognition model training
│   ├── data_preparation/                   # Data preparation scripts
│   │   ├── augmentation.py                 # Data augmentation
│   │   └── preprocessing.py                # Image preprocessing
│   ├── training/                           # Model training scripts
│   │   ├── mask_detector.py                # Mask detection model training
│   │   └── glasses_detector.py             # Glasses detection model training
│   └── evaluation/                         # Model evaluation scripts
│       ├── accuracy_test.py                # Accuracy testing
│       └── benchmark.py                    # Performance benchmarking
├── datasets/                               # Training datasets
│   ├── faces/                              # Face images
│   ├── masks/                              # Mask/no-mask images
│   └── glasses/                            # Glasses/no-glasses images
└── models/                                 # Trained model outputs
    ├── mask_model/                         # Mask detection model files
    ├── glasses_model/                      # Glasses detection model files
    └── conversion/                         # Model conversion scripts
        └── tf_to_tfjs.py                   # TensorFlow to TensorFlow.js converter
```

## System Architecture

The application follows a modern microservices-inspired architecture:

1. **Core Modules**:
   - Authentication
   - Student Management
   - Teacher Management
   - Staff Management
   - Admin Management
   - Notification System

2. **Infrastructure Layer**:
   - Common utilities
   - Security configurations
   - Redis caching
   - Exception handling
   - WebSocket configuration

3. **Data Layer**:
   - Entities
   - Repositories
   - Data Transfer Objects (DTOs)

## Database Schema

The system uses a relational database with the following key entities:

### Core Entities
- **UserStudent**: Student information including face embedding data
- **UserStaff**: Staff member information
- **UserTeacher**: Teacher information
- **UserAdmin**: Administrator information
- **Facility**: Physical locations where classes take place
- **Subject**: Academic subjects
- **Semester**: Academic terms

### Workshop & Attendance Entities
- **Factory**: Workshop groups that students are assigned to
- **Project**: Projects that factories work on
- **Plan**: Planning periods for workshops
- **PlanFactory**: Association between plans and factories
- **PlanDate**: Specific dates and times for workshop sessions
- **Attendance**: Individual student attendance records
- **AttendanceRecovery**: Records for makeup sessions

### Relationships
- Students are assigned to Factories through UserStudentFactory
- Factories are associated with Plans through PlanFactory
- PlanDates are created for each session within a PlanFactory
- Attendance records link Students to specific PlanDates
- Each entity inherits from PrimaryEntity which provides:
  - UUID-based primary keys
  - Status tracking (ACTIVE/INACTIVE)
  - Audit fields (createdAt, updatedAt)

## Advanced Features

### Face Recognition System

The system implements a sophisticated face recognition system for secure and efficient attendance tracking:

#### Technology Stack
- **TensorFlow.js**: Client-side machine learning framework
- **Human.js (@vladmandic/human)**: Advanced face detection and recognition library
- **Face-api.js**: Face landmark detection and face descriptor extraction
- **Python** & **OpenCV**: Server-side image processing and model training

#### Auxiliary Models
- **Mask Detection Model**: Custom TensorFlow.js model to detect if a student is wearing a face mask
  - Located at: `/models/maskes/model.json`
  - Input: 224x224 RGB image
  - Output: Binary classification (mask/no mask)
  - Accuracy: ~95% on test dataset
  - Cached in IndexedDB as `maskes-model`
  - Training dataset: 2,000 images (1,000 with mask, 1,000 without)
  - Architecture: MobileNetV2 with custom classification head

- **Glasses Detection Model**: Custom TensorFlow.js model to detect if a student is wearing glasses
  - Located at: `/models/glasses/model.json`
  - Input: 224x224 RGB image
  - Output: Binary classification (glasses/no glasses)
  - Accuracy: ~92% on test dataset
  - Cached in IndexedDB as `glasses-model`
  - Training dataset: 2,000 images (1,000 with glasses, 1,000 without)
  - Architecture: MobileNetV2 with custom classification head

- **Anti-Spoofing Model**: Dedicated model for detecting presentation attacks 
  - Located at: `/models/antispoof/model.json`
  - Input: 224x224 RGB image
  - Output: Probability score for real face vs. spoofing attempt
  - Accuracy: ~97% on test dataset
  - Cached in IndexedDB as `antispoof-model`
  - Training dataset: 2,000 images (real faces, photos, screens, masks)
  - Architecture: EfficientNet-lite with attention mechanism

- **Human.js Core Models**:
  - **Face Detection**: Multiple models available including:
    - `blazeface.json`: Lightweight face detector optimized for web
    - `blazeface-front.json`: Front-facing optimized detector
    - `blazeface-back.json`: Back camera optimized detector
  - **Face Mesh**: Advanced facial landmark detection
    - `facemesh.json`: 68 landmark points for precise facial feature tracking
    - `facemesh-attention.json`: Enhanced version with attention mechanism
    - `facemesh-attention-pinto.json`: Higher accuracy version
  - **Face Recognition**: Advanced embedding models
    - `mobilefacenet.json`: Generates 1024-dimensional face descriptors
    - `mobileface.json`: Lightweight version for mobile devices
    - `faceres.json`: High-resolution face recognition model
    - `faceres-deep.json`: Deep model for maximum accuracy
  - **Emotion Recognition**: 
    - `emotion.json`: Classifies 7 basic emotions (neutral, happy, sad, angry, fear, disgust, surprise)
    - `affectnet-mobilenet.json`: Advanced emotion model trained on AffectNet dataset
  - **Additional Analysis**:
    - `age.json` & `gender.json`: Age and gender estimation
    - `liveness.json`: Advanced liveness detection
    - `gear.json`: Detects face accessories (glasses, masks, etc.)
    - `iris.json`: Precise iris tracking for gaze estimation
  - **Pose Detection**:
    - Multiple models available for body pose analysis including `blazepose-lite.json`, `blazepose-full.json`, and `blazepose-heavy.json`

#### Model Training Process
1. **Data Collection**: Gathering diverse face datasets with different angles, lighting conditions, and demographics
2. **Data Preprocessing**:
   - Image normalization and standardization
   - Face alignment using facial landmarks
   - Data augmentation (rotation, brightness, contrast adjustments)
3. **Model Architecture Selection**:
   - MobileNetV2 backbone for efficient mobile execution
   - Custom classification heads for specific tasks
4. **Training Process**:
   - Transfer learning from pre-trained models
   - Fine-tuning on domain-specific datasets
   - Hyperparameter optimization
5. **Model Evaluation**:
   - Cross-validation on diverse test sets
   - Confusion matrix analysis
   - ROC curve and AUC metrics
6. **Model Conversion**:
   - Converting TensorFlow models to TensorFlow.js format
   - Quantization for size reduction
   - Optimization for browser execution
7. **Deployment**:
   - Model hosting on CDN
   - Progressive loading strategy
   - Browser caching via IndexedDB

#### Face Recognition Process
1. **Registration**: Students register their face by capturing multiple angles
2. **Face Embedding**: System generates a mathematical representation (embedding) of the face
3. **Storage**: Face embeddings are securely stored in the database
4. **Verification**: During attendance, new face captures are compared against stored embeddings
5. **Threshold Matching**: Configurable similarity threshold determines successful matches

#### Security Measures
- **Liveness Detection**: Prevents spoofing attempts using photos or videos
- **Multiple Angle Verification**: Requires successful matching from different angles
- **Embedding Encryption**: Face data is stored as encrypted embeddings, not images
- **Configurable Thresholds**: Different thresholds for registration vs. attendance
- **Blink Detection**: Verifies that the user blinks during the registration process
- **Movement Analysis**: Tracks subtle changes in face position to ensure a live person
- **Lighting Analysis**: Checks for balanced lighting and sufficient brightness

#### Face Registration Workflow
1. **Model Loading**: Loads all required models with fallback to different backends (WebGPU, WebGL, CPU)
2. **Multi-Angle Capture**: Guides the user through a series of face positions:
   - Straight facing
   - 15° right turn
   - 15° left turn
   - Final straight facing verification
3. **Quality Checks**:
   - Brightness level verification (80-180 average pixel value)
   - Balanced lighting check (comparing left/right face halves)
   - No strong emotions detection (happiness, anger, surprise, disgust)
   - No glasses or masks detection
4. **Anti-Spoofing**: Analyzes real/live scores and movement patterns
5. **Face Descriptor Generation**: Creates normalized 1024-dimensional face embedding vectors
6. **Verification**: Ensures consistency between initial and final face captures

#### Face Verification Workflow
1. **Simplified Capture**: Only requires straight-facing position
2. **Same Quality and Anti-Spoofing Checks**: All security measures still apply
3. **Server-Side Verification**: Compares submitted embedding with stored embeddings using cosine similarity
4. **Threshold-Based Decision**: Configurable threshold determines acceptance

### Geolocation Verification

The system uses geolocation to verify student presence at the correct location:

#### Features
- **Facility Geofencing**: Define geographic boundaries for each facility
- **IP Address Verification**: Optional verification of facility network connection
- **Location Accuracy**: Configurable accuracy requirements based on facility size
- **Hybrid Verification**: Combines GPS and IP verification for enhanced security

#### Implementation
- **Browser Geolocation API**: Accesses device GPS or network-based location
- **Vue Leaflet**: Interactive maps for facility location management
- **Geofencing Logic**: Server-side verification of student location within facility bounds
- **Fallback Mechanisms**: Alternative verification methods when location services are unavailable

### Attendance Recovery System

The system includes a comprehensive attendance recovery mechanism:

- **Recovery Planning**: Staff can create recovery sessions for missed classes
- **Student Assignment**: Automatic or manual assignment of students to recovery sessions
- **Tracking**: Complete tracking of original absences and recovery attendance
- **Reporting**: Comprehensive reporting on recovery attendance rates

### Real-time Notifications

The system provides real-time notifications for various events:

- **WebSocket Integration**: Instant updates for attendance status changes
- **Push Notifications**: Browser notifications for important events
- **Email Alerts**: Automated emails for critical notifications
- **Dashboard Updates**: Real-time dashboard updates for administrators and teachers

### Email Notification System

The system includes a comprehensive email notification service for important alerts and updates:

#### Technology Stack
- **Spring Mail**: Core email sending functionality
- **Thymeleaf**: HTML email template engine
- **Jakarta Mail API**: Standard JavaMail implementation
- **HTML/CSS**: Responsive email templates

#### Email Types
- **Attendance Confirmations**: Sent to students after successful check-in/out
- **Absence Notifications**: Alerts for students and teachers about absences
- **Recovery Session Invitations**: Notifications for makeup sessions
- **Status Change Alerts**: Notifications when attendance status changes
- **System Alerts**: Administrative notifications for system events
- **Weekly Reports**: Scheduled summary reports for teachers and staff

#### Implementation
- **Template-based**: All emails use Thymeleaf templates for consistent styling
- **Queued Processing**: Asynchronous email sending to prevent blocking operations
- **Retry Mechanism**: Failed emails are automatically retried
- **Tracking**: Email delivery status tracking
- **Customization**: Templates include personalization tokens
- **Localization**: Multi-language support for all email templates
- **Attachment Support**: PDF and Excel attachments for reports

#### Configuration
- **SMTP Settings**: Configurable SMTP server connection
- **Rate Limiting**: Controls on email sending frequency
- **Scheduled Delivery**: Options for delayed or scheduled email delivery
- **Environment-specific**: Different configurations for development, testing, and production

### Automated Reporting

The system generates various reports automatically:

- **Daily Attendance Reports**: Summary of daily attendance statistics
- **Weekly Performance Reports**: Weekly attendance trends and anomalies
- **Monthly Analytics**: Comprehensive monthly attendance analytics
- **Custom Reports**: User-configurable reports based on specific criteria

## Performance Optimizations

### Redis Caching Strategy

Redis is used as a high-performance caching solution to improve response time and reduce database load.

#### Key Components
- **RedisTemplateConfig**: Configuration using Lettuce Client with modern features
- **RedisService/RedisServiceImpl**: Core service providing Redis interaction methods
- **QueryCacheService**: Service for caching complex query results
- **CacheWrapper**: Wrapper class storing additional metadata for cached objects

#### Cache Types
- **Simple Key-Value**: Basic object storage with configurable TTL
- **Hash Storage**: Efficient storage for related data structures
- **Query Results**: Caching complex database queries for performance

#### Advanced Features
- **JSON Serialization**: Automatic conversion between Java objects and JSON
- **Flexible TTL**: Different expiration times based on data type
- **Pattern-based Operations**: Bulk operations using key patterns
- **Null Caching**: Support for caching null values to prevent cache misses

#### Cache Key Patterns
- **Schedule**: `schedule:list:{userId}:{requestParams}`
- **Statistics**: `statistics:{userId}:{semesterId}`
- **Factory**: `factory:{userId}:{requestParams}`
- **Student**: `student:{facilityId}:{requestParams}`
- **User**: `user:{userId}`
- **Token**: `token:{tokenValue}`
- **Session**: `session:{sessionId}`
- **Subject**: `admin:subject:{id}`
- **Facility**: `admin:facility:{id}`
- **Query**: `query:{queryIdentifier}`

#### TTL (Time To Live) Strategy
- **Frequently changing data**: 30 minutes - 1 hour
- **Less frequently changing data**: 2 - 4 hours
- **Reference data**: 6 - 12 hours
- **User information**: 24 hours
- **Statistics**: 3 - 6 hours

> For detailed information about Redis caching implementation in Vietnamese, see [REDIS-CACHE.md](REDIS-CACHE.md)

### Face Recognition Optimization
- **Client-side Processing**: All face processing happens in the browser
- **Model Caching**: Models stored in IndexedDB for faster loading
- **Optimized Backends**: Automatic selection between WebGPU, WebGL, and CPU
- **Frame Skipping**: Processing only every 10th frame to reduce CPU load
- **Progressive Loading**: Core models loaded first, auxiliary models loaded as needed
- **Tensor Cleanup**: Proper disposal of TensorFlow tensors to prevent memory leaks
- **Model Quantization**: 8-bit quantization for smaller model size and faster inference
- **Selective Feature Computation**: Only computing necessary features based on context
- **Adaptive Resolution**: Dynamically adjusting input resolution based on device capabilities
- **WebWorker Offloading**: Heavy computations run in separate threads

### Testing

The system includes a comprehensive test suite to ensure functionality and reliability. The tests are built using JUnit 5 and Mockito frameworks.

#### Testing Architecture

- **Test Framework**: JUnit 5 with Mockito for mocking
- **Test Database**: H2 in-memory database
- **Profiles**: Uses Spring's `test` profile with dedicated configuration

#### Test Structure

The test code follows a structure that mirrors the main application:

```
Backend/src/test/
├── java/udpm/hn/studentattendance/
│   ├── core/                           # Tests for core modules
│   │   ├── admin/                      # Tests for admin module
│   │   ├── authentication/             # Tests for authentication module
│   │   │   └── services/               # Tests for services
│   │   │       └── impl/               # Tests for service implementations
│   │   ├── notification/               # Tests for notification module
│   │   ├── staff/                      # Tests for staff module
│   │   ├── student/                    # Tests for student module
│   │   └── teacher/                    # Tests for teacher module
│   └── StudentAttendanceApplicationTests.java  # Application startup test
└── resources/
    └── application-test.properties     # Test environment configuration
```

Each test class follows naming conventions:
- Unit tests: `ClassNameTest.java`
- Integration tests: `ClassNameIntegrationTest.java`
- Repository tests: `RepositoryNameRepositoryTest.java`
- Controller tests: `ControllerNameControllerTest.java`

#### Types of Tests

##### Unit Tests
- Focus on testing individual components in isolation
- Mock all dependencies using Mockito
- Test business logic, validation rules, and edge cases
- Example: `AuthenticationServiceImplTest` verifies authentication logic

##### Integration Tests
- Test the interaction between components
- Use Spring's `@SpringBootTest` for loading the application context
- Verify proper database operations and service coordination
- Example: `StudentAttendanceApplicationTests` verifies context loading

#### Test Configuration

The test environment uses a dedicated configuration:

```properties
# Test Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# JPA Configuration for Tests
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Disable Redis for testing
spring.cache.type=none
```

#### Running Tests

##### Running All Tests

```bash
# From the project root directory
cd Backend
./gradlew test
```

##### Running Specific Tests

```bash
# Run tests in a specific class
./gradlew test --tests "udpm.hn.studentattendance.core.authentication.services.impl.AuthenticationServiceImplTest"

# Run tests with a specific tag
./gradlew test --tests "*IntegrationTest"
```

##### Test Reports

After running tests, reports are available at:
- HTML Reports: `Backend/build/reports/tests/test/index.html`
- XML Reports: `Backend/build/test-results/test/`

#### Test Coverage

The project aims for high test coverage, particularly for critical components:
- Authentication services: 90%+ coverage
- Core business logic: 85%+ coverage
- API endpoints: 80%+ coverage

To generate coverage reports:

```bash
./gradlew jacocoTestReport
```

Coverage reports will be available at `Backend/build/reports/jacoco/test/html/index.html`

### Mobile Optimization
- **Responsive UI**: Adapts to different screen sizes and orientations
- **Lazy Loading**: Components and assets loaded only when needed
- **Image Optimization**: Compressed images and responsive sizing
- **Network-aware Loading**: Adapts to connection quality
- **Battery-aware Processing**: Reduces computational load on low battery
- **Touch-optimized UI**: Large touch targets and intuitive gestures
- **Minimal Data Transfer**: Optimized API requests to reduce bandwidth usage

### Database Optimizations
- **Indexing Strategy**: Carefully designed indexes for common queries
- **Query Optimization**: Optimized SQL queries for performance
- **Connection Pooling**: Efficient database connection management
- **Batch Processing**: Batch operations for bulk data handling

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Different permissions for students, teachers, staff, and admins
- **OAuth2 Integration**: Login with Google accounts
- **HTTPS**: Secure communication via Cloudflare SSL/TLS
- **Input Validation**: Protection against injection attacks
- **CORS Configuration**: Controlled cross-origin resource sharing
- **Rate Limiting**: Protection against brute force attacks
- **IP Filtering**: Geolocation-based access restrictions
- **Data Encryption**: Sensitive data encrypted at rest and in transit
- **Audit Logging**: Comprehensive logging of security-relevant events
- **Regular Security Audits**: Scheduled security reviews and penetration testing
- **Secure Password Policies**: Enforced password complexity and rotation

## Monitoring and Observability

### System Monitoring
- **Prometheus**: Metrics collection and alerting
- **Grafana**: Visualization of system metrics
- **Health Checks**: Regular service health verification
- **Alerting**: Automated alerts for system issues

### Application Monitoring
- **Error Tracking**: Centralized error logging and analysis
- **Performance Metrics**: Response time and throughput monitoring
- **User Activity**: Tracking of user interactions and sessions
- **Resource Usage**: CPU, memory, and network utilization tracking

### Log Management
- **Centralized Logging**: All logs collected in a central repository
- **Log Analysis**: Automated analysis of log patterns
- **Anomaly Detection**: Identification of unusual system behavior
- **Audit Trail**: Complete record of system activities

## Deployment Architecture

The application is deployed using Docker Compose with the following services:

### Services
- **attendance-db**: MySQL 8.0 database server
  - Configured with optimized InnoDB parameters
  - Persistent volume for data storage
  - Health checks to ensure availability

- **attendance-fe**: Frontend Nginx server
  - Serves the Vue.js application
  - Configured with security headers
  - Optimized static asset caching
  - Reverse proxy for API requests

- **attendance-be**: Backend Spring Boot application
  - Java 17 runtime
  - Configurable via environment variables
  - Optimized JVM settings
  - Health checks for monitoring

- **attendance-redis**: Redis 7.2 cache server
  - Password protection
  - Persistence configuration
  - Memory optimization

### Networking
- All services connected via internal Docker network
- Exposed ports:
  - 80: Frontend web interface
  - 8765: Backend API (optional, can be internal only)
  - 3306: MySQL (optional, for development)
  - 6379: Redis (optional, for development)

### Resource Limits
- CPU and memory limits for each container
- Resource reservations to ensure minimum performance

## Getting Started

### Prerequisites
- Java 17+
- Node.js 16+
- Docker and Docker Compose
- MySQL 8+
- Redis

### Environment Setup
1. Clone the repository
2. Configure environment variables in `.env` file
3. Start the database and Redis using Docker Compose
4. Build and run the backend
5. Install frontend dependencies and run the development server

### Configuration
The system is highly configurable through environment variables:
- Database connection parameters
- Redis settings
- Authentication secrets
- Email configuration
- Face recognition thresholds
- Geolocation settings

## Deployment

The application can be deployed using Docker Compose:
```
docker-compose up -d
```

This will start:
- MySQL database
- Redis cache
- Backend Spring Boot application
- Frontend Nginx server
- Mobile App React native

## Contributing

Please read the contribution guidelines before submitting pull requests.

## Version Control and CI/CD

### Branching Strategy
- **main**: Production branch - contains stable, released code
- **dev**: Main development branch where features are integrated
- **dev-{username}**: Personal development branches for individual contributors
- **feature/**: Feature branches for specific functionality development
- **bugfix/**: Branches for bug fixes
- **hotfix/**: Urgent fixes that go directly to production

### GitHub Actions Workflow
The project uses GitHub Actions for continuous integration and deployment:

1. **Build and Test**:
   - Triggers on pull requests to dev and main branches
   - Builds the application and runs unit tests
   - Validates code quality with Qodana

2. **Docker Image Build**:
   - Builds Docker images using buildx for cross-platform compatibility
   - Publishes to Docker Hub with appropriate tags
   - Uses Makefile commands for consistent build processes

3. **Deployment**:
   - Automatically deploys to staging environment from dev branch
   - Manual approval required for production deployment
   - Uses SSH to connect to the VPS and update Docker containers

### Code Quality Tools
- **Qodana**: Static code analysis for Java code
- **ESLint**: JavaScript/Vue.js linting
- **Prettier**: Code formatting

## Development Environment Setup

### Prerequisites
- Java 17+
- Node.js 16+
- Docker and Docker Compose
- MySQL 8+
- Redis

### Local Setup Steps
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/fpl-student-attendance.git
   cd fpl-student-attendance
   ```

2. Create a `.env` file in the project root with the following variables:
   ```
   # Database
   MYSQL_ROOT_PASSWORD=your_root_password
   MYSQL_USER=your_db_user
   MYSQL_PASSWORD=your_db_password
   
   # Server
   SERVER_ALLOWED_ORIGIN=http://localhost:3000
   
   # Authentication
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   GOOGLE_SCOPE=email,profile
   AUTHENTICATION_SECRET_KEY=your_jwt_secret_key
   
   # Mail
   MAILER_USERNAME=your_email@gmail.com
   MAILER_PASSWORD=your_app_password
   
   # Redis
   REDIS_PASSWORD=your_redis_password
   
   # Face Recognition
   APP_CONFIG_FACE_THRESHOLD_CHECKIN=0.5
   APP_CONFIG_FACE_THRESHOLD_REGISTER=0.65
   
   # Admin Generator
   DB_USER_NAME=Admin User
   DB_USER_CODE=ADMIN
   DB_USER_EMAIL=admin@example.com
   DB_FACILITY_NAME=Main Campus
   
   # Logging
   LOG_PATH=./logs
   LOG_FILE=attendance.log
   LOG_PATTERN=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n
   ```

3. Start the backend:
   ```bash
   cd Backend
   ./gradlew bootRun
   ```

4. Start the frontend:
   ```bash
   cd Frontend
   npm install
   npm run dev
   ```

5. For development with Docker:
   ```bash
   docker-compose -f docker-compose.dev.yml up
   ```

### IDE Configuration
- **IntelliJ IDEA**: Recommended for backend development
  - Enable annotation processing for Lombok
  - Set Java compiler to 17
- **VS Code**: Recommended for frontend development
  - Install Volar extension for Vue.js
  - Install ESLint and Prettier extensions

## License

This project is proprietary software of FPL Education.


