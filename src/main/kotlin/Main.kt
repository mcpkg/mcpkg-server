package org.mcpkg.server;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.*;
import java.security.MessageDigest;

data class Version(var rev:     String = "",
                   var sha256:  String = "",
                   var version: String = "");

data class InputMod(val name: String,
                    val repo: String,
                    val out:  String);

data class ModInfo(var name:     String = "",
                   var repo:     String = "",
                   var cache:    String = "",
                   var out:      String = "",
                   val versions: MutableList<Version> = ArrayList<Version>());

data class OutputList(val mods: MutableList<ModInfo> = ArrayList<ModInfo>());

fun load_package(file: FileWrapper): InputMod {
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
    println("name: ${mod.name}");
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
        val hashString = ObjectId.toString(tag.value.objectId);
        println("tag: ${tag.key} hash: ${hashString} cmd: ${cmd}");
        val proc = Runtime.getRuntime().exec(cmd);
        proc.waitFor();
        val stdoutRd = BufferedReader(InputStreamReader(proc.inputStream));
        var hash: String;
        do {
            val line = stdoutRd.readLine();
            hash = line ?: "";
            println("msg: ${line}");
        } while(line != null);
        val stderrRd = BufferedReader(InputStreamReader(proc.errorStream));
        do{
            val line = stderrRd.readLine();
            println("stderr: ${line}");
        } while(line != null);
        ver.sha256 = hash;
        info.versions.add(ver);
    }
    return info;
}

fun update_packages(dir: FileWrapper): OutputList {
    val out = OutputList();
    for(p in dir.list()) {
        var pkg = dir.get(p);
        val hasher = MessageDigest.getInstance("SHA-256");
        hasher.update(pkg.readBytes());
        val hash = "%064x".format(java.math.BigInteger(1, hasher.digest()));
        val parsed = load_package(pkg);
        val correct_name = "%s-%s.json".format(hash, parsed.name);
        if(correct_name != p) {
            val newPath = dir.get(correct_name);
            pkg.renameTo(newPath);
            pkg = newPath;
            // FIXME: use newPath
        }
        out.mods.add(updateGitCache(parsed));
    }
    return out;
}

fun main(args: Array<String>) {
    val package_source = FileRootNative(File("/tmp/packages/"));
    val package_directory = package_source.get(".");

    val result = update_packages(package_directory);
    val gson = Gson();
    try {
        val output = FileWriter(File("/tmp/output.json"));
        output.write(gson.toJson(result));
        output.close();
    } catch(e: IOException) {
        e.printStackTrace(); // FIXME: handle error properly
    }
}
