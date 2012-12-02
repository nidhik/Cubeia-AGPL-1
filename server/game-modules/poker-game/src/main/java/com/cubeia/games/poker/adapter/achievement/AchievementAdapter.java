package com.cubeia.games.poker.adapter.achievement;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.bonus.event.GameEvent;
import com.cubeia.bonus.firebase.api.AchievementsService;
import com.cubeia.firebase.guice.inject.Service;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;

public class AchievementAdapter {
	
	Logger log = LoggerFactory.getLogger(getClass());

	@Service AchievementsService service;
	
	/**
	 * Report hand end result to the achievment service
	 * @param handResult
	 * @param handEndStatus
	 * @param tournamentTable
	 */
	public void notifyHandEnd(HandResult handResult, HandEndStatus handEndStatus, boolean tournamentTable) {
		log.warn("Notify hand end");
		Map<PokerPlayer, Result> map = handResult.getResults();
		for (PokerPlayer player : map.keySet()) {
			sendPlayerHandEnd(player, map.get(player), handResult);	
		}
	}
	
	private void sendPlayerHandEnd(PokerPlayer player, Result result, HandResult handResult) {
		log.warn("Notify player hand end to achievment system");
		GameEvent event = new GameEvent();
		event.game = "poker";
		event.type = "roundEnd";
		event.player = player.getPlayerId()+"";
		event.attributes.put("stake", calculateStake(result)+"");
		event.attributes.put("winAmount", result.getWinningsIncludingOwnBets()+"");
		
		if (calculateIsWin(result)) {
			event.attributes.put("win", "true");
		} else {
			event.attributes.put("lost", "true");
		}

		RatedPlayerHand hand = getRatedPlayerHand(player, handResult);
		if (hand != null) {
			event.attributes.put("handType", hand.getBestHandType().name());
		}
		
		log.warn(" --------  Send game event: "+event);
		service.sendEvent(event);
	}

	private RatedPlayerHand getRatedPlayerHand(PokerPlayer player, HandResult handResult) {
		for (RatedPlayerHand rphand : handResult.getPlayerHands()) {
			if (rphand.getPlayerId() == player.getId()) {
				return rphand;
			}
		}
		log.warn("Could not find hand type for player["+player.getPlayerId()+"]");
		return null;
	}

	private boolean calculateIsWin(Result result) {
		return result.getNetResult() > 0;
	}

	private long calculateStake(Result result) {
		return result.getWinningsIncludingOwnBets() - result.getNetResult();
	}
}
