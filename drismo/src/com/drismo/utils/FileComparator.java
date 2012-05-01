package com.drismo.utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<String> {

    FileController controller;

    public FileComparator(FileController controller) {
        this.controller = controller;
    }

    @Override
    public int compare(String s1, String s2) {
        File f1 = controller.getFile(s1);
        File f2 = controller.getFile(s2);

        if      (f1.lastModified() > f2.lastModified()) return -1;
        else if (f1.lastModified() < f2.lastModified()) return  1;
        else    return 0;
    }
}
