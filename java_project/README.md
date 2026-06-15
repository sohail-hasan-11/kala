# Student Management System - Ubuntu Setup Guide

A complete, beautiful, and modern Student Management System built using Java 17, Swing, JDBC, MySQL, and styled with FlatLaf Dark theme.

---

## Prerequisites

Before starting, ensure your Ubuntu system is updated:
```bash
sudo apt update && sudo apt upgrade -y
```

---

## 1. Install Java Development Kit (JDK) 17

Install OpenJDK 17 on Ubuntu using the following command:
```bash
sudo apt install openjdk-17-jdk -y
```

Verify the installation:
```bash
java -version
javac -version
```
*Expected output: OpenJDK version "17.x.x".*

---

## 2. Install and Setup MySQL Server

### Step A: Install MySQL
Run the following command to install the MySQL database server:
```bash
sudo apt install mysql-server -y
```

### Step B: Start & Enable MySQL Service
Ensure the MySQL daemon is running and starts automatically on boot:
```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

### Step C: Configure MySQL User & Password
By default, MySQL on Ubuntu allows the `root` user to log in via unix_socket (without a password when run with `sudo`). 
To ensure compatibility with JDBC, log into MySQL:
```bash
sudo mysql
```

Run the following SQL commands to set a password for the `root` user or configure it to use standard password authentication:
```sql
-- Replace 'your_password' with your desired database password, or leave it empty if you want:
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
FLUSH PRIVILEGES;
EXIT;
```
*(Note: If you set a custom password, remember to update it in `src/com/sms/DBConnection.java` line 14).*

---

## 3. Initialize Database Schema

Use the provided `schema.sql` to create the database, table, constraints, and insert the sample mock data.
Run this command from the project root directory:
```bash
# If your root password is empty:
mysql -u root < schema.sql

# If you configured a password for root:
mysql -u root -p < schema.sql
```

---

## 4. Compile and Setup the Application

We use a helper script `setup.sh` which:
1. Creates the directory layout (`lib`, `bin`, `src`).
2. Downloads the necessary open-source library dependencies (**FlatLaf** for the modern visual styling, and **MySQL Connector/J** for JDBC).
3. Compiles the Java source files.

Run the script:
```bash
chmod +x setup.sh run.sh
./setup.sh
```

---

## 5. Run the Application

Once compiled, start the Student Management System:
```bash
./run.sh
```

---

## Project Structure
- `src/com/sms/Student.java` - Represents the Student entity/model.
- `src/com/sms/DBConnection.java` - Manages MySQL JDBC connectivity.
- `src/com/sms/StudentDAO.java` - Contains MySQL CRUD query implementations.
- `src/com/sms/MainFrame.java` - Handles user interaction, form validation, and displays data on JTable.
- `lib/` - Holds FlatLaf and MySQL Connector/J JARs.
- `bin/` - Holds compiled class files.
- `schema.sql` - Holds the database structure and test records.
