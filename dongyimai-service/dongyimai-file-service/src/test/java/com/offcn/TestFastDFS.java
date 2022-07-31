package com.offcn;

import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.IOException;

public class TestFastDFS {
    public static void main(String[] args) throws IOException, MyException {
        ClientGlobal.init("B:\\system\\dongyimai-parent\\dongyimai-service\\dongyimai-file-service\\src\\main\\resources\\fdfs_client.conf");
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = null;
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        String[] strings = storageClient.upload_file("C:\\Users\\rog\\Pictures\\touxiang.jpg", "jpg",
                null);
        for (String string : strings) {
            System.out.println(string);
        }
    }
}