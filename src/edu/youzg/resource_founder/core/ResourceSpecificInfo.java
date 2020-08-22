package edu.youzg.resource_founder.core;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import edu.youzg.resource_founder.exception.FileDoesNotExistException;

/**
 * (单)子资源详细信息<br/>
 * 可能是目标文件的 子文件，也可能是目标文件<br/>
 * 文件编号、文件所在本地位置、文件大小
 */
public class ResourceSpecificInfo implements Serializable {	// 序列化，防止fastjson转换失败
	private static final long serialVersionUID = 7198662964849667723L;

	private int fileNo; // 文件的编号
    private String filePath;    // 文件所在本地相对位置(相对根目录 的路径)
    private long fileSize;  // 文件大小

    public ResourceSpecificInfo() {
    }

    /**
     * 设置文件路径，<br/>
     * 并根据设置的文件路径，初始化成员属性
     * @param fileNo 文件编号
     * @param absoluteRoot 该文件 的根路径(可能是文件夹)
     * @param filePath 当前(子)文件路径
     * @throws FileDoesNotExistException
     */
    public void setFilePath(int fileNo, String absoluteRoot, String filePath) throws FileDoesNotExistException {
        this.fileNo = fileNo;
        String absoluteFilePath = absoluteRoot + filePath;  // 计算文件的 真实完整路径
        File file = new File(absoluteFilePath);
        if (!file.exists()) {
            throw new FileDoesNotExistException("文件[" + absoluteFilePath + "]不存在！");
        }

        this.filePath = filePath;
        this.fileSize = file.length();
    }

    public void setFileNo(int fileNo) {
        this.fileNo = fileNo;
    }

    public int getFileNo() {
        return fileNo;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceSpecificInfo fileInfo = (ResourceSpecificInfo) o;
        return fileNo == fileInfo.fileNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileNo);
    }

    @Override
    public String toString() {
        return fileNo + " : " + filePath + " : " + fileSize;
    }

}
