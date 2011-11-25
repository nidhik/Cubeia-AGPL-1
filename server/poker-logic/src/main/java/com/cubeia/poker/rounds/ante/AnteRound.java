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

package com.cubeia.poker.rounds.ante;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import com.cubeia.poker.GameType;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.action.PossibleAction;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.RoundVisitor;

public class AnteRound implements Round {

	private static final long serialVersionUID = -6452364533249060511L;

	private static transient Logger log = Logger.getLogger(AnteRound.class);

	private final GameType game;

	private AnteRoundHelper anteRoundHelper;

	public AnteRound(GameType game, AnteRoundHelper anteRoundHelper) {
		this.game = game;
        this.anteRoundHelper = anteRoundHelper;
		
		clearPlayerActionOptions();
		
		Collection<PokerPlayer> players = game.getState().getCurrentHandSeatingMap().values();
		
		// TODO: how do we decide dealer? for now always use first player...
		//moveDealerButton();
		
		handleAnteRequests(players);
	}
	
	/**
	 * Check if any players are sitting out but are in the hand and make them
	 * auto-decline ante.
	 * 
	 * Request ante from all other players.
	 * 
	 * @param players
	 */
	private void handleAnteRequests(Collection<PokerPlayer> players) {
		Collection<PokerPlayer> activePlayers = new ArrayList<PokerPlayer>(players);
		for (PokerPlayer player : players) {
			if (player.isSittingOut()) {
				activePlayers.remove(player);
				declineAnteForPlayer(player);
			} 
		}
		requestAnteFromAllPlayersInHand(activePlayers);
    }
	
	private void declineAnteForPlayer(PokerPlayer player) {
		PokerAction action = new PokerAction(player.getId(), PokerActionType.DECLINE_ENTRY_BET);
		act(action);
	}

	private void requestAnteFromAllPlayersInHand(Collection<PokerPlayer> players) {
	    anteRoundHelper.requestAntes(players, game.getBlindsInfo().getAnteLevel(), game);
    }

//    private void moveDealerButton() {
//    	Collection<PokerPlayer> players = game.getState().getCurrentHandSeatingMap().values();
//		int newDealerSeatId = players.iterator().next().getSeatId();
//		
//		BlindsInfo blindsInfo = game.getBlindsInfo();
//		blindsInfo.setDealerButtonSeatId(newDealerSeatId);
//		game.getState().notifyDealerButton(blindsInfo.getDealerButtonSeatId());
//	}
	
	private void clearPlayerActionOptions() {
		SortedMap<Integer, PokerPlayer> seatingMap = game.getState().getCurrentHandSeatingMap();
		for (PokerPlayer p : seatingMap.values()) {
			p.clearActionRequest();
		}
	}
	
	public void act(PokerAction action) {
		log.debug("Act: "+action);
		PokerPlayer player = game.getState().getPlayerInCurrentHand(action.getPlayerId());
		verifyValidAnte(player);
		
		switch (action.getActionType()) {
		case ANTE:
			player.addBet(game.getBlindsInfo().getAnteLevel());
			player.setHasActed(true);
			player.setHasPostedEntryBet(true);
			action.setBetAmount(game.getBlindsInfo().getAnteLevel());
			game.getServerAdapter().notifyActionPerformed(action, player);
			game.getServerAdapter().notifyPlayerBalance(player);
			game.getState().notifyBetStacksUpdated();
			
			break;
		case DECLINE_ENTRY_BET:
            player.setHasActed(true);
            player.setHasFolded(true);
            player.setHasPostedEntryBet(false);
            game.getServerAdapter().notifyActionPerformed(action, player);
            game.getServerAdapter().notifyPlayerBalance(player);
            setPlayerSitOut(player);
            break;
		default:
			throw new IllegalArgumentException(action.getActionType() + " is not legal here");
		}
		
		player.clearActionRequest();
		
        Collection<PokerPlayer> playersInHand = game.getState().getCurrentHandSeatingMap().values();
		
		if (anteRoundHelper.isImpossibleToStartRound(playersInHand)) {
		    log.debug("impossible to start hand, too few players payed ante, will cancel");
			Collection<PokerPlayer> declinedPlayers = anteRoundHelper.setAllPendingPlayersToDeclineEntryBet(playersInHand);
			for (PokerPlayer declinedPlayer : declinedPlayers) {
		        PokerAction declineAction = new PokerAction(declinedPlayer.getId(), PokerActionType.DECLINE_ENTRY_BET);
	            game.getServerAdapter().notifyActionPerformed(declineAction, player);
			}
		}
	}
	
	private Collection<PokerPlayer> getAllSeatedPlayers() {
		return game.getState().getCurrentHandSeatingMap().values();
	}

	private void setPlayerSitOut(PokerPlayer player) {
		game.getState().playerIsSittingOut(player.getId(), SitOutStatus.MISSED_ANTE);
	}

	/**
	 * Verify that this player is allowed to place ante.
	 * 
	 * @param player
	 * @throws IllegalArgumentException if the player was not allowed to place ANTE
	 */
	private void verifyValidAnte(PokerPlayer player) {
		PossibleAction option = player.getActionRequest().getOption(PokerActionType.ANTE);
		if (option == null) {
			throw new IllegalArgumentException("Illegal ante request from player ["+player+"]");
		}
	} 

	public void timeout() {
		List<PokerPlayer> playersToSitOut = new ArrayList<PokerPlayer>();
		
		for (PokerPlayer player : getAllSeatedPlayers()) {
	        if (!player.hasActed()) {
	            log.debug("Player["+player+"] ante timed out. Will decline entry bet.");
	            PokerAction action = new PokerAction(player.getId(), PokerActionType.DECLINE_ENTRY_BET, true);
	            
	            player.setHasActed(true);
	            player.setHasFolded(true);
	            player.setHasPostedEntryBet(false);
	            player.clearActionRequest();
	            game.getServerAdapter().notifyActionPerformed(action, player);
	            game.getServerAdapter().notifyPlayerBalance(player);
	            playersToSitOut.add(player);
	        }
	    }
		
		// sit out all the players
		for (PokerPlayer player : playersToSitOut) {			
			setPlayerSitOut(player);
		}
	}

	public String getStateDescription() {
		return "currentState=null";
	}

	public boolean isFinished() {
	    for (PokerPlayer player : getAllSeatedPlayers()) {
	        if (!player.hasActed() && !player.isSittingOut()) {
	            return false;
	        }
	    }
		return true;
	}
	
	public void visit(RoundVisitor visitor) {
		visitor.visit(this);
	}

    public boolean isCanceled() {
        Collection<PokerPlayer> players = game.getState().getCurrentHandSeatingMap().values();
        return anteRoundHelper.hasAllPlayersActed(players)  &&  anteRoundHelper.numberOfPlayersPayedAnte(players) < 2;
    }
}
