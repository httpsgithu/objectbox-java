/*
 * Copyright 2017 ObjectBox Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.objectbox.internal;

import io.objectbox.BoxStore;
import org.greenrobot.essentials.io.IoUtils;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

/**
 * Separate class, so we can mock BoxStore.
 */
public class NativeLibraryLoader {

    private static final String OBJECTBOX_JNI = "objectbox-jni";

    static {
        String libname = OBJECTBOX_JNI;
        String filename = libname + ".so";

        final String vendor = System.getProperty("java.vendor");
        final String osName = System.getProperty("os.name").toLowerCase();

        // Some Android devices are detected as neither Android or Linux below,
        // so assume Linux by default to always fallback to Android
        boolean isLinux = true;
        // For Android, os.name is also "Linux", so we need an extra check
        // Is not completely reliable (e.g. Vivo devices), see workaround on load failure
        // Note: can not use check for Android classes as testing frameworks (Robolectric)
        // may provide them on non-Android devices
        final boolean android = vendor.contains("Android");
        if (!android) {
            String cpuArchPostfix = "-" + getCpuArch();
            if (osName.contains("windows")) {
                isLinux = false;
                libname += "-windows" + cpuArchPostfix;
                filename = libname + ".dll";
                checkUnpackLib(filename);
            } else if (osName.contains("linux")) {
                libname += "-linux" + cpuArchPostfix;
                filename = "lib" + libname + ".so";
                checkUnpackLib(filename);
            } else if (osName.contains("mac")) {
                isLinux = false;
                libname += "-macos" + cpuArchPostfix;
                filename = "lib" + libname + ".dylib";
                checkUnpackLib(filename);
            }
        }
        try {
            File file = new File(filename);
            if (file.exists()) {
                System.load(file.getAbsolutePath());
            } else {
                try {
                    if (android) {
                        boolean success = loadLibraryAndroid();
                        if (!success) {
                            System.loadLibrary(libname);
                        }
                    } else {
                        System.err.println("File not available: " + file.getAbsolutePath());
                        System.loadLibrary(libname);
                    }
                } catch (UnsatisfiedLinkError e) {
                    if (!android && isLinux) {
                        // maybe is Android, but check failed: try loading Android lib
                        boolean success = loadLibraryAndroid();
                        if (!success) {
                            System.loadLibrary(OBJECTBOX_JNI);
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } catch (UnsatisfiedLinkError e) {
            String osArch = System.getProperty("os.arch");
            String sunArch = System.getProperty("sun.arch.data.model");
            String message = String.format(
                    "Loading ObjectBox native library failed: vendor=%s,os=%s,os.arch=%s,sun.arch=%s,android=%s,linux=%s",
                    vendor, osName, osArch, sunArch, android, isLinux
            );
            throw new LinkageError(message, e); // UnsatisfiedLinkError does not allow a cause; use its super class
        }
    }

    /**
     * Get CPU architecture of the JVM (Note: this can not be used for Android, Android decides arch on its own
     * and looks for library in appropriately named folder).
     * <p>
     * Note that this may not be the architecture of the actual hardware
     * (e.g. when running a x86 JVM on an amd64 machine).
     */
    private static String getCpuArch() {
        // See https://github.com/openjdk/jdk/blob/master/make/autoconf/platform.m4 for possible values.
        // Note: any CPU architecture starting with "arm" is reported as "arm", aarch64 is reported as "aarch64".
        String osArch = System.getProperty("os.arch");
        String cpuArch = null;
        if (osArch != null) {
            osArch = osArch.toLowerCase();
            if (osArch.equalsIgnoreCase("amd64") || osArch.equalsIgnoreCase("x86_64")) {
                cpuArch = "x64";
            } else if (osArch.equalsIgnoreCase("x86")) {
                cpuArch = "x86";
            } else if (osArch.equals("aarch64")) {
                cpuArch = "arm64";
            } else if (osArch.equals("arm")) {
                // Decide if ARMv6 or ARMv7 library should be used, need to get actual architecture from OS.
                String cpuArchOSOrNull = getCpuArchOSOrNull();
                if (cpuArchOSOrNull != null) {
                    String cpuArchOSlower = cpuArchOSOrNull.toLowerCase();
                    if (cpuArchOSlower.startsWith("armv6")) {
                        cpuArch = "armv6";
                    } else {
                        // ARMv7 or 32-bit ARMv8
                        cpuArch = "armv7";
                    }
                } else {
                    cpuArch = "armv7";
                    System.err.println("Failed to get arch from OS - ObjectBox is defaulting to " + cpuArch);
                }
            }
        }
        if (cpuArch == null) {
            String sunArch = System.getProperty("sun.arch.data.model");
            cpuArch = "32".equals(sunArch) ? "x86" : "x64";
            System.err.println("Unknown os.arch \"" + osArch + "\" - ObjectBox is defaulting to " + cpuArch);
        }
        return cpuArch;
    }

    /**
     * Get architecture using operating system tools. Currently only Linux is supported (using uname).
     */
    @Nullable
    private static String getCpuArchOSOrNull() {
        String archOrNull = null;
        try {
            // Linux
            Process exec = Runtime.getRuntime().exec("uname -m");
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            archOrNull = reader.readLine();
            reader.close();
        } catch (Exception ignored) {
        }
        return archOrNull;
    }

    private static void checkUnpackLib(String filename) {
        String path = "/native/" + filename;
        URL resource = NativeLibraryLoader.class.getResource(path);
        if (resource == null) {
            System.err.println("Not available in classpath: " + path);
        } else {
            File file = new File(filename);
            try {
                URLConnection urlConnection = resource.openConnection();
                int length = urlConnection.getContentLength();
                long lastModified = urlConnection.getLastModified();
                if (!file.exists() || file.length() != length || file.lastModified() != lastModified) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    try {
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                        try {
                            IoUtils.copyAllBytes(in, out);
                        } finally {
                            IoUtils.safeClose(out);
                        }
                    } finally {
                        IoUtils.safeClose(in);
                    }
                    if (lastModified > 0) {
                        file.setLastModified(lastModified);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean loadLibraryAndroid() {
        if (BoxStore.getContext() == null) {
            return false;
        }

        //noinspection TryWithIdenticalCatches
        try {
            Class<?> context = Class.forName("android.content.Context");
            if (BoxStore.getRelinker() == null) {
                // use default ReLinker
                Class<?> relinker = Class.forName("com.getkeepsafe.relinker.ReLinker");
                Method loadLibrary = relinker.getMethod("loadLibrary", context, String.class, String.class);
                loadLibrary.invoke(null, BoxStore.getContext(), OBJECTBOX_JNI, BoxStore.JNI_VERSION);
            } else {
                // use custom ReLinkerInstance
                Method loadLibrary = BoxStore.getRelinker().getClass().getMethod("loadLibrary", context, String.class, String.class);
                loadLibrary.invoke(BoxStore.getRelinker(), BoxStore.getContext(), OBJECTBOX_JNI, BoxStore.JNI_VERSION);
            }
        } catch (NoSuchMethodException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        } catch (InvocationTargetException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
        // note: do not catch Exception as it will swallow ReLinker exceptions useful for debugging
        // note: can't catch ReflectiveOperationException, is K+ (19+) on Android

        return true;
    }

    public static void ensureLoaded() {
    }
}
