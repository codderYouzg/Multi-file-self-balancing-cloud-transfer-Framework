package edu.youzg.resource_founder.resourcer;

import java.util.List;

import edu.youzg.balance.DefaultNetNode;
import edu.youzg.balance.INetNode;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.core.ResourceBaseInfo;
import edu.youzg.rmi_impl.core.RMIFactory;
import edu.youzg.rmi_impl.server.RMIServer;

/**
 * 封装 资源持有者 的基本功能:<br/>
 * 默认 配置文件 全路径名：/resource/ResourceHolder-RMI.xml
 */
public class ResourceHolder extends Resourcer {
    private static final String DEFAULT_CONFIG_PATH = "/resource/ResourceHolder-RMI.xml";

    private RMIServer rmiServer;
    private INetNode netNode;

    public ResourceHolder(String ip, int port) {
        this.netNode = new DefaultNetNode(ip, port);
        this.rmiServer = new RMIServer();
        this.rmiServer.setRmiPort(port);
        this.rmiServer.startUp();
    }

    /**
     * 通过扫描 默认路径的配置文件，初始化RMI工厂
     */
    public static void scanRMIMapping() {
        scanRMIMapping(DEFAULT_CONFIG_PATH);
    }

    /**
     * 通过扫描 指定路径的配置文件，初始化RMI工厂
     * @param mappingFile 指定的 配置文件路径
     */
    public static void scanRMIMapping(String mappingFile) {
        RMIFactory.scanRMIMapping(mappingFile);
    }

    public void setHolderIp(String ip) {
        this.netNode.setIp(ip);
    }

    public void setHolderPort(int serverPort) {
        this.rmiServer.setRmiPort(serverPort);
        this.netNode.setPort(serverPort);
    }

    /**
     * 开启 RMI服务器
     */
    public void startUp() {
        this.rmiServer.startUp();
    }

    /**
     * 注册一个资源 的持有信息
     * @param info 目标资源的信息
     */
    public void registry(ResourceBaseInfo info, List<ResourceSpecificInfo> fileInfoList) {
        this.irc.registry(info, fileInfoList, (DefaultNetNode)netNode);
    }

    /**
     * 注销一个资源 的持有信息
     * @param info 目标资源的信息
     */
    public void logout(ResourceBaseInfo info) {
        this.irc.logout(info, (DefaultNetNode) this.netNode);
    }
    
    /**
     * 关闭 RMI服务器
     */
    public void shutdown() {
        this.rmiServer.shutdown();
    }

}
