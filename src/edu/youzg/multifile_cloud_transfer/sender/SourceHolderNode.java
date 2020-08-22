package edu.youzg.multifile_cloud_transfer.sender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.youzg.multifile_cloud_transfer.exception.RegistryIpIsNullException;
import edu.youzg.multifile_cloud_transfer.resourcer.Resources;
import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.resource_founder.core.ResourceBaseInfo;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.resourcer.ResourceHolder;
import edu.youzg.util.PropertiesParser;

/**
 * 资源拥有者 节点<br/>
 * 主要功能为：<br/>
 * initConfig() —— 通过配置文件初始化 成员的值<br/>
 * newInstance() —— 单例性 构造对象<br/>
 * reportResource() —— 注册 资源<br/>
 * logoutResource() —— 注销 资源<br/>
 * getSourceFileList() —— 获取 资源 信息
 */
public class SourceHolderNode extends ResourceHolder {
    private volatile static SourceHolderNode me;    // 保证 单例性构造
    private static Resources resources; // 资源 根节点、子文件内容列表 映射关系池
    private static String ip;   // 当前节点 ip
    private static int port;    // 当前节点 port
    private static String registryIp;   // 资源注册中心 ip
    private static int registryPort;    // 资源注册中心 port

    private Logger log = Logger.getLogger(SourceHolderNode.class);

    /**
     * 注意：使用前要先使用 initConfig()方法<br/>
     * (因为 此处的ip和port均未赋值！)
     */
    public SourceHolderNode() {
        super(ip, port);
    }

    public static void setIp(String holderIp) {
        SourceHolderNode.ip = holderIp;
    }

    public static void setPort(int holderPort) {
        SourceHolderNode.port = holderPort;
    }

    /**
     * (DCL单例性)构造
     * @return SourceHolderNode类 的 单例对象
     * @throws RegistryIpIsNullException 未初始化 registryIp，需要在调用本方法前，先调用initConfig()方法
     */
    public static SourceHolderNode newInstance() throws RegistryIpIsNullException {
        if (me == null) {
            synchronized (SourceHolderNode.class) {
                if (me == null) {
                    me = new SourceHolderNode();
                    if (registryIp == null) {
                        throw new RegistryIpIsNullException("未初始化registryIp！");
                    }
                    me.setRmiServerIp(registryIp);
                    me.setRmiServerPort(registryPort);
                    resources = new Resources();
                }
            }
        }
        return me;
    }

    /**
     * (DCL单例性)构造
     *
     * @return SourceHolderNode类 的 单例对象
     * @throws RegistryIpIsNullException 未初始化 registryIp，需要在调用本方法前，先调用initConfig()方法
     */
    public static SourceHolderNode newInstance(String holderIp, int holderPort) throws RegistryIpIsNullException {
        if (me == null) {
            synchronized (SourceHolderNode.class) {
                if (me == null) {
                    SourceHolderNode.setIp(holderIp);
                    SourceHolderNode.setPort(holderPort);
                    me = new SourceHolderNode();
                    if (registryIp == null) {
                        throw new RegistryIpIsNullException("未初始化registryIp！");
                    }
                    me.setRmiServerIp(registryIp);
                    me.setRmiServerPort(registryPort);
                    resources = new Resources();
                }
            }
        }
        return me;
    }

    public void setServerPort(int port) {
        super.setHolderPort(port);
    }

    public void setServerIp(String ip) {
        super.setHolderIp(ip);
    }

    /**
     * 根据配置文件，配置成员属性
     * @param configFilePath 配置文件路径
     */
    public static void initConfig(String configFilePath) {
        PropertiesParser.loadProperties(configFilePath);
        registryIp = PropertiesParser.value("registry_ip");
        registryPort = Integer.valueOf(PropertiesParser.value("registry_port"));
        port = Integer.valueOf(PropertiesParser.value("port"));
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册 资源信息
     * @param baseInfo 资源 基本信息
     * @param srcfileList 资源 子文件列表 与 根路径
     */
    public void reportResource(ResourceBaseInfo baseInfo, SourceFileList srcfileList) {
        if (!resources.isResourceExist(baseInfo)) {
            resources.addResource(baseInfo, srcfileList);
            List<ResourceSpecificInfo> fileList = srcfileList.getFileList();
            me.registry(baseInfo, fileList);
            log.info("当前节点 的资源信息：[" + baseInfo + "] 注册成功！");
        }
    }

    /**
     * 注销 资源信息
     * @param baseInfo 资源 基本信息
     */
    public void logoutResource(ResourceBaseInfo baseInfo) {
        if (resources.isResourceExist(baseInfo)) {
            resources.removeResource(baseInfo);
            me.logout(baseInfo);
        }
    }

    /**
     * 获取 资源 子文件列表 及 根路径
     * @param baseInfo 资源 基本信息
     * @return 资源 子文件列表 与 根路径
     */
    public SourceFileList getSourceFileList(ResourceBaseInfo baseInfo) {
        return resources.getSourceFileList(baseInfo);
    }

}
