package edu.youzg.multifile_cloud_transfer.sender;

/**
 * 封装了 一个 发送者 的 “健康状况”：<br/>
 * 1. 当前并发数 —— sendconcurrentCount<br/>
 * 2. 发送总个数 —— sendTotalAmount<br/>
 * 主要 用于 “负载均衡”
 */
public class SendCounter {
    private volatile int sendconcurrentCount; // 当前并发数
    private volatile int sendTotalAmount;   // 发送总个数

    public SendCounter() {
        this.sendTotalAmount = 0;
        this.sendconcurrentCount = 0;
    }

    /**
     * 使得 当前并发数 - 1
     */
    public void incSendconcurrentCount() {
        ++sendconcurrentCount;
    }

    /**
     * 使得 当前并发数 + 1
     */
    public void decSendconcurrentCount() {
        --sendconcurrentCount;
    }

    /**
     * 使得 发送总个数 + 1
     */
    public void incSendTotalAmount() {
        ++sendTotalAmount;
    }

    /**
     * 获取 当前并发数
     *
     * @return 发送的总个数
     */
    public int getSendconcurrentCount() {
        return sendconcurrentCount;
    }

    /**
     * 获取 发送的总个数
     *
     * @return 发送的总个数
     */
    public int getSendTotalAmount() {
        return sendTotalAmount;
    }

}
