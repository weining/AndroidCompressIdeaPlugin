package com.weining.ks.briefplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Created by shenjj on 2017/2/27.
 */
public class ProgressDialog extends DialogWrapper {
    private int maxImages = 0;
    private int currentIndex = 0;
    private int mCollectNum = 0;
    JPanel jPanel;
    JProgressBar jProgressBar;
    JProgressBar jProgressCollectBar;
//    JTextArea reminderText;
    JTextArea linkText;
    private Logger logger = Logger.getLogger("ProgressDialog");
    private CompressPngImagesAction compressImagesAction;

    public void setMaxImages(int maxImages) {
        this.maxImages = maxImages;
        jProgressCollectBar.setVisible(false);
        jProgressBar.setVisible(true);
        jProgressBar.setMaximum(maxImages);
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void setCollectNum(int collectNum) {
        mCollectNum = collectNum;
    }

    public ProgressDialog(@Nullable Project project, CompressPngImagesAction action) {
        super(project);
        init();
        compressImagesAction = action;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        jPanel = new JPanel();

        jProgressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, maxImages);
        jProgressBar.setString(currentIndex + "/" + maxImages);
        jProgressBar.setValue(currentIndex);
        jProgressBar.setStringPainted(true);
        jProgressBar.setVisible(false);

//        reminderText = new JTextArea("每月500张图片限制已用完,请获取新KEY");
//        reminderText.setVisible(false);
////        reminderText.setForeground(Color.CYAN);
//        reminderText.setBackground(new Color(255, 255, 255, 0));

        jPanel.setLayout(new GridLayout(2, 1));
        jPanel.add(jProgressBar);
        jProgressCollectBar = new JProgressBar(SwingConstants.HORIZONTAL);
        jProgressCollectBar.setString("收集可压缩png图片 : 0");
        jProgressCollectBar.setStringPainted(true);
        jProgressCollectBar.setIndeterminate(true);
        jPanel.add(jProgressCollectBar);
        setOKActionEnabled(false);
//        jPanel.add(reminderText);
        return jPanel;
    }

//    public void showError() {
//        jProgressBar.setVisible(false);
//        reminderText.setText(errorStr);
//        reminderText.setVisible(true);
//    }

    public void revalidate() {
        if (jProgressBar.isVisible()) {
            jProgressBar.setString(currentIndex + "/" + maxImages);
            jProgressBar.setValue(currentIndex);
            jProgressBar.revalidate();
            if (currentIndex == maxImages) {
                setOKActionEnabled(true);
            }
        }
        if (jProgressCollectBar.isVisible()) {
            jProgressCollectBar.setString("收集可压缩png图片 : " + mCollectNum);
            jProgressCollectBar.revalidate();
        }
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
        compressImagesAction.isCompressStoping = true;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void setTitle(@Nls(capitalization = Nls.Capitalization.Title) String title) {
        super.setTitle(title);
    }
}
