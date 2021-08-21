package io.morrissey.iot.server.persistence

import org.jetbrains.exposed.sql.Database
import java.io.File
import java.nio.file.Files
import java.util.*

class TestDb : ApplicationDatabase {
    override fun initialize() {
        val TEST_DB_DIR = "/tmp/test-dbs"
        val dbDir = File(TEST_DB_DIR)

        if (dbDir.exists()) {
            deleteDir(dbDir)
        }
        dbDir.mkdirs()

        val dir =
            File("$TEST_DB_DIR/tmp_${Random().nextInt(100000)}") //Database.connect(url = "jdbc:h2:mem:test", driver = "org.h2.Driver")
        Database.connect(url = "jdbc:h2:file:${dir.canonicalFile.absolutePath}", driver = "org.h2.Driver")
        createProdDbTables()
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
