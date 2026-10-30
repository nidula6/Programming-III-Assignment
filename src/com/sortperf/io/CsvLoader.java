package com.sortperf.io;

import com.sortperf.model.Dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a comma separated file into a {@link Dataset}.
 * Supports quoted fields, escaped quotes (""), embedded commas/new lines and a UTF-8 BOM.
 */
public final class CsvLoader {

    private CsvLoader() {
    }

    public static Dataset load(File file) throws CsvLoadException {
        validate(file);

        List<String[]> records;
        try (PushbackReader reader = new PushbackReader(new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), 1 << 16), 2)) {
            records = parse(reader);
        } catch (IOException e) {
            throw new CsvLoadException("The file could not be read: " + e.getMessage(), e);
        }

        if (records.isEmpty()) {
            throw new CsvLoadException("The file contains no data.");
        }

        List<String> headers = buildHeaders(records.get(0));
        int columns = headers.size();

        List<String[]> rows = new ArrayList<>(records.size() - 1);
        for (int i = 1; i < records.size(); i++) {
            rows.add(normalise(records.get(i), columns));
        }
        if (rows.isEmpty()) {
            throw new CsvLoadException("The file has a header row but no data rows.");
        }
        return new Dataset(file.getName(), headers, rows);
    }

    private static void validate(File file) throws CsvLoadException {
        if (file == null || !file.exists()) {
            throw new CsvLoadException("The selected file does not exist.");
        }
        if (!file.isFile()) {
            throw new CsvLoadException("The selection is not a file.");
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new CsvLoadException("Invalid file type. Please select a .csv file.");
        }
        if (!file.canRead()) {
            throw new CsvLoadException("The file cannot be read (permission denied).");
        }
        if (file.length() == 0) {
            throw new CsvLoadException("The file is empty.");
        }
    }

    private static List<String> buildHeaders(String[] firstRecord) {
        List<String> headers = new ArrayList<>(firstRecord.length);
        for (int i = 0; i < firstRecord.length; i++) {
            String name = firstRecord[i].trim();
            headers.add(name.isEmpty() ? "Column " + (i + 1) : name);
        }
        return headers;
    }

    /** Pads short rows with blanks and trims over-long rows so every row matches the header. */
    private static String[] normalise(String[] record, int columns) {
        String[] row = new String[columns];
        for (int i = 0; i < columns; i++) {
            row[i] = i < record.length ? record[i] : "";
        }
        return row;
    }

    /** Character-level CSV state machine. */
    private static List<String[]> parse(PushbackReader in) throws IOException, CsvLoadException {
        List<String[]> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean firstChar = true;

        int c;
        while ((c = in.read()) != -1) {
            char ch = (char) c;

            if (firstChar) {
                firstChar = false;
                if (ch == '\uFEFF') { // skip UTF-8 byte order mark
                    continue;
                }
            }

            if (inQuotes) {
                if (ch == '"') {
                    int next = in.read();
                    if (next == '"') {
                        field.append('"'); // escaped quote
                    } else {
                        inQuotes = false;
                        if (next != -1) {
                            in.unread(next);
                        }
                    }
                } else {
                    field.append(ch);
                }
                continue;
            }

            if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (ch == '\r') {
                int next = in.read();
                if (next != '\n' && next != -1) {
                    in.unread(next);
                }
                endRecord(records, fields, field);
            } else if (ch == '\n') {
                endRecord(records, fields, field);
            } else {
                field.append(ch);
            }
        }

        if (inQuotes) {
            throw new CsvLoadException("Malformed CSV: a quoted value is never closed.");
        }
        if (field.length() > 0 || !fields.isEmpty()) {
            endRecord(records, fields, field);
        }
        return records;
    }

    private static void endRecord(List<String[]> records, List<String> fields, StringBuilder field) {
        fields.add(field.toString());
        field.setLength(0);
        boolean blankLine = fields.size() == 1 && fields.get(0).trim().isEmpty();
        if (!blankLine) {
            records.add(fields.toArray(new String[0]));
        }
        fields.clear();
    }
}
