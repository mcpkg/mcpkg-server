package org.mcpkg.server;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.*;
import org.wasabi.app.AppServer
import java.security.MessageDigest;
import kotlin.text.Regex


data class Repo(val name: String, val url: String);

data class InputMod(val name: String,
                    val repo: String,
                    val out:  String,
                    val skipped_versions: List<String>?);

data class ModInfo(var name:     String = "",
                   var repo:     String = "",
                   var cache:    String = "",
                   var out:      String = "",
                   val versions: MutableList<Version> = ArrayList<Version>());

data class OutputList(val mods: MutableList<ModInfo> = ArrayList<ModInfo>());

fun loadPackage(file: FileWrapper): InputMod {
    val gson = Gson();
    val obj = gson.fromJson(file.readText(), InputMod::class.java);
    return obj;
}

fun updateGitCache(db:DatabaseInterface, mod: InputMod): ModInfo {
    val info = ModInfo();
    info.name = mod.name;
    info.repo = mod.repo;
    info.out = mod.out;
    info.cache = "/tmp/cache-${mod.name}.git";
    println("name: ${mod.name}"); // DEBUG
    val cachedir = File(info.cache);
    var git: Git? = null;
    if(cachedir.exists()) {
        val builder = FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.setGitDir(cachedir);
        try {
            val repo = builder.build();
            if(repo.objectDatabase.exists()) {
                git = Git(repo);
                val fetcher = git.fetch();
                fetcher.call();
            } else {
                // FIXME: directory is not a valid git, delete and re-clone?
            }
        } catch(e: IOException) {
            e.printStackTrace(); // FIXME: handle error properly
        } catch(e: InvalidRemoteException) {
            e.printStackTrace(); // FIXME: handle error properly
        } catch(e: TransportException) {
            e.printStackTrace(); // FIXME: handle error properly
        } catch(e: GitAPIException) {
            e.printStackTrace(); // FIXME: handle error properly
        }
    } else {
        val cc = CloneCommand();
        cc.setBare(true);
        cc.setDirectory(cachedir);
        cc.setURI(mod.repo);
        try {
            git = cc.call();
        } catch(e: InvalidRemoteException) {
            e.printStackTrace(); // FIXME: handle error properly
        } catch(e: TransportException) {
            e.printStackTrace(); // FIXME: handle error properly
        } catch(e: GitAPIException) {
            e.printStackTrace(); // FIXME: handle error properly
        }
    }
    val tags = git!!.repository.tags;
    val i = tags.entries.iterator();
    while(i.hasNext()) {
        val tag = i.next();

        if (mod.skipped_versions?.contains(tag.key) ?: false) continue;

        val ver = db.queryModVersion(info.name,tag.key);
        if (ver != null) {
            if (ver.rev == ObjectId.toString(tag.value.objectId)) {
                info.versions.add(ver);
                continue;
            }
            println("key: ${tag.key} cache: ${ver.rev} actual: ${ObjectId.toString(tag.value.objectId)}");
        }
        val ver2 = Version();
        ver2.version = tag.key;
        ver2.rev = ObjectId.toString(tag.value.objectId);
        parseVersionInfo(ver2, info);
        if (ver2.deps != null) {
            info.versions.add(ver2);
            db.saveVersion(info,ver2);
        }
    }
    val branches = git!!.branchList().call();
    for (branch in branches) {
        if (mod.skipped_versions?.contains(branch.name.split("/")[2]) ?: false) continue;

        val ver = db.queryModVersion(info.name, branch.name.split("/")[2]);
        if (ver != null) {
            if (ver.rev == ObjectId.toString(branch.objectId)) {
                info.versions.add(ver);
                continue;
            }
            println("key: ${branch.name} cache: ${ver.rev} actual: ${ObjectId.toString(branch.objectId)}");
        }
        val ver2 = Version();
        ver2.version = branch.name.split("/")[2];
        ver2.rev = ObjectId.toString(branch.objectId);
        parseVersionInfo(ver2,info);
        if (ver2.deps != null) {
            info.versions.add(ver2);
            db.saveVersion(info,ver2);
        }
    }
    return info;
}

fun parseVersionInfo(ver: Version, info: ModInfo) {
    val cmd = "nix-prefetch-git ${info.cache} --rev ${ver.rev}";
    println("tag:  ${ver.version}"); // DEBUG
    //println("hash: ${ObjectId.toString(tag.value.objectId)}"); // DEBUG
    //println("cmd:  ${cmd}"); // DEBUG
    val proc = Runtime.getRuntime().exec(cmd);
    proc.waitFor();
    val stdoutRd = BufferedReader(InputStreamReader(proc.inputStream));
    var hash: String? = null;
    stdoutRd.forEachLine {
        hash = it;
        println("msg: $it"); // DEBUG
    }
    val stderrRd = BufferedReader(InputStreamReader(proc.errorStream));
    stderrRd.forEachLine {
        println("stderr: $it"); // DEBUG
        if (it.startsWith("path is ")) {
            var words = it.split(" ");
            var localpath = words[2];
            //println("found path ${localpath}");
            ver.deps = parseDeps(localpath,info.name);
        }
    };
    ver.sha256 = hash ?: "";
}

