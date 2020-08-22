package edu.youzg.resource_founder.core;

import edu.youzg.resource_founder.node.NodePool;
import edu.youzg.rmi_impl.core.RMIFactory;
import edu.youzg.rmi_impl.server.RMIServer;

import java.util.List;

/**
 * 资源注册中心：<br/>
 * 默认端口：6666<br/>
 * 配置文件名：ResourceRegistryCenter-RMI.xml
 */
public class ResourceRegistryCenter implements IResourceSpeaker {
    private static final int DEFAULT_PORT = 6666;
    private static final String DEFAULT_CONFIG_PATH = "/resource/ResourceRegistryCenter-RMI.xml";

    private RMIServer rmiServer;
    private volatile boolean startup;

    private List<IResourceListener> listenerList;

    public ResourceRegistryCenter() {
        this(DEFAULT_PORT);
    }

    public ResourceRegistryCenter(int rmiServerPort) {
        this.rmiServer = new RMIServer();
        this.rmiServer.setRmiPort(rmiServerPort);
    }

    public void initRegistryCenter() {
        initRegistryCenter(DEFAULT_CONFIG_PATH);
    }

    public void initRegistryCenter(String configFilePath) {
        RMIFactory.scanRMIMapping(configFilePath);
    }

    public void setRmiServerPort(int rmiServerPort) {
        this.rmiServer.setRmiPort(rmiServerPort);
    }

    public void startup() {
        if (this.startup == true) {
            speakOut("注册中心 已启动");
            return;
        }
        this.startup = true;
        this.rmiServer.startUp();
        NodePool.startScanNode();
        speakOut("注册中心 启动成功");
    }

    public void shutdown() {
        if (this.startup == false) {
            speakOut("注册中心 已关闭");
            return;
        }
        this.startup = false;
        this.rmiServer.shutdown();
        NodePool.stopScanNode();
        speakOut("注册中心 关闭成功");
    }

    @Override
    public List<IResourceListener> getListenerList() {
        return listenerList;
    }

    @Override
    public void setListenerList(List<IResourceListener> listenerList) {
        this.listenerList = listenerList;
    }

}
