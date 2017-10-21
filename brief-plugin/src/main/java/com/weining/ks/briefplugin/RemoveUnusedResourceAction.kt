package com.weining.ks.briefplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.io.File

class RemoveUnusedResourceAction : AnAction() {
    var mProject : Project? = null
    val mResourceFiles : ArrayList<VirtualFile> = ArrayList()

    override fun actionPerformed(e: AnActionEvent?) {
        mProject = e!!.project
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
        descriptor.title = "选择resource.txt文件"
        val removeUnusedResourceVF = FileChooser.chooseFile(descriptor, mProject, null) ?: return
        val file = File(removeUnusedResourceVF.path)

//        val descriptorImage = FileChooserDescriptor(false, true, false, false, false, false)
//        descriptorImage.title = "选择需要删除的project"
//        val selectedFiles = FileChooser.chooseFile(descriptorImage, mProject, null) ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(mProject, "删除unused资源") {
            override fun run(indicator: ProgressIndicator) {
                VfsUtilCore.visitChildrenRecursively(mProject!!.baseDir, object : VirtualFileVisitor<Unit>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (file.isDirectory && file.path.contains("/build")) {
                            return false
                        }
                        if (!file.isDirectory) {
                            if (file.path.contains("src/main/res") && !file.path.contains("/values/")) {
                                mResourceFiles.add(file)
                            }
                        }
                        return true
                    }
                })
                file.readLines().filter {
                    it.startsWith("Skipped unused resource")
                }.map {
                    it.substring(it.lastIndexOf("/")+1, it.indexOf(":"))
                }.forEach { fileName ->
                    mResourceFiles.forEach {
                        if (it.name == fileName) {
                            it.delete(null)
                        }
                    }
                }
            }
        })

    }
}