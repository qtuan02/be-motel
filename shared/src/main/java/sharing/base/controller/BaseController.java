package sharing.base.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sharing.base.entity.BaseEntity;
import sharing.base.service.BaseService;
import sharing.dto.PagedRequest;
import sharing.dto.PagedResponse;

public abstract class BaseController<T extends BaseEntity, ID, REQ, RES, S extends BaseService<T, ID, REQ, RES>> {

    protected final S service;

    protected BaseController(S service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RES> create(@Valid @RequestBody REQ request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES> getById(@PathVariable ID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<RES>> search(
            @Valid @ModelAttribute PagedRequest request, HttpServletRequest httpRequest) {
        request.setFilter(extractDynamicFilters(httpRequest));
        return ResponseEntity.ok(service.search(request));
    }

    @GetMapping
    public ResponseEntity<List<RES>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES> update(@PathVariable ID id, @Valid @RequestBody REQ request) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private static final List<String> RESERVED_PARAMS =
            Arrays.asList("pageNumber", "pageSize", "sortBy", "sortDir", "keyword", "startDate", "endDate");

    private Map<String, Object> extractDynamicFilters(HttpServletRequest request) {
        Map<String, Object> filter = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!RESERVED_PARAMS.contains(key) && values != null && values.length > 0 && !values[0].isEmpty()) {
                filter.put(key, values[0]);
            }
        });
        return filter;
    }
}
