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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDestinationNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

/**
 * Helper with methods for modify PDF Outlines (bookmarks).
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class OutlineHelper {

    protected static final String  SEPARATOR                            = "|";

    protected static final String  OUTLINE_TEMPLATE_WITHOUT_PAGE_NUMBER = "%s%s";
    protected static final String  OUTLINE_TEMPLATE_WITH_PAGE_NUMBER    = OUTLINE_TEMPLATE_WITHOUT_PAGE_NUMBER
            + SEPARATOR + "%d";

    protected static final String  SHIFT                                = "    ";

    protected static final Pattern OUTLINE_LINE_PATTERN                 = Pattern
            .compile("^(?<shift>\\s*)(?<title>\\S.*)$");

    /**
     * Clean Outline (bookmark) title from not common used symbols.
     * 
     * @param title
     *            Outline (bookmark) title.
     * @return clean title.
     */
    protected static String cleanTitle(final String title) {
        //@formatter:off
        final String cleanLine = title
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

    private static Integer getOutlinesPageNumber(final PDPageDestination pageDestination, final PDPageTree pages) {
        return pages.indexOf(pageDestination.getPage()) + 1;
    }

    private static Integer getOutlinesPageNumber(final PDOutlineItem item, final PDPageTree pages,
            final PDDestinationNameTreeNode destinations) throws IOException {
        final PDDestination destination = item.getDestination();
        if (destination != null) {
            if (destination instanceof PDPageDestination) {
                final PDPageDestination pageDestination = (PDPageDestination) destination;

                return getOutlinesPageNumber(pageDestination, pages);
            }
            if (destination instanceof PDNamedDestination) {
                final PDNamedDestination namedDestination = (PDNamedDestination) destination;

                final PDPageDestination pageDestination = destinations.getValue(namedDestination.getNamedDestination());
                return getOutlinesPageNumber(pageDestination, pages);
            }
        }

        return null;
    }

    private static String outlineToLine(final PDOutlineItem item, final PDPageTree pages,
            final PDDestinationNameTreeNode destinations, final int shift) throws IOException {
        // Shift.
        final StringBuilder bm = new StringBuilder();
        for (int i = 0; i < shift; ++i) {
            bm.append(SHIFT);
        }

        // Title.
        final String title = item.getTitle();

        // Page number.
        final Integer pageNumber = getOutlinesPageNumber(item, pages, destinations);

        // Convert Outline (bookmark) to line.
        if (pageNumber == null) {
            return String.format(OUTLINE_TEMPLATE_WITHOUT_PAGE_NUMBER, bm.toString(), title);
        } else {
            return String.format(OUTLINE_TEMPLATE_WITH_PAGE_NUMBER, bm.toString(), title, pageNumber);
        }
    }

    private static List<String> outlinesToLineList(final PDOutlineItem item, final PDPageTree pages,
            final PDDestinationNameTreeNode destinations, final int shift) throws IOException {
        final List<String> lines = new ArrayList<>();

        // Add Outline (bookmark) line.
        lines.add(outlineToLine(item, pages, destinations, shift));

        // Add lines for children Outlines (bookmarks).
        for (PDOutlineItem child : item.children()) {
            lines.addAll(outlinesToLineList(child, pages, destinations, shift + 1));
        }

        return lines;
    }

    /**
     * Convert Outlines (bookmarks) to list of lines.
     * 
     * @param documentOutline
     *            Source Outlines (bookmarks) object.
     * @param pageTree
     *            Pages of PDF file.
     * @param destinationNameTree
     *            Named destinations of PDF file. Can be <code>null</code>.
     * @return list of lines with Outlines (bookmarks) representation.
     * @throws IOException
     */
    public static List<String> outlinesToLineList(final PDDocumentOutline documentOutline, final PDPageTree pageTree,
            final PDDestinationNameTreeNode destinationNameTree) throws IOException {
        final List<String> lines = new ArrayList<>();

        for (PDOutlineItem outlineItem : documentOutline.children()) {
            lines.addAll(outlinesToLineList(outlineItem, pageTree, destinationNameTree, 0));
        }

        return lines;
    }

    /**
     * Convert list of lines to Outlines (bookmarks) object.
     * 
     * @param lineList
     *            Source list of lines with Outlines (bookmarks) representation.
     * @return Outlines (bookmarks) object.
     */
    public static PDDocumentOutline lineListToOutlines(final PDPageTree pages, final List<String> lineList) {
        final PDDocumentOutline outlines = new PDDocumentOutline();

        if (lineList != null) {
            final List<Integer> shifts = new ArrayList<>();
            final Map<String, PDOutlineItem> linesToBookmarks = new HashMap<>();

            for (int i = 0; i < lineList.size(); ++i) {
                final String line = lineList.get(i);

                // Parse string.
                final Matcher matcher = OUTLINE_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    final int shift = matcher.group("shift").length();

                    final String title = matcher.group("title");

                    PDOutlineItem bookmark = null;
                    final int separatorLastIndex = title.lastIndexOf("|");
                    if (separatorLastIndex >= 0) {
                        try {
                            // Create Outline (bookmark) with page number.
                            bookmark = new PDOutlineItem();

                            // Set title.
                            final String correctTitle = title.substring(0, separatorLastIndex);
                            bookmark.setTitle(correctTitle);

                            // Set page destination.
                            final int pageNumber = Integer.parseInt(title.substring(separatorLastIndex + 1));
                            final PDPageDestination destination = new PDPageFitDestination();
                            destination.setPage(pages.get(pageNumber - 1));
                            bookmark.setDestination(destination);
                        } catch (NumberFormatException e) {
                            // Ignore: we have Outline (bookmark) without page number.
                        }
                    }
                    if (bookmark == null) {
                        // Create Outline (bookmark) without page number.
                        bookmark = new PDOutlineItem();

                        // Set title.
                        bookmark.setTitle(title);
                    }
                    // Map lines to generated Outline (bookmark).
                    linesToBookmarks.put(line, bookmark);
                    // Remember level of Outline (bookmark).
                    shifts.add(shift);
                    // Find parent position.
                    int parentPosition = i - 1;
                    while (parentPosition >= 0 && shifts.get(parentPosition) >= shift) {
                        --parentPosition;
                    }
                    if (parentPosition >= 0) {
                        final String parentLine = lineList.get(parentPosition);
                        final PDOutlineItem parentBookmark = linesToBookmarks.get(parentLine);
                        parentBookmark.addLast(bookmark);
                    } else {
                        outlines.addLast(bookmark);
                    }
                } else {
                    shifts.add(Integer.MAX_VALUE);
                    // Ignore wrong Outline (bookmark) lines, but print error message into console.
                    System.err.println(String.format("Outline (bookmark) have a wrong format: '%s'!", line));
                }
            }
        }

        return outlines;
    }
}
