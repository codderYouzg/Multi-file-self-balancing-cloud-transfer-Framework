package edu.youzg.multifile_cloud_transfer.receive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.balance.RandomBalance;
import edu.youzg.multifile_cloud_transfer.exception.RegistryIpIsNullException;
import edu.youzg.multifile_cloud_transfer.exception.ResourceNotExistException;
import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.multifile_cloud_transfer.sender.IResourceSender;
import edu.youzg.multifile_cloud_transfer.sender.SendCounter;
import edu.youzg.multifile_cloud_transfer.sender.SourceHolderNode;
import edu.youzg.multifile_cloud_transfer.strategy.FileDistribution;
import edu.youzg.multifile_cloud_transfer.strategy.IDistributionStrategy;
import edu.youzg.multifile_cloud_transfer.strategy.ISenderSelectedStrategy;
import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.resourcer.ResourceRequester;
import edu.youzg.rmi_impl.client.RMIClient;

/**
 * 资源接收方<br/>
 */
public class ResourceReceiver {
    private static final String DEFAULT_IP = "localhost";
    private static final int DEFAULT_PORT = 6666;

    // 在 获取到发送端数量 的时候 初始化值 为 发送端数量
    // 当一个端发送完毕之后就进行减一，当为值0时说明接收完毕
    static volatile AtomicInteger curSenderCount = null;

    private ResourceBaseInfo baseInfo;
    private SourceFileList fileList;
    private ResourceRequester requester;    // 用于 获取“资源持有者” 的 节点信息
    private FileDistribution fileDistribution;  // 文件分发器
    private ISenderSelectedStrategy senderSelectedStrategy; // 选择发送者(负载均衡) 策略

    private RMIClient receiveClient;    // 访问 资源发送端 用的
    private IResourceSender resourceSender; // 通过RMI技术，使得发送端发送文件 的接口

    private ReceiveServer receiveServer;    // 接收，并显示模态框

    private String centerIp;    // 注册中心 ip
    private int centerPort; // 注册中心 port

    private Logger log = Logger.getLogger(ResourceReceiver.class);

    public ResourceReceiver() {
        this(DEFAULT_IP, DEFAULT_PORT);
    }

    public ResourceReceiver(String centerIp, int centerPort) {
        this.requester = new ResourceRequester();
        this.requester.setRmiServerIp(centerIp);
        this.requester.setRmiServerPort(centerPort);

        this.fileDistribution = new FileDistribution();
        this.receiveServer = new ReceiveServer();

        // 获取 发送端 的 代理对象
        this.receiveClient = new RMIClient();
        this.resourceSender = this.receiveClient.getProxy(IResourceSender.class);

        this.centerIp = centerIp;
        this.centerPort = centerPort;
    }

    public void setCenterIp(String centerIp) {
        this.centerIp = centerIp;
        this.requester.setRmiServerIp(centerIp);
    }

    public void setCenterPort(int centerPort) {
        this.centerPort = centerPort;
        this.requester.setRmiServerPort(centerPort);
    }

    public void setReceiveServerPort(int port) {
        this.receiveServer.setPort(port);
    }

    public void setMaxFileSectionSize(int maxSectionSize) {
        this.fileDistribution.setMaxSectionSize(maxSectionSize);
    }

    /**
     * 设置 负载均衡 策略
     * @param senderSelectedStrategy 负载均衡 策略
     */
    public void setSenderSelectedStrategy(ISenderSelectedStrategy senderSelectedStrategy) {
        this.senderSelectedStrategy = senderSelectedStrategy;
    }

    /**
     * 设置 文件分发 策略
     * @param strategy 文件分发 策略
     */
    public void setDistributionStrategy(IDistributionStrategy strategy) {
        this.fileDistribution.setDistributionStrategy(strategy);
    }

    public void setBaseInfo(ResourceBaseInfo baseInfo) {
        this.baseInfo = baseInfo;
    }

    public void setFileList(SourceFileList fileList) {
        this.fileList = fileList;
    }

    public void setAbsoluteRoot(String absoluteRoot) {
        this.fileList.setAbsoluteRoot(absoluteRoot);
    }

    /**
     * 获取 负载均衡 后 的发送者列表
     * @return 负载均衡 后 的发送者列表
     * @throws ResourceNotExistException 该资源未注册 异常
     */
    private List<DefaultNetNode> getSenderList() throws ResourceNotExistException {
        List<DefaultNetNode> senderList = requester.getTotalAddressList(this.receiveServer.getIp(), this.receiveServer.getPort(), baseInfo);

        if (senderList == null || senderList.isEmpty()) {
            throw new ResourceNotExistException("资源[" + baseInfo + "]不存在");
        }

        if (this.senderSelectedStrategy == null) {
            this.senderSelectedStrategy = new SenderSelect();
        }

        senderList = senderSelectedStrategy.selectSender(senderList);

        ResourceReceiver.curSenderCount = new AtomicInteger(senderList.size());
        return senderList;
    }

    public List<ResourceBaseInfo> getResourceList() {
        return this.requester.getResourceList();
    }

    public List<ResourceSpecificInfo> getFileInfoListByResourceInfo(ResourceBaseInfo ri) {
        return this.requester.getFilePathListByResourceInfo(ri);
    }

