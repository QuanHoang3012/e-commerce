export interface ApiResponse<T> {
  timestamp: string;
  path: string;
  success: boolean;
  code: number;
  message: string;
  data: T;
  errors?: Record<string, string>;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
