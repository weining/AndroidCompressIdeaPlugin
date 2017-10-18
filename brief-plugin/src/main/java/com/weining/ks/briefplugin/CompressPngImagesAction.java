package com.weining.ks.briefplugin;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.pngquant.PngQuant;
import org.pngquant.Constant;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CompressPngImagesAction extends AnAction {
    private static final Logger logger = Logger.getInstance(CompressPngImagesAction.class);
    private Project mProject;
    private ArrayList<VirtualFile> pictureFiles = new ArrayList<>();
    private static final String LIB_PATH = "lib_path";
    private static int currentIndex = 0;
    private ProgressDialog progressDialog;
    protected boolean isCompressStoping = false;

    public void actionPerformed(AnActionEvent event) {
        logger.info("start");
        mProject = event.getProject();

        String libPath = PropertiesComponent.getInstance(mProject).getValue(LIB_PATH);
        try {
            if (TextUtils.isEmpty(libPath)) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
                descriptor.setTitle("选择libimagequant.jnilib or libimagequant.dll文件");
                VirtualFile file = FileChooser.chooseFile(descriptor, mProject, null);
                if (file == null) {
                    return;
                }
                Constant.libPath = file.getCanonicalPath();
                PngQuant.loadLib(Constant.libPath);
                new PngQuant();
                PropertiesComponent.getInstance(mProject).setValue(LIB_PATH, Constant.libPath);
            }
            else {
                Constant.libPath = libPath;
                PngQuant.loadLib(Constant.libPath);
                new PngQuant();
            }
        }
        catch (UnsatisfiedLinkError e) {
            PropertiesComponent.getInstance(mProject).setValue(LIB_PATH, "");
        }

        logger.info("after load lib");
        FileChooserDescriptor descriptorImage = new FileChooserDescriptor(true, true, false, false, false, true);
        descriptorImage.setTitle("选择需要压缩的图像文件(夹)");

        logger.info("after files select");
        VirtualFile[] selectedFiles = FileChooser.chooseFiles(descriptorImage, mProject, null);
        if (selectedFiles.length == 0) {
            return;
        }
        pictureFiles.clear();
        isCompressStoping = false;
        filterAllPictures(selectedFiles);
        progressDialog = new ProgressDialog(mProject, this);
        progressDialog.setTitle("处理进度");
        progressDialog.revalidate();
        progressDialog.setModal(false);
        progressDialog.show();
    }

    private void filterAllPictures(VirtualFile[] selectedFiles) {
        logger.info("begin filterAllPictures");
        new Thread(() -> {
            try {
                for (int i = 0; i < selectedFiles.length; i++) {
                    logger.info("for filterAllPictures");
                    if (isCompressStoping) {
                        return;
                    }
                    VirtualFile selectedFile = selectedFiles[i];
                    logger.info("for filterAllPictures 89");
                    if (selectedFile.isDirectory()) {
                        VfsUtilCore.visitChildrenRecursively(selectedFile, new VirtualFileVisitor() {
                            @Override
                            public boolean visitFile(@NotNull VirtualFile file) {
                                logger.info("for filterAllPictures 94");
                                if (!file.isDirectory()) {
                                    if (file.getPath().contains("src/main/res")) {
                                        if (file.getExtension() != null
                                                && (file.getExtension().equals("png") && !file.getName().endsWith(".9.png"))) {
                                            pictureFiles.add(file);
                                            progressDialog.setTitle(file.getPath());
                                            progressDialog.setCollectNum(pictureFiles.size());
                                            progressDialog.revalidate();
                                            logger.info("add file" + file.getPath());
                                        }
                                    }
                                }
                                return true;
                            }
                        });
                    } else if (selectedFile.getPath().contains("src/main/res")) {
                        logger.info("for filterAllPictures 111");
                        if (selectedFile.getExtension() != null
                                && (selectedFile.getExtension().equals("png") && !selectedFile.getName().endsWith(".9.png"))) {
                            pictureFiles.add(selectedFile);
                            progressDialog.setTitle(selectedFile.getPath());
                            progressDialog.setCollectNum(pictureFiles.size());
                            progressDialog.revalidate();
                            logger.info("add file" + selectedFile.getPath());
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.error(e);
            }
            logger.info("for filterAllPictures 124");
            progressDialog.setMaxImages(pictureFiles.size());
            compress();
        }).start();
    }

    private void compress() {
        currentIndex = 0;
        new Thread(() -> {
            PngQuant png = new PngQuant();
            for (int i = 0; i < pictureFiles.size(); i++) {
                if (isCompressStoping) {
                    if (png != null) {
                        png.close();
                    }
                    return;
                }
                VirtualFile virtualFile = pictureFiles.get(i);
                try {
                    File file = new File(virtualFile.getPath());
                    progressDialog.setTitle(virtualFile.getPath());
                    String extension = virtualFile.getExtension();
                    BufferedImage result = png.getRemapped(ImageIO.read(file));
                    File tempFile = new File(virtualFile.getPath() + ".temp");
                    ImageIO.write(result, extension == null ? "png" : extension, tempFile);
                    if (tempFile.length() > file.length()) {
                        tempFile.delete();
                    }
                    else {
                        file.delete();
                        tempFile.renameTo(file);
                    }
                }
                catch (IOException e) {
                    System.console().printf(e.getMessage());
                }
                catch (Exception e) {

                }
                currentIndex++;
                progressDialog.setCurrentIndex(currentIndex);
                progressDialog.revalidate();
            }
            png.close();
        }).start();
//        ExecutorService executorService = Executors.newCachedThreadPool();
//        for (int i = 0; i < pictureFiles.size(); i++) {
//            VirtualFile virtualFile = pictureFiles.get(i);
//            executorService.submit(new CompressRunnable(virtualFile));
//        }
//        executorService.shutdown();
    }

//    public class CompressRunnable implements Runnable {
//        VirtualFile virtualFile;
//        public CompressRunnable(VirtualFile file) {
//            virtualFile = file;
//        }
//
//        @Override
//        public void run() {
//            PngQuant png = new PngQuant();
//            try {
//                File file = new File(virtualFile.getPath());
//                String extension = virtualFile.getExtension();
//                BufferedImage result = png.getRemapped(ImageIO.read(file));
//                ImageIO.write(result, extension == null ? "png" : extension, file);
//                currentIndex++;
//                progressDialog.setCurrentIndex(currentIndex);
//                progressDialog.revalidate();
//                png.close();
//            }
//            catch (IOException e) {
//
//            }
//        }
//    }
}