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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper with methods for modify PDF bookmarks.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class BookmarksHelper {

    protected static final String  SEPARATOR                                  = "|";

    protected static final String  BOOKMARK_LINE_TEMPLATE_WITHOUT_PAGE_NUMBER = "%s%s";
    protected static final String  BOOKMARK_LINE_TEMPLATE_WITH_PAGE_NUMBER    = BOOKMARK_LINE_TEMPLATE_WITHOUT_PAGE_NUMBER
            + SEPARATOR + "%d";

    /*
     * See:
     *      http://developers.itextpdf.com/reference/com.itextpdf.text.pdf.PdfDestination/
     *      http://www.programcreek.com/java-api-examples/index.php?api=com.lowagie.text.pdf.PdfDestination
     */
    protected static final String  BOOKMARK_PAGE_TEMPLATE                     = "%d XYZ -1 10000 0";

    protected static final String  SHIFT                                      = "    ";

    private static final String    PAGE_NUMBER_PATTERN                        = "(?<pageNumber>[1-9][0-9]*)";
    protected static final Pattern BOOKMARK_PAGE_PATTERN                      = Pattern
            .compile(String.format("^%s (.+)$", PAGE_NUMBER_PATTERN));

    private static final String    SHIFT_PATTERN                              = "(?<shift>\\s*)";
    private static final String    TITLE_PATTERN                              = "(?<title>\\S.*)";
    protected static final Pattern BOOKMARK_LINE_PATTERN                      = Pattern
            .compile(String.format("^%s%s$", SHIFT_PATTERN, TITLE_PATTERN));

    public static final String     TITLE_KEY                                  = "Title";

    public static final String     PAGE_KEY                                   = "Page";
    public static final String     NAMED_KEY                                  = "Named";

    public static final String     KIDS_KEY                                   = "Kids";

    public static final String     OPEN_KEY                                   = "Open";
    public static final String     OPEN_VALUE                                 = Boolean.toString(false);

    public static final String     ACTION_KEY                                 = "Action";
    public static final String     ACTION_VALUE                               = "GoTo";

    /**
     * Clean title from not common used symbols.
     * 
     * @param line
     *            Source line.
     * @return clean line.
     */
    protected static String cleanTitle(final String line) {
        //@formatter:off
        final String cleanLine = line
            .trim()
            .replaceAll("`",                "'"     )
            .replaceAll("‘",                "'"     )
            .replaceAll("’",                "'"     )
            .replaceAll("”",                "\""    )
            .replaceAll("“",                "\""    )
            .replaceAll("''",               "\""    )
            .replaceAll(" - ",              " – "   )
            .replaceAll("(?<=\\S)–(?=\\S)", " – "   )
            .replaceAll("…",                "..."   )
            .replaceAll("\\. \\. \\.",      "..."   )
            .replaceAll(" ,",               ","     )
            .replaceAll(" \\.",             "."     )
            .replaceAll("\t",               SHIFT   )
            .replaceAll("\\s{2,}",          " "     )
            ;
        //@formatter:on
        if (cleanLine.isEmpty()) {
            throw new IllegalArgumentException("Line is empty!");
        }
        return cleanLine;
    }

    /**
     * Get title from bookmark structure.
     * 
     * @param bookmark
     *            Bookmark structure.
     * @return bookmark title. Can not be <code>null</code> or empty!
     */
    public static String getBookmarkTitle(final HashMap<String, Object> bookmark) {
        return cleanTitle((String) bookmark.get(TITLE_KEY));
    }

    public static void setBookmarkTitle(final HashMap<String, Object> bookmark, final String title) {
        bookmark.put(TITLE_KEY, cleanTitle(title));
    }

    protected static Integer parsePageNumber(final String pageNumber) {
        if (pageNumber != null) {
            final Matcher matcher = BOOKMARK_PAGE_PATTERN.matcher(pageNumber);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        String.format("Bookmark page number have a wrong format: '%s'!", pageNumber));
            }
            // Page number have a correct format -- it was checked by regular expression.
            return Integer.parseInt(matcher.group("pageNumber"));
        } else {
            return null;
        }
    }

    /**
     * Get page number from bookmark structure.
     * 
     * @param bookmark
     *            Bookmark structure.
     * @param namedDestinations
     *            PDF named destinations.
     * @return number of bookmark page or null if bookmark have no page.
     */
    public static Integer getBookmarkPageNumber(final HashMap<String, Object> bookmark,
            final HashMap<String, String> namedDestinations) {
        String pageNumber = (String) bookmark.get(PAGE_KEY);
        if (pageNumber == null) {
            final String named = (String) bookmark.get(NAMED_KEY);
            if (named != null) {
                pageNumber = namedDestinations.get(named);
            }
        }
        return parsePageNumber(pageNumber);
    }

    public static void setBookmarkPageNumber(final HashMap<String, Object> bookmark, final int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Page number can not be less or equals thean 0!");
        }

        bookmark.put(PAGE_KEY, String.format(BOOKMARK_PAGE_TEMPLATE, pageNumber));
        bookmark.put(ACTION_KEY, ACTION_VALUE);
    }

    /**
     * Get children bookmarks from bookmark structure.
     * 
     * @param parentBookmark
     *            Bookmark structure.
     * @return list of children bookmarks structure or null if bookmark have no children.
     */
    public static List<HashMap<String, Object>> getBookmarkChildren(final HashMap<String, Object> parentBookmark) {
        @SuppressWarnings("unchecked")
        final List<HashMap<String, Object>> kids = (List<HashMap<String, Object>>) parentBookmark
                .get(BookmarksHelper.KIDS_KEY);
        if (kids == null) {
            return null;
        }
        if (kids.isEmpty()) {
            return null;
        }
        return kids;
    }

    /**
     * Add one bookmark as a child bookmark to another.
     * 
     * @param parentBookmark
     *            Parent bookmark.
     * @param childBookmark
     *            Child bookmark.
     */
    public static void addBookmarkChild(final HashMap<String, Object> parentBookmark,
            final HashMap<String, Object> childBookmark) {
        List<HashMap<String, Object>> kids = getBookmarkChildren(parentBookmark);
        if (kids == null) {
            kids = new ArrayList<HashMap<String, Object>>();
            parentBookmark.put(KIDS_KEY, kids);
            parentBookmark.put(OPEN_KEY, OPEN_VALUE);
        }
        kids.add(childBookmark);
    }

    /**
     * Convert bookmark to string representation.
     * 
     * @param bookmark
     *            Source bookmark object.
     * @param namedDestinations
     *            Named destinations of PDF file. Can be <code>null</code>.
     * @param shift
     *            Shift of bookmark. If bookmark have a parent bookmark it have a shift. If bookmark is top-level
     *            bookmark shift is 0.
     * @return string representation of bookmark.
     */
    public static String bookmarkToString(final HashMap<String, Object> bookmark,
            final HashMap<String, String> namedDestinations, final int shift) {
        // Shift.
        if (shift < 0) {
            throw new IllegalArgumentException("Shift value can not be less than 0!");
        }
        final StringBuilder bm = new StringBuilder();
        for (int i = 0; i < shift; ++i) {
            bm.append(SHIFT);
        }

        // Title.
        final String title = getBookmarkTitle(bookmark);

        // Page number.
        final Integer pageNumber = getBookmarkPageNumber(bookmark, namedDestinations);

        // Generate a bookmark line.
        if (pageNumber == null) {
            return String.format(BOOKMARK_LINE_TEMPLATE_WITHOUT_PAGE_NUMBER, bm.toString(), title);
        } else {
            return String.format(BOOKMARK_LINE_TEMPLATE_WITH_PAGE_NUMBER, bm.toString(), title, pageNumber);
        }
    }

    private static List<String> bookmarkListToStringList(final List<HashMap<String, Object>> bookmarkList,
            final HashMap<String, String> namedDestinations, final int shift) {
        final List<String> lines = new ArrayList<>();

        if (bookmarkList != null) {
            for (HashMap<String, Object> bookmark : bookmarkList) {
                try {
                    lines.add(bookmarkToString(bookmark, namedDestinations, shift));

                    final List<HashMap<String, Object>> kids = getBookmarkChildren(bookmark);
                    if (kids != null) {
                        // If children exists.
                        lines.addAll(bookmarkListToStringList(kids, namedDestinations, shift + 1));
                    }
                } catch (Exception e) {
                    // Ignore wrong bookmarks, but print error message into console.
                    e.printStackTrace();
                }
            }
        }

        return lines;
    }

    /**
     * Convert bookmark list to string representation.
     * 
     * @param bookmarkList
     *            Source bookmark list.
     * @param namedDestinations
     *            Named destinations of PDF file. Can be <code>null</code>.
     * @return string string representation of bookmarks.
     */
    public static List<String> bookmarkListToStringList(final List<HashMap<String, Object>> bookmarkList,
            final HashMap<String, String> namedDestinations) {
        return bookmarkListToStringList(bookmarkList, namedDestinations, 0);
    }

    /**
     * Generate bookmarks from string representation of bokmarks.
     * 
     * @param lineList
     *            Source string representation of bokmarks.
     * @return bookmarks object.
     */
    public static List<HashMap<String, Object>> stringListToBookmarkList(final List<String> lineList) {
        final List<HashMap<String, Object>> bookmarkList = new ArrayList<>();

        if (lineList != null) {
            final List<Integer> shifts = new ArrayList<>();
            final Map<String, HashMap<String, Object>> linesToBookmarks = new HashMap<>();

            for (int i = 0; i < lineList.size(); ++i) {
                final String line = lineList.get(i);
                if (line == null) {
                    throw new IllegalArgumentException("Line list can not contain null values!");
                }

                // Parse string.
                final Matcher matcher = BOOKMARK_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    final int shift = matcher.group("shift").length();

                    final String title = matcher.group("title");

                    HashMap<String, Object> bookmark = null;
                    final int separatorLastIndex = title.lastIndexOf("|");
                    if (separatorLastIndex >= 0) {
                        try {
                            final String correctTitle = title.substring(0, separatorLastIndex);
                            final int pageNumber = Integer.parseInt(title.substring(separatorLastIndex + 1));
                            // Create bookmark with page number
                            bookmark = new HashMap<String, Object>();
                            setBookmarkTitle(bookmark, correctTitle);
                            setBookmarkPageNumber(bookmark, pageNumber);
                        } catch (NumberFormatException e) {
                            // Ignore: we have bookmark without page number.
                        }
                    }
                    if (bookmark == null) {
                        // Create bookmark without page number.
                        bookmark = new HashMap<String, Object>();
                        setBookmarkTitle(bookmark, title);
                    }
                    // Map lines to generated bookmarks.
                    linesToBookmarks.put(line, bookmark);
                    // Remember level of bookmark.
                    shifts.add(shift);
                    // Find parent position.
                    int parentPosition = i - 1;
                    while (parentPosition >= 0 && shifts.get(parentPosition) >= shift) {
                        --parentPosition;
                    }
                    if (parentPosition >= 0) {
                        final String parentLine = lineList.get(parentPosition);
                        final HashMap<String, Object> parentBookmark = linesToBookmarks.get(parentLine);
                        addBookmarkChild(parentBookmark, bookmark);
                    } else {
                        bookmarkList.add(bookmark);
                    }
                } else {
                    shifts.add(Integer.MAX_VALUE);
                    // Ignore wrong bookmark lines, but print error message into console.
                    System.err.println(String.format("Bookmark have a wrong format: '%s'!", line));
                }
            }
        }

        return bookmarkList;
    }
}
