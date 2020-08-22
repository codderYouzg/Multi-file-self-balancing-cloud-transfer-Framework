package edu.youzg.resource_founder.node;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.balance.INetNode;
import edu.youzg.resource_founder.core.IResourceHolder;
import edu.youzg.rmi_impl.client.RMIClient;

/**
 * 资源持有者 节点：<br/>
 * 1. ResourceHolderNode(INetNode node) 初始化成员属性
 * 2. isActive()判断存活情况
 */
public class ResourceHolderNode extends DefaultNetNode {

    private IResourceHolder resourceHolder;

    public ResourceHolderNode(INetNode node) {
        super(node.getIp(), node.getPort());
        RMIClient rmiClient = new RMIClient();
        rmiClient.setRmiServerIp(getIp());
        rmiClient.setRmiServerPort(getPort());
        this.resourceHolder = rmiClient.getProxy(IResourceHolder.class);
    }

    public boolean isActive() throws Exception {
        return resourceHolder.isActive();
    }

}
