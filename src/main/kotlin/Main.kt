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

data class Dependency(var groupId: String = "", var artifactId: String = "", var version: String = "");

data class Repo(val name: String, val url: String);

data class Version(var rev:     String = "",
                   var sha256:  String = "",
                   var version: String = "",
                   var deps:    List<Dependency>? = ArrayList<Dependency>());

data class InputMod(val name: String,
                    val repo: String,
                    val out:  String);

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

fun updateGitCache(mod: InputMod): ModInfo {
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
        val ver = Version();
        val tag = i.next();
        ver.version = tag.key;
        ver.rev = ObjectId.toString(tag.value.objectId);
        val cmd = "nix-prefetch-git ${info.cache} --rev ${ver.rev}";
        println("tag:  ${tag.key}"); // DEBUG
        println("hash: ${ObjectId.toString(tag.value.objectId)}"); // DEBUG
        println("cmd:  ${cmd}"); // DEBUG
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
                println("found path ${localpath}");
                ver.deps = parseDeps(localpath,info.name);
            }
        };
        ver.sha256 = hash ?: "";
        if (ver.deps != null) {
            info.versions.add(ver);
        }
    }
    return info;
}

fun parseDeps(localPath: String, name: String): List<Dependency>? {
    val cmd = "gradle --init-script init.gradle -q showRepos -p $localPath --project-cache-dir /tmp/gradle-$name"
    println("cmd is $cmd");
    val proc = Runtime.getRuntime().exec(cmd);
    val stdoutRd = BufferedReader(InputStreamReader(proc.inputStream));
    var abort = false;
    var repo_list: MutableList<Repo> = ArrayList<Repo>();
    try {
        stdoutRd.forEachLine {
            println("msg: $it"); // DEBUG
            // parses a line in the form of REPO_LIST;['test-2'='http://nixcache.localnet/maven2', 'forge'='http://files.minecraftforge.net/maven', 'MavenRepo'='https://repo1.maven.org/maven2/', 'minecraft'='https://libraries.minecraft.net/', 'CB Maven FS'='http://chickenbones.net/maven/', 'Waila Mobius Repo'='http://mobiusstrip.eu/maven', 'FireBall API Depot'='http://dl.tsr.me/artifactory/libs-release-local', forgeFlatRepo=flat]
            if (it.startsWith("REPO_LIST;")) {
                var repos = it.split(";")[1].trim('[', ']').split(",");
                for (repo in repos) {
                    val parts = repo.trim().split("=");
                    repo_list.add(Repo(parts[0], parts[1]));
                }
            }
        }
        val stderrRd = BufferedReader(InputStreamReader(proc.errorStream));
        stderrRd.forEachLine {
            println("stderr: $it"); // DEBUG
        };
    } catch (e: IOException) {
    } // destroy closes the stream, causing an IOException
    if (abort) return null;

    val webserver = WebServer(repo_list);
    webserver.start();
    val proxy_config = """allprojects {
    repositories {
        maven {
            name = "test-2"
            url "http://localhost:%d/maven2"
        }
    }
}""".format(webserver.port);
    var proxy_file = File.createTempFile("proxy",".gradle");
    proxy_file.writeText(proxy_config);

    val inspect_cmd = "gradle --no-daemon -p $localPath --project-cache-dir /tmp/gradle-$name --init-script ${proxy_file.absolutePath} dependencies";

    var inspect_proc = Runtime.getRuntime().exec(inspect_cmd);
    BufferedReader(InputStreamReader(inspect_proc.inputStream)).forEachLine {
        println(it);
    }
    BufferedReader(InputStreamReader(inspect_proc.errorStream)).forEachLine {
        println(it);
    }
    proxy_file.delete();
    webserver.stop();

    return ArrayList<Dependency>();
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

fun updatePackages(dir: FileWrapper): OutputList {
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
        out.mods.add(updateGitCache(parsed));
    }
    return out;
}

fun main(args: Array<String>) {
    val packageSource = FileRootNative(File("/tmp/packages/"));
    val packageDirectory = packageSource.get(".");

    val result = updatePackages(packageDirectory);
    val gson = Gson();
    try {
        val output = FileWriter(File("/tmp/output.json"));
        output.write(gson.toJson(result));
        output.close();
    } catch(e: IOException) {
        e.printStackTrace(); // FIXME: handle error properly
    }
}
