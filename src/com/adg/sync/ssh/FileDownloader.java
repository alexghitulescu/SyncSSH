package com.adg.sync.ssh;

import com.adg.sync.ssh.util.Logger;
import com.jcraft.jsch.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by adg on 10/06/2015.
 */
public class FileDownloader {

    public static boolean downloadFile(Session session, String rFile, String relativePath, String localFolder) throws Exception {
        if (relativePath == null) {
            relativePath = "";
        }
        relativePath = relativePath.trim();

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + rFile;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            //System.out.println("filesize="+filesize+", file="+file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            String localPath = localFolder + "/" + (relativePath.isEmpty() ? "" : relativePath + "/") + file;
            // read a content of lfile
            FileOutputStream fos = new FileOutputStream(localPath);
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // error
                    break;
                }
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }
            fos.close();

            if (checkAck(in) != 0) {
                Logger.logError("invalid ack for file " + rFile);
                return false;
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }
        return true;
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
