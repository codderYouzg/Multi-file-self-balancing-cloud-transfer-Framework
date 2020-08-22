package edu.youzg.multifile_cloud_transfer.strategy;

import edu.youzg.balance.DefaultNetNode;

import java.util.List;

/**
 * 选取发送者 策略
 */
public interface ISenderSelectedStrategy {
    List<DefaultNetNode> selectSender(List<DefaultNetNode> senderList);
}
