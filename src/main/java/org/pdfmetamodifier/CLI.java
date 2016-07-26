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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    private static final String PROGRAM_VERSION = "2.0.0";

    public static void main(final String[] args) throws IOException {
        final OptionParser parser = new OptionParser();

        // Configure.
        parser.posixlyCorrect(true);
        parser.formatHelpWith(new BuiltinHelpFormatter(120, 2));

        //@formatter:off
        // Show program version.
        final OptionSpecBuilder version         = parser.acceptsAll(
                Arrays.asList("v",  "version"   ), 
                "Show program version."
                )
                ;

        // Help.
        final OptionSpecBuilder help            = parser.acceptsAll(
                Arrays.asList("h",  "help"      ), 
                "Print help."
                )
                ;
        help.forHelp();

        // PDF file.
        final OptionSpec<File> pdf              = parser.acceptsAll(
                Arrays.asList("p",  "pdf"       ), 
                "Source PDF file."
                ).requiredUnless(version, help)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Outline (bookmarks).
        final OptionSpec<File> saveOutlines     = parser.accepts(
                "save-outlines", 
                "Save Outline (bookmarks) to specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Update Outline (bookmarks).
        final OptionSpec<File> updateOutlines   = parser.accepts(
                "update-outlines", 
                "Update Outline (bookmarks) from specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Metadata.
        final OptionSpec<File> saveMetadata     = parser.accepts(
                "save-metadata", 
                "Save Metadata to specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Update Metadata.
        final OptionSpec<File> updateMetadata   = parser.accepts(
                "update-metadata", 
                "Update Metadata from specified file."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;

        // Save Embedded (attached) files.
        final OptionSpec<File> saveEmbedded     = parser.accepts(
                "save-embedded", 
                "Save Embedded (attached) files to specified directory."
                ).availableIf(pdf)
                .withRequiredArg()
                .ofType(File.class)
                ;
        // Remove Embedded (attached) files.
        final OptionSpecBuilder removeEmbedded  = parser.accepts(
                "remove-embedded", 
                "Remove Embedded (attached) files from PDF file."
                ).availableIf(pdf)
                ;
        // Add Embedded (attached) files.
        final OptionSpec<File> addEmbedded      = parser.accepts(
                "add-embedded", 
                "Add Embedded (attached) files from specified file."
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

                // Save Embedded (attached) files.
                final File saveEmbeddedFiles = options.valueOf(saveEmbedded);
                if (saveEmbeddedFiles != null) {
                    IOHelper.saveAttachments(pdfFile, saveEmbeddedFiles);
                }
                // Remove Embedded (attached) files.
                if (options.has(removeEmbedded)) {
                    IOHelper.removeAttachments(pdfFile);
                }
                // Add Embedded (attached) files.
                final List<File> addEmbeddedFile = options.valuesOf(addEmbedded);
                if (addEmbeddedFile != null) {
                    IOHelper.addAttachments(pdfFile, addEmbeddedFile);
                }
            }
        } catch (OptionException e) {
            System.out.println(e.getMessage()); // Print info about problem with parameters.
            System.out.println(); // Separator.
            parser.printHelpOn(System.out); // Print help.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
