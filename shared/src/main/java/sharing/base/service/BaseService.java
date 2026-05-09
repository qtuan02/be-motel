package sharing.base.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sharing.base.entity.BaseEntity;
import sharing.dtos.PagedRequest;
import sharing.dtos.PagedResponse;

public interface BaseService<T extends BaseEntity, ID, REQ, RES> {

    RES create(REQ request);

    RES update(ID id, REQ request);

    Optional<RES> findById(ID id);

    boolean existsById(ID id);

    List<RES> findAll();

    Page<RES> findAll(Pageable pageable);

    PagedResponse<RES> search(PagedRequest request);

    List<RES> findAllById(Iterable<ID> ids);

    long count();

    void deleteById(ID id);

    void deleteAllById(Iterable<? extends ID> ids);

    void deleteAll();
}
