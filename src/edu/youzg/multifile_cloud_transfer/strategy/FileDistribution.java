package edu.youzg.multifile_cloud_transfer.strategy;

import java.util.ArrayList;
import java.util.List;

import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;

/**
 * 文件分发器<br/>
 * 提供 拆分每个文件到文件片段的功能：<br/>
 * distributeSection
 */
public class FileDistribution {
    public static final int DEFAULT_SECTION_SIZE = 1 << 23; // 默认 最大片段大小

    private int maxSectionSize; // 最大片段大小
    private IDistributionStrategy distributionStrategy; // 分发策略

    public FileDistribution() {
        this.maxSectionSize = DEFAULT_SECTION_SIZE;
        this.distributionStrategy = new DefaultDistributionStrategy();
    }

    public FileDistribution setMaxSectionSize(int maxSectionSize) {
        this.maxSectionSize = maxSectionSize;
        return this;
    }

    public FileDistribution setDistributionStrategy(IDistributionStrategy distributionStrategy) {
        this.distributionStrategy = distributionStrategy;
        return this;
    }

    /**
     * 分发 文件片段列表
     * @return 分配好的 文件列表 的列表
     */
    public List<List<FileSectionInfo>> distributeSection(SourceFileList sourceFileList, int count) {
        List<FileSectionInfo> sectionList = new ArrayList<>();

        List<ResourceSpecificInfo> fileList = sourceFileList.getFileList();
        for (ResourceSpecificInfo fileInfo : fileList) {    // 将每个文件，分成文件片段
            int fileNo = fileInfo.getFileNo();
            long fileSize = fileInfo.getFileSize();
            int len;
            long offset = 0;
            while (fileSize > 0) {
                len = (int) (fileSize > maxSectionSize ? maxSectionSize : fileSize);
                FileSectionInfo section = new FileSectionInfo(fileNo, offset, len);
                sectionList.add(section);

                fileSize -= len;
                offset += len;
            }
        }
        
        List<List<FileSectionInfo>> result = distributionStrategy.distributionFileSection(sectionList, count);

        return result;
    }

    /**
     * 按照 文件片段列表 和 发送端数量，分配 文件片段列表
     * @param sectionList 文件片段列表
     * @param count 发送端数量
     * @return 分配好的 文件片段列表
     */
    public List<List<FileSectionInfo>> getFileSectionInfoListList(List<FileSectionInfo> sectionList, int count) {
        return this.distributionStrategy.distributionFileSection(sectionList, count);
    }

}
