package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.ToString;

/**
 * 封装分页相关的信息.
 */
@Getter
@ToString
public class Page {

    private int current = 1;// 当前页码
    private int limit = 10;// 显示上限行数
    private int rows;// 数据总数（用于计算总页数）
    private String path;// 查询路径（用于复用分页链接）

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行.
     *
     * @return 当前页的起始行
     */
    public int getOffset() {
        // current * limit - limit
        return (current - 1) * limit;
    }

    /**
     * 获取总页数.
     *
     * @return 总页数
     */
    public int getTotal() {
        // rows / limit [+1]
        return rows / limit + (rows % limit == 0 ? 0 : 1);
    }

    /**
     * 获取起始页码，这里设置为显示当前页码前两页和后两页。
     *
     * @return 起始页码
     */
    public int getFrom() {
        // current - 2
        return Math.max(current - 2, 1);
    }

    /**
     * 获取结束页码，这里设置为显示当前页码前两页和后两页。
     *
     * @return 结束页码
     */
    public int getTo() {
        // current + 2
        int total = getTotal();
        return Math.min(current + 2, total);
    }
}
