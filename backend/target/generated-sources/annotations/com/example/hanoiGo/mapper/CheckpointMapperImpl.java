package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.model.Checkpoint;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T17:35:07+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class CheckpointMapperImpl implements CheckpointMapper {

    @Override
    public CheckpointResponse toCheckpointResponse(Checkpoint checkpoint, LocationResponse locationResponse, UserResponse userResponse) {
        if ( checkpoint == null && locationResponse == null && userResponse == null ) {
            return null;
        }

        CheckpointResponse checkpointResponse = new CheckpointResponse();

        if ( checkpoint != null ) {
            checkpointResponse.setCheckedInTime( checkpoint.getCheckedInTime() );
        }
        if ( locationResponse != null ) {
            checkpointResponse.setLocationName( locationResponse.getName() );
        }
        if ( userResponse != null ) {
            checkpointResponse.setUserName( userResponse.getUsername() );
            if ( userResponse.getPoints() != null ) {
                checkpointResponse.setUserPoint( userResponse.getPoints() );
            }
        }

        return checkpointResponse;
    }
}
