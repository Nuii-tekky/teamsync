package com.teamsync.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> items;
    private long totalCount;
    private int currentPage;
    private int totalPages;
    private boolean hasNext;

    public PaginatedResponse(List<T> items, long totalCount, int currentPage, int totalPages, boolean hasNext) {
        this.items = items;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    // Getters and setters
    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}