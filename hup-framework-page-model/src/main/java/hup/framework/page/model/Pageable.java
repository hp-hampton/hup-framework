package hup.framework.page.model;

import java.util.List;

/**
 * 分页接口
 *
 * @version 1.0.0
 * @param <T>
 */
public interface Pageable<T> {

    /**
     * 第几页
     *
     * @return
     */
    Integer getPageNo();

    /**
     * 每页记录数
     * @return
     */
    Integer getPageSize();

    /**
     * 总页数
     *
     * @return
     */
    Integer getTotalPages();

    /**
     * 总记录数
     * @return
     */
    Long getTotalRecords();

    /**
     * 单页记录
     * @return
     */
    List<T> getPageRecords();
}
