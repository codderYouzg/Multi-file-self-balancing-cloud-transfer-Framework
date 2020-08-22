package edu.youzg.multifile_cloud_transfer.sender;

import java.util.List;

import org.apache.log4j.Logger;

import edu.youzg.multifile_cloud_transfer.exception.RegistryIpIsNullException;
import edu.youzg.multifile_cloud_transfer.receive.ResourceFilePool;
import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;

/**
 * 资源发送方<br/>
 * 封装 资源发送 方法：<br/>
 * sendResource<br/>
 * 也封装了 负载均衡 的相关方法：<br/>
 * decSendCount、incSendAccount、getSendCounter
 */
public class ResourceSender implements IResourceSender {
    private SourceHolderNode holder;
    private static final SendCounter sendCounter = new SendCounter();

    private Logger log = Logger.getLogger(ResourceSender.class);

    public ResourceSender() {
        try {
            holder = SourceHolderNode.newInstance();
        } catch (RegistryIpIsNullException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使得 当前发送者的 当前并发数量 - 1
     */
    public static void decSendconcurrentCount() {
        sendCounter.decSendconcurrentCount();
    }

    /**
     * 使得 当前发送者的 发送总个数 + 1
     */
    public static void incSendTotalAmount() {
        sendCounter.incSendTotalAmount();
    }

    /**
     * 发送资源
     * @param receiveIp 资源发送者 ip
     * @param receivePort 资源发送者 port
     * @param baseInfo 资源基本信息
     * @param sectionList 资源片段列表信息
     * @return 是否发送成功
     */
    @Override
    public boolean sendResource(String receiveIp, int receivePort,
    		ResourceBaseInfo baseInfo, List<FileSectionInfo> sectionList) {
        log.info("发现一个 发送请求，来自 :[ " + receiveIp + " : " + receivePort + "]，请求资源：[" + baseInfo + "]");

        sendCounter.incSendconcurrentCount();
        SourceFileList fileList = holder.getSourceFileList(baseInfo);

        ResourceFilePool resourceFilePool = new ResourceFilePool();
        resourceFilePool.addFileList(fileList);
        FileSender fileSender = new FileSender(receiveIp, receivePort, resourceFilePool, sectionList);
        fileSender.startSend();

        return true;
    }

    /**
     * 获取 当前发送者 的 健康状况
     * @return 当前发送者 的 健康状况
     */
    @Override
    public SendCounter getSendCounter() {
        return sendCounter;
    }

}
