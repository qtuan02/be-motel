package sharing.base.service.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import sharing.base.entity.BaseEntity;
import sharing.base.exception.ResourceNotFoundException;
import sharing.base.mapper.BaseMapper;
import sharing.base.repository.BaseRepository;
import sharing.base.service.BaseService;
import sharing.dto.PagedRequest;
import sharing.dto.PagedResponse;

public abstract class BaseServiceImpl<T extends BaseEntity, ID, REQ, RES, R extends BaseRepository<T, ID>>
        implements BaseService<T, ID, REQ, RES> {

    protected final R repository;
    protected final BaseMapper<T, REQ, RES> mapper;
    protected final Class<T> entityClass;

    protected BaseServiceImpl(R repository, BaseMapper<T, REQ, RES> mapper, Class<T> entityClass) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = entityClass;
    }

    protected T mapToEntity(REQ request) {
        return mapper.toEntity(request);
    }

    protected RES mapToResponse(T entity) {
        return mapper.toResponse(entity);
    }

    protected void updateEntity(T entity, REQ request) {
        mapper.updateEntity(entity, request);
    }

    @Override
    public RES create(REQ request) {
        T entity = mapToEntity(request);
        return mapToResponse(repository.save(entity));
    }

    @Override
    public RES update(ID id, REQ request) {
        T entity = repository
                .findById(id)
                .filter(e -> e.getDeletedAt() == 0L)
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName(), "id", id));
        updateEntity(entity, request);
        return mapToResponse(repository.save(entity));
    }

    @Override
    public Optional<RES> findById(ID id) {
        return repository.findById(id).filter(e -> e.getDeletedAt() == 0L).map(this::mapToResponse);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.findById(id).filter(e -> e.getDeletedAt() == 0L).isPresent();
    }

    @Override
    public List<RES> findAll() {
        return repository.findAll(notDeleted()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RES> findAll(Pageable pageable) {
        return repository.findAll(notDeleted(), pageable).map(this::mapToResponse);
    }

    @Override
    public PagedResponse<RES> search(PagedRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy()));
        Page<RES> page =
                repository.findAll(buildSpecification(request), pageable).map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    public List<RES> findAllById(Iterable<ID> ids) {
        return StreamSupport.stream(ids.spliterator(), false)
                .map(repository::findById)
                .filter(opt -> opt.isPresent() && opt.get().getDeletedAt() == 0L)
                .map(opt -> mapToResponse(opt.get()))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return repository.count(notDeleted());
    }

    @Override
    public void deleteById(ID id) {
        repository.findById(id).filter(e -> e.getDeletedAt() == 0L).ifPresent(this::softDelete);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll() {
        repository.findAll(notDeleted()).forEach(this::softDelete);
    }

    protected Specification<T> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deletedAt"), 0L);
    }

    protected Specification<T> buildSpecification(PagedRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deletedAt"), 0L));

            if (request.getKeywords() != null && !request.getKeywords().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("code")), "%" + request.getKeywords().toLowerCase() + "%"));
            }

            if (request.getFilter() != null) {
                request.getFilter().forEach((field, value) -> {
                    if (value != null && !value.toString().isBlank()) {
                        try {
                            predicates.add(cb.equal(root.get(field), value));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                });
            }

            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void softDelete(T entity) {
        entity.setDeletedAt(System.currentTimeMillis());
        repository.save(entity);
    }
}
