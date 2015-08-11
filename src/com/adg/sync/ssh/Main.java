package com.adg.sync.ssh;

import com.adg.sync.ssh.util.*;


/**
 * Created by adg on 10/06/2015.
 */
public class Main {

    public static final String COMMAND_DOWN = "download";
    public static final String COMMAND_UP = "upload";
    public static final String COMMAND_PASS = "pass";
    public static final String COMMAND_HELP = "help";
    public static final String COMMAND_TEST = "test";

    public static void main(String[] args) {
        if (args.length == 0) {
            onError("Invalid arguments", 1);
            return;
        }

        switch (args[0].trim()) {
            case COMMAND_DOWN:
                if (args.length != 2) {
                    onError("invalid arguments", 1);
                    return;
                }
                process(args[1].trim(), false);
                break;
            case COMMAND_UP:
                if (args.length != 2) {
                    onError("invalid arguments", 1);
                    return;
                }
                process(args[1].trim(), true);
                break;
            case COMMAND_PASS:
                if (args.length != 2) {
                    onError("invalid arguments", 1);
                    return;
                }
                try {
                    System.out.println(AESEncrypt.encrypt(args[1].trim()));
                } catch (Exception e) {
                    e.printStackTrace();
                    onError("could not encrypt", 2);
                }
                break;
            case COMMAND_HELP:
                StringBuilder sb = new StringBuilder("Use one of the following commands: ");
                sb.append("\n").append(COMMAND_DOWN).append(" second argument is the location of the init file");
                sb.append("\n").append(COMMAND_UP).append(" second argument is the location of the init file");
                sb.append("\n").append(COMMAND_PASS).append(" second argument is the password to be encrypted");
                sb.append("\n").append(COMMAND_HELP).append("\n");
                System.out.println(sb);
                break;
            case COMMAND_TEST:
                test();
                break;
            default:
                onError("Invalid command. Use " + COMMAND_HELP + " for a list of commands.", 1);
                break;
        }

        if (Logger.getErrorCount() == 0) {
            System.exit(0);
        } else {
            System.exit(3);
        }
    }

    private static void process(String path, boolean upload) {
        Logger.resetErrorCount();
        Logger.setFolder(path);
        Worker worker = new Worker(path, upload);
        worker.sync();
        worker.close();
    }

    private static void onError(String message, int code) {
        System.out.println(message);
        System.exit(code);
    }

    private static void test() {
        String path = "C:/BuildService/Test";
        Logger.resetErrorCount();
        Logger.setFolder(path);
        Initializer initializer = new Initializer();
        initializer.init(path);

        Connector connector = new Connector(initializer);
        connector.connect();

        initializer.getActionDescriptors().stream().filter(actionDescriptor -> !actionDescriptor.isUpload()).forEach(actionDescriptor ->

                        connector.getRemoteFiles(actionDescriptor.getRemoteFolder(), "", actionDescriptor.getExtensions(), true).forEach(System.out::println)

        );

        initializer.getActionDescriptors().stream().filter(ActionDescriptor::isUpload).forEach(actionDescriptor ->

                        FileInfo.getLocalFiles(actionDescriptor.getLocalFolder(), "", actionDescriptor.getExtensions(), true).forEach(System.out::println)

        );
    }
}
