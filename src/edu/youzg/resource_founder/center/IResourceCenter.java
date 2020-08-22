package edu.youzg.resource_founder.center;

import java.util.List;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;

/**
 * 注册中心 基本功能接口
 */
public interface IResourceCenter {
    void registry(ResourceBaseInfo info, List<ResourceSpecificInfo> fileInfoList, DefaultNetNode netNode);
    void logout(ResourceBaseInfo res, DefaultNetNode addr);
    List<DefaultNetNode> getTotalAddressList(String recieveIp, int receivePort, ResourceBaseInfo res);
    List<ResourceSpecificInfo> getFileInfoListByResourceInfo(ResourceBaseInfo ri);
    List<ResourceBaseInfo> getResourceList();
}
