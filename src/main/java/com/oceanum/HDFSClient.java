package com.oceanum;

import com.oceanum.common.Environment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HDFSClient<jsonObjectS> {
    //单文件
    public InputStream down1(String cloudPath) throws IOException, InterruptedException, URISyntaxException {
        // 1获取对象
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.10.136:8022"), conf, "root");
        FSDataInputStream in = fs.open(new Path(cloudPath));
        return in;
    }

    //多文件
    public OutputStream down2(String cloudPath) throws IOException, InterruptedException, URISyntaxException {
        // 1获取对象
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.10.136:8022"), conf, "root");
        OutputStream out = new FileOutputStream("C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\file");
        ZipOutputStream zos = new ZipOutputStream(out);
        compress(cloudPath, zos, fs);
        zos.close();
        return out;
    }

    public  void compress(String baseDir, ZipOutputStream zipOutputStream, FileSystem fs) throws IOException {

        FileStatus[] fileStatulist = fs.listStatus(new Path(baseDir));
        //
        System.out.println("basedir = " + baseDir);
        String[] strs = baseDir.split("/");
        String lastName = strs[strs.length - 1];

        //zos = new ZipOutputStream(new FileOutputStream(zipFile));
        for (int i = 0; i < fileStatulist.length; i++) {

            String name = fileStatulist[i].getPath().toString().substring(25);
            name = name.substring(name.indexOf("/"+lastName));

            if (fileStatulist[i].isFile()) {
                Path path = fileStatulist[i].getPath();
                FSDataInputStream inputStream = fs.open(path);
                System.out.println("fileStatulist[i].getPath().getName()" + fileStatulist[i].getPath().getName());
                zipOutputStream.putNextEntry(new ZipEntry( name));
                IOUtils.copyBytes(inputStream, zipOutputStream, 1024);
                inputStream.close();// Folder2 1.m3p

            } else {
                System.out.println(fileStatulist[i].getPath().getName() + "/");
                zipOutputStream.putNextEntry(new ZipEntry(name + "/"));
                System.out.println("fileStatulist[i].getPath().toString().substring(25) = " + fileStatulist[i].getPath().toString().substring(25));
                compress(fileStatulist[i].getPath().toString().substring(25), zipOutputStream, fs);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new HDFSClient<>().down2("/tmp/chenmingkun").flush();
    }
}