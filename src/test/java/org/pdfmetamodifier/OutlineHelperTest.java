/*
 * Copyright (c) 2012-2018 PdfMetaModifier Team
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test for {@link OutlineHelper}.
 * 
 * @author Dmitry Zavodnikov (d.zavodnikov@gmail.com)
 */
public class OutlineHelperTest {

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces1() {
        OutlineHelper.cleanTitle("");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces2() {
        OutlineHelper.cleanTitle(" ");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces3() {
        OutlineHelper.cleanTitle("  ");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces4() {
        OutlineHelper.cleanTitle("\t");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces5() {
        OutlineHelper.cleanTitle(" \t \t  ");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces6() {
        OutlineHelper.cleanTitle("\r");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces7() {
        OutlineHelper.cleanTitle("\n");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cleanTitle_emptyOrWhitespaces8() {
        OutlineHelper.cleanTitle("\r\n");
    }

    /**
     * Test for {@link OutlineHelper#cleanTitle(String)}.
     */
    @Test
    public void cleanTitle() {
        //@formatter:off
        assertEquals("a",       OutlineHelper.cleanTitle("a"                ));
        assertEquals("a",       OutlineHelper.cleanTitle(" a"               ));
        assertEquals("a",       OutlineHelper.cleanTitle("a  "              ));
        assertEquals("a",       OutlineHelper.cleanTitle("  a   "           ));
        assertEquals("a",       OutlineHelper.cleanTitle("\ta\t\t"          ));
        assertEquals("a b",     OutlineHelper.cleanTitle("  a   b    "      ));
        assertEquals("a b c",   OutlineHelper.cleanTitle("  a   b    c     "));
        assertEquals("a – b",   OutlineHelper.cleanTitle("a–b"              ));
        //@formatter:on
    }

    /**
     * Test for {@link OutlineHelper#lineListToOutlines(org.apache.pdfbox.pdmodel.PDPageTree, java.util.List)} and
     * {@link OutlineHelper#outlinesToLineList(org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline, org.apache.pdfbox.pdmodel.PDPageTree, org.apache.pdfbox.pdmodel.PDDestinationNameTreeNode)}.
     * 
     * @throws IOException
     */
    @Test
    public void lineListToOutlinesAndBack() throws IOException {
        final List<String> lineList = new ArrayList<>();
        lineList.add("Bookmarks");
        lineList.add("    Title 1|1");
        lineList.add("        Title 1.1|2");
        lineList.add("        Title 1.2|3");
        lineList.add("");
        lineList.add("    Title 2|5");
        lineList.add("        Title 2.1|6");
        lineList.add("        Title 2.2|7");
        lineList.add("");
        lineList.add("    Title 3|9");
        lineList.add("        Title 3.1|10");
        lineList.add("        Title 3.2|11");
        lineList.add("");

        final List<String> cleanLineList = new ArrayList<>();
        for (String line : lineList) {
            if (!line.isEmpty()) {
                cleanLineList.add(line);
            }
        }

        final PDPageTree pageTree = mock(PDPageTree.class);
        final List<PDPage> mockPages = new ArrayList<>();
        when(pageTree.get(anyInt())).then(new Answer<PDPage>() {

            @Override
            public PDPage answer(final InvocationOnMock invocation) throws Throwable {
                final int idx = (int) invocation.getArguments()[0];
                for (int i = mockPages.size(); i <= idx; ++i) {
                    mockPages.add(new PDPage());
                }
                return mockPages.get(idx);
            }
        });
        when(pageTree.indexOf(any(PDPage.class))).then(new Answer<Integer>() {

            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable {
                final PDPage page = (PDPage) invocation.getArguments()[0];
                return mockPages.indexOf(page);
            }
        });

        final PDDocumentOutline documentOutline = OutlineHelper.lineListToOutlines(pageTree, lineList);

        final List<String> resultLineList = OutlineHelper.outlinesToLineList(documentOutline, pageTree, null);

        assertEquals(cleanLineList.size(), resultLineList.size());
        for (int i = 0; i < cleanLineList.size(); ++i) {
            assertEquals(cleanLineList.get(i), resultLineList.get(i));
        }
    }
}
