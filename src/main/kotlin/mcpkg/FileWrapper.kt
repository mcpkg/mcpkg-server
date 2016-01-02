package org.mcpkg.server

import java.io.File;

abstract class FileRoot {
    abstract fun get(path:String) : FileWrapper;
}
abstract class FileWrapper {
    abstract fun list() : Array<String>;
    abstract fun get(path:String) : FileWrapper;
    abstract fun readBytes() : ByteArray;
    abstract fun readText() : String;
    abstract fun renameTo(target: FileWrapper);
}

class FileRootNative(rootdir: File) : FileRoot() {
    val rootdir = rootdir;

    override fun get(path:String) : FileWrapper {
        return FileWrapperNative(this,File(rootdir,path));
    }
}
class FileWrapperNative(root:FileRootNative, path:File): FileWrapper() {
    val root = root;
    val internal = path;

    override fun list() : Array<String> {
        return internal.list();
    }
    override fun get(path:String) : FileWrapper {
        return FileWrapperNative(root,File(internal,path));
    }
    override fun readBytes() : ByteArray {
        return internal.readBytes();
    }
    override fun renameTo(target: FileWrapper) {
        internal.renameTo((target as FileWrapperNative).internal);
    }
    override fun readText() : String {
        return internal.readText();
    }
}