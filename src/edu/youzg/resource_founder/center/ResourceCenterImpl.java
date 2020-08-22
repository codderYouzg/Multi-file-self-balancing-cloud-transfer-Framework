package edu.youzg.resource_founder.center;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.balance.INetNode;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;
import edu.youzg.resource_founder.core.ResourceNodePool;

/**
 * 注册中心 基本功能 实现类
 */
public class ResourceCenterImpl implements IResourceCenter {
	private Map<ResourceBaseInfo, List<ResourceSpecificInfo>> resourcePool
		= new ConcurrentHashMap<ResourceBaseInfo, List<ResourceSpecificInfo>>();
	
	private Logger log = Logger.getLogger(ResourceCenterImpl.class);
	
    public ResourceCenterImpl() {
    }

    @Override
    public void registry(ResourceBaseInfo info, List<ResourceSpecificInfo> fileInfoList, DefaultNetNode netNode) {
    	ResourceNodePool.registry(info, netNode);
        if (!this.resourcePool.containsKey(info)) {
        	this.resourcePool.put(info, fileInfoList);
        }
        log.info("节点" + netNode + "：资源[" + info + "]注册成功！");
    }

    @Override
    public void logout(ResourceBaseInfo info, DefaultNetNode netNode) {
        ResourceNodePool.logout(info, netNode);
        if (resourcePool.get(info).size() <= 0) {
			resourcePool.remove(info);
			log.info("资源[" + info + "]拥有者已全部注销，该资源当前不存在！");
		}
        log.info("节点" + netNode + "：资源[" + info + "]注销成功！");

    }

    @Override
    public List<DefaultNetNode> getTotalAddressList(String recieveIp, int receivePort, ResourceBaseInfo res) {
    	log.info("节点[" + recieveIp + ":" + receivePort + "]：请求资源[" + res + "]");
        List<DefaultNetNode> result = new ArrayList<DefaultNetNode>();

        List<INetNode> nodeList = ResourceNodePool.getAddressList(res);
        if (nodeList == null || nodeList.isEmpty()) {
            return result;
        }

        for (INetNode node : nodeList) {
            result.add((DefaultNetNode) node);
        }
        return result;
    }

    @Override
    public List<ResourceBaseInfo> getResourceList() {
        return ResourceNodePool.getResourceList();
    }

	@Override
	public List<ResourceSpecificInfo> getFileInfoListByResourceInfo(ResourceBaseInfo ri) {
		return this.resourcePool.get(ri);
	}

}
