package sharing.base.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<T, REQ, RES> {

    T toEntity(REQ request);

    RES toResponse(T entity);

    void updateEntity(@MappingTarget T entity, REQ request);
}
