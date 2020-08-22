package edu.youzg.resource_founder.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.youzg.balance.INetNode;
import edu.youzg.resource_founder.node.NodePool;

/**
 * 资源-拥有者 映射池：<br/>
 * 注册/注销 资源拥有信息、查询拥有者信息
 */
public class ResourceNodePool {
    private static final Map<Integer, List<INetNode>> rnPool
            = new ConcurrentHashMap(); // 以 资源信息的hashcode 为键，该资源的拥有者list为值，存储map
    private static final List<ResourceBaseInfo> resourceList
            = new CopyOnWriteArrayList();   // 保存 已注册 的资源信息

    private static Logger log = Logger.getLogger(ResourceNodePool.class);

    /**
     * 注册 一个资源的拥有者 的信息
     * @param resourceInfo 目标资源
     * @param netNode 持有者的节点信息
     */
    public static void registry(ResourceBaseInfo resourceInfo, INetNode netNode) {
        int key = resourceInfo.hashCode();
        List<INetNode> addrList = null;
        synchronized (rnPool) {
            addrList = (List) rnPool.get(key);
            if (addrList == null) {
                addrList = new CopyOnWriteArrayList();
                rnPool.put(key, addrList);
                resourceList.add(resourceInfo);
            }
        }

        addrList.add(netNode);
        NodePool.addNode(netNode);
    }

    /**
     * 注销 一个资源拥有者的 指定资源 信息
     * @param resourceInfo 目标资源
     * @param netNode 持有者的节点信息
     */
    public static void logout(ResourceBaseInfo resourceInfo, INetNode netNode) {
        int key = resourceInfo.hashCode();
        List<INetNode> addrList = null;
        synchronized (rnPool) {
            addrList = rnPool.get(key);
            if (addrList == null) {
                // 日志：资源不存在异常！
                log.error("资源["+ resourceInfo.toString() + "]不存在！");
                return;
            }
            addrList.remove(netNode);
            if (addrList.isEmpty()) {
                rnPool.remove(key);
                resourceList.remove(resourceInfo);
            }
        }
    }

    /**
     * 注销 指定节点 的 所有资源
     * @param netNode 要注销的 节点
     */
    public static synchronized void logout(INetNode netNode) {
        int key = 0;
        List<INetNode> netNodes = null;
        for (ResourceBaseInfo resourceInfo : resourceList) {
            key = resourceInfo.hashCode();
            netNodes = rnPool.get(key);
            boolean remove = netNodes.remove(netNode);
            if (remove) {
                if (netNodes.isEmpty()) {
                    rnPool.remove(key);
                    resourceList.remove(resourceInfo);
                }
            }
        }
        NodePool.removeNode(netNode);
    }

    /**
     * 获取 指定资源 的节点列表
     * @param resourceInfo 目标资源
     * @return 该资源的 拥有者节点列表
     */
    public static synchronized List<INetNode> getAddressList(ResourceBaseInfo resourceInfo) {
        return rnPool.get(resourceInfo.hashCode());
    }

    /**
     * 获取 当前被注册的 资源列表
     * @return 当前被注册的 资源列表
     */
    public static List<ResourceBaseInfo> getResourceList() {
        return resourceList;
    }

}
