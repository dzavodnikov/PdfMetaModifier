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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper with methods for modify PDF metadata.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class MetadataHelper {

    public static final String  METADATA_LINE_TEMPLATE = "%s|%s";

    public static final Pattern METADATA_LINE_PATTERN  = Pattern.compile("(?<key>.+)\\|(?<value>.*)");

    /**
     * Convert metadata structure to list of strings.
     * 
     * @param metadata
     *            Source metadata object.
     * @return string representation of metadata.
     */
    public static List<String> metadataToStringList(final Map<String, String> metadata) {
        final List<String> stringList = new ArrayList<>();
        if (metadata != null) {
            final List<String> keys = new ArrayList<>(metadata.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                final String value = metadata.get(key);

                stringList.add(String.format(METADATA_LINE_TEMPLATE, key, value != null ? value : ""));
            }
        }
        return stringList;
    }

    /**
     * Convert string representation of metadata to metadata object.
     * 
     * @param stringList
     *            String representation of metadata.
     * @return metadata object.
     */
    public static Map<String, String> stringListToMetadata(final List<String> stringList) {
        final Map<String, String> metadata = new HashMap<>();
        if (stringList != null) {
            for (String line : stringList) {
                final Matcher matcher = METADATA_LINE_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException(String.format("Metadata line have a wrong format: '%s'!", line));
                }
                metadata.put(matcher.group("key"), matcher.group("value"));
            }
        }
        return metadata;
    }
}
