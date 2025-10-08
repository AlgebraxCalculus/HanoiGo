package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.model.LocationDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T12:08:59+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class LocationMapperImpl implements LocationMapper {

    @Override
    public LocationResponse toLocationResponse(LocationDetail ld) {
        if ( ld == null ) {
            return null;
        }

        LocationResponse locationResponse = new LocationResponse();

        locationResponse.setAddress( ld.getAddress() );
        locationResponse.setDefaultPicture( ld.getDefaultPicture() );
        locationResponse.setDescription( ld.getDescription() );
        locationResponse.setLatitude( ld.getLatitude() );
        locationResponse.setLongitude( ld.getLongitude() );
        locationResponse.setName( ld.getName() );

        return locationResponse;
    }
}
