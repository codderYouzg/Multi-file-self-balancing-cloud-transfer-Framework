package edu.youzg.multifile_cloud_transfer.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import edu.youzg.multifile_cloud_transfer.exception.ReceiveSectionOutOfRangeException;
import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.multifile_cloud_transfer.unreceive.UnreceiveSection;
import edu.youzg.multifile_cloud_transfer.view.ReceiveProgressDialog;
import edu.youzg.network_transmission.core.FileReadWrite;
import edu.youzg.network_transmission.core.FileSectionSendReceive;
import edu.youzg.network_transmission.net.NetSpeed;
import edu.youzg.network_transmission.section.FileReadWriteIntercepterAdapter;
import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.network_transmission.section.IFileReadWriteIntercepter;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.util.exceptions.FrameIsNullException;

/**
 * 接收服务器 线程类<br/>
 * 接收文件片段，并显示模态框
 */
public class ReceiveServer implements Runnable {
    private static final int DEFAULT_RECEIVE_PORT = 5555;

    private ServerSocket server;
    private String ip;
    private int port;

    private int senderCount;    // 发送端 个数
    private volatile boolean goon;
    private ThreadPoolExecutor threadPool;

    private SourceFileList sourceFileList;
    private ResourceFilePool resourceFilePool;  // 目标资源 的所有子文件 的 FileReadWrite和UnreceiveSection
    private ReceiveProgressDialog receiveProgressDialog;
    private IFileReadWriteIntercepter fileReadWriteAction;
    private int receiveFileCount;   // 已接收文件 的个数

    private Logger log = Logger.getLogger(ReceiveServer.class);

    public ReceiveServer() {
        this(DEFAULT_RECEIVE_PORT);
    }

