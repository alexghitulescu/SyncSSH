package com.adg.sync.ssh;

import com.adg.sync.ssh.util.Logger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.*;

/**
 * Created by adg on 11/06/2015.
 */
public class FileUploader {

    public static boolean uploadFile(Session session, String lFile, String fileName, String remoteFolder) {
        FileInputStream fis = null;
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand("mkdir -p " + remoteFolder);
            channelExec.connect();
            channelExec.disconnect();

            String rFile = remoteFolder + "/" + fileName;

            // exec 'scp -t rfile' remotely
            String command = "scp " + " -t " + rFile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            File _lfile = new File(lFile);

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (lFile.lastIndexOf('/') > 0) {
                command += lFile.substring(lFile.lastIndexOf('/') + 1);
            } else {
                command += lFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                return false;
            }

            // send a content of lfile
            fis = new FileInputStream(lFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                return false;
            }
            out.close();

            channel.disconnect();

        } catch (Exception e) {
            Logger.logError(e);
            try {
                if (fis != null) fis.close();
            } catch (Exception ee) {
                Logger.logError(ee);
            }
            return false;
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
            StringBuilder sb = new StringBuilder();
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
