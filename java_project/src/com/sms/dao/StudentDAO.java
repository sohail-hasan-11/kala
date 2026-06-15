package com.sms.dao;

import com.sms.model.Student;
import com.sms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the 'students' table in the layered package architecture.
 */
public class StudentDAO {

    /**
     * Inserts a new student.
     */
    public void addStudent(Student student) throws SQLException {
        String query = "INSERT INTO students (student_id, first_name, last_name, email, phone, gender, course, gpa) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getCourse());
            ps.setDouble(8, student.getGpa());
            ps.executeUpdate();
        }
    }

    /**
     * Inserts multiple students in a single database transaction.
     */
    public void addStudentsBulk(List<Student> students) throws SQLException {
        String query = "INSERT INTO students (student_id, first_name, last_name, email, phone, gender, course, gpa) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Student s : students) {
                    ps.setString(1, s.getStudentId());
                    ps.setString(2, s.getFirstName());
                    ps.setString(3, s.getLastName());
                    ps.setString(4, s.getEmail());
                    ps.setString(5, s.getPhone());
                    ps.setString(6, s.getGender());
                    ps.setString(7, s.getCourse());
                    ps.setDouble(8, s.getGpa());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Retrieves all student records.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM students ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(extractStudentFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Updates an existing student record.
     */
    public void updateStudent(Student student) throws SQLException {
        String query = "UPDATE students SET student_id = ?, first_name = ?, last_name = ?, email = ?, phone = ?, " +
                       "gender = ?, course = ?, gpa = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getCourse());
            ps.setDouble(8, student.getGpa());
            ps.setInt(9, student.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a student record by ID.
     */
    public void deleteStudent(int id) throws SQLException {
        String query = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Searches students.
     */
    public List<Student> searchStudents(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM students WHERE " +
                       "student_id LIKE ? OR " +
                       "first_name LIKE ? OR " +
                       "last_name LIKE ? OR " +
                       "email LIKE ? OR " +
                       "course LIKE ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, searchPattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractStudentFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Gets next available Student ID.
     */
    public String getNextStudentId() throws SQLException {
        String query = "SELECT student_id FROM students ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String lastId = rs.getString("student_id");
                if (lastId != null && lastId.startsWith("STU-")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(4));
                        return "STU-" + (num + 1);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return "STU-1001";
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("id"),
            rs.getString("student_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("gender"),
            rs.getString("course"),
            rs.getDouble("gpa")
        );
    }
}
