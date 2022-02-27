package com.trss.bi.service.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseEntityMapper<D, E> implements EntityMapper<D, E> {
    @Override
    abstract public E toEntity(D dto);

    @Override
    abstract public D toDto(E entity);

    @Override
    public List<E> toEntity(List<D> dtoList) {
        return dtoList.stream()
            .filter(Objects::nonNull)
            .map(this::toEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<D> toDto(List<E> entityList) {
        return entityList.stream()
            .filter(Objects::nonNull)
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
