package org.mcpkg.server;

import org.sqlite.JDBC;
import java.sql.*
import java.util.*

/**
 * Created by clever on 11/28/15.
 */

data class Dependency(var groupId: String = "", var artifactId: String = "", var version: String = "");
data class Version(var rev:     String = "",
                   var sha256:  String = "",
                   var version: String = "",
                   var deps:    List<Dependency>? = ArrayList<Dependency>());

abstract class DatabaseInterface {
    abstract fun queryModVersion(modName:String, version: String) : Version?;

    abstract fun saveVersion(info: ModInfo, ver: Version): Boolean;
}

class SqliteMcpkgDatabase : DatabaseInterface() {
    val database_version = 1;

    private val queryVersionStatement: PreparedStatement by lazy {
        connection.prepareStatement("SELECT rev,sha256,version FROM versions WHERE modslug = ? AND version = ?")
    }
    private val insertVersionStatement: PreparedStatement by lazy {
        connection.prepareStatement("INSERT OR REPLACE INTO versions (rev,sha256,version,modslug) VALUES (?,?,?,?)");
    }
    private val setVersion: PreparedStatement by lazy {
        connection.prepareStatement("INSERT OR REPLACE INTO config (key,value) VALUES ('version',?)");
    }
    override fun queryModVersion(modName: String, version: String): Version? {
        val query = queryVersionStatement;
        query.setString(1,modName);
        query.setString(2,version);
        val result = query.executeQuery();
        if (result.next()) {
            val ver = Version();
            ver.rev = result.getString("rev");
            ver.sha256 = result.getString("sha256");
            ver.version = result.getString("version");
            return ver;
        }
        return null;
    }
    override fun saveVersion(info: ModInfo, ver: Version): Boolean {
        val query = insertVersionStatement;
        query.setString(1,ver.rev);
        query.setString(2,ver.sha256);
        query.setString(3,ver.version);
        query.setString(4,info.name);
        return query.execute();
    }

    val connection: Connection;
    init {
        //connection = JDBC.createConnection("test.db", Properties());
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
        val query1 = connection.createStatement();
        try {
            query1.execute("CREATE TABLE IF NOT EXISTS config (key,value)");
            query1.execute("CREATE UNIQUE INDEX IF NOT EXISTS config_key ON config(key)")
            query1.execute("CREATE UNIQUE INDEX IF NOT EXISTS version_key ON versions(modslug,version)")
            query1.execute("CREATE TABLE versions (modslug,version,rev,sha256)");
        } catch (e:SQLException) {

        }
        val result = query1.executeQuery("SELECT value FROM config WHERE key = 'version'");
        var current_version = 0;
        if (result.next()) {
            current_version = result.getInt("value");
        }
        var x = current_version;
        while (x < database_version) {
            doUpgrade(x,x+1);
            x++;
            saveVersion(x);
        }
    }
    private fun saveVersion(ver: Int) {
        val query = setVersion;
        query.setInt(1,ver);
        query.execute();
    }
    // upgrade the db schema from ver current to ver target
    private fun doUpgrade(current: Int, target: Int) {
    }
}