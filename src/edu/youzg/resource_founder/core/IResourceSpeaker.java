package edu.youzg.resource_founder.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源信息发布者 接口
 */
public interface IResourceSpeaker {

    /**
     * 增加 指定的订阅者
     * @param listener 目标订阅者
     */
    default void addListener(IResourceListener listener) {
        List<IResourceListener> listenerList = getListenerList();
        if (listenerList == null) {
            synchronized (IResourceSpeaker.class) {
                listenerList = getListenerList();
                if (listenerList == null) {
                    listenerList = new ArrayList<>();
                    setListenerList(listenerList);
                }
            }
        }
        if (listenerList.contains(listener)) {
            return;
        }
        listenerList.add(listener);
    }

    /**
     * 移除 指定的订阅者
     * @param listener 指定的订阅者
     */
    default void removeListener(IResourceListener listener) {
        List<IResourceListener> listenerList = getListenerList();
        if (listenerList == null || !listenerList.contains(listener)) {
            return;
        }
        listenerList.remove(listener);
    }

    /**
     * 向所有订阅者 发布消息
     * @param message 要发布的消息
     */
    default void speakOut(String message) {
        List<IResourceListener> listenerList = getListenerList();
        if (listenerList == null || listenerList.isEmpty()) {
            return;
        }
        for (IResourceListener listener : listenerList) {
            listener.dealMessage(message);
        }
    }

    /**
     * 获取订阅者 列表
     * @return 订阅者 列表
     */
    List<IResourceListener> getListenerList();

    /**
     * 设置 订阅者 列表
     * @param listenerList 订阅者 列表
     */
    void setListenerList(List<IResourceListener> listenerList);

}