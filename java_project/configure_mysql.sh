#!/bin/bash

# Ensure the script is run with sudo/root privileges
if [ "$EUID" -ne 0 ]; then
    echo "ERROR: Please run this script with sudo:"
    echo "sudo ./configure_mysql.sh"
    exit 1
fi

echo "1. Creating database 'student_db' and importing tables..."
mysql < schema.sql

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to import schema.sql. Is MySQL server running?"
    exit 1
fi

echo "2. Creating dedicated MySQL user 'sms_user'..."
mysql -e "CREATE USER IF NOT EXISTS 'sms_user'@'localhost' IDENTIFIED BY 'sms_password';"
mysql -e "GRANT ALL PRIVILEGES ON student_db.* TO 'sms_user'@'localhost';"
mysql -e "FLUSH PRIVILEGES;"

echo "--------------------------------------------------------"
echo "Database configuration completed successfully!"
echo "MySQL User: sms_user"
echo "MySQL Password: sms_password"
echo "--------------------------------------------------------"
