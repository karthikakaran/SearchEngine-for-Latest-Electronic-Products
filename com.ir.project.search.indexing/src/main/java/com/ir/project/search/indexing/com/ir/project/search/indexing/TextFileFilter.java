package com.ir.project.search.indexing.com.ir.project.search.indexing;

import java.io.File;
import java.io.FileFilter;

public class TextFileFilter implements FileFilter {

   public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().startsWith("doc");
   }
}