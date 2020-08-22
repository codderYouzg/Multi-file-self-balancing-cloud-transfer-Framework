package edu.youzg.resource_founder.resourcer;

import edu.youzg.resource_founder.center.IResourceCenter;
import edu.youzg.rmi_impl.client.RMIClient;

/**
 * 封装 “访问 资源注册中心” 的基本属性
 */
public class Resourcer {
    protected RMIClient rmiClient;  // 请求 资源注册中心
    protected IResourceCenter irc;  // 所要请求执行的方法 执行接口

    protected Resourcer() {
        this.rmiClient = new RMIClient();
        this.irc = this.rmiClient.getProxy(IResourceCenter.class);
    }

    public void setRmiServerIp(String rmiServerIp) {
        this.rmiClient.setRmiServerIp(rmiServerIp);
    }

    public void setRmiServerPort(int rmiServerPort) {
        this.rmiClient.setRmiServerPort(rmiServerPort);
    }

}
