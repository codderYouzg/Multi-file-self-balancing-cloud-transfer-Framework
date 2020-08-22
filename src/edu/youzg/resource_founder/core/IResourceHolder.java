package edu.youzg.resource_founder.core;

/**
 * 判断 当前资源拥有者是否存活
 */
public interface IResourceHolder {
    default boolean isActive() throws Exception {
        return  false;
    }
}
