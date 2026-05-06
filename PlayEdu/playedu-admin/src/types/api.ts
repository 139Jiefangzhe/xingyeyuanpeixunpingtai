export interface ApiResult<T> {
  code: string | number;
  msg: string;
  data: T;
  version?: string;
  timestamp?: number;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

export interface TableQuery {
  pageNum: number;
  pageSize: number;
}

export interface EnumOption {
  label: string;
  value: string | number;
}

export type EnumDictionary = Record<string, EnumOption[]>;
