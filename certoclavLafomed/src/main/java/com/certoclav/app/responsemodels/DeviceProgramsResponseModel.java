package com.certoclav.app.responsemodels;

import com.certoclav.app.database.Profile;

import java.util.List;

/**
 * Created by musaq on 2/13/2017.
 */

public class DeviceProgramsResponseModel extends ResponseModel {

    private List<Profile> programs;

    public List<Profile> getPrograms() {
        return programs;
    }
}
