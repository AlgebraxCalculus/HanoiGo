package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.model.LocationDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {
    LocationResponse toLocationResponse(LocationDetail ld);
}
