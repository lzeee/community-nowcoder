package com.gsz.community.entity;

/**
 * 封装分页相关信息
 * */
public class Page {
    // 当前页
    private int current = 1;
    // 上限
    private int limit = 10;
    // 数据总数，用于计算总的页数
    private int rows;
    // 查询路径，方便点击可以直接查询
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1 && limit<=50)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //根据页码获取offset，因为post服务里面是根据偏移量来查的
    public int getOffset(){
        return (current-1)*limit;
    }

    //获取总的页数
    public int getTotal(){
        if(rows%limit==0){
            return rows/limit;
        }
        else{
            return rows/limit + 1;
        }
    }

    //获取起始页码
    public int getFrom(){
        int from = current - 2;
        return from < 1? 1:from;
    }

    //获取终止页码
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return to > total? total : to;
    }
}
