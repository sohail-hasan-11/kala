package com.sms.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class to export JTable data into standard CSV format.
 */
public class CSVExporter {

    /**
     * Exports the JTable rows and headers to a CSV file.
     * @param table JTable source
     * @param file Output file destination
     * @throws IOException if write fails
     */
    public static void exportToCSV(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            
            // 1. Write Header Row (Skipping the hidden internal ID column at index 0)
            boolean firstCol = true;
            for (int j = 0; j < model.getColumnCount(); j++) {
                String colName = model.getColumnName(j);
                if (colName.equalsIgnoreCase("ID") || colName.equalsIgnoreCase("Record ID")) {
                    continue; // Skip database key
                }
                if (!firstCol) {
                    bw.write(",");
                }
                bw.write(escapeCSV(colName));
                firstCol = false;
            }
            bw.newLine();

            // 2. Write Data Rows
            for (int i = 0; i < model.getRowCount(); i++) {
                firstCol = true;
                for (int j = 0; j < model.getColumnCount(); j++) {
                    String colName = model.getColumnName(j);
                    if (colName.equalsIgnoreCase("ID") || colName.equalsIgnoreCase("Record ID")) {
                        continue;
                    }
                    if (!firstCol) {
                        bw.write(",");
                    }
                    Object value = model.getValueAt(i, j);
                    bw.write(value != null ? escapeCSV(value.toString()) : "");
                    firstCol = false;
                }
                bw.newLine();
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
