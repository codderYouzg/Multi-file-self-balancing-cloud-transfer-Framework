package edu.youzg.multifile_cloud_transfer.sender;

import java.util.List;

import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;

/**
 * 资源发送者 基本功能 接口<br/>
 * 1、封装 资源发送 方法：<br/>
 * sendResource<br/>
 * 2、也封装了 负载均衡 的相关方法：<br/>
 * getSendCounter
 */
public interface IResourceSender {

    /**
     * 发送资源
     * @param receiveIp 资源发送者 ip
     * @param receivePort 资源发送者 port
     * @param baseInfo 资源基本信息
     * @param sectionList 资源片段列表信息
     * @return
     */
    boolean sendResource(String receiveIp, int receivePort, ResourceBaseInfo baseInfo, List<FileSectionInfo> sectionList);

    /**
     * 获取 当前发送者 的 健康状况<br/>
     * 用于 负载均衡
     * @return 当前发送者 的 健康状况
     */
    SendCounter getSendCounter();

}
