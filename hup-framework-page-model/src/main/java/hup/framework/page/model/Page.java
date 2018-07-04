package hup.framework.page.model;

import java.util.List;

/**
 * Page Object
 *
 * @param <T>
 * @author
 * @version 1.0.0
 */
public class Page<T> implements Pageable<T> {

    public static final Integer MAX_PAGE_SIZE = Integer.parseInt(System.getProperty("page.maxPageSize", "100"));

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private List<T> pageRecords;

    private Long totalRecords;

    public Page() {
    }

    public Page(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Page(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    @Override
    public Integer getPageNo() {
        return pageNo;
    }

    @Override
    public Integer getPageSize() {
        return pageSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageSize;
    }

    @Override
    public Integer getTotalPages() {
        if (totalRecords == null) {
            totalRecords = 0L;
        }
        int totalPages = Math.toIntExact(totalRecords / getPageSize());
        return totalRecords % pageSize == 0 ? totalPages : totalPages + 1;
    }

    @Override
    public Long getTotalRecords() {
        return totalRecords;
    }

    @Override
    public List<T> getPageRecords() {
        return pageRecords;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageRecords(List<T> pageRecords) {
        this.pageRecords = pageRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

}
