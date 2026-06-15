#!/bin/bash

# Create necessary directories
echo "Creating directory structure..."
mkdir -p lib bin src/com/sms/model src/com/sms/util src/com/sms/dao src/com/sms/view

# Download FlatLaf JAR for modern UI styling
if [ ! -f lib/flatlaf-3.5.1.jar ]; then
    echo "Downloading FlatLaf Look and Feel Library..."
    curl -L -o lib/flatlaf-3.5.1.jar https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.1/flatlaf-3.5.1.jar || \
    wget -O lib/flatlaf-3.5.1.jar https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.1/flatlaf-3.5.1.jar
fi

# Download MySQL Connector/J JAR for JDBC
if [ ! -f lib/mysql-connector-j-8.4.0.jar ]; then
    echo "Downloading MySQL Connector/J..."
    curl -L -o lib/mysql-connector-j-8.4.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar || \
    wget -O lib/mysql-connector-j-8.4.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar
fi

# Compile the source files recursively
echo "Compiling Java source files..."
javac -d bin -cp "lib/*" src/com/sms/*/*.java 2>/dev/null

if [ $? -eq 0 ]; then
    if ls src/com/sms/*/*.java 1> /dev/null 2>&1; then
        echo "Compilation successful! Run application with './start.sh'"
    else
        echo "Directories set up. Add Java source files before compiling."
    fi
else
    echo "Setup finished. Compilation skipped (Java source files might not be present yet)."
fi
