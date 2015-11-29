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
    private val queryVersionStatement: PreparedStatement by lazy {
        connection.prepareStatement("SELECT rev,sha256,version FROM versions WHERE modslug = ? AND version = ?")
    }
    private val insertVersionStatement: PreparedStatement by lazy {
        connection.prepareStatement("INSERT OR REPLACE INTO versions (rev,sha256,version,modslug) VALUES (?,?,?,?)");
    }
    override fun queryModVersion(modName: String, version: String): Version? {
        val query = queryVersionStatement;
        query.setString(1,modName);
        query.setString(2,version);
        val result = query.executeQuery();
        if (result.next()) {
            val version = Version();
            version.rev = result.getString("rev");
            version.sha256 = result.getString("sha256");
            version.version = result.getString("version");
            return version;
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
            query1.execute("CREATE TABLE versions (modslug,version,rev,sha256)");
        } catch (e:SQLException) {

        }
    }
}