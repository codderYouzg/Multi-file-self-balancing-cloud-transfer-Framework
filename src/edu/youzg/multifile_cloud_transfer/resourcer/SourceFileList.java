package edu.youzg.multifile_cloud_transfer.resourcer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.youzg.resource_founder.core.ResourceSpecificInfo;
import edu.youzg.resource_founder.exception.FileDoesNotExistException;

/**
 * 一个资源 的详细信息<br/>
 * 根路径、子文件信息 列表
 */
public class SourceFileList {
    private String absoluteRoot;    // 根路径
    private List<ResourceSpecificInfo> fileList;    // 子文件信息 列表

    public SourceFileList() {
        this.fileList = new ArrayList<ResourceSpecificInfo>();
    }

    public void setAbsoluteRoot(String absoluteRoot) {
        if (!absoluteRoot.endsWith(File.separator)) {
            this.absoluteRoot = absoluteRoot + File.separator;
        }
        this.absoluteRoot = absoluteRoot;
    }

    /**
     * 设置 文件信息列表
     *
     * @param fileList 文件信息列表
     */
    public void setFileList(List<ResourceSpecificInfo> fileList) {
        this.fileList = fileList;
    }

    /**
     * 根据 子文件编号，获取其大小
     * @param fileNo 子文件编号
     * @return 目标 子文件的大小
     */
    public long getFileSizeByFileNo(int fileNo) {
    	ResourceSpecificInfo temp = new ResourceSpecificInfo();
        temp.setFileNo(fileNo);
        ResourceSpecificInfo target = fileList.get(fileList.indexOf(temp));
        if (target==null) {
            return 0;
        }
        return target.getFileSize();
    }

    public String getAbsoluteRoot() {
        return absoluteRoot;
    }

    /**
     * 获取 子文件信息 列表
     * @return 子文件信息 列表
     */
    public List<ResourceSpecificInfo> getFileList() {
        return fileList;
    }

    /**
     * 向 SourceFileList 增加一个 子文件信息<br/>
     * 并更新其编号信息
     * @param filePath
     * @throws FileDoesNotExistException
     */
    public void addFile(String filePath) throws FileDoesNotExistException {
    	ResourceSpecificInfo fileInfo = new ResourceSpecificInfo();
        int fileNo = this.fileList.size() + 1;
        fileInfo.setFilePath(fileNo, this.absoluteRoot, filePath);
        this.fileList.add(fileInfo);
    }

    /**
     * 扫描 指定路径，将其中子文件，都存入fileList中
     * @param curPath 要扫描的路径
     */
    private void collectFiles(String curPath) {
        File curDir = new File(curPath);
        File[] files = curDir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                String filePath = file.getPath().replace(this.absoluteRoot, "");
                try {
                    addFile(filePath);
                } catch (FileDoesNotExistException e) {
                    e.printStackTrace();
                }
            } else if (file.isDirectory()) {
                collectFiles(file.getAbsolutePath());
            }
        }
    }

    /**
     * 扫描 根路径，将其中子文件，都存入fileList中
     */
    public void collectFiles() {
        if (this.absoluteRoot == null) {
            return;
        }
        collectFiles(absoluteRoot);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("资源根:").append(this.absoluteRoot).append('\n');
        for (ResourceSpecificInfo fileInfo : fileList) {
            result.append("\t").append(fileInfo).append('\n');
        }

        return result.toString();
    }

}
