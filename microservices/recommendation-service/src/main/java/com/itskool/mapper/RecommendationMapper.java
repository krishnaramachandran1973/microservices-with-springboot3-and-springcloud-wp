package com.itskool.mapper;

import com.itskool.domain.Recommendation;
import com.itskool.dto.RecommendationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    RecommendationDto entityToDto(Recommendation recommendation);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    Recommendation dtoToEntity(RecommendationDto recommendationDto);

    List<RecommendationDto> entityToDtoList(List<Recommendation> recommendations);
    List<Recommendation> dtoToEntityList(List<RecommendationDto> recommendationDtos);
}