    /**
     * 通过 RMI代理对象，使得被选中的发送端 发送指定的 文件片段列表
     *
     * @param senderCount             发送端的个数
     * @param fileSectionInfoListList 每一个发送端 所需发送的 文件片段列表
     * @param senderList              被选中的发送端 列表
     */
    private void sendFileSections(int senderCount, List<List<FileSectionInfo>> fileSectionInfoListList, List<DefaultNetNode> senderList) {
        // 在接收端 设置“随机式”负载均衡
        RandomBalance randomBalance = new RandomBalance();
        randomBalance.addNodeList(senderList);
        for (int index = 0; index < senderCount; index++) {

            DefaultNetNode node = randomBalance.getNode();
            randomBalance.removeNode(node);

            List<FileSectionInfo> sectionList = fileSectionInfoListList.get(index);

            this.receiveClient.setRmiServerIp(node.getIp());
            this.receiveClient.setRmiServerPort(node.getPort());

            this.resourceSender.sendResource(this.receiveServer.getIp(), this.receiveServer.getPort(), baseInfo, sectionList);
        }
    }

    /**
     * 首次通过 RMI技术 调用 发送端的发送方法，<br/>
     * 并启动接收线程接收并保存发送端发来的文件片段
     * @param sourceFileList 该资源 的 全部子文件信息
     * @throws ResourceNotExistException
     */
    private void firstReceive(SourceFileList sourceFileList) throws ResourceNotExistException {
        List<DefaultNetNode> senderList = getSenderList();

        int senderCount = senderList.size();

        List<List<FileSectionInfo>> fileSectionInfoListList = this.fileDistribution.distributeSection(sourceFileList, senderCount);

        this.receiveServer.setReceiveFilePool(this.fileList);
        this.receiveServer.setSenderCount(senderCount);

        this.receiveServer.enabledReceiveProgress(null, sourceFileList.getFileList().size());

        this.receiveServer.startup();
        sendFileSections(senderCount, fileSectionInfoListList, senderList);
    }

    private void registerMe() {
        try {
            // 将当前节点当作 拥有者 启动前，先关闭之前的请求线程
            // 以免造成“端口冲突”问题
            this.receiveServer.shutdown();

            SourceHolderNode.scanRMIMapping();
            SourceHolderNode sourceHolderNode = SourceHolderNode.newInstance(receiveServer.getIp(), receiveServer.getPort());
            sourceHolderNode.setRmiServerPort(this.centerPort);
            sourceHolderNode.setRmiServerIp(this.centerIp);
            sourceHolderNode.setServerPort(this.receiveServer.getPort());
            sourceHolderNode.setServerIp(InetAddress.getLocalHost().getHostAddress());
            sourceHolderNode.reportResource(this.baseInfo, fileList);

            sourceHolderNode.startUp();
            log.info("当前节点 已成为 资源[" + baseInfo + "]的拥有者节点");
        } catch (RegistryIpIsNullException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过 RMI代理对象 调用 发送端的发送方法
     * @throws ResourceNotExistException 请求的资源不存在 异常
     */
    public void getResourceFiles() throws ResourceNotExistException {
        // 先接收一次
        firstReceive(this.fileList);

        // 当第一次接收 还未完成，阻塞主线程
        while (ResourceReceiver.curSenderCount.get() != 0) {
            Thread.yield();
        }

        ResourceFilePool resourceFilePool = this.receiveServer.getResourceFilePool();

        while (!resourceFilePool.isEmpty()) {   // 判断是否存在 发送端异常宕机情况(即：出现了 “断点”)
            System.out.println("检测到断点，断点信息如下：");
            resourceFilePool.showBreakPoint();

            List<FileSectionInfo> unreceiveSectionList = resourceFilePool.getUnreceiveSectionList();
            List<DefaultNetNode> curSenderList = getSenderList();

            int curSenderCount = curSenderList.size();
            this.receiveServer.setSenderCount(curSenderCount);
            this.receiveServer.breakpointResume();

            List<List<FileSectionInfo>> fileSectionInfoListList = this.fileDistribution.getFileSectionInfoListList(unreceiveSectionList, curSenderCount);
            sendFileSections(curSenderCount, fileSectionInfoListList, curSenderList);

            // 当前接收 还未完成，阻塞主线程
            while (ResourceReceiver.curSenderCount.get() != 0) {
                Thread.yield();
            }
        }

        // 资源接收成功后，当前节点也会变为 资源拥有者节点
        registerMe();
    }

    /**
     * 负载均衡 选取发送者
     */
    class SenderSelect implements ISenderSelectedStrategy {

        public SenderSelect() {
        }

        @Override
        public List<DefaultNetNode> selectSender(List<DefaultNetNode> senderList) {
            // 执行RMI方法
            // 获取资源持有者当前的发送任务数量
            List<DefaultNetNode> sendList = new ArrayList<DefaultNetNode>();
            for (DefaultNetNode netNode : senderList) {
                int senderPort = netNode.getPort();
                receiveClient.setRmiServerIp(netNode.getIp());
                receiveClient.setRmiServerPort(senderPort);
                try {
                    // 负载均衡 简化版
                    SendCounter sendCounter = resourceSender.getSendCounter();  // 此处执行的是RMI方式调用方法，所以根据port的改变而改变执行对象
                    if (sendCounter.getSendTotalAmount() > 100 || sendCounter.getSendconcurrentCount() > 20) {
                        continue;
                    }
                    sendList.add(netNode);
                } catch (Exception e) {
                    log.warn("发送端[" + netNode.getIp() + ":" + netNode.getPort() + "]已宕机!");
                }
            }
            return sendList;
        }

    }

}