fun parseDeps(localPath: String, name: String): List<Dependency>? {
//    val cmd = "gradle --init-script init.gradle -q showRepos -p $localPath --project-cache-dir /tmp/gradle-$name"
//    println("cmd is $cmd");
//    val proc = Runtime.getRuntime().exec(cmd);
//    val stdoutRd = BufferedReader(InputStreamReader(proc.inputStream));
//    var abort = false;
//    var repo_list: MutableList<Repo> = ArrayList<Repo>();
//    try {
//        stdoutRd.forEachLine {
//            println("msg: $it"); // DEBUG
//            // parses a line in the form of REPO_LIST;['test-2'='http://nixcache.localnet/maven2', 'forge'='http://files.minecraftforge.net/maven', 'MavenRepo'='https://repo1.maven.org/maven2/', 'minecraft'='https://libraries.minecraft.net/', 'CB Maven FS'='http://chickenbones.net/maven/', 'Waila Mobius Repo'='http://mobiusstrip.eu/maven', 'FireBall API Depot'='http://dl.tsr.me/artifactory/libs-release-local', forgeFlatRepo=flat]
//            if (it.startsWith("REPO_LIST;")) {
//                var repos = it.split(";")[1].trim('[', ']').split(",");
//                for (repo in repos) {
//                    val parts = repo.trim().split("=");
//                    repo_list.add(Repo(parts[0], parts[1]));
//                }
//            }
//        }
//        val stderrRd = BufferedReader(InputStreamReader(proc.errorStream));
//        stderrRd.forEachLine {
//            println("stderr: $it"); // DEBUG
//        };
//    } catch (e: IOException) {
//    } // destroy closes the stream, causing an IOException
//    if (abort) return null;
//
//    val webserver = WebServer(repo_list);
//    webserver.start();
//    val proxy_config = """allprojects {
//    repositories {
//        maven {
//            name = "test-2"
//            url "http://localhost:%d/maven2"
//        }
//    }
//}""".format(webserver.port);
//    var proxy_file = File.createTempFile("proxy",".gradle");
//    proxy_file.writeText(proxy_config);

    val inspect_cmd = "gradle --no-daemon -p $localPath --project-cache-dir /tmp/gradle-$name dependencies";

    var inspect_proc = Runtime.getRuntime().exec(inspect_cmd);
    val exp = Regex("[^ ]+:[^ ]+:[^ ]+");
    val results: MutableSet<String> = HashSet<String>();
    BufferedReader(InputStreamReader(inspect_proc.inputStream)).forEachLine {
//        println(it);
        val res = exp.find(it);
        if (res != null) {
            results.add(res.value);
        }
    }
//    BufferedReader(InputStreamReader(inspect_proc.errorStream)).forEachLine {
//        println(it);
//    }
    val output = ArrayList<Dependency>();
    for (dep in results) {
        println("found "+dep);
        val parts = dep.split(":");
        output.add(Dependency(parts[0],parts[1],parts[2]));
    }
//    proxy_file.delete();
//    webserver.stop();

    return output;
}

class WebServer(repolist: List<Repo>) {
    var port = 0;
    val server = AppServer();
    fun start() {
        server.get("***",{
            println(request.uri);
            next();
        },{
        });
        server.start(false); // TODO, increment port and retry to co-exist with self
        port = server.configuration.port;
    }

    fun stop() {
        server.stop();
    }
}

fun updatePackages(db: DatabaseInterface, dir: FileWrapper): OutputList {
    val out = OutputList();
    for(p in dir.list()) {
        var pkg = dir.get(p);
        val hasher = MessageDigest.getInstance("SHA-256");
        hasher.update(pkg.readBytes());
        val hash = "%064x".format(java.math.BigInteger(1, hasher.digest()));
        val parsed = loadPackage(pkg);
        val correctName = "%s-%s.json".format(hash, parsed.name);
        if(correctName != p) {
            val newPath = dir.get(correctName);
            pkg.renameTo(newPath);
            pkg = newPath;
            // FIXME: use newPath
        }
        out.mods.add(updateGitCache(db,parsed));
    }
    return out;
}

fun main(args: Array<String>) {
    val packageSource = FileRootNative(File("/tmp/packages/"));
    val packageDirectory = packageSource.get(".");
    val database:DatabaseInterface = SqliteMcpkgDatabase();

    val result = updatePackages(database,packageDirectory);
    val gson = Gson();
    try {
        val output = FileWriter(File("/tmp/output.json"));
        output.write(gson.toJson(result));
        output.close();
    } catch(e: IOException) {
        e.printStackTrace(); // FIXME: handle error properly
    }
}
