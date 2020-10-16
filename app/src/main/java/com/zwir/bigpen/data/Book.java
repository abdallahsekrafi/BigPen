package com.zwir.bigpen.data;

import java.util.ArrayList;

public class Book {

    private String bookTitle;
    private String bookCoverUrl;
    private Double bookPrice;
    private ArrayList<BookPage> bookPages;

    public Book() {
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public Double getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(Double bookPrice) {
        this.bookPrice = bookPrice;
    }

    public ArrayList<BookPage> getBookPages() {
        return bookPages;
    }

    public void setBookPages(ArrayList<BookPage> bookPages) {
        this.bookPages = bookPages;
    }
}
