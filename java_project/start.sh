#!/bin/bash

# Navigate to the project directory
cd /home/abdullah/Desktop/java_praject

# 1. Compile the Java files recursively
echo "Compiling project source files..."
javac -d bin -cp "lib/*" src/com/sms/*/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful! Starting the Student Management System..."
    # 2. Run the application
    java -cp "bin:lib/*" com.sms.view.MainFrame
else
    echo "ERROR: Compilation failed."
    exit 1
fi
