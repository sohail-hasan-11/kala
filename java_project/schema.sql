-- Create Student Management System Database
CREATE DATABASE IF NOT EXISTS student_db;
USE student_db;

-- Drop table if it already exists
DROP TABLE IF EXISTS students;

-- Create students table
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    gender VARCHAR(15),
    course VARCHAR(100),
    gpa DOUBLE CHECK (gpa >= 0.0 AND gpa <= 4.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO students (student_id, first_name, last_name, email, phone, gender, course, gpa) VALUES
('STU-1001', 'Alice', 'Smith', 'alice.smith@example.com', '123-456-7890', 'Female', 'Computer Science', 3.8),
('STU-1002', 'Bob', 'Jones', 'bob.jones@example.com', '234-567-8901', 'Male', 'Mechanical Engineering', 3.2),
('STU-1003', 'Charlie', 'Brown', 'charlie.brown@example.com', '345-678-9012', 'Male', 'Mathematics', 3.9);
