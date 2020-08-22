package edu.youzg.resource_founder.core;

/**
 * 此类并未完成，若有需要，请使用者自行实现！
 * 判断 当前资源拥有者 是否 存活<br/>
 * 此处可以添加 负载均衡策略
 */
public class ResourceHolderImpl implements IResourceHolder {
    public ResourceHolderImpl() {
    }

    @Override
    public boolean isActive() throws Exception {
        return false;
    }
}
