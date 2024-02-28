package com.uefa.platform.service.b2bpush;

import com.uefa.platform.dto.competition.v2.Person;
import com.uefa.platform.dto.competition.v2.Player;
import com.uefa.platform.dto.competition.v2.Team;

public class CompetitionTestModels {

    private CompetitionTestModels() {
    }

    public static Player dummyPlayer(String id) {
        return new Player(null, null, null, null, null, id,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }

    public static Team dummyTeam(String id) {
        return new Team(id, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    public static Person dummyPerson(String id) {
        return new Person(id, null, null, null);
    }
}
