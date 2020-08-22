package edu.youzg.multifile_cloud_transfer.sender;

import edu.youzg.multifile_cloud_transfer.receive.ResourceFilePool;
import edu.youzg.network_transmission.core.FileReadWrite;
import edu.youzg.network_transmission.core.FileSectionSendReceive;
import edu.youzg.network_transmission.section.FileSectionInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * 发送 文件片段列表
 */
public class FileSender implements Runnable {
    private String receiveIp;   // 接收端ip
    private int receivePort;    // 接收端port
    private ResourceFilePool rscFilePool;   // 当前资源的所有文件
    private List<FileSectionInfo> sectionList;  // 文件片段信息 列表
    private FileSectionSendReceive fileSectionSend; // 网络发送文件片段 工具

    public FileSender(String receiveIp, int receivePort, ResourceFilePool rscFilePool, List<FileSectionInfo> sectionList) {
        this.receiveIp = receiveIp;
        this.receivePort = receivePort;
        this.rscFilePool = rscFilePool;
        this.sectionList = sectionList;
        this.fileSectionSend = new FileSectionSendReceive();
    }

    /**
     * 开启 发送线程<br/>
     * 发送 文件片段列表的内容，并且当 整个列表发送完毕，发送 当前发送端的“终止信号”
     */
    public void startSend() {
        new Thread(this, "文件发送端").start();
    }

    @Override
    public void run() {
        Socket socket = null;
        DataOutputStream dos = null;
        try {
            socket = new Socket(receiveIp, receivePort);
            dos = new DataOutputStream(socket.getOutputStream());
            for (FileSectionInfo sectionInfo : sectionList) {
                FileReadWrite readWrite = rscFilePool.getFileAccept(sectionInfo.getFileNo());   // 获取指定文件列表的 FileReadWrite对象
                sectionInfo = readWrite.readSection(sectionInfo);   // 封装 文件片段信息
                fileSectionSend.sendSection(dos, sectionInfo);  // 发送 文件片段
            }
            // 整个列表发送完毕，发送 当前发送端的“终止信号”
            fileSectionSend.sendLastSection(dos);

            ResourceSender.incSendTotalAmount();    // 使得 发送的总数 + 1，用于“负载均衡”
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ResourceSender.decSendconcurrentCount();  // 发送完毕，使得 当前正在发送的数量-1，用于“负载均衡”
            close(socket, dos);
        }
    }

    /**
     * 关闭 指定的 socket和dos
     * @param socket 指定的socket
     * @param dos 指定的dos
     */
    private void close(Socket socket, DataOutputStream dos) {
        try {
            if (dos != null) {
                dos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
