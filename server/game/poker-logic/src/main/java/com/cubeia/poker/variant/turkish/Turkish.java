/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.variant.turkish;

import static com.cubeia.poker.variant.RoundCreators.ante;
import static com.cubeia.poker.variant.RoundCreators.bettingRound;
import static com.cubeia.poker.variant.RoundCreators.blinds;
import static com.cubeia.poker.variant.RoundCreators.dealFaceDownCards;
import static com.cubeia.poker.variant.RoundCreators.dealNewCards;
import static com.cubeia.poker.variant.RoundCreators.discardRound;
import static com.cubeia.poker.variant.RoundCreators.fromOpener;
import static com.cubeia.poker.variant.RoundCreators.turkishOpenRound;

import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.rounds.betting.BettingRoundCreator;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.PokerGameBuilder;
import com.cubeia.poker.variant.RoundCreators;
import com.cubeia.poker.variant.turkish.hand.TurkishHandStrengthEvaluator;

public class Turkish {

    public static GameType createGame() {
        return new PokerGameBuilder().
            withRounds(
                ante(),
                dealFaceDownCards(5),
                bettingRound(turkishOpenRound()),
                discardRound(4),
                dealNewCards(), 
                bettingRound(fromOpener()))
            .withDeckProvider(new TurkishDeckFactory())
            .withHandEvaluator(new TurkishHandStrengthEvaluator(Rank.SEVEN))
            .build();
    }
}