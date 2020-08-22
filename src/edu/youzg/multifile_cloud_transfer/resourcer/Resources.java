package edu.youzg.multifile_cloud_transfer.resourcer;

import java.util.HashMap;
import java.util.Map;

import edu.youzg.resource_founder.core.ResourceBaseInfo;

/**
 * 注册中心 存储 资源信息与其内容映射关系 的池子<br/>
 * 提供了 单例构造、多例构造
 */
public class Resources {
    private static Map<ResourceBaseInfo, SourceFileList> resourcePool;
    private static volatile Resources me;

    /**
     * (多例式)构造
     */
    public Resources() {
        resourcePool = new HashMap<>();
    }

    /**
     * (DCL单例式)构造
     * @return
     */
    public static Resources newInstance() {
        if (me == null) {
            synchronized (Resources.class) {
                if (me == null) {
                    me = new Resources();
                }
            }
        }
        return me;
    }

    /**
     * 加入 资源信息
     * @param resourceInfo 资源信息
     * @param sourceFileList 资源 根目录 及 子文件列表
     */
    public void addResource(ResourceBaseInfo resourceInfo, SourceFileList sourceFileList) {
        resourcePool.put(resourceInfo, sourceFileList);
    }

    /**
     * 删除 指定的资源信息
     * @param resourceInfo 资源信息
     */
    public void removeResource(ResourceBaseInfo resourceInfo) {
        resourcePool.remove(resourceInfo);
    }

    /**
     * 根据指定的 资源信息，<br/>
     * 获取其 根目录 及 子文件列表
     * @param resourceInfo 指定的 资源信息
     * @return 根目录 及 子文件列表
     */
    public SourceFileList getSourceFileList(ResourceBaseInfo resourceInfo) {
        return resourcePool.get(resourceInfo);
    }

    /**
     * 通过 资源信息，判断该是否存在
     * @param resourceInfo 目标资源信息
     * @return 目标资源是否存在
     */
    public boolean isResourceExist(ResourceBaseInfo resourceInfo) {
        return resourcePool.containsKey(resourceInfo);
    }

}
