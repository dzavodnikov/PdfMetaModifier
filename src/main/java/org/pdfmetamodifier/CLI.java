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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.DocumentException;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

/**
 * Command-Line Interface.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class CLI {

    private static final String PROGRAM_NAME    = "PdfMetaModifier";

    private static final String PROGRAM_VERSION = "1.0.0";

    public static void main(final String[] args) throws IOException, DocumentException {
        final OptionParser parser = new OptionParser();

        // Configure.
        parser.posixlyCorrect(true);
        parser.formatHelpWith(new BuiltinHelpFormatter(120, 2));

        //@formatter:off
        // Show program version.
        final OptionSpecBuilder version             = parser.acceptsAll(
                Arrays.asList("v",  "version"   ), 
                "Show program version."
                )
                ;

        // Help.
        final OptionSpecBuilder help                = parser.acceptsAll(
                Arrays.asList("h",  "help"      ), 
                "Print help."
                )
                ;
        help.forHelp();

        // PDF file.
        final OptionSpec<File> pdf                  = parser.acceptsAll(
                Arrays.asList("p",  "pdf"       ), 
                "Source PDF file."
                ).requiredUnless(version, help)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Outline (bookmarks).
        final OptionSpec<File> saveOutlines         = parser.accepts(
                "save-outlines", 
                "Save Outline (bookmarks) to specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Update Outline (bookmarks).
        final OptionSpec<File> updateOutlines       = parser.accepts(
                "update-outlines", 
                "Update Outline (bookmarks) from specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Metadata.
        final OptionSpec<File> saveMetadata         = parser.accepts(
                "save-metadata", 
                "Save Metadata to specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Update Metadata.
        final OptionSpec<File> updateMetadata       = parser.accepts(
                "update-metadata", 
                "Update Metadata from specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Attachments.
        final OptionSpec<File> saveAttachments      = parser.accepts(
                "save-attachments", 
                "Save Attachments to specified directory."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Remove Attachments.
        final OptionSpecBuilder removeAttachments   = parser.accepts(
                "remove-attachments", 
                "Remove Attachments from PDF file."
                ).availableIf(pdf)
                ;
        // Add Attachments.
        final OptionSpec<File> addAttachment        = parser.accepts(
                "add-attachment", 
                "Add Attachment from specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        //@formatter:on

        // Parse.
        try {
            final OptionSet options = parser.parse(args);

            // Print help.
            if (options.has(help)) {
                parser.printHelpOn(System.out);
            }

            // Print version.
            if (options.has(version)) {
                System.out.println(String.format("%s ver. %s", PROGRAM_NAME, PROGRAM_VERSION));
            }

            // Execute action.
            final File pdfFile = options.valueOf(pdf);

            if (pdfFile != null) {
                // Save Outline (bookmarks).
                final File saveOutlinesFile = options.valueOf(saveOutlines);
                if (saveOutlines != null) {
                    IOHelper.saveOutlines(pdfFile, saveOutlinesFile);
                }
                // Update Outline (bookmarks).
                final File updateOutlinesFile = options.valueOf(updateOutlines);
                if (updateOutlinesFile != null) {
                    IOHelper.updateOutlines(pdfFile, updateOutlinesFile);
                }

                // Save Metadata.
                final File saveMetadataFile = options.valueOf(saveMetadata);
                if (saveMetadataFile != null) {
                    IOHelper.saveMetadata(pdfFile, saveMetadataFile);
                }
                // Update Metadata.
                final File updateMetadataFile = options.valueOf(updateMetadata);
                if (updateMetadataFile != null) {
                    IOHelper.updateMetadata(pdfFile, updateMetadataFile);
                }

                // Save attached (embedded) files.
                final File saveAttachmentsFile = options.valueOf(saveAttachments);
                if (saveAttachmentsFile != null) {
                    IOHelper.saveAttachments(pdfFile, saveAttachmentsFile);
                }
                // Remove attached (embedded) files.
                if (options.has(removeAttachments)) {
                    IOHelper.removeAttachments(pdfFile);
                }
                // Add attached (embedded) files.
                final List<File> addAttachmentFile = options.valuesOf(addAttachment);
                if (addAttachmentFile != null) {
                    IOHelper.addAttachments(pdfFile, addAttachmentFile);
                }
            }
        } catch (OptionException e) {
            System.out.println(e.getMessage()); // Print info about problem with parameters.
            System.out.println(); // Separator.
            parser.printHelpOn(System.out); // Print help.
        }
    }
}
