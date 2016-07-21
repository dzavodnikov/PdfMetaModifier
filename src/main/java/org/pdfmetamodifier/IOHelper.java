/*
 * PdfBookmarksModifier -- Simple CLI utility for save/update bookmarks into PDF files
 * Copyright (c) 2012-2016 PdfBookmarksModifier Team
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pdfmetamodifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.SimpleNamedDestination;

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
            final FileInputStream fis = new FileInputStream(file);
            final InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue; // Ignore empty lines.
                }

                lines.add(line);
            }

            br.close();
            isr.close();
            fis.close();
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

            final FileOutputStream fos = new FileOutputStream(file);
            final OutputStreamWriter osw = new OutputStreamWriter(fos);
            final BufferedWriter bw = new BufferedWriter(osw);

            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }

            bw.close();
            osw.close();
            fos.close();
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

            final FileOutputStream fos = new FileOutputStream(file);

            fos.write(bytes);

            fos.close();
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
     *      http://developers.itextpdf.com/examples/miscellaneous/bookmark-examples/
     */
    public static void saveOutlines(final File pdfFile, final File outlinesFile) throws IOException {
        // Read PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());

        // Get data from PDF file.
        List<HashMap<String, Object>> bookmarks = SimpleBookmark.getBookmark(reader);
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }
        HashMap<String, String> namedDestinations = SimpleNamedDestination.getNamedDestination(reader, false);
        if (namedDestinations == null) {
            namedDestinations = new HashMap<>();
        }

        // Close original PDF file.
        reader.close();

        // Convert.
        final List<String> lines = BookmarksHelper.bookmarkListToStringList(bookmarks, namedDestinations);

        // Write Outlines (bookmarks) list into the text file.
        saveLinesToFile(lines, outlinesFile);
    }

    /**
     * Update Outlines (bookmarks).
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param bookmarksFile
     *            File with bookmarks in user-frendly format.
     * @throws IOException
     * @throws DocumentException
     */
    /*
     * See:
     *      http://developers.itextpdf.com/examples/miscellaneous/bookmark-examples/
     */
    public static void updateOutlines(final File pdfFile, final File bookmarksFile)
            throws IOException, DocumentException {
        // Read bookmark list from text file.
        final List<String> lines = readLinesFromFile(bookmarksFile);

        // Convert.
        final List<HashMap<String, Object>> bookmarks = BookmarksHelper.stringListToBookmarkList(lines);

        // Create temporary PDF file for result.
        if (TEMP_PDF.exists()) {
            TEMP_PDF.delete();
        }

        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(TEMP_PDF);
        final PdfStamper stamper = new PdfStamper(reader, fos);

        // Set bookmarks.
        stamper.setOutlines(bookmarks);

        // Close output PDF file.
        stamper.close();
        fos.close();

        // Close original PDF file.
        reader.close();

        // Replace original PDF file.
        pdfFile.delete();
        Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
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
     *      http://developers.itextpdf.com/examples/miscellaneous/adding-metadata/
     *      http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-12/
     */
    public static void saveMetadata(final File pdfFile, final File metadataFile) throws IOException {
        // Read PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());

        // Get Metadata from PDF file.
        final Map<String, String> metadata = reader.getInfo();

        // Convert.
        final List<String> lines = MetadataHelper.metadataToStringList(metadata);

        // Write Metadata list into the text file.
        saveLinesToFile(lines, metadataFile);

        // Close original PDF file.
        reader.close();
    }

    /**
     * Update Metadata.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param metadataFile
     *            File with Metadata in user-frendly format.
     * @throws IOException
     * @throws DocumentException
     */
    /*
     * See:
     *      http://developers.itextpdf.com/examples/miscellaneous/adding-metadata/
     *      http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-12/
     */
    public static void updateMetadata(final File pdfFile, final File metadataFile)
            throws IOException, DocumentException {
        // Read Metadata from text file.
        final List<String> lines = readLinesFromFile(metadataFile);

        // Convert.
        final Map<String, String> newMetadata = MetadataHelper.stringListToMetadata(lines);

        // Create temporary PDF file for result.
        if (TEMP_PDF.exists()) {
            TEMP_PDF.delete();
        }

        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(TEMP_PDF);
        final PdfStamper stamper = new PdfStamper(reader, fos);

        // Remove existing metadata.
        final Map<String, String> originalMetadata = reader.getInfo();
        for (String key : originalMetadata.keySet()) {
            originalMetadata.put(key, null);
        }
        stamper.setMoreInfo(originalMetadata);

        // Set Metadata.
        stamper.setMoreInfo(newMetadata);

        // Close output PDF file.
        stamper.close();
        fos.close();

        // Close original PDF file.
        reader.close();

        // Replace original PDF file.
        pdfFile.delete();
        Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
    }

    /*
     * See:
     *      http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-16#614-kubrickdvds.java
     */
    private static void getAttachmentsPageLevel(final File pdfFile, final File outputDir)
            throws IOException, DocumentException {
        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());

        // Extract attached files.
        for (int i = 1; i <= reader.getNumberOfPages(); ++i) { // Page number starts from 1.
            final PdfArray array = reader.getPageN(i).getAsArray(PdfName.ANNOTS);
            if (array != null) {
                for (int j = 0; j < array.size(); ++j) {
                    final PdfDictionary annot = array.getAsDict(j);
                    if (PdfName.FILEATTACHMENT.equals(annot.getAsName(PdfName.SUBTYPE))) {
                        final PdfDictionary fs = annot.getAsDict(PdfName.FS);
                        final PdfDictionary refs = fs.getAsDict(PdfName.EF);
                        for (PdfName name : refs.getKeys()) {
                            // Get file stream.
                            final PRStream fileStream = (PRStream) refs.getAsStream(name);

                            // File.
                            final String filename = fs.getAsString(name).toString();
                            final File file = new File(outputDir.getAbsolutePath() + File.separatorChar + filename);

                            // Save file content.
                            saveBytesToFile(PdfReader.getStreamBytes(fileStream), file);
                        }
                    }
                }
            }
        }

        // Close original PDF file.
        reader.close();
    }

    /*
     * See:
     *      http://developers.itextpdf.com/examples/miscellaneous/embedded-files/
     */
    private static void getAttachmentsDocumentLevel(final File pdfFile, final File outputDir)
            throws IOException, DocumentException {
        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());

        // Extract attached files.
        final PdfDictionary catalog = reader.getCatalog();
        final PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
        if (names != null) {
            final PdfDictionary embeddedFiles = names.getAsDict(PdfName.EMBEDDEDFILES);
            if (embeddedFiles != null) {
                final PdfArray fileSpecs = embeddedFiles.getAsArray(PdfName.NAMES);
                if (fileSpecs != null) {
                    for (int i = 0; i < fileSpecs.size(); ++i) {
                        final PdfDictionary fileArray = fileSpecs.getAsDict(i);
                        if (fileArray != null) {
                            final PdfDictionary refs = fileArray.getAsDict(PdfName.EF);
                            for (PdfName name : refs.getKeys()) {
                                // Get file stream.
                                final PRStream fileStream = (PRStream) PdfReader
                                        .getPdfObject(refs.getAsIndirectObject(name));

                                // File.
                                final String filename = fileArray.getAsString(name).toString();
                                final File file = new File(outputDir.getAbsolutePath() + File.separatorChar + filename);

                                // Save file content.
                                saveBytesToFile(PdfReader.getStreamBytes(fileStream), file);
                            }
                        }
                    }
                }
            }
        }

        // Close original PDF file.
        reader.close();
    }

    /**
     * Save all Attached (embedded) files to some directory.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param outputDir
     *            Target directory.
     * @throws IOException
     * @throws DocumentException
     */
    public static void saveAttachments(final File pdfFile, final File outputDir) throws IOException, DocumentException {
        getAttachmentsPageLevel(pdfFile, outputDir); // FIXME: Not works on a sample files: remove?
        getAttachmentsDocumentLevel(pdfFile, outputDir);
    }

    /**
     * Remove all Attached (embedded) files.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @throws IOException
     * @throws DocumentException
     */
    /*
     * See:
     *      http://developers.itextpdf.com/examples/miscellaneous/embedded-files/
     */
    public static void removeAttachments(final File pdfFile) throws IOException, DocumentException {
        // Create temporary PDF file for result.
        if (TEMP_PDF.exists()) {
            TEMP_PDF.delete();
        }

        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(TEMP_PDF);
        final PdfStamper stamper = new PdfStamper(reader, fos);

        // Remove attached files.
        final PdfDictionary catalog = reader.getCatalog();
        final PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
        if (names != null) {
            names.remove(PdfName.EMBEDDEDFILES);

            // Clean dictionary.
            if (names.size() == 0) {
                catalog.remove(PdfName.NAMES);
            }
        }

        // Close output PDF file.
        stamper.close();
        fos.close();

        // Close original PDF file.
        reader.close();

        // Replace original PDF file.
        pdfFile.delete();
        Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
    }

    /**
     * Add new Attached (embedded) files.
     * 
     * @param pdfFile
     *            Source PDF file.
     * @param attachmentFiles
     *            Files that will be attached (embedded).
     * @throws IOException
     * @throws DocumentException
     */
    /*
     * See:
     *      http://developers.itextpdf.com/examples/miscellaneous/embedded-files/
     */
    public static void addAttachments(final File pdfFile, final List<File> attachmentFiles)
            throws IOException, DocumentException {
        // Create temporary PDF file for result.
        if (TEMP_PDF.exists()) {
            TEMP_PDF.delete();
        }

        // Open PDF file.
        final PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());
        final FileOutputStream fos = new FileOutputStream(TEMP_PDF);
        final PdfStamper stamper = new PdfStamper(reader, fos);

        // Add attachments.
        if (attachmentFiles != null) {
            for (File file : attachmentFiles) {
                if (file != null) {
                    if (file.exists() && file.isFile()) {
                        final String filePath = file.getAbsolutePath();
                        final String fileDisplay = file.getName();
                        final PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(stamper.getWriter(), filePath,
                                fileDisplay, null);

                        final String description = "";
                        stamper.addFileAttachment(description, fs);
                    }
                }
            }
        }

        // Close output PDF file.
        stamper.close();
        fos.close();

        // Close original PDF file.
        reader.close();

        // Replace original PDF file.
        pdfFile.delete();
        Files.move(Paths.get(TEMP_PDF.toURI()), Paths.get(pdfFile.toURI()));
    }
}
