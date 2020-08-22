package edu.youzg.multifile_cloud_transfer.view;

import edu.youzg.util.ISwingHelper;
import edu.youzg.util.UnitConverter;
import edu.youzg.util.exceptions.FrameIsNullException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接收进度 模态框
 */
public abstract class ReceiveProgressDialog extends JDialog implements ISwingHelper {
    private static final long serialVersionUID = -7786721631557848720L;
    public static final int WIDTH = 340;
    public static final int MIN_HEIGHT = 170;

    public static final String RECEIVE_FILE_TOTAL_COUNT = "本次共接收#个文件";

    private int receiveFileCount;   // 要接收的 文件数量
    private JLabel jlblReceiveFileCount;
    private JProgressBar jpgbReceiveFileCount;  // 当前资源的 子文件接收进度 的 进度条

    private JLabel jlblCurrentSpeed;
    private JLabel jlblAverageSpeed;
    private JPanel jpnlFiles;

    private AtomicInteger tmpCount;

    private Map<Integer, ReceiveFileProgress> receiveFilePool;  // 以文件编号为键，该文件的进度条为值，存储的map

    /**
     * 初始化一个进度条，使其放置在指定的父容器内，并显示
     * @param parent 父容器
     * @param receiveFileCount 要接收的文件数量
     */
    public ReceiveProgressDialog(JFrame parent, int receiveFileCount) {
        super(parent, true);
        this.receiveFileCount = receiveFileCount;
        this.tmpCount = new AtomicInteger(0);
        this.receiveFilePool = new HashMap<>();
        initView();
    }

    /**
     * 移除一个正在接收的文件的接收进度
     * @param fileNo 要移除的文件编号
     */
    public void removeReceiveFile(int fileNo) {
        ReceiveFileProgress receiveFileProgress =
                this.receiveFilePool.get(fileNo);
        if (receiveFileProgress != null) {
            this.jpnlFiles.remove(receiveFileProgress);
            receiveFilePool.remove(fileNo);

            // 更改 资源的子文件 的接收进度
            int count = tmpCount.incrementAndGet();
            this.jpgbReceiveFileCount.setString(count + " / " + this.receiveFileCount); // 设置 进度的 字符串
            this.jpgbReceiveFileCount.setValue(count);  // 设置 进度的 值

            // 重新调整界面大小
            resizeDialog();
        }
    }

    /**
     * 判断指定文件是否存在
     * @param fileNo 指定文件的编号
     * @return 指定文件是否存在
     */
    public boolean receiveFileExist(int fileNo) {
        return this.receiveFilePool.containsKey(fileNo);
    }

    /**
     * 根据 接收文件片段，改变进度条的显示
     * @param fileNo 文件片段所属文件的编号
     * @param receiveLen 接收到的长度
     */
    public void receiveFileSection(int fileNo, int receiveLen) {
        ReceiveFileProgress receiveFileProgress = this.receiveFilePool.get(fileNo);
        receiveFileProgress.receiveFileSection(receiveLen);
    }

    /**
     * 更新 速度的显示值
     * @param allSize 总 接收大小
     * @param curSize 当前时刻 接收大小
     */
    public void updataSpeed(long allSize, long curSize) {
        this.jlblAverageSpeed.setText(UnitConverter.sizeToByte(allSize, UnitConverter.CHAR_CN));
        this.jlblCurrentSpeed.setText(UnitConverter.sizeToByte(curSize, UnitConverter.CHAR_CN));
    }

    /**
     * 增加 要接收的 文件 的进度条
     * @param fileNo 目标文件的 编号
     * @param fileName 目标文件的 文件名
     * @param fileSize 目标文件的 大小
     */
    public void addReceiveFile(int fileNo, String fileName, long fileSize) {
        addReceiveFile(fileNo, fileName, 0L, fileSize);
    }

    /**
     * 增加 要接收的 文件 的进度条，可设置初始接收长度(为“断点续传”打下基础)
     * @param fileNo 目标文件的 编号
     * @param fileName 目标文件的 文件名
     * @param receivedSize
     * @param fileSize 目标文件的 大小
     */
    public void addReceiveFile(int fileNo, String fileName, long receivedSize, long fileSize) {
        ReceiveFileProgress receiveFileProgress =
                new ReceiveFileProgress(jpnlFiles, fileName, receivedSize, fileSize);
        this.receiveFilePool.put(fileNo, receiveFileProgress);
        this.jpnlFiles.add(receiveFileProgress);
        resizeDialog();
    }

    /**
     * 根据当前正在接收的文件数量，调整模态框的大小
     */
    private void resizeDialog() {
        int receiveFileProgressCount = this.receiveFilePool.size();
        int height = MIN_HEIGHT
                + receiveFileProgressCount * (ReceiveFileProgress.HEIGHT)
                + (receiveFileProgressCount > 1
                ? (receiveFileProgressCount - 1) * ISwingHelper.PADDING * 3
                : 0);
        this.setSize(WIDTH, height);
        this.setLocationRelativeTo(this.getParent());   // 将当前模态框放置到父容器中，并居中对齐
    }

    @Override
    public void reinit() {}

