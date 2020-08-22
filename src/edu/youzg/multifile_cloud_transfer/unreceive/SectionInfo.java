package edu.youzg.multifile_cloud_transfer.unreceive;

/**
 * 封装了 偏移量、文件片段长度 的类
 */
public class SectionInfo {
    private long offset;
    private long length;

    public SectionInfo() {
    }

    public SectionInfo(long offset, long length) {
        this.offset = offset;
        this.length = length;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getOffset() {
        return offset;
    }

    public long getLength() {
        return length;
    }

    /**
     * 判断所传 偏移量 是否在当前未接收片段 中
     * @param offset 要判断的偏移量
     * @return 是否在当前未接收片段中
     */
    public boolean isRange(long offset) {
        return this.offset + this.length > offset;
    }

    @Override
    public String toString() {
        return offset + " : " + length;
    }

}
