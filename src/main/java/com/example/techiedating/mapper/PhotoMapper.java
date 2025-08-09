package com.example.techiedating.mapper;

import com.example.techiedating.dto.PhotoDTO;
import com.example.techiedating.model.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoMapper INSTANCE = Mappers.getMapper(PhotoMapper.class);

    @Mapping(target = "primary", source = "primary")
    @Mapping(target = "createdAt", source = "createdAt")
    PhotoDTO toPhotoDTO(Photo photo);

    @Mapping(target = "isPrimary", source = "primary")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Photo toPhoto(PhotoDTO photoDTO);
}
