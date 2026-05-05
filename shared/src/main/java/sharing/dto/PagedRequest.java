package sharing.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import sharing.constant.DateConstant;

@Data
public class PagedRequest {
    private int pageNumber = 0;

    private int pageSize = 10;

    private String sortBy = "createdAt";

    private String sortDir = "DESC";

    private String keywords;

    @DateTimeFormat(pattern = DateConstant.DATE_TIME_FORMAT)
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = DateConstant.DATE_TIME_FORMAT)
    private LocalDateTime endDate;

    private Map<String, Object> filter = new HashMap<>();
}
