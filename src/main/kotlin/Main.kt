package org.mcpkg.server

import java.io.*;
import java.util.*;

import com.google.gson.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.*;
import java.security.MessageDigest

data class Version(var rev : String = "",
                   var sha256 : String = "",
                   var version : String = "")

data class InputMod(val name : String, val repo : String, val out : String)

data class ModInfo(var name : String = "",
                   var repo : String = "",
                   var cache : String = "",
                   var out : String = "",
                   val versions : MutableList<Version> = ArrayList<Version>())

data class OutputList(val mods : MutableList<ModInfo> = ArrayList<ModInfo>())

fun load_package(file: FileWrapper) : InputMod {
    val gson = Gson();
    val obj = gson.fromJson(file.readText(),InputMod::class.java);
    return obj;
}

fun updateGitCache(mod: InputMod) : ModInfo {
    val info = ModInfo();
    info.name = mod.name;
    info.repo = mod.repo;
    info.out = mod.out;
    info.cache = "/tmp/cache-" + mod.name + ".git";
    System.out.println("name:" + mod.name);
    val cachedir = File(info.cache);
    var git : Git? = null;
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
                // directory is not a valid git, delete and re-clone?
            }
        } catch(e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(e: InvalidRemoteException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(e: TransportException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(e: GitAPIException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } else {
        val cc = CloneCommand();
        cc.setBare(true);
        cc.setDirectory(cachedir);
        cc.setURI(mod.repo);
        try {
            git = cc.call();
        } catch(e: InvalidRemoteException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(e: TransportException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(e: GitAPIException) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        println("tag:${tag.key} hash:${ObjectId.toString(tag.value.objectId)} cmd:${cmd}");
        val proc = Runtime.getRuntime().exec(cmd);
        proc.waitFor();
        val reader = BufferedReader(InputStreamReader(proc.inputStream));
        var hash = "";
        do{
            val line = reader.readLine();
            hash = line ?: "";
            println("msg:"+line);
        } while (line != null);
        val reader2 = BufferedReader(InputStreamReader(proc.errorStream));
        do{
            val line = reader.readLine();
            println("stderr:"+line);
        } while (line != null);
        ver.sha256 = hash;
        info.versions.add(ver);
    }
    return info;
}

fun update_packages(dir: FileWrapper) : OutputList {
    val out = OutputList();
    for (p in dir.list()) {
        var pkg = dir.get(p);
        val hasher = MessageDigest.getInstance("SHA-256");
        hasher.update(pkg.readBytes());
        val hash = "%064x".format(java.math.BigInteger(1,hasher.digest()));
        val parsed = load_package(pkg);
        val correct_name = "%s-%s.json".format(hash,parsed.name);
        if (correct_name != p) {
            val newpath = dir.get(correct_name);
            pkg.renameTo(newpath);
            pkg = newpath;
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
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}
