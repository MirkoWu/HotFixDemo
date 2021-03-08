package com.mirkowu.hotfixdemo.hotfix;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class SdkDexClassLoader {

    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();


    static {
        loadedDex.clear();
    }

    public SdkDexClassLoader() {

    }

    public static void loadFixedDex(Context context) {

    }

    public static void loadFixedDex(Context context, File patchFilesDir) {

        //找到热修复文件路径
        File fileDir = patchFilesDir != null ? patchFilesDir : new File(context.getFilesDir(), DEX_DIR);//// data/data/包名/files/odex（这个可以任意位置）

        Log.d("fileDir", "" + fileDir.exists());
        Log.d("fileDir", "" + fileDir.isDirectory());
        File[] listFiles = fileDir.listFiles();
        if (listFiles == null) return;
        for (File file : listFiles) {
            String name = file.getName();
            if (name.endsWith(DEX_SUFFIX)
                    || name.endsWith(APK_SUFFIX)
                    || name.endsWith(JAR_SUFFIX)
                    || name.endsWith(ZIP_SUFFIX)
            ) {
                loadedDex.add(file);//符合的文件加入集合
            }
        }

        doDexInject(context, loadedDex);
    }

    private static void doDexInject(Context context, HashSet<File> loadedDex) {
        //data/data/包名/files/optimize_dex（解压后的dex存放目录，这个必须是自己程序下的目录）
        String optimizeDir = context.getFilesDir().getAbsoluteFile() + File.separator + OPTIMIZE_DEX_DIR;

        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdir();
        }

        try {

            PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();

            for (File dex : loadedDex) {
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),//dex补丁所在目录
                        fopt.getAbsolutePath(),//存放dex的解压目录用于jar、zip、apk格式的补丁）
                        null,//加载dex时需要的库
                        pathLoader.getParent()//父类加载器
                );
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(pathLoader);
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);

                //合并 新旧 DexElements
                Object finalDexElements = combineArray(leftDexElements, rightDexElements);

                Object pathList = getPathList(pathLoader);// 一定要重新获取，不要用pathPathList，会报错
                // 重写给PathList里面的Element[] dexElements;赋值
                setField(pathList, pathList.getClass(), "dexElements", finalDexElements);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {

        }

    }

    /**
     * 通过反射 给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cls, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cls, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }


    private static Object combineArray(Object leftArrays, Object rightArrays) {
        Class<?> componentType = leftArrays.getClass().getComponentType();
        int i = Array.getLength(leftArrays);
        int j = Array.getLength(rightArrays);
        int len = i + j;
        Object result = Array.newInstance(componentType, len);
        System.arraycopy(leftArrays, 0, result, 0, i);
        System.arraycopy(rightArrays, 0, result, i, j);
        return result;

    }

    /**
     * 反射 获取到PathList 中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 反射获取到类加载器中的 pathList
     */
    private static Object getPathList(BaseDexClassLoader classLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(classLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }
}
