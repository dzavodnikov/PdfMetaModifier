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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pdfmetamodifier.MetadataHelper;

/**
 * Test for {@link MetadataHelper}.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class MetadataHelperTest {

    /**
     * Test for {@link MetadataHelper#metadataToStringList(java.util.Map)}.
     */
    @Test
    public void metadataToStringList_null() {
        final List<String> stringList = MetadataHelper.metadataToStringList(null);
        assertEquals(0, stringList.size());
    }

    /**
     * Test for {@link MetadataHelper#metadataToStringList(java.util.Map)}.
     */
    @Test
    public void metadataToStringList_empty() {
        final List<String> stringList = MetadataHelper.metadataToStringList(new HashMap<>());
        assertEquals(0, stringList.size());
    }

    /**
     * Test for {@link MetadataHelper#metadataToStringList(java.util.Map)}.
     */
    @Test
    public void metadataToStringList() {
        final Map<String, String> metadata = new HashMap<>();
        final String key = "key";
        final String value = "value";
        metadata.put(key, value);

        final List<String> stringList = MetadataHelper.metadataToStringList(metadata);
        assertEquals(1, stringList.size());
        assertEquals(String.format(MetadataHelper.METADATA_LINE_TEMPLATE, key, value), stringList.get(0));
    }

    /**
     * Test for {@link MetadataHelper#metadataToStringList(java.util.Map)}.
     */
    @Test
    public void metadataToStringList_nullValue() {
        final Map<String, String> metadata = new HashMap<>();
        final String key = "key";
        metadata.put(key, null);

        final List<String> stringList = MetadataHelper.metadataToStringList(metadata);
        assertEquals(1, stringList.size());
        assertEquals(String.format(MetadataHelper.METADATA_LINE_TEMPLATE, key, ""), stringList.get(0));
    }

    /**
     * Test for {@link MetadataHelper#stringListToMetadata(java.util.List)}.
     */
    @Test
    public void stringListToMetadata_null() {
        final Map<String, String> metadata = MetadataHelper.stringListToMetadata(null);
        assertEquals(0, metadata.keySet().size());
    }

    /**
     * Test for {@link MetadataHelper#stringListToMetadata(java.util.List)}.
     */
    @Test
    public void stringListToMetadata_empty() {
        final Map<String, String> metadata = MetadataHelper.stringListToMetadata(new ArrayList<>());
        assertEquals(0, metadata.keySet().size());
    }

    /**
     * Test for {@link MetadataHelper#stringListToMetadata(java.util.List)}.
     */
    @Test
    public void stringListToMetadata() {
        final List<String> stringList = new ArrayList<>();
        final String key = "key";
        final String value = "value";
        stringList.add(String.format(MetadataHelper.METADATA_LINE_TEMPLATE, key, value));

        final Map<String, String> metadata = MetadataHelper.stringListToMetadata(stringList);
        assertEquals(1, metadata.keySet().size());
        assertEquals(value, metadata.get(key));
    }

    /**
     * Test for {@link MetadataHelper#stringListToMetadata(java.util.List)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void stringListToMetadata_wrongFormat() {
        final List<String> stringList = new ArrayList<>();
        stringList.add("aaa");

        MetadataHelper.stringListToMetadata(stringList);
    }
}
