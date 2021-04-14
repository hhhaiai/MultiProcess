package com.analysys.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

public class AllStringTransform extends Transform {
    Project project
    boolean islib;

    AllStringTransform(Project project, boolean islib) {
        this.project = project
        this.islib = islib
    }

    @Override
    String getName() {
        return "AllStrMix"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        if (islib) {
            Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT,
            );
        } else {
            TransformManager.SCOPE_FULL_PROJECT
        }
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '//===============asm visit ReplaceStrMix start===============//'
        def startTime = System.currentTimeMillis()

        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name
                        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                !"R.class".equals(name) && !"BuildConfig.class".equals(name)
                                && !name.contains("StrMix")) {

                            byte[] injData = repStrMix(file.bytes)
                            byte[] code = allStrMix(injData)

                            FileOutputStream fos = new FileOutputStream(
                                    file.parentFile.absolutePath + File.separator + name)
                            fos.write(code)
                            fos.close()
                        }
                    }
                }

                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)


                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)

                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        def cost = (System.currentTimeMillis() - startTime) / 1000

        println "plugin cost $cost secs"
        println '//===============asm visit end===============//'
    }

    private byte[] allStrMix(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        //由 lysys2020ana base64而来
        ClassVisitor cv = new AllStrMixClassVisitor("bHlzeXMyMDIwYW5h", cw)

        cr.accept(cv, EXPAND_FRAMES)

        byte[] code = cw.toByteArray()
        code
    }

    private byte[] repStrMix(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        //由 lysys2020ana base64而来
        ClassVisitor cv = new StrMixClassVisitor("bHlzeXMyMDIwYW5h", cw, project)

        cr.accept(cv, EXPAND_FRAMES)

        byte[] code = cw.toByteArray()
        code
    }
}