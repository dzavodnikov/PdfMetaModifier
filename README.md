![Travis PdfMetaModifier master status](https://travis-ci.org/dzavodnikov/PdfBookmarksModifier.svg?branch=master)


Overview
========
Simple CLI utility for save/update Outlines (bookmarks), Metadata and Embedded (attached) files into PDF files.


Features
========
 * save/update Outlines (bookmarks);
 * save/update Metadata;
 * save/remove/add Embedded (attached) files.

Usage
=====
Outlines (bookmarks)
-------------------- 
Save bookmarks into the text file:

    $ pmm --pdf Book.pdf --save-outlines Book_bookmarks.txt

Update bookmarks from the text file:

    $ pmm --pdf Book.pdf --update-outlines Book_bookmarks.txt

Bookmarks text file have following format:

    [<tabs or spaces>]<title>[|<page number>]

For example:

    Title 1|3
    Title 2|5
        Title 2.1|5
        Title 2.2|6
            Title 2.2.1|7
            Title 2.2.2|8
        Title 2.3|10
    Appendix
        A1|11
        A2|12


Metadata
--------
Save metadata into the text file:

    $ pmm --pdf Book.pdf --save-metadata Book_metadata.txt

Update metadata from the text file:

    $ pmm --pdf Book.pdf --update-metadata Book_metadata.txt

Metadata text file have following format:

    <key>|[<value>]

For example:

    CreationDate|D:20160419132404+05'00'
    Creator|PlotSoft PDFill 5.0
    ModDate|D:20160419132404+05'00'
    Producer|GPL Ghostscript 9.19
    Title|


Embedded (attached) files
-------------------------
Save all Embedded (attached) files from PDF to specified directory:

    $ pmm --pdf Book.pdf --save-attachments Book_attachments

Remove all Embedded (attached) files from PDF file:

    $ pmm --pdf Book.pdf --remove-attachments

Add 2 Embedded (attached) files (`Cover.png` and `Source.tar.gz`) to PDF file:

    $ pmm --pdf Book.pdf --add-attachment Cover.png --add-attachment Source.tar.gz


License
=======
Distributed under Apache License 2.0.
