package edu.youzg.resource_founder.resourcer;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;

import java.util.List;

/**
 * 封装 资源请求者 的基本功能:<br/>
 * 1. setRmiServerIp() 和 setRmiServerPort()<br/>
 * 2. getAddressList(ResourceInfo res)<br/>
 * 3. getResourceList()
 */
public class ResourceRequester extends Resourcer {

    public ResourceRequester() {
        super();
    }

    /**
     * 获取 目标资源的 拥有者列表
     * @param recieveIp 请求者ip
     * @param receivePort 请求者port
     * @param res 目标资源信息
     * @return 目标资源的 拥有者列表
     */
    public List<DefaultNetNode> getTotalAddressList(String recieveIp, int receivePort, ResourceBaseInfo res) {
        return irc.getTotalAddressList(recieveIp, receivePort, res);
    }

    /**获取 资源中心 当前持有的 资源列表
     * 
     * @return 资源中心 当前持有的 资源列表
     */
    public List<ResourceBaseInfo> getResourceList() {
        return irc.getResourceList();
    }

    /**
     * 根据 目标资源信息，获取 该资源的 子文件相对路径列表
     * @param ri 目标资源信息
     * @return 该资源的 子文件相对路径列表
     */
    public List<ResourceSpecificInfo> getFilePathListByResourceInfo(ResourceBaseInfo ri) {
		return this.irc.getFileInfoListByResourceInfo(ri);
	}

}
