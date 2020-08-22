package edu.youzg.multifile_cloud_transfer.receive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.youzg.multifile_cloud_transfer.resourcer.SourceFileList;
import edu.youzg.multifile_cloud_transfer.unreceive.SectionInfo;
import edu.youzg.multifile_cloud_transfer.unreceive.UnreceiveSection;
import edu.youzg.network_transmission.core.FileReadWrite;
import edu.youzg.network_transmission.section.FileSectionInfo;
import edu.youzg.network_transmission.section.IFileReadWriteIntercepter;
import edu.youzg.resource_founder.core.ResourceSpecificInfo;

/**
 * 一个资源 的 所有子文件 池<br/>
 * 主要用于 操作 每个子文件的FileReadWrite和UnreceiveSection
 */
public class ResourceFilePool {
    // 资源id为键，当前文件的 FileReadWrite 对象为值
    private Map<Integer, FileReadWrite> fileAcceptPool;
    // 资源id为键，当前文件的未接收片段集合为值
    private Map<Integer, UnreceiveSection> fileUnreceivePool;
    private IFileReadWriteIntercepter fileReadWriteIntercepter; // 接收片段 拦截器

    public ResourceFilePool() {
        this.fileAcceptPool = new HashMap<>();
        this.fileUnreceivePool = new HashMap<>();
    }

    public void setFileReadWriteIntercepter(IFileReadWriteIntercepter fileReadWriteIntercepter) {
        this.fileReadWriteIntercepter = fileReadWriteIntercepter;
    }

    /**
     * 向两个池子中 增加指定的文件信息
     * @param fileNo 文件id
     * @param filePath 文件所在路径
     * @param fileSize 文件大小
     */
    public void addFileInfo(int fileNo, String filePath, long fileSize) {
        FileReadWrite readWrite = new FileReadWrite(fileNo, filePath);
        if (this.fileReadWriteIntercepter != null) {
            readWrite.setFileReadWriteIntercepter(this.fileReadWriteIntercepter);
        }
        this.fileAcceptPool.put(fileNo, readWrite);

        UnreceiveSection unreceiveSection = new UnreceiveSection(fileNo, fileSize);
        this.fileUnreceivePool.put(fileNo, unreceiveSection);
    }

    /**
     * 向两个池子中，增加一个资源的所有子文件的信息
     * @param srcFileList 一个资源的所有子文件的信息
     */
    public void addFileList(SourceFileList srcFileList) {
        List<ResourceSpecificInfo> fileList = srcFileList.getFileList();

        for (ResourceSpecificInfo fileInfo : fileList) {
            int fileNo = fileInfo.getFileNo();
            FileReadWrite readWrite = new FileReadWrite(fileNo, srcFileList.getAbsoluteRoot() + fileInfo.getFilePath());
            // TODO 这里的拦截器需要设置
            if (this.fileReadWriteIntercepter != null) {
                readWrite.setFileReadWriteIntercepter(this.fileReadWriteIntercepter);
            }
            fileAcceptPool.put(fileInfo.getFileNo(), readWrite);

            //最开始没有进行收发的时候，所有文件都是未接收状态
            UnreceiveSection unreceiveSection = new UnreceiveSection(fileNo, fileInfo.getFileSize());
            this.fileUnreceivePool.put(fileNo, unreceiveSection);
        }
    }

    /**
     * 移除 一个未接收片段
     * @param fileNo 目标片段的 序号
     */
    public void removeUnreceiveSection(int fileNo) {
        this.fileUnreceivePool.remove(fileNo);
    }

    /**
     * 判断当前资源 是否 接收完毕
     * @return 当前资源 是否 接收完毕
     */
    public boolean isEmpty() {
        return this.fileUnreceivePool.isEmpty();
    }

    /**
     * 通过子文件的序号，获取其 FileReadWrite对象
     * @param fileNo 子文件的序号
     * @return 该子文件的 FileReadWrite对象
     */
    public FileReadWrite getFileAccept(int fileNo) {
        return this.fileAcceptPool.get(fileNo);
    }

    /**
     * 通过子文件的序号，获取其未接收片段的信息
     * @param fileNo 子文件的序号
     * @return 未接收片段的信息
     */
    public UnreceiveSection getUnreceiveSection(int fileNo) {
        return this.fileUnreceivePool.get(fileNo);
    }

    /**
     * 根据 fileNo 获取 相应的FileReadWrite
     * @param fileNo 指定的文件 的编号
     * @return 相应的FileReadWrite
     */
    public FileReadWrite getFileReadWriteByFileNo(int fileNo) {
        return fileAcceptPool.get(fileNo);
    }

    /**
     * 获取 未接收文件片段 列表
     * @return 未接收文件片段 列表
     */
    public List<FileSectionInfo> getUnreceiveSectionList() {
        List<FileSectionInfo> fileSectionInfoList = new ArrayList<FileSectionInfo>();
        UnreceiveSection unreceiveSection = null;
        List<SectionInfo> sectionList = null;

        for (Integer key : fileUnreceivePool.keySet()) {
            unreceiveSection = fileUnreceivePool.get(key);
            int fileNo = unreceiveSection.getFileNo();
            sectionList = unreceiveSection.getSectionList();
            for (SectionInfo section : sectionList) {
                int length = (int) section.getLength();
                long offset = section.getOffset();

                FileSectionInfo sectionInfo = new FileSectionInfo(fileNo, offset, (int)length);
                fileSectionInfoList.add(sectionInfo);
            }
        }
        return fileSectionInfoList;
    }

    /**
     * 展示 断点信息:<br/>
     * 即：启动一次 接收线程 后，还未接收的片段(即为：断点)
     */
    public void showBreakPoint() {
        for (Integer key : fileUnreceivePool.keySet()) {
            UnreceiveSection unreceiveSection = fileUnreceivePool.get(key);
            System.out.println(unreceiveSection.getFileNo() + " : " + unreceiveSection.getSectionList());
        }
    }

}
