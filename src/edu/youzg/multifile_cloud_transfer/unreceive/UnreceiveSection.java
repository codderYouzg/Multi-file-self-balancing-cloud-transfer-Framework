package edu.youzg.multifile_cloud_transfer.unreceive;

import edu.youzg.multifile_cloud_transfer.exception.ReceiveSectionOutOfRangeException;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * 接收端
 * 维护了一个 未接受文件片段列表<br/>
 * 并提供了 增加未接收片段信息、接收指定的片段 的功能
 */
public class UnreceiveSection {
    private int fileNo;
    private List<SectionInfo> sectionList;

    private static Logger log = Logger.getLogger(UnreceiveSection.class);

    public UnreceiveSection(int fileNo) {
        this.fileNo = fileNo;
        this.sectionList = new LinkedList<>();
    }

    /**
     * 将整个文件都设置为“未接收状态”
     * @param fileNo 文件id
     * @param fileSize 文件大小
     */
    public UnreceiveSection(int fileNo, long fileSize) {
        this.fileNo = fileNo;
        this.sectionList = new LinkedList<>();
        this.sectionList.add(new SectionInfo(0, fileSize));
    }

    public int getFileNo() {
        return fileNo;
    }

    /**
     * 获取 还未接收 的文件片段 列表
     * @return 还未接收 的文件片段 列表
     */
    public List<SectionInfo> getSectionList() {
        return this.sectionList;
    }

    /**
     * 判断当前文件是否接收完毕
     *
     * @return 当前文件是否接收完毕
     */
    public boolean isReceived() {
        return this.sectionList.isEmpty();
    }

    /**
     * 添加一个 未接收片段信息
     * @param offset 目标文件片段的 偏移量
     * @param length 目标文件片段的 大小
     */
    public void addUnreceiveSection(long offset, long length) {
        this.sectionList.add(new SectionInfo(offset, length));
    }

    /**
     * 查询指定偏移量 所属 未接收文件片段列表中的下标
     * @param recOffset 要查询的偏移量
     * @return 其在 未接收文件片段列表 中的 下标
     * @throws ReceiveSectionOutOfRangeException
     */
    private int searchSection(long recOffset) throws ReceiveSectionOutOfRangeException {
        for (int i = 0; i < sectionList.size(); i++) {
            SectionInfo section = sectionList.get(i);
            if (section.isRange(recOffset)) {
                return i;
            }
        }

        throw new ReceiveSectionOutOfRangeException("文件号:" + fileNo + " 片段偏移量:" + recOffset);
    }

    /**
     * 接收端 接收到 文件片段后，善后操作
     * @param recOff 接收片段的 偏移量
     * @param recLen 接收片段的 大小
     * @throws ReceiveSectionOutOfRangeException
     */
    public void receiveSection(long recOff, long recLen)
            throws ReceiveSectionOutOfRangeException {
        int index = searchSection(recOff);
        SectionInfo curSection = sectionList.get(index);

        long curOff = curSection.getOffset();
        long curLen = curSection.getLength();

        // 因为接收的片段可能将 一个未接收片段 分为 三份：
        // 左仍未接收 + 接收到的片段 + 右仍未接收
        long lOff = curOff; // 左偏移量
        long lLen = recOff - curOff;    // 左大小
        long rOff = recOff + recLen;    // 右偏移量
        long rLen = curOff + curLen - rOff; // 右大小

        sectionList.remove(index);
        // 先判断右边是否读取完毕
        if (rLen > 0) {
            sectionList.add(index, new SectionInfo(rOff, rLen));
        }
        // 再判断右边是否读取完毕，
        // 若未完毕，插入在右边片段之前，保证了 “有序性”
        if (lLen > 0) {
            sectionList.add(index, new SectionInfo(lOff, lLen));
        }

        // 为了解决“接收端异常结束造成无法获取未接收片段信息”问题，
        // 需要在这里处理将所接收片段保存到 外存
        log.info("接收到文件[" + fileNo + "]的片段：" + "[" + recOff + ":" + recLen + "]");
    }

}
