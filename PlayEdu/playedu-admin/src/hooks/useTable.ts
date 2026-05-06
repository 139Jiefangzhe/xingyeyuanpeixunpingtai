import type { TablePaginationConfig } from "antd";
import { useEffect, useState } from "react";
import type { ApiResult, PageResult, TableQuery } from "../types/api";

const EMPTY_PAGE_RESULT: PageResult<any> = {
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  pages: 0,
};

export function useTable<T, Q extends TableQuery>(
  fetcher: (query: Q) => Promise<ApiResult<PageResult<T>>>,
  initialQuery: Q
) {
  const [query, setQuery] = useState<Q>(initialQuery);
  const [data, setData] = useState<PageResult<T>>(
    EMPTY_PAGE_RESULT as PageResult<T>
  );
  const [loading, setLoading] = useState(false);
  const [reloadIndex, setReloadIndex] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetcher(query)
      .then((result) => {
        if (!active) {
          return;
        }
        setData(result.data || (EMPTY_PAGE_RESULT as PageResult<T>));
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [fetcher, query, reloadIndex]);

  const updateQuery = (patch: Partial<Q>) => {
    setQuery((current) => ({
      ...current,
      ...patch,
    }));
  };

  const resetQuery = (nextQuery?: Partial<Q>) => {
    setQuery({
      ...initialQuery,
      ...nextQuery,
    });
  };

  const reload = () => {
    setReloadIndex((value) => value + 1);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    updateQuery({
      pageNum: pagination.current || query.pageNum,
      pageSize: pagination.pageSize || query.pageSize,
    } as Partial<Q>);
  };

  return {
    query,
    data,
    loading,
    updateQuery,
    resetQuery,
    reload,
    handleTableChange,
    pagination: {
      current: data.pageNum,
      pageSize: data.pageSize,
      total: data.total,
      showSizeChanger: true,
    } satisfies TablePaginationConfig,
  };
}