    public ReceiveServer(int port) {
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
            this.port = port;

            this.threadPool = new ThreadPoolExecutor(5, 20, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
            this.resourceFilePool = new ResourceFilePool();
            this.fileReadWriteAction = new FileReadWriteAction();
            this.resourceFilePool.setFileReadWriteIntercepter(fileReadWriteAction);
            this.goon = false;
            this.senderCount = 0;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSenderCount(int senderCount) {
        this.senderCount = senderCount;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getReceiveFileCount() {
        return receiveFileCount;
    }

    public ResourceFilePool getResourceFilePool() {
        return resourceFilePool;
    }

    /**
     * 设置 资源信息
     *
     * @param sourceFileList 一个资源的全部子文件信息
     */
    public void setReceiveFilePool(SourceFileList sourceFileList) {
        this.sourceFileList = sourceFileList;
        List<ResourceSpecificInfo> fileList = sourceFileList.getFileList();
        this.receiveFileCount = fileList.size();

        this.resourceFilePool.setFileReadWriteIntercepter(this.fileReadWriteAction);
        this.resourceFilePool.addFileList(sourceFileList);
    }

    /**
     * 启用 接收进度条<br/>
     * 启动成功后，启动主线程
     * @param parent 父容器
     * @param receiveFileCount 接收文件的进度
     */
    public void enabledReceiveProgress(JFrame parent, int receiveFileCount) {
        this.receiveProgressDialog = new ReceiveProgressDialog(parent, receiveFileCount) {
			private static final long serialVersionUID = 8123375912875950460L;

			@Override
            public void afterDialogShow() {
                ReceiveServer.this.goon = true;
                new Thread(ReceiveServer.this, "接收服务器").start();
            }
        };
        ((FileReadWriteAction)this.fileReadWriteAction).setProgressDialog(this.receiveProgressDialog);
    }

    /**
     * 开启一个线程，并且 显示接收进度控制对话框
     */
    public void startup() {
        if (this.goon) {
            return;
        }
        try {
            this.server = new ServerSocket(port);
            // 显示 接收进度控制对话框
            if (this.receiveProgressDialog != null) {   // 若我们设置了 模态框，则在设置时就启动了主线程
                ReceiveProgressShower shower =
                        new ReceiveProgressShower(receiveProgressDialog);
                new Thread(shower).start();
            } else {
                this.goon = true;
                new Thread(this, "接收服务器").start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断点续传<br/>
     * 不考虑 goon 的值，不开启模态框
     */
    public void breakpointResume() {
        // 再次开启 接收线程
        new Thread(this, "服务器接收端").start();
    }

    /**
     * 关闭 当前接收服务器
     */
    public void shutdown() {
        if (!goon) {
            return;
        }
        if (this.server != null && !this.server.isClosed()) {
            try {
                this.server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.goon = false;
        this.receiveProgressDialog.closeView();
    }

    /**
     * 创建一个线程去接收
     */
    @Override
    public void run() {
        if (this.senderCount <= 0) {
            return;
        }

        for (int count = 0; count < this.senderCount; count++) {
            try {
                Socket sender = this.server.accept();
                Receiver receiver = new Receiver(sender);
                this.threadPool.execute(receiver);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 接收文件片段 线程类<br/>
     * 接收 指定的dis 的 全部文件片段信息
     */
    class Receiver implements Runnable {
        private DataInputStream dis;
        private FileSectionSendReceive sectionReceive;

        public Receiver(Socket socket) throws IOException {
            this.sectionReceive = new FileSectionSendReceive();
            this.dis = new DataInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                this.sectionReceive.setSpeed(NetSpeed.newInstance());
                FileSectionInfo sectionInfo = this.sectionReceive.receiveSection(this.dis);

                FileReadWrite fileReadWrite = null;
                while (sectionInfo.getLength() > 0) {
                    fileReadWrite = resourceFilePool.getFileAccept(sectionInfo.getFileNo());
                    fileReadWrite.writeSection(sectionInfo);    // 将读取到的文件片段，写入本机指定位置

                    sectionInfo = sectionReceive.receiveSection(this.dis);
                }
            } catch (Exception e) {
                log.error("有一个发送端 在发送过程中 宕机！");
            } finally {
                ResourceReceiver.curSenderCount.decrementAndGet();
            }
        }

    }

    /**
     * 接收进度条 显示器
     */
    class ReceiveProgressShower implements Runnable {
        private ReceiveProgressDialog receiveProgressDialog;

        public ReceiveProgressShower(ReceiveProgressDialog receiveProgressDialog) {
            this.receiveProgressDialog = receiveProgressDialog;
        }

        @Override
        public void run() {
            try {
                this.receiveProgressDialog.showView();
            } catch (FrameIsNullException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 本机文件读写拦截器 实现类<br/>
     * 关于进度条的 自动化 添加/删除
     */
    class FileReadWriteAction extends FileReadWriteIntercepterAdapter {
        private ReceiveProgressDialog progressDialog;

        public FileReadWriteAction() {
        }

        public void setProgressDialog(ReceiveProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        /**
         * 写入之前，保证存在当前文件的 接收进度条
         * @param filePath 文件保存的地址
         * @param sectionInfo 文件片段信息
         */
        @Override
        public void beforeWrite(String filePath, FileSectionInfo sectionInfo) {
            if (this.progressDialog == null) {
                return;
            }

            // 判断当前文件是否存在
            int fileNo = sectionInfo.getFileNo();
            if (!this.progressDialog.receiveFileExist(fileNo)) {    // 若不存在，则向其中加入当前文件的进度条
                long fileSize = sourceFileList.getFileSizeByFileNo(fileNo);
                this.progressDialog.addReceiveFile(fileNo, filePath, fileSize);
            }

        }

        /**
         * 写入之后:<br/>
         * 1. 若当前文件片段已经接收完毕，保证该文件的FileReadWrite被关闭<br/>
         * 2. 若接收完毕，则移除该文件的进度条
         * @param sectionInfo 文件片段信息
         */
        @Override
        public void afterWritten(FileSectionInfo sectionInfo) {
            // 根据文件片段信息，获取 该文件的未接收片段
            int fileNo = sectionInfo.getFileNo();
            UnreceiveSection unreceiveSection = resourceFilePool.getUnreceiveSection(fileNo);

            try {
                unreceiveSection.receiveSection(sectionInfo.getOffset(), sectionInfo.getLength());
                if (unreceiveSection.isReceived()) {    // 若当前文件接收完毕，则关闭其FileReadWrite，并从池子中移除
                    FileReadWrite readWrite = resourceFilePool.getFileAccept(fileNo);
                    resourceFilePool.removeUnreceiveSection(fileNo);
                    readWrite.close();
                }
            } catch (ReceiveSectionOutOfRangeException e) {
                e.printStackTrace();
            }

            if (this.progressDialog != null) {
                this.progressDialog.receiveFileSection(fileNo, sectionInfo.getLength());
                this.progressDialog.updataSpeed(NetSpeed.getAverSpeed(), NetSpeed.getCurSpeed());
                if (unreceiveSection.isReceived()) {    // 若接收完毕，则移除该文件的进度条
                    this.progressDialog.removeReceiveFile(fileNo);
                }
            }
        }
    }

}
