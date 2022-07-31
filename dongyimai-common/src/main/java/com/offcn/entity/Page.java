package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {

    private List<T> list; // 每页查询出来的数据存放的集合
    private int pageSize = 10; // 每页显示的记录数
    private int pageNo; // 当前页，通过用户传入
    //	private int totalPageNo; // 总页数，通过计算得到
    private long totalRecord; // 总记录数，通过查询数据库得到

    private int firstPage;    //分页页码集合起始记录

    private int lastPage;      //分页页码集合结束记录

    private int maxPage;  //分页最大页码

    private boolean firstDot = true;
    private boolean lastDot = true;

    public Page() {
    }

    //构建分页管理类时提供三个参数
    public Page(int pageSize, int pageNo, long totalRecord) {
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.totalRecord = totalRecord;
        this.firstPage = 1;
        this.lastPage = getTotalPageNo();
        this.maxPage = getTotalPageNo();
        setPageLabel();

    }

    //用于计算显示第一页、显示最后一页以及前后...的值
    private void setPageLabel() {
        //总页数小于等于5
        if (this.maxPage <= 5) {
            this.firstPage = 1;
            this.lastPage = this.maxPage;
            this.firstDot = false;
            this.lastDot = false;
        } else {
            if (this.pageNo <= 3) {
                this.firstPage = 1;
                this.lastPage = 5;
                this.firstDot = false;
                this.lastDot = true;
            } else if (this.pageNo >= this.maxPage - 2) {
                this.firstPage = this.maxPage - 4;
                this.lastPage = this.maxPage;
                this.firstDot = true;
                this.lastDot = false;
            } else {
                this.firstPage = this.pageNo - 2;
                this.lastPage = this.pageNo + 2;
                this.firstDot = true;
                this.lastDot = true;
            }
        }
    }

    //求总页数
    public int getTotalPageNo() {
        //总记录数/每页行数，
        if (this.totalRecord % this.pageSize == 0) {
            // 能整除
            return (int) (this.totalRecord / this.pageSize);
        } else {
            //不能整除+1
            return (int) (this.totalRecord / this.pageSize + 1);
        }
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        if (this.pageNo < 1) {
            return 1;
        }
        if (this.pageNo > this.maxPage) {
            return this.maxPage;
        }
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(int firstPage) {
        this.firstPage = firstPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public boolean isFirstDot() {
        return firstDot;
    }

    public void setFirstDot(boolean firstDot) {
        this.firstDot = firstDot;
    }

    public boolean isLastDot() {
        return lastDot;
    }

    public void setLastDot(boolean lastDot) {
        this.lastDot = lastDot;
    }

    // 判断是否有上一页
    public boolean hasPrev() {
        return getPageNo() > 1;
    }

    // 获取上一页
    public long getPrev() {
        return hasPrev() ? getPageNo() - 1 : 1;
    }

    // 判断是否有下一页
    public boolean hasNext() {
        return getPageNo() < getTotalPageNo();
    }

    // 获取下一页
    public long getNext() {
        return hasNext() ? getPageNo() + 1 : getTotalPageNo();
    }
}