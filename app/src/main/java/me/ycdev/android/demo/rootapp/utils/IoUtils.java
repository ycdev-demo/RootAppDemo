package me.ycdev.android.demo.rootapp.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
    private static final int IO_BUF_SIZE = 1024 * 16; // 16KB

    private IoUtils() {
    }

    /**
     * Close the closeable target and eat possible exceptions.
     * @param target The target to close. Can be null.
     */
    public static void closeQuietly(Closeable target) {
        try {
            if (target != null) {
                target.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Save the input stream into a file.</br>
     * Note: This method will not close the input stream.
     */
    public static void saveAsFile(InputStream is, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        try {
            copyStream(is, fos);
        } finally {
            closeQuietly(fos);
        }
    }

    /**
     * Copy data from the input stream to the output stream.</br>
     * Note: This method will not close the input stream and output stream.
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[IO_BUF_SIZE];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        os.flush();
    }

}
