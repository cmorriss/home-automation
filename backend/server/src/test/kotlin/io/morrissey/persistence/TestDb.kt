package io.morrissey.persistence

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.nio.file.Files
import java.util.*

class TestDb : HomeDao by HomeH2DB("jdbc:h2:file:$TEST_DB_DIR/tmp_${Random().nextInt(100000)}") {
    companion object {
        private const val TEST_DB_DIR = "/tmp/test-dbs"
        private val dbDir = File(TEST_DB_DIR)

        @BeforeAll
        @JvmStatic
        fun setup() {
            if (dbDir.exists()) {
                deleteDir(dbDir)
            }
            dbDir.mkdirs()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            deleteDir(File("/tmp/test-dbs"))
        }

        private fun deleteDir(file: File) {
            val contents = file.listFiles()
            if (contents != null) {
                for (f in contents) {
                    if (!Files.isSymbolicLink(f.toPath())) {
                        deleteDir(f)
                    }
                }
            }
            file.delete()
        }
    }

}