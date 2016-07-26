/*
 * Copyright (c) 2012-2016 PdfMetaModifier Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * This file is part of PdfMetaModifier.
 */
package org.pdfmetamodifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDestinationNameTreeNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;

/**
 * Helper with methods for modify PDF file.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class IOHelper {

    protected static final File TEMP_PDF = new File(
            System.getProperty("java.io.tmpdir") + File.separatorChar + "TEMP.pdf");

    private static List<String> readLinesFromFile(final File file) throws IOException {
        final List<String> lines = new ArrayList<>();

        if (file != null && file.exists()) {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue; // Ignore empty lines.
                    }

                    lines.add(line);
                }
            } finally {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        }

        return lines;
    }

    private static void saveLinesToFile(final List<String> lines, final File file) throws IOException {
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream fos = null;
            OutputStreamWriter osw = null;
            BufferedWriter bw = null;
            try {
                fos = new FileOutputStream(file);
                osw = new OutputStreamWriter(fos);
                bw = new BufferedWriter(osw);

                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            } finally {
                if (bw != null) {
                    bw.close();
                }
                if (osw != null) {
                    osw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private static void saveBytesToFile(final byte[] bytes, final File file) throws IOException {
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);

                fos.write(bytes);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private static byte[] readBytesToFile(final File file) throws IOException {
        if (file != null) {
            FileInputStream fis = null;
            try {
                final byte[] buffer = new byte[(int) file.length()];

                fis = new FileInputStream(file);
                if (fis.read(buffer) == -1) {
                    throw new IOException("EOF reached while trying to read the whole file.");
                }

                return buffer;
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Save Outlines (bookmarks).
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param outlinesFile
     *            File with Outlines (bookmarks) in user-frendly format.
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/CreateBookmarks.java?view=markup
     */
    public static void saveOutlines(final File pdfFile, final File outlinesFile) throws IOException {
        PDDocument document = null;
        try {
            // Read PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Get data from PDF file.
            final PDDocumentCatalog catalog = document.getDocumentCatalog();

            final PDDocumentOutline outlines = catalog.getDocumentOutline();

            final PDPageTree pageTree = catalog.getPages();

            PDDestinationNameTreeNode destinationNameTree = null;
            final PDDocumentNameDictionary nameDict = catalog.getNames();
            if (nameDict != null) {
                destinationNameTree = nameDict.getDests();
            }

            // Convert.
            final List<String> lines = OutlineHelper.outlinesToLineList(outlines, pageTree, destinationNameTree);

            // Write line list into the text file.
            saveLinesToFile(lines, outlinesFile);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Update Outlines (bookmarks).
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param outlinesFile
     *            File with Outlines (bookmarks) in user-frendly format.
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/CreateBookmarks.java?view=markup
     */
    public static void updateOutlines(final File pdfFile, final File outlinesFile) throws IOException {
        // Read bookmark list from text file.
        final List<String> lines = readLinesFromFile(outlinesFile);

        PDDocument document = null;
        try {
            // Open PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Get data from PDF file.
            final PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            final PDPageTree pageTree = documentCatalog.getPages();

            // Convert.
            final PDDocumentOutline documentOutline = OutlineHelper.lineListToOutlines(pageTree, lines);

            // Set outlines.
            documentCatalog.setDocumentOutline(documentOutline);

            // Create temporary PDF file for result.
            if (TEMP_PDF.exists()) {
                TEMP_PDF.delete();
            }

            // Save result to temporary PDF file.
            document.save(TEMP_PDF);

            // Replace original PDF file.
            pdfFile.delete();
            Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Save Metadata.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param metadataFile
     *            File with Metadata in user-frendly format.
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/ExtractMetadata.java?view=markup
     */
    public static void saveMetadata(final File pdfFile, final File metadataFile) throws IOException {
        PDDocument document = null;
        try {
            // Read PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Get data from PDF file.
            final PDDocumentInformation documentInformation = document.getDocumentInformation();

            // Convert.
            final List<String> lines = MetadataHelper.metadataToLineList(documentInformation);

            // Write line list into the text file.
            saveLinesToFile(lines, metadataFile);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Update Metadata.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param metadataFile
     *            File with Metadata in user-frendly format.
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/ExtractMetadata.java?view=markup
     */
    public static void updateMetadata(final File pdfFile, final File metadataFile) throws IOException {
        // Read bookmark list from text file.
        final List<String> lines = readLinesFromFile(metadataFile);

        PDDocument document = null;
        try {
            // Open PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Convert.
            final PDDocumentInformation documentInformation = MetadataHelper.stringListToMetadata(lines);

            // Set Metadata.
            document.setDocumentInformation(documentInformation);

            // Create temporary PDF file for result.
            if (TEMP_PDF.exists()) {
                TEMP_PDF.delete();
            }

            // Save result to temporary PDF file.
            document.save(TEMP_PDF);

            // Replace original PDF file.
            pdfFile.delete();
            Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private static void extractFiles(final File outputDir, final Map<String, PDComplexFileSpecification> names)
            throws IOException {
        if (names != null) {
            for (PDComplexFileSpecification fileSpec : names.values()) {
                extractFile(outputDir, fileSpec);
            }
        }
    }

    private static void extractFile(final File outputDir, final PDComplexFileSpecification fileSpec)
            throws IOException {
        final File file = new File(outputDir.getAbsolutePath() + File.separatorChar + fileSpec.getFilename());

        final PDEmbeddedFile embeddedFile = getEmbeddedFile(fileSpec);
        saveBytesToFile(embeddedFile.toByteArray(), file);

    }

    private static PDEmbeddedFile getEmbeddedFile(final PDComplexFileSpecification fileSpec) {
        // Search for the first available alternative of the Embedded (attached) file.
        if (fileSpec != null) {
            //@formatter:off
            final PDEmbeddedFile[] file = {
                    fileSpec.getEmbeddedFileUnicode(), 
                    fileSpec.getEmbeddedFileDos(), 
                    fileSpec.getEmbeddedFileMac(), 
                    fileSpec.getEmbeddedFileUnix(), 
                    fileSpec.getEmbeddedFile()
                };
            //@formatter:on

            for (PDEmbeddedFile embeddedFile : file) {
                if (embeddedFile != null) {
                    return embeddedFile;
                }
            }
        }

        return null;
    }

    /**
     * Save all Attached (embedded) files to some directory.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param outputDir
     *            Target directory.
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/ExtractEmbeddedFiles.java?view=markup
     */
    public static void saveAttachments(final File pdfFile, final File outputDir) throws IOException {
        PDDocument document = null;
        try {
            // Read PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Extract Embedded (attached) files.
            final PDDocumentNameDictionary documentNameDictionary = new PDDocumentNameDictionary(
                    document.getDocumentCatalog());
            final PDEmbeddedFilesNameTreeNode embeddedFilesNameTree = documentNameDictionary.getEmbeddedFiles();
            if (embeddedFilesNameTree != null) {
                extractFiles(outputDir, embeddedFilesNameTree.getNames());

                final List<PDNameTreeNode<PDComplexFileSpecification>> kids = embeddedFilesNameTree.getKids();
                if (kids != null) {
                    for (PDNameTreeNode<PDComplexFileSpecification> nameTreeNode : kids) {
                        extractFiles(outputDir, nameTreeNode.getNames());
                    }
                }
            }

            // Extract Embedded (attached) from annotations.
            for (PDPage page : document.getPages()) {
                for (PDAnnotation annotation : page.getAnnotations()) {
                    if (annotation instanceof PDAnnotationFileAttachment) {
                        final PDAnnotationFileAttachment fileAttach = (PDAnnotationFileAttachment) annotation;

                        final PDComplexFileSpecification fileSpec = (PDComplexFileSpecification) fileAttach.getFile();
                        extractFile(outputDir, fileSpec);
                    }
                }
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Remove all Attached (embedded) files.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @throws IOException
     */
    public static void removeAttachments(final File pdfFile) throws IOException {
        PDDocument document = null;
        try {
            // Read PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Add the tree to the document catalog.
            final PDDocumentNameDictionary documentNameDictionary = new PDDocumentNameDictionary(
                    document.getDocumentCatalog());
            documentNameDictionary.setEmbeddedFiles(null);
            document.getDocumentCatalog().setNames(documentNameDictionary);
        } finally {
            document.close();
        }
    }

    /**
     * Add new Attached (embedded) files.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param attachmentFiles
     *            Files that will be attached (embedded).
     * @throws IOException
     */
    /*
     * See:
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/EmbeddedFiles.java?view=markup
     */
    public static void addAttachments(final File pdfFile, final List<File> attachmentFiles) throws IOException {
        PDDocument document = null;
        try {
            // Read PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Embedded (attached) files are stored in a named tree.
            final PDEmbeddedFilesNameTreeNode root = new PDEmbeddedFilesNameTreeNode();
            final List<PDEmbeddedFilesNameTreeNode> kids = new ArrayList<PDEmbeddedFilesNameTreeNode>();
            root.setKids(kids);

            // Add the tree to the document catalog.
            final PDDocumentNameDictionary documentNameDictionary = new PDDocumentNameDictionary(
                    document.getDocumentCatalog());
            documentNameDictionary.setEmbeddedFiles(root);
            document.getDocumentCatalog().setNames(documentNameDictionary);

            // For all Embedded (attached) files.
            for (File file : attachmentFiles) {
                final String filename = file.getName();

                // First create the file specification, which holds the Embedded (attached) file.
                final PDComplexFileSpecification complexFileSpecification = new PDComplexFileSpecification();
                complexFileSpecification.setFile(filename);

                // Create a dummy file stream, this would probably normally be a FileInputStream.
                final ByteArrayInputStream fileStream = new ByteArrayInputStream(readBytesToFile(file));
                final PDEmbeddedFile embededFile = new PDEmbeddedFile(document, fileStream);
                complexFileSpecification.setEmbeddedFile(embededFile);

                // Create a new tree node and add the Embedded (attached) file.
                final PDEmbeddedFilesNameTreeNode embeddedFilesNameTree = new PDEmbeddedFilesNameTreeNode();
                embeddedFilesNameTree.setNames(Collections.singletonMap(filename, complexFileSpecification));

                // Add the new node as kid to the root node.
                kids.add(embeddedFilesNameTree);
            }
        } finally {
            document.close();
        }
    }
}
