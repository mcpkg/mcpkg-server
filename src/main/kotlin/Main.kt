package org.mcpkg.server

import java.io.*;
import java.util.*;

import com.google.gson.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.*;
import org.eclipse.jgit.transport.*;

data class Version(var rev : String = "",
                   var sha256 : String = "",
                   var version : String = "")

data class InputMod(val name : String, val url : String, val out : String)

data class ModInfo(var name : String = "",
                   var url : String = "",
                   var cache : String = "",
                   var out : String = "",
                   val versions : MutableList<Version> = ArrayList<Version>())

data class InputList(val mods : MutableList<InputMod> = ArrayList<InputMod>())

data class OutputList(val mods : MutableList<ModInfo> = ArrayList<ModInfo>())

fun main(args: Array<String>) {
    val gson = Gson();
    var mods: InputList? = null;
    try {
        mods = gson.fromJson(FileReader(args[0]), InputList::class.java);
    } catch(e: FileNotFoundException) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    val itr = mods!!.mods.iterator();
    val out = OutputList();
    while(itr.hasNext()) {
        val mod = itr.next();
        val info = ModInfo();
        info.name = mod.name;
        info.url = mod.url;
        info.out = mod.out;
        info.cache = "/tmp/cache-" + mod.name + ".git";
        System.out.println("name:" + mod.name);
        val cachedir = File("/tmp/cache-" + mod.name + ".git");
        var git : Git? = null;
        if(cachedir.exists()) {
            val builder = FileRepositoryBuilder();
            builder.setMustExist(true);
            builder.setGitDir(cachedir);
            try {
                val repo = builder.build();
                if(repo.getObjectDatabase().exists()) {
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
            cc.setURI(mod.url);
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
        val tags = git!!.getRepository().getTags();
        val i = tags.entries.iterator();
        while(i.hasNext()) {
            val ver = Version();
            val tag = i.next();
            System.out.println("tag:" + tag.value.getObjectId());
            ver.version = tag.key;
            ver.rev = ObjectId.toString(tag.value.getObjectId());
            info.versions.add(ver);
        }
        out.mods.add(info);
    }
    try {
        val output = FileWriter(File("/tmp/output.json"));
        output.write(gson.toJson(out));
        output.close();
    } catch(e: IOException) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}
