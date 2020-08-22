package edu.youzg.resource_founder.node;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.youzg.balance.INetNode;
import edu.youzg.resource_founder.core.ResourceNodePool;
import edu.youzg.util.Didadida;

/**
 * 存储/删除节点，并可以扫描每一个存储的节点的健康情况：<br/>
 * 以当前节点 的hashcode为键，当前节点的节点信息为值，存储的map
 */
public class NodePool {
    private static final long DEFAULT_DELAY_TIME = 3000L;
    private static final Map<Integer, INetNode> nodePool
    	= new ConcurrentHashMap();
    
    private static NodePool.ScanTimer scanTimer = new NodePool.ScanTimer(DEFAULT_DELAY_TIME);

    private static Logger log = Logger.getLogger(NodePool.class);
    
    public NodePool() {
    }

    /**
         * 开始扫描每一个节点
     */
    public static void startScanNode() {
        scanTimer.start();
    }

    /**
         * 停止扫描线程
     */
    public static void stopScanNode() {
        scanTimer.stop();
    }

    /**
         * 新增一个 资源拥有者 节点
     * @param node 资源拥有者 节点信息
     */
    public static void addNode(INetNode node) {
        int key = node.hashCode();
        if (!nodePool.containsKey(key)) {
            nodePool.put(key, new ResourceHolderNode(node));
        }
    }

    /**
         * 删除一个 资源拥有者 节点
     * @param node 资源拥有者 节点信息
     */
    public static void removeNode(INetNode node) {
        int key = node.hashCode();
        if (nodePool.containsKey(key)) {
            nodePool.remove(key);
        }
    }

    static class ScanTimer extends Didadida {

        public ScanTimer() {
        }

        public ScanTimer(long delay) {
            super(delay);
        }

        /**
                * 心跳检测<br/>
                * 保证 当前的列表 中 只保存 “活”的资源拥有者节点
         */
        @Override
        protected void doTask() {
            if (!NodePool.nodePool.isEmpty()) {
                Iterator nodeList = NodePool.nodePool.values().iterator();

                while (nodeList.hasNext()) {
                    INetNode node = (INetNode) nodeList.next();

                    try {
                        ((ResourceHolderNode) node).isActive();
                    } catch (Exception e) {
                    	log.warn("节点[" + node.getIp() + ":" + node.getPort() + "]异常宕机！注册中心已将其 拥有资源信息 注销！");
                        ResourceNodePool.logout(node);  // 当前节点 失活，注销该节点
                    }
                }
            }
        }

    }
}
