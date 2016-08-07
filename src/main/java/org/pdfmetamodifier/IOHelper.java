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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
     *      https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/PrintBookmarks.java?view=markup
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

            final PDPageTree pages = catalog.getPages();

            final PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(catalog);
            final PDDestinationNameTreeNode destinations = namesDictionary.getDests();

            // Convert.
            final List<String> lines = OutlineHelper.outlinesToLineList(outlines, pages, destinations);

            // Write line list into the text file.
            Files.write(outlinesFile.toPath(), lines);
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
        final List<String> lines = Files.readAllLines(outlinesFile.toPath());

        PDDocument document = null;
        try {
            // Open PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Get data from PDF file.
            final PDDocumentCatalog catalog = document.getDocumentCatalog();

            final PDPageTree pages = catalog.getPages();

            // Convert.
            final PDDocumentOutline outlines = OutlineHelper.lineListToOutlines(pages, lines);

            // Set outlines.
            catalog.setDocumentOutline(outlines);

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
            final PDDocumentInformation information = document.getDocumentInformation();

            // Convert.
            final List<String> lines = MetadataHelper.metadataToLineList(information);

            // Write line list into the text file.
            Files.write(metadataFile.toPath(), lines);
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
        final List<String> lines = Files.readAllLines(metadataFile.toPath());

        PDDocument document = null;
        try {
            // Open PDF file.
            document = PDDocument.load(pdfFile);
            if (document.isEncrypted()) {
                throw new IOException("Document is encrypted.");
            }

            // Convert.
            final PDDocumentInformation information = MetadataHelper.stringListToMetadata(lines);

            // Set Metadata.
            document.setDocumentInformation(information);

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

    private static void extractFile(final File outputDir, final PDComplexFileSpecification fileSpec)
            throws IOException {
        final File file = new File(outputDir.getAbsolutePath() + File.separatorChar + fileSpec.getFilename());
        final PDEmbeddedFile embeddedFile = getEmbeddedFile(fileSpec);
        Files.write(file.toPath(), embeddedFile.toByteArray());
    }

    private static void extractFiles(final File outputDir, final Map<String, PDComplexFileSpecification> names)
            throws IOException {
        if (names != null) {
            for (PDComplexFileSpecification fileSpec : names.values()) {
                extractFile(outputDir, fileSpec);
            }
        }
    }

    private static PDEmbeddedFile getEmbeddedFile(final PDComplexFileSpecification fileSpec) {
        // Search for the first available alternative of the Embedded (attached) file.
        if (fileSpec != null) {
            //@formatter:off
            final PDEmbeddedFile[] files = {
                    fileSpec.getEmbeddedFileUnicode(), 
                    fileSpec.getEmbeddedFileUnix(), 
                    fileSpec.getEmbeddedFileDos(), 
                    fileSpec.getEmbeddedFileMac(), 
                    fileSpec.getEmbeddedFile()
                };
            //@formatter:on

            for (PDEmbeddedFile embeddedFile : files) {
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

            // Clean the tree to the document catalog.
            document.getDocumentCatalog().setNames(null);

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
            final PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(
                    document.getDocumentCatalog());
            namesDictionary.setEmbeddedFiles(root);
            document.getDocumentCatalog().setNames(namesDictionary);

            // For all Embedded (attached) files.
            for (File file : attachmentFiles) {
                final String filename = file.getName();

                // First create the file specification, which holds the Embedded (attached) file.
                final PDComplexFileSpecification complexFileSpecification = new PDComplexFileSpecification();
                complexFileSpecification.setFile(filename);

                // Create a dummy file stream, this would probably normally be a FileInputStream.
                final ByteArrayInputStream fileStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
                final PDEmbeddedFile embededFile = new PDEmbeddedFile(document, fileStream);
                complexFileSpecification.setEmbeddedFile(embededFile);

                // Create a new tree node and add the Embedded (attached) file.
                final PDEmbeddedFilesNameTreeNode embeddedFilesNameTree = new PDEmbeddedFilesNameTreeNode();
                embeddedFilesNameTree.setNames(Collections.singletonMap(filename, complexFileSpecification));

                // Add the new node as kid to the root node.
                kids.add(embeddedFilesNameTree);
            }

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
}
