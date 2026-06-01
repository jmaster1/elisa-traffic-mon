package com.geolog.server.model.userworksite;

import com.geolog.server.model.worksite.Worksite;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserWorksiteOption {

    private final Worksite worksite;

    private final boolean assigned;
}
