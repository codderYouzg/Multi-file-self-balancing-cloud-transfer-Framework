package edu.youzg.resource_founder.core;

/**
 * 资源信息订阅者 接口
 */
public interface IResourceListener {
	void dealMessage(String message);
}