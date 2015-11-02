package ru.rem.server.session.fileHandler;

import java.io.File;

public class FileInfo {

    public String name;
    public long size;
    public int index;

    public FileInfo(File file, int index){
        this.name = file.getName();
        this.size = file.length();
        this.index = index;
    }
    
    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public int getIndex() {
        return index;
    }
     
}
