package com.app.cyclejournal.export.csv

import org.junit.Assert.*
import org.junit.Test

class CsvExportHelperTest {

    @Test
    fun testCsvEscaping_plainStringRemainsUnquoted() {
        assertEquals("SimpleText", CsvExportHelper.escapeCsv("SimpleText"))
        assertEquals("36.50", CsvExportHelper.escapeCsv("36.50"))
    }

    @Test
    fun testCsvEscaping_commasAndQuotes() {
        // String with comma
        assertEquals("\"pelvis, lower back\"", CsvExportHelper.escapeCsv("pelvis, lower back"))

        // String with internal quote
        assertEquals("\"Severe \"\"cramp\"\" feeling\"", CsvExportHelper.escapeCsv("Severe \"cramp\" feeling"))

        // String with newline
        assertEquals("\"Line1\nLine2\"", CsvExportHelper.escapeCsv("Line1\nLine2"))
    }
}
