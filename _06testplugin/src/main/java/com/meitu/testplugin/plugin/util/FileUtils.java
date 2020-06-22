package com.meitu.testplugin.plugin.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;

import com.meitu.testplugin.plugin.core.PluginHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Created by chidehang on 2019-11-12
 */
public class FileUtils {

    private static final int BUF_SIZE = 4092;

    public static void copyFileFromAssets(Context context, String fileName, File destFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File temp = new File(destFile.getAbsolutePath() + ".temp");
            in = context.getApplicationContext().getAssets().open(fileName);
            out = new FileOutputStream(temp);
            byte[] buffer = new byte[BUF_SIZE];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            temp.renameTo(destFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeQuietly(in);
                closeQuietly(out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFileFromZip(AssetManager assetManager, String zipPath, String targetName) {
        String result = "";
        InputStream is = null;
        try {
            is = assetManager.open(zipPath);
            result = readFileFromZip(is, targetName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(is);
        }
        return result;
    }

    public static String readFileFromZip(String zipPath, String targetName) {
        String result = "";
        InputStream is = null;
        try {
            File file = new File(zipPath);
            if (!file.exists()) {
                return result;
            }
            is = new FileInputStream(file);
            result = readFileFromZip(is, targetName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(is);
        }
        return result;
    }

    public static String readFileFromZip(InputStream inputStream, String targetName) {
        String result = "";
        ZipInputStream zis = null;
        ByteArrayOutputStream baos = null;
        try {
            zis = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            // 遍历zip压缩包文件，直到找到需要读取的文件
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(targetName)) {
                    byte[] buffer = new byte[BUF_SIZE];
                    int len = 0;
                    baos = new ByteArrayOutputStream();
                    while ((len = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    if (baos.size() > 0) {
                        // 读取到内容
                        result = baos.toString();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(zis);
            closeQuietly(baos);
        }
        return result;
    }

    public static boolean mkDirs(String path) {
        File file = new File(path);
        return !file.exists() && file.mkdirs();
    }

    public static boolean exists(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void delete(File file) {
        try {
            File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
            file.renameTo(to);
            to.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileNameWithoutSuffix(File file) {
        try {
            if (file.exists() && file.isFile()) {
                String name = file.getName();
                return name.substring(0, name.lastIndexOf("."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void copyNativeLib(File apk, Context context, PackageInfo packageInfo, File nativeLibDir) throws Exception {
        ZipFile zipfile = new ZipFile(apk.getAbsolutePath());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (String cpuArch : Build.SUPPORTED_ABIS) {
                    if (findAndCopyNativeLib(zipfile, context, cpuArch, packageInfo, nativeLibDir)) {
                        return;
                    }
                }

            } else {
                if (findAndCopyNativeLib(zipfile, context, Build.CPU_ABI, packageInfo, nativeLibDir)) {
                    return;
                }
            }

            findAndCopyNativeLib(zipfile, context, "armeabi", packageInfo, nativeLibDir);

        } finally {
            zipfile.close();
        }
    }

    private static boolean findAndCopyNativeLib(ZipFile zipfile, Context context, String cpuArch, PackageInfo packageInfo, File nativeLibDir) throws Exception {
        boolean findLib = false;
        boolean findSo = false;
        byte buffer[] = null;
        String libPrefix = "lib/" + cpuArch + "/";
        ZipEntry entry;
        Enumeration e = zipfile.entries();

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();

            if (entryName.charAt(0) < 'l') {
                continue;
            }
            if (entryName.charAt(0) > 'l') {
                break;
            }
            if (!findLib && !entryName.startsWith("lib/")) {
                continue;
            }
            findLib = true;
            if (!entryName.endsWith(".so") || !entryName.startsWith(libPrefix)) {
                continue;
            }

            if (buffer == null) {
                findSo = true;
                buffer = new byte[8192];
            }

            String libName = entryName.substring(entryName.lastIndexOf('/') + 1);
            File libFile = new File(nativeLibDir, libName);
            String key = packageInfo.packageName + "_" + libName;
            if (libFile.exists()) {
                int VersionCode = getSoVersion(context, key);
                if (VersionCode == packageInfo.versionCode) {
                    continue;
                }
            }
            FileOutputStream fos = new FileOutputStream(libFile);
            copySo(buffer, zipfile.getInputStream(entry), fos);
            setSoVersion(context, key, packageInfo.versionCode);
        }

        if (!findLib) {
            return true;
        }

        return findSo;
    }

    private static void copySo(byte[] buffer, InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;

        while ((count = bufferedInput.read(buffer)) > 0) {
            bufferedOutput.write(buffer, 0, count);
        }
        bufferedOutput.flush();
        closeQuietly(bufferedOutput);
        closeQuietly(output);
        closeQuietly(bufferedInput);
        closeQuietly(input);
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final String FILE_NAME = "VirtualAPK_Settings";

    public static void setSoVersion(Context context, String name, int version) {
        SharedPreferences preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, version);
        editor.commit();
    }

    public static int getSoVersion(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(name, 0);
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     * @throws IOException
     */
    public static File createSDFile(String fileName) throws IOException {
        createDir();
        File file = new File(PluginHelper.getPluginStorageDir() + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 在SD卡上创建目录
     * @return 文件目录
     */
    public static File createDir(){
        File dir = new File(PluginHelper.getPluginStorageDir());
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


    public static String is2String(InputStream is) throws IOException {

        //连接后，创建一个输入流来读取response
        BufferedReader bufferedReader = new BufferedReader(new
                InputStreamReader(is,"utf-8"));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        String response = "";
        //每次读取一行，若非空则添加至 stringBuilder
        while((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }
        //读取所有的数据后，赋值给 response
        response = stringBuilder.toString().trim();
        return response;
    }

    /**
     * 获取文件md5
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
