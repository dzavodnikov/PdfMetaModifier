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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/**
 * Helper with methods for modify PDF Metadata.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class MetadataHelper {

    public static final String  METADATA_LINE_TEMPLATE = "%s" + OutlineHelper.SEPARATOR + "%s";

    public static final Pattern METADATA_LINE_PATTERN  = Pattern
            .compile("(?<key>.+)\\" + OutlineHelper.SEPARATOR + "(?<value>.*)");

    /**
     * Convert Metadata object to list of lines.
     * 
     * @param metadata
     *            Source Metadata object.
     * @return list of lines with Metadata representation.
     */
    public static List<String> metadataToLineList(final PDDocumentInformation documentInformation) {
        final List<String> lineList = new ArrayList<>();

        if (documentInformation != null) {
            final List<String> matadataKeys = new ArrayList<>(documentInformation.getMetadataKeys());
            Collections.sort(matadataKeys);

            for (String key : matadataKeys) {
                final String value = documentInformation.getCustomMetadataValue(key);
                if (value != null) {
                    lineList.add(String.format(METADATA_LINE_TEMPLATE, key, value));
                }
            }
        }

        return lineList;
    }

    /**
     * Convert list of lines to Metadata object.
     * 
     * @param lineList
     *            Source list of lines with Metadata representation.
     * @return Metadata object.
     */
    public static PDDocumentInformation stringListToMetadata(final List<String> lineList) {
        final PDDocumentInformation documentInformation = new PDDocumentInformation();

        if (lineList != null) {
            for (String line : lineList) {
                final Matcher matcher = METADATA_LINE_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException(String.format("Metadata line have a wrong format: '%s'!", line));
                }
                documentInformation.setCustomMetadataValue(matcher.group("key"), matcher.group("value"));
            }
        }

        return documentInformation;
    }
}