    /**
     * 模态框 显示之后的操作
     */
    public abstract void afterDialogShow();

    /**
     * 根据指定大小，更改 RECEIVE_FILE_TOTAL_COUNT 的值，并 返回
     *
     * @param fileCount 指定大小
     * @return 更改后的 RECEIVE_FILE_TOTAL_COUNT 的值
     */
    private String getFileCountString(int fileCount) {
        return RECEIVE_FILE_TOTAL_COUNT.replaceAll("#", String.valueOf(fileCount));
    }

    /**
     * 因为 模态框线程一旦启动，就会阻塞主线程，<br/>
     * 使得我们无法通过外界来关闭模态框<br/>
     * 因此，此处在模态框获取焦点后(即：显示后)为其增加一个 事件监听器
     */
    @Override
    public void dealEvent() {
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                afterDialogShow();
            }
        });
    }

    @Override
    public RootPaneContainer getFrame() {
        return this;
    }

    /**
     * 初始化 模态框
     */
    @Override
    public void init() {
        this.setSize(WIDTH, MIN_HEIGHT);
        this.setLocationRelativeTo(this.getParent());
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setResizable(false);

        JLabel jlblEast = new JLabel(" ");
        jlblEast.setFont(smallFont);
        this.add(jlblEast, BorderLayout.EAST);

        JLabel jlblWest = new JLabel(" ");
        jlblWest.setFont(smallFont);
        this.add(jlblWest, BorderLayout.WEST);

        // 标题和文件接收数量
        JPanel jpnlHead = new JPanel(new BorderLayout());
        this.add(jpnlHead, BorderLayout.NORTH);

        JLabel jlblHeadEast = new JLabel(" ");
        jlblHeadEast.setFont(smallFont);
        jpnlHead.add(jlblHeadEast, BorderLayout.EAST);

        JLabel jlblHeadWest = new JLabel(" ");
        jlblHeadWest.setFont(smallFont);
        jpnlHead.add(jlblHeadWest, BorderLayout.WEST);

        JLabel jlblTopic = new JLabel("文件接收进度", JLabel.CENTER);
        jlblTopic.setFont(topicFont);
        jlblTopic.setForeground(topicColor);
        jpnlHead.add(jlblTopic, BorderLayout.NORTH);

        JPanel jpnlFileCount = new JPanel(new GridLayout(0, 1));
        jpnlHead.add(jpnlFileCount, BorderLayout.CENTER);

        this.jlblReceiveFileCount = new JLabel(
                getFileCountString(this.receiveFileCount), JLabel.CENTER);
        this.jlblReceiveFileCount.setFont(normalFont);
        jpnlFileCount.add(this.jlblReceiveFileCount);

        this.jpgbReceiveFileCount = new JProgressBar(0, receiveFileCount);
        this.jpgbReceiveFileCount.setFont(normalFont);
        this.jpgbReceiveFileCount.setStringPainted(true);
        this.jpgbReceiveFileCount.setString("0 / " + this.receiveFileCount);
        jpnlFileCount.add(this.jpgbReceiveFileCount);

        // 设置 文件接收进度 面板(JPanel)
        jpnlFiles = new JPanel();
        GridLayout gdltFiles = new GridLayout(0, 1);    // 网格布局，1行1列
        gdltFiles.setVgap(PADDING);
        jpnlFiles.setLayout(gdltFiles);
        this.add(jpnlFiles, BorderLayout.CENTER);

        // 设置 网速监控 面板(JPanel)
        JPanel jpnlFooter = new JPanel(new GridLayout(1, 2));
        this.add(jpnlFooter, BorderLayout.SOUTH);   // 将这个面板 置为 模态框底部

        JPanel jpnlCurrentSpeed = new JPanel(new FlowLayout(FlowLayout.LEFT));  // 当前网速 面板
        jpnlFooter.add(jpnlCurrentSpeed);

        JLabel jlblCurrentSpeedTopic = new JLabel("当前速率 : ");
        jlblCurrentSpeedTopic.setFont(smallFont);
        jpnlCurrentSpeed.add(jlblCurrentSpeedTopic);

        this.jlblCurrentSpeed = new JLabel(
                UnitConverter.sizeToByte(0L, UnitConverter.CHAR_CN));
        this.jlblCurrentSpeed.setFont(smallFont);
        jpnlCurrentSpeed.add(this.jlblCurrentSpeed);

        JPanel jpnlAverageSpeed = new JPanel(new FlowLayout(FlowLayout.LEFT));  // 平均网速 面板
        jpnlFooter.add(jpnlAverageSpeed);

        JLabel jlblAverageSpeedTopic = new JLabel("平均速率 : ");
        jlblAverageSpeedTopic.setFont(smallFont);
        jpnlAverageSpeed.add(jlblAverageSpeedTopic);

        this.jlblAverageSpeed = new JLabel(
                UnitConverter.sizeToByte(0L, UnitConverter.CHAR_CN));
        this.jlblAverageSpeed.setFont(smallFont);
        jpnlAverageSpeed.add(this.jlblAverageSpeed);
    }

    public void closeView() {
        try {
            exitView();
        } catch (FrameIsNullException e) {
            e.printStackTrace();
        }
    }

}
