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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.pdfmetamodifier.BookmarksHelper;

/**
 * Test for {@link BookmarksHelper}.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class BookmarksHelperTest {

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces1() {
        BookmarksHelper.cleanTitle("");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces2() {
        BookmarksHelper.cleanTitle(" ");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces3() {
        BookmarksHelper.cleanTitle("  ");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces4() {
        BookmarksHelper.cleanTitle("\t");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces5() {
        BookmarksHelper.cleanTitle(" \t \t  ");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces6() {
        BookmarksHelper.cleanTitle("\r");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces7() {
        BookmarksHelper.cleanTitle("\n");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces8() {
        BookmarksHelper.cleanTitle("\r\n");
    }

    /**
     * Test for {@link BookmarksHelper#cleanTitle(String)}.
     */
    @Test
    public void cleanTitle() {
        //@formatter:off
        assertEquals("a",       BookmarksHelper.cleanTitle("a"));
        assertEquals("a",       BookmarksHelper.cleanTitle(" a"));
        assertEquals("a",       BookmarksHelper.cleanTitle("a  "));
        assertEquals("a",       BookmarksHelper.cleanTitle("  a   "));
        assertEquals("a",       BookmarksHelper.cleanTitle("\ta\t\t"));
        assertEquals("a b",     BookmarksHelper.cleanTitle("  a   b    "));
        assertEquals("a b c",   BookmarksHelper.cleanTitle("  a   b    c     "));
        assertEquals("a – b",   BookmarksHelper.cleanTitle("a–b"));
        //@formatter:on
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkTitle(HashMap)}.
     */
    @Test
    public void getBookmarkTitle() {
        final HashMap<String, Object> bookmark = new HashMap<>();
        final String title = "Title";
        bookmark.put(BookmarksHelper.TITLE_KEY, title);
        assertEquals(title, BookmarksHelper.getBookmarkTitle(bookmark));
    }

    /**
     * Test for {@link BookmarksHelper#setBookmarkTitle(HashMap, String)}.
     */
    @Test
    public void setBookmarkTitle() {
        final HashMap<String, Object> bookmark = new HashMap<>();
        final String title = "Title";
        BookmarksHelper.setBookmarkTitle(bookmark, title);
        assertEquals(title, bookmark.get(BookmarksHelper.TITLE_KEY));
    }

    /**
     * Test for {@link BookmarksHelper#parsePageNumber(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void parsePageNumber_wrongFormat() {
        BookmarksHelper.parsePageNumber("ABC");
    }

    /**
     * Test for {@link BookmarksHelper#parsePageNumber(String)}.
     */
    @Test
    public void parsePageNumber_null() {
        assertNull(BookmarksHelper.parsePageNumber(null));
    }

    /**
     * Test for {@link BookmarksHelper#parsePageNumber(String)}.
     */
    @Test
    public void parsePageNumber() {
        assertEquals(new Integer(1), BookmarksHelper.parsePageNumber("1 XYZ -1 10000 0"));
        assertEquals(new Integer(2), BookmarksHelper.parsePageNumber("2 Fit"));
        assertEquals(new Integer(3), BookmarksHelper.parsePageNumber("3 ABC"));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkPageNumber(HashMap, HashMap)}.
     */
    @Test
    public void getBookmarkPageNumber_fromBookmark() {
        final HashMap<String, Object> bookmark = new HashMap<>();
        bookmark.put(BookmarksHelper.PAGE_KEY, String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, 1));
        assertEquals(new Integer(1), BookmarksHelper.getBookmarkPageNumber(bookmark, null));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkPageNumber(HashMap, HashMap)}.
     */
    @Test
    public void getBookmarkPageNumber_fromNamedDestination() {
        final Integer pageNumber = new Integer(1);
        final String nd = "G1";

        final HashMap<String, String> namedDestinations = new HashMap<>();
        namedDestinations.put(nd, String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, pageNumber));

        final HashMap<String, Object> bookmark = new HashMap<>();
        bookmark.put(BookmarksHelper.NAMED_KEY, nd);

        assertEquals(pageNumber, BookmarksHelper.getBookmarkPageNumber(bookmark, namedDestinations));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkPageNumber(HashMap, HashMap)}.
     */
    @Test
    public void getBookmarkPageNumber_noPageNumber() {
        assertNull(BookmarksHelper.getBookmarkPageNumber(new HashMap<>(), new HashMap<>()));
    }

    /**
     * Test for {@link BookmarksHelper#setBookmarkPageNumber(HashMap, int)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setBookmarkPageNumber_zero() {
        BookmarksHelper.setBookmarkPageNumber(new HashMap<>(), 0);
    }

    /**
     * Test for {@link BookmarksHelper#setBookmarkPageNumber(HashMap, int)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setBookmarkPageNumber_negative() {
        BookmarksHelper.setBookmarkPageNumber(new HashMap<>(), -1);
    }

    /**
     * Test for {@link BookmarksHelper#setBookmarkPageNumber(HashMap, int)}.
     */
    @Test
    public void setBookmarkPageNumber() {
        final int pageNumber = 1;
        final HashMap<String, Object> bookmark = new HashMap<>();
        BookmarksHelper.setBookmarkPageNumber(bookmark, pageNumber);
        assertEquals(String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, pageNumber),
                bookmark.get(BookmarksHelper.PAGE_KEY));
        assertEquals(BookmarksHelper.ACTION_VALUE, bookmark.get(BookmarksHelper.ACTION_KEY));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkChildren(HashMap)}.
     */
    @Test
    public void getBookmarkChildren_null() {
        final HashMap<String, Object> parentBookmark = new HashMap<>();
        assertNull(BookmarksHelper.getBookmarkChildren(parentBookmark));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkChildren(HashMap)}.
     */
    @Test
    public void getBookmarkChildren_empty() {
        final HashMap<String, Object> parentBookmark = new HashMap<>();
        final List<HashMap<String, Object>> kids = new ArrayList<HashMap<String, Object>>();
        parentBookmark.put(BookmarksHelper.KIDS_KEY, kids);
        assertNull(BookmarksHelper.getBookmarkChildren(parentBookmark));
    }

    /**
     * Test for {@link BookmarksHelper#getBookmarkChildren(HashMap)}.
     */
    @Test
    public void getBookmarkChildren() {
        final HashMap<String, Object> parentBookmark = new HashMap<>();
        final List<HashMap<String, Object>> kids = new ArrayList<HashMap<String, Object>>();
        parentBookmark.put(BookmarksHelper.KIDS_KEY, kids);
        kids.add(new HashMap<>());
        kids.add(new HashMap<>());
        assertEquals(kids, BookmarksHelper.getBookmarkChildren(parentBookmark));
    }

    /**
     * Test for {@link BookmarksHelper#addBookmarkChild(HashMap, HashMap)}.
     */
    @Test
    public void addBookmarkChild() {
        final HashMap<String, Object> parentBookmark = new HashMap<>();

        final HashMap<String, Object> childBookmark1 = new HashMap<>();
        final HashMap<String, Object> childBookmark2 = new HashMap<>();

        BookmarksHelper.addBookmarkChild(parentBookmark, childBookmark1);
        BookmarksHelper.addBookmarkChild(parentBookmark, childBookmark2);

        @SuppressWarnings("unchecked")
        final List<HashMap<String, Object>> kids = (List<HashMap<String, Object>>) parentBookmark
                .get(BookmarksHelper.KIDS_KEY);
        assertEquals(2, kids.size());
        assertEquals(childBookmark1, kids.get(0));
        assertEquals(childBookmark2, kids.get(1));
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkToString(HashMap, int)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void bookmarkToString_shiftLessZero() {
        BookmarksHelper.bookmarkToString(new HashMap<String, Object>(), new HashMap<String, String>(), -1);
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkToString(HashMap, int)}.
     */
    @Test
    public void bookmarkToString_withoutPageNumber() {
        final String title = "Title";

        final HashMap<String, Object> bookmark = new HashMap<String, Object>();
        BookmarksHelper.setBookmarkTitle(bookmark, title);

        final String shift0 = "";
        assertEquals(String.format("%s%s", shift0, title), BookmarksHelper.bookmarkToString(bookmark, null, 0));

        final String shift1 = BookmarksHelper.SHIFT;
        assertEquals(String.format("%s%s", shift1, title), BookmarksHelper.bookmarkToString(bookmark, null, 1));

        final String shift2 = BookmarksHelper.SHIFT + BookmarksHelper.SHIFT;
        assertEquals(String.format("%s%s", shift2, title), BookmarksHelper.bookmarkToString(bookmark, null, 2));
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkToString(HashMap, int)}.
     */
    @Test
    public void bookmarkToString_withPageNumber() {
        final String title = "Title";
        final int pageNumber = 1;

        final HashMap<String, Object> bookmark = new HashMap<String, Object>();
        BookmarksHelper.setBookmarkTitle(bookmark, title);
        BookmarksHelper.setBookmarkPageNumber(bookmark, pageNumber);

        final String shift0 = "";
        assertEquals(String.format("%s%s|%d", shift0, title, pageNumber),
                BookmarksHelper.bookmarkToString(bookmark, null, 0));

        final String shift1 = BookmarksHelper.SHIFT;
        assertEquals(String.format("%s%s|%d", shift1, title, pageNumber),
                BookmarksHelper.bookmarkToString(bookmark, null, 1));

        final String shift2 = BookmarksHelper.SHIFT + BookmarksHelper.SHIFT;
        assertEquals(String.format("%s%s|%d", shift2, title, pageNumber),
                BookmarksHelper.bookmarkToString(bookmark, null, 2));
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkListToStringList(List, int)}.
     */
    @Test
    public void bookmarkListToStringList_null() {
        final List<String> lineList = BookmarksHelper.bookmarkListToStringList(null, null);
        assertEquals(0, lineList.size());
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkListToStringList(List, int)}.
     */
    @Test
    public void bookmarkListToStringList() {
        final List<HashMap<String, Object>> bookmarkList = new ArrayList<>();

        final HashMap<String, Object> title1 = new HashMap<String, Object>();
        bookmarkList.add(title1);
        BookmarksHelper.setBookmarkTitle(title1, "Title 1");
        BookmarksHelper.setBookmarkPageNumber(title1, 1);

        final HashMap<String, Object> title2 = new HashMap<String, Object>();
        bookmarkList.add(title2);
        BookmarksHelper.setBookmarkTitle(title2, "Title 2");

        final HashMap<String, Object> title21 = new HashMap<String, Object>();
        BookmarksHelper.addBookmarkChild(title2, title21);
        BookmarksHelper.setBookmarkTitle(title21, "Title 2.1");
        BookmarksHelper.setBookmarkPageNumber(title21, 2);

        final HashMap<String, Object> title22 = new HashMap<String, Object>();
        BookmarksHelper.addBookmarkChild(title2, title22);
        BookmarksHelper.setBookmarkTitle(title22, "Title 2.2");
        BookmarksHelper.setBookmarkPageNumber(title22, 3);

        final List<String> expectedLineList = new ArrayList<>();
        expectedLineList.add(String.format("%s|%d", "Title 1", 1));
        expectedLineList.add("Title 2");
        expectedLineList.add(String.format("%s%s|%d", BookmarksHelper.SHIFT, "Title 2.1", 2));
        expectedLineList.add(String.format("%s%s|%d", BookmarksHelper.SHIFT, "Title 2.2", 3));

        final List<String> lineList = BookmarksHelper.bookmarkListToStringList(bookmarkList, null);
        assertEquals(expectedLineList.size(), lineList.size());
        for (int i = 0; i < expectedLineList.size(); ++i) {
            assertEquals(expectedLineList.get(i), lineList.get(i));
        }
    }

    /**
     * Test for {@link BookmarksHelper#bookmarkListToStringList(List, int)}.
     */
    @Test
    public void bookmarkListToStringList_exception() {
        final List<HashMap<String, Object>> bookmarkList = new ArrayList<>();

        final HashMap<String, Object> title1 = new HashMap<String, Object>();
        bookmarkList.add(title1);
        BookmarksHelper.setBookmarkTitle(title1, "Title 1");

        final HashMap<String, Object> title2 = new HashMap<String, Object>();
        bookmarkList.add(title2);

        final HashMap<String, Object> title3 = new HashMap<String, Object>();
        bookmarkList.add(title3);
        BookmarksHelper.setBookmarkTitle(title3, "Title 3");

        final List<String> lineList = BookmarksHelper.bookmarkListToStringList(bookmarkList, null);
        assertEquals(2, lineList.size());
    }

    /**
     * Test for {@link BookmarksHelper#stringListToBookmarkList(List)}.
     */
    @Test
    public void stringListToBookmarkList_null() {
        final List<HashMap<String, Object>> bookmarkList = BookmarksHelper.stringListToBookmarkList(null);
        assertEquals(0, bookmarkList.size());
    }

    /**
     * Test for {@link BookmarksHelper#stringListToBookmarkList(List)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void stringListToBookmarkList_nullElement() {
        final List<String> lineList = new ArrayList<String>();
        lineList.add("Title 1|1");
        lineList.add(null);
        lineList.add("Title 2");

        BookmarksHelper.stringListToBookmarkList(lineList);
    }

    /**
     * Test for {@link BookmarksHelper#stringListToBookmarkList(List)}.
     */
    @Test
    public void stringListToBookmarkList() {
        final List<String> lineList = new ArrayList<String>();
        lineList.add("Title 1|1");
        lineList.add("");
        lineList.add("Title 2");
        lineList.add(String.format("%s%s|%d", BookmarksHelper.SHIFT, "Title 2.1", 2));
        lineList.add(String.format("%s%s|%d", BookmarksHelper.SHIFT, "Title 2.2", 3));
        lineList.add("");

        final List<HashMap<String, Object>> bookmarkList = BookmarksHelper.stringListToBookmarkList(lineList);
        assertEquals(2, bookmarkList.size());

        final HashMap<String, Object> title1 = bookmarkList.get(0);
        assertEquals("Title 1", title1.get(BookmarksHelper.TITLE_KEY));
        assertEquals(String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, 1), title1.get(BookmarksHelper.PAGE_KEY));
        assertEquals(BookmarksHelper.ACTION_VALUE, title1.get(BookmarksHelper.ACTION_KEY));
        assertFalse(title1.containsKey(BookmarksHelper.KIDS_KEY));

        final HashMap<String, Object> title2 = bookmarkList.get(1);
        assertEquals("Title 2", title2.get(BookmarksHelper.TITLE_KEY));
        assertFalse(title2.containsKey(BookmarksHelper.PAGE_KEY));
        assertFalse(title2.containsKey(BookmarksHelper.ACTION_KEY));
        assertTrue(title2.containsKey(BookmarksHelper.KIDS_KEY));

        @SuppressWarnings("unchecked")
        final List<HashMap<String, Object>> kids = (List<HashMap<String, Object>>) title2.get(BookmarksHelper.KIDS_KEY);
        assertEquals(2, kids.size());

        final HashMap<String, Object> title21 = kids.get(0);
        assertEquals("Title 2.1", title21.get(BookmarksHelper.TITLE_KEY));
        assertEquals(String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, 2), title21.get(BookmarksHelper.PAGE_KEY));
        assertEquals(BookmarksHelper.ACTION_VALUE, title21.get(BookmarksHelper.ACTION_KEY));
        assertFalse(title1.containsKey(BookmarksHelper.KIDS_KEY));

        final HashMap<String, Object> title22 = kids.get(1);
        assertEquals("Title 2.2", title22.get(BookmarksHelper.TITLE_KEY));
        assertEquals(String.format(BookmarksHelper.BOOKMARK_PAGE_TEMPLATE, 3), title22.get(BookmarksHelper.PAGE_KEY));
        assertEquals(BookmarksHelper.ACTION_VALUE, title22.get(BookmarksHelper.ACTION_KEY));
        assertFalse(title1.containsKey(BookmarksHelper.KIDS_KEY));
    }
}
