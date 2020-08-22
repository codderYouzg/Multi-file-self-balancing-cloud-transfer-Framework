package edu.youzg.multifile_cloud_transfer.view;

import edu.youzg.util.ISwingHelper;

import javax.swing.*;
import java.awt.*;

/**
 * (单个文件) 接收进度条 的panel
 */
public class ReceiveFileProgress extends JPanel {
	private static final long serialVersionUID = 1017850420343075206L;
	public static final int HEIGHT = 30;
	
	private long receivedLen;	// “已接收” 的长度
	private long fileSize;	// 文件大小
	
	private JProgressBar jpgbReceiveFile;	// 进度条

	/**
	 * 初始化一个 带进度条的panel
	 * @param jpnlParent 父容器
	 * @param fileName 表示的 文件名称
	 * @param fileSize 表示的 文件大小
	 */
	public ReceiveFileProgress(JPanel jpnlParent, String fileName, long fileSize) {
		this(jpnlParent, fileName, 0L, fileSize);
	}

	/**
	 * 初始化一个 带进度条的panel，并设置其 初始进度
	 * @param jpnlParent 父容器
	 * @param fileName 表示的 文件名称
	 * @param receivedLen 初始进度
	 * @param fileSize 表示的 文件大小
	 */
	public ReceiveFileProgress(JPanel jpnlParent, String fileName, long receivedLen, long fileSize) {
		this.fileSize = fileSize;
		this.receivedLen = receivedLen;
		this.setLayout(new GridLayout(0, 1));
		int parentWidth = jpnlParent.getWidth();
		this.setSize(parentWidth, HEIGHT);
		
		JLabel jlblFileName = new JLabel(fileName, JLabel.CENTER);
		jlblFileName.setFont(ISwingHelper.normalFont);
		this.add(jlblFileName);
		
		this.jpgbReceiveFile = new JProgressBar();
		this.jpgbReceiveFile.setFont(ISwingHelper.normalFont);
		this.jpgbReceiveFile.setStringPainted(true);
		this.jpgbReceiveFile.setMaximum((int) this.fileSize);	// 设置 显示的最大值
		this.jpgbReceiveFile.setValue((int) this.receivedLen);	// 设置进度条 显示的进度
		this.add(this.jpgbReceiveFile);	// 将此进度条 加入当前 ReceiveFileProgress
	}

	/**
	 * 根据接收的长度，改变 进度条的进度的 值
	 * @param receiveLength 接收的长度
	 */
	public void receiveFileSection(int receiveLength) {
		this.receivedLen += receiveLength;
		this.jpgbReceiveFile.setValue((int) this.receivedLen);
	}

}
