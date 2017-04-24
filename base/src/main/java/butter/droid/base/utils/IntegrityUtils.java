package butter.droid.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class IntegrityUtils {

    public static class SHA1 {

        private static final int BUFFER_SIZE = 8192;

        public static String calculate(String file) {
            return calculate(new File(file));
        }

        public static String calculate(File file) {
            final byte[] buf = new byte[BUFFER_SIZE];
            int length;
            try {
                final MessageDigest md = MessageDigest.getInstance("SHA1");

                final FileInputStream fis = new FileInputStream(file);
                final BufferedInputStream bis = new BufferedInputStream(fis);

                while ((length = bis.read(buf)) != -1) {
                    md.update(buf, 0, length);
                }

                final byte[] array = md.digest();

                final StringBuilder sb = new StringBuilder();
                for (byte anArray : array) {
                    sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
                }

                return sb.toString();
            } catch (Exception e) {
                throw new SHA1Exception(e);
            }
        }

        public static class SHA1Exception extends RuntimeException {

            public SHA1Exception(final Exception e) {
                super(e);
            }
        }
    }

    public static class Checksums {

        public static int crc32(String str) {
            final byte bytes[] = str.getBytes();
            Checksum checksum = new CRC32();
            checksum.update(bytes, 0, bytes.length);
            return (int) checksum.getValue();
        }

    }
}
