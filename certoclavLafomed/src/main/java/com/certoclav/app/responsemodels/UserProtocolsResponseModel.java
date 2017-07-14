package com.certoclav.app.responsemodels;

import com.certoclav.app.database.Protocol;

import java.util.List;

/**
 * Created by musaq on 2/13/2017.
 */

public class UserProtocolsResponseModel extends ResponseModel {

    private List<Protocol> protocols;

    public List<Protocol> getProtocols() {
        return protocols;
    }
}
