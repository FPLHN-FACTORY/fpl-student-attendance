-- Test Database Schema
-- Create tables for testing

-- User Student table
CREATE TABLE IF NOT EXISTS "user_student" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "email" VARCHAR(255),
    "name" VARCHAR(255),
    "code" VARCHAR(255),
    "image" TEXT,
    "face_embedding" TEXT,
    "id_facility" VARCHAR(255)
);

-- User Staff table
CREATE TABLE IF NOT EXISTS "user_staff" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "email_fe" VARCHAR(255),
    "email_fpt" VARCHAR(255),
    "name" VARCHAR(255),
    "code" VARCHAR(255),
    "image" TEXT
);

-- User Admin table
CREATE TABLE IF NOT EXISTS "user_admin" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "email" VARCHAR(255),
    "name" VARCHAR(255),
    "code" VARCHAR(255),
    "image" TEXT
);

-- Facility table
CREATE TABLE IF NOT EXISTS "facility" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "code" VARCHAR(255),
    "name" VARCHAR(255),
    "position" INTEGER
);

-- Role table
CREATE TABLE IF NOT EXISTS "role" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "code" VARCHAR(255),
    "id_facility" VARCHAR(255),
    "id_user_staff" VARCHAR(255)
);

-- Subject table
CREATE TABLE IF NOT EXISTS "subject" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "code" VARCHAR(255),
    "name" VARCHAR(255)
);

-- Semester table
CREATE TABLE IF NOT EXISTS "semester" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "code" VARCHAR(255),
    "name" VARCHAR(255),
    "from_date" BIGINT,
    "to_date" BIGINT,
    "year" INTEGER
);

-- Project table
CREATE TABLE IF NOT EXISTS "project" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "name" VARCHAR(255),
    "description" TEXT,
    "id_level_project" VARCHAR(255),
    "id_subject_facility" VARCHAR(255),
    "id_semester" VARCHAR(255)
);

-- Plan table
CREATE TABLE IF NOT EXISTS "plan" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "name" VARCHAR(255),
    "description" TEXT,
    "from_date" BIGINT,
    "to_date" BIGINT,
    "max_late_arrival" INTEGER,
    "id_project" VARCHAR(255)
);

-- Plan Date table
CREATE TABLE IF NOT EXISTS "plan_date" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "description" TEXT,
    "start_date" BIGINT,
    "end_date" BIGINT,
    "shift" VARCHAR(255),
    "late_arrival" INTEGER,
    "link" TEXT,
    "room" VARCHAR(255),
    "type" INTEGER,
    "required_location" INTEGER,
    "required_ip" INTEGER,
    "required_checkin" INTEGER,
    "required_checkout" INTEGER,
    "id_plan_factory" VARCHAR(255)
);

-- Notification table
CREATE TABLE IF NOT EXISTS "notification" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_user" VARCHAR(255),
    "type" INTEGER,
    "data" TEXT
);

-- Settings table
CREATE TABLE IF NOT EXISTS "settings" (
    "key" VARCHAR(255) PRIMARY KEY,
    "value" TEXT
);

-- Subject Facility table
CREATE TABLE IF NOT EXISTS "subject_facility" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_facility" VARCHAR(255),
    "id_subject" VARCHAR(255)
);

-- Level Project table
CREATE TABLE IF NOT EXISTS "level_project" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "code" VARCHAR(255),
    "name" VARCHAR(255),
    "description" TEXT
);

-- User Student Factory table
CREATE TABLE IF NOT EXISTS "user_student_factory" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_factory" VARCHAR(255),
    "id_user_student" VARCHAR(255)
);

-- User Activity Log table
CREATE TABLE IF NOT EXISTS "user_activity_log" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_user" VARCHAR(255),
    "role" VARCHAR(255),
    "message" TEXT,
    "id_facility" VARCHAR(255)
);

-- Import Log table
CREATE TABLE IF NOT EXISTS "import_log" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_user" VARCHAR(255),
    "code" VARCHAR(255),
    "file_name" VARCHAR(255),
    "type" INTEGER,
    "id_facility" VARCHAR(255)
);

-- Import Log Detail table
CREATE TABLE IF NOT EXISTS "import_log_detail" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "line" INTEGER,
    "message" TEXT,
    "id_import_log" VARCHAR(255) NOT NULL
);

-- Attendance table
CREATE TABLE IF NOT EXISTS "attendance" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_plan_date" VARCHAR(255),
    "id_user_student" VARCHAR(255),
    "attendance_status" INTEGER,
    "late_checkin" BIGINT,
    "late_checkout" BIGINT,
    "id_attendance_recovery" VARCHAR(255)
);

-- Attendance Recovery table
CREATE TABLE IF NOT EXISTS "attendance_recovery" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "name" VARCHAR(255),
    "description" TEXT,
    "day" BIGINT,
    "total_student" INTEGER,
    "id_import_log" VARCHAR(255),
    "id_facility" VARCHAR(255)
);

-- Facility IP table
CREATE TABLE IF NOT EXISTS "facility_ip" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "type" INTEGER,
    "ip" VARCHAR(255),
    "id_facility" VARCHAR(255) NOT NULL
);

-- Factory table
CREATE TABLE IF NOT EXISTS "factory" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "name" VARCHAR(255),
    "description" TEXT,
    "id_project" VARCHAR(255),
    "id_user_staff" VARCHAR(255)
);

-- Plan Factory table
CREATE TABLE IF NOT EXISTS "plan_factory" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "id_plan" VARCHAR(255),
    "id_factory" VARCHAR(255)
);

-- Facility Location table
CREATE TABLE IF NOT EXISTS "facility_location" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "name" VARCHAR(255),
    "latitude" DOUBLE,
    "longitude" DOUBLE,
    "radius" INTEGER,
    "id_facility" VARCHAR(255) NOT NULL
);

-- Facility Shift table
CREATE TABLE IF NOT EXISTS "facility_shift" (
    "id" VARCHAR(255) PRIMARY KEY,
    "status" INTEGER,
    "createdAt" BIGINT,
    "updatedAt" BIGINT,
    "shift" INTEGER,
    "from_hour" INTEGER,
    "from_minute" INTEGER,
    "to_hour" INTEGER,
    "to_minute" INTEGER,
    "id_facility" VARCHAR(255)
); 