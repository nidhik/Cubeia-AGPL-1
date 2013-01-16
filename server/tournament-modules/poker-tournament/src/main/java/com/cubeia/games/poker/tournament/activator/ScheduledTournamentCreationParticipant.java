/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.tournament.activator;

import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;
import com.cubeia.games.poker.tournament.configuration.ScheduledTournamentInstance;
import com.cubeia.games.poker.tournament.configuration.lifecycle.ScheduledTournamentLifeCycle;
import com.cubeia.games.poker.tournament.configuration.lifecycle.TournamentLifeCycle;
import com.cubeia.games.poker.tournament.state.PokerTournamentState;
import com.cubeia.games.poker.tournament.status.PokerTournamentStatus;
import com.cubeia.poker.tournament.history.storage.api.TournamentHistoryPersistenceService;

import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.IDENTIFIER;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.REGISTRATION_OPENING_TIME;
import static com.cubeia.games.poker.tournament.PokerTournamentLobbyAttributes.START_TIME;

public class ScheduledTournamentCreationParticipant extends PokerTournamentCreationParticipant {

    private ScheduledTournamentInstance instanceConfiguration;

    private String pattern = "yyyy-MM-dd HH:mm";

    public ScheduledTournamentCreationParticipant(ScheduledTournamentInstance config, TournamentHistoryPersistenceService storageService) {
        super(config.getConfiguration(), storageService);
        instanceConfiguration = config;
    }

    @Override
    protected int getMinutesVisibleAfterFinished() {
        return instanceConfiguration.getSchedule().getMinutesVisibleAfterFinished();
    }

    @Override
    protected TournamentLifeCycle getTournamentLifeCycle() {
        return new ScheduledTournamentLifeCycle(instanceConfiguration.getStartTime(), instanceConfiguration.getOpenRegistrationTime());
    }

    @Override
    protected void tournamentCreated(MTTStateSupport stateSupport, PokerTournamentState pokerState, LobbyAttributeAccessor lobbyAttributeAccessor) {
        super.tournamentCreated(stateSupport, pokerState, lobbyAttributeAccessor);
        pokerState.setResurrectingPlayers(instanceConfiguration.getResurrectingPlayers());
        setStatus(pokerState, lobbyAttributeAccessor, PokerTournamentStatus.ANNOUNCED);
        lobbyAttributeAccessor.setStringAttribute(IDENTIFIER.name(), instanceConfiguration.getIdentifier());
        lobbyAttributeAccessor.setStringAttribute(START_TIME.name(), instanceConfiguration.getStartTime().toString(pattern));
        lobbyAttributeAccessor.setStringAttribute(REGISTRATION_OPENING_TIME.name(), instanceConfiguration.getOpenRegistrationTime().toString(pattern));
        setScheduledStartTime(pokerState);
    }

    private void setScheduledStartTime(PokerTournamentState pokerState) {
        if (storageService != null) {
            storageService.setScheduledStartTime(pokerState.getHistoricId(), instanceConfiguration.getStartTime().toDate());
        }
    }

    // Overriding so we don't create a new historic tournament when resurrecting tournaments.
    @Override
    protected void createHistoricTournament(MTTStateSupport state, PokerTournamentState pokerState) {
        if (instanceConfiguration.getResurrectingPlayers().isEmpty()) {
            super.createHistoricTournament(state, pokerState);
        } else {
            pokerState.setHistoricId(instanceConfiguration.getHistoricId());
        }
    }

    @Override
    protected boolean isSitAndGo() {
        return false;
    }

    @Override
    protected int getConfigurationTemplateId() {
        return instanceConfiguration.getTemplateId();
    }

    @Override
    protected String getType() {
        return "scheduled";
    }

    public ScheduledTournamentInstance getInstance() {
        return instanceConfiguration;
    }
}
