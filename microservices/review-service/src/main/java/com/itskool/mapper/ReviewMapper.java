package com.itskool.mapper;

import com.itskool.domain.Review;
import com.itskool.dto.ReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    ReviewDto entityToDto(Review entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    Review dtoToEntity(ReviewDto dto);

    List<ReviewDto> entityListToDtoList(List<Review> entity);

    List<Review> dtoListToEntityList(List<ReviewDto> dtos);

}
