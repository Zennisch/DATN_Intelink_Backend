package intelink.dto.request;

import lombok.Data;

@Data
public class SearchShortUrlRequest {
    private String query;
    private String status;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 10;
}
