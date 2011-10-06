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

package com.cubeia.poker.tickets;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.action.PokerActionType;

/**
 * Test for Ticket 55:
 * 
 * We get reported ignored table time outs in the log;
 * 
 * 2010-04-12 01:51:53,292 WARN  ReceivingGameEventDaemon-44 games.poker.handler.PokerHandler - Table[6] Ignoring scheduled command, 
 * current-seq[35] command-seq[-1] - command[pid[0] type[TIMEOUT] seq[-1]] state[PokerState - state[PlayingState] 
 * type[TexasHoldem, current round[BettingRound, isFinished[false]] roundId[0] ]]
 * 
 * This is not ok behavior. A table timeout should never be overridden by a player timeout. 
 * We most likely have more cases like the previous timeout mayhem where multiple timeouts are scheduled and the last one wins the 
 * race to be the correct sequence for the response.
 * 
 */
public class Ticket55_TimeoutOOB extends AbstractTexasHandTester {

	/**
	 * Mock Game is staked at 10/5'
	 * Player default balance: 5000
	 */
	public void testAllInHoldemHand() {
		game.setAnteLevel(10);
		MockPlayer[] mp = testUtils.createMockPlayers(4);
		int[] p = testUtils.createPlayerIdArray(mp);
		addPlayers(game, mp);
		
		assertEquals(1, mockServerAdapter.getTimeoutRequests());
		// Force start
		game.timeout();
		
		// Blinds
		System.out.println("Scheduled timeouts: "+mockServerAdapter.getTimeoutRequests());
		act(p[1], PokerActionType.SMALL_BLIND);	
		act(p[2], PokerActionType.BIG_BLIND);	
		act(p[3], PokerActionType.RAISE, 5000);
		act(p[0], PokerActionType.CALL);
		act(p[1], PokerActionType.CALL);
		act(p[2], PokerActionType.CALL); // Everyone should be all in now
		
		assertTrue(mp[0].isAllIn());
		assertTrue(mp[1].isAllIn());
		assertTrue(mp[2].isAllIn());
		assertTrue(mp[3].isAllIn());
		
		// Trigger deal community cards
		game.timeout();
		// FLOP
		game.timeout();
		
		// Trigger deal community cards
		game.timeout();
		// TURN
		game.timeout();
		
		// Trigger deal community cards
		game.timeout();
		// RIVER
		mockServerAdapter.clear();
		game.timeout();
		
		assertTrue(game.isFinished());
		
		System.out.println("Scheduled timeouts: "+mockServerAdapter.getTimeoutRequests());
		
	}

}
