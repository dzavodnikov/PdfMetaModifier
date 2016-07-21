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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;

import org.junit.Test;

import com.itextpdf.text.DocumentException;

/**
 * Test for {@link IOHelper}.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class IOHelperTest {

    private final static String TEST_PATH = "src" + File.separatorChar + "test" + File.separatorChar + "resources";

    private static long fileCRC32(final File file) throws IOException {
        final CRC32 crc32 = new CRC32();
        crc32.update(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        return crc32.getValue();
    }

    private static void fileCompare(final File file1, final File file2) throws IOException {
        assertEquals(fileCRC32(file1), fileCRC32(file2));
    }

    private static void dirCompare(final File dir1, final File dir2) throws IOException {
        assertTrue(dir1.isDirectory());
        assertTrue(dir2.isDirectory());

        final String[] childrens1 = dir1.list();
        final String[] childrens2 = dir2.list();
        assertEquals(childrens1.length, childrens2.length);

        for (int i = 0; i < childrens1.length; ++i) {
            //@formatter:off
            fileCompare(
                    new File(dir1.getAbsolutePath() + File.separatorChar + childrens1[i]),
                    new File(dir2.getAbsolutePath() + File.separatorChar + childrens2[i])
                );
            //@formatter:on
        }
    }

    private static void dirDelete(final File file) throws IOException {
        if (file.isFile()) {
            file.delete();
        }
        if (file.isDirectory()) {
            for (String child : file.list()) {
                dirDelete(new File(file.getAbsolutePath() + File.separatorChar + child));
            }
            file.delete();
        }
    }

    /**
     * Test for {@link IOHelper#saveOutlines(File, File)}.
     * 
     * @throws IOException
     */
    @Test
    public void saveBookmarks() throws IOException {
        final String rootPath = TEST_PATH + File.separatorChar + "bookmarks";
        final String[] testFiles = new String[] { "bookmarks", "cmp_changed_bookmarks" };
        for (String filename : testFiles) {
            final String basePath = rootPath + File.separatorChar + filename;
            final File pdfFile = new File(basePath + ".pdf");
            final File bookmarksFile = new File(basePath + "_bookmarks.txt");
            final File tempBookmarksFile = new File(basePath + "_bookmarks_temp.txt");

            // Execute.
            IOHelper.saveOutlines(pdfFile, tempBookmarksFile);

            // Compare results.
            fileCompare(bookmarksFile, tempBookmarksFile);

            // Clean.
            tempBookmarksFile.delete();
        }
    }

    /**
     * Test for {@link IOHelper#saveMetadata(File, File)}.
     * 
     * @throws IOException
     */
    @Test
    public void saveMetadata() throws IOException {
        final String rootPath = TEST_PATH + File.separatorChar + "metadata";
        final String[] testFiles = new String[] { "title-bar", "cmp_state_metadata" };
        for (String filename : testFiles) {
            final String basePath = rootPath + File.separatorChar + filename;
            final File pdfFile = new File(basePath + ".pdf");
            final File metadataFile = new File(basePath + "_metadata.txt");
            final File tempMetadataFile = new File(basePath + "_metadata_temp.txt");

            // Execute.
            IOHelper.saveMetadata(pdfFile, tempMetadataFile);

            // Compare results.
            fileCompare(metadataFile, tempMetadataFile);

            // Clean.
            tempMetadataFile.delete();
        }
    }

    /**
     * Test for {@link IOHelper#saveAttachments(File, File)}.
     * 
     * @throws IOException
     * @throws DocumentException
     */
    @Test
    public void saveAttachments() throws IOException, DocumentException {
        final String rootPath = TEST_PATH + File.separatorChar + "attachments";
        final String[] testFiles = new String[] { "cmp_hello_with_attachment", "cmp_hello_with_attachments" };
        for (String filename : testFiles) {
            final String basePath = rootPath + File.separatorChar + filename;
            final File pdfFile = new File(basePath + ".pdf");
            final File attachmentsFile = new File(basePath + "_metadata");
            final File tempAttachmentsFile = new File(basePath + "_metadata_temp");

            // Execute.
            IOHelper.saveAttachments(pdfFile, tempAttachmentsFile);

            // Compare results.
            dirCompare(attachmentsFile, tempAttachmentsFile);

            // Clean.
            dirDelete(tempAttachmentsFile);
        }
    }
}
