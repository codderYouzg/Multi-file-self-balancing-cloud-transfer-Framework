package edu.youzg.multifile_cloud_transfer.strategy;

import edu.youzg.network_transmission.section.FileSectionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件片段 的分发策略 接口<br/>
 * 默认按照 要分发的列表大小 均匀分发
 */
public interface IDistributionStrategy {

    /**
     * 分发文件片段 策略(按照 参数列表大小 均匀分配)
     * @param sectionList 要分发的 文件片段列表
     * @param count 发送端的个数
     * @return 每个发送端要发送的文件列表，的列表
     */
    default List<List<FileSectionInfo>> distributionFileSection(List<FileSectionInfo> sectionList, int count) {
        List<List<FileSectionInfo>> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            result.add(new ArrayList<>());  // 初始化容器(count个List)
        }

        // 按照 参数列表大小 均匀分配
        int index = 0;
        for (FileSectionInfo section : sectionList) {
            List<FileSectionInfo> sectionInfoList = result.get(index);
            sectionInfoList.add(section);
            index = (index + 1) % count;
        }

        return result;
    }

}
