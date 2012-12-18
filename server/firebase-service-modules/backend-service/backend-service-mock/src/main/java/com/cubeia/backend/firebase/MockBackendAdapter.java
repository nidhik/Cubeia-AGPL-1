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

package com.cubeia.backend.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backend.cashgame.CashGamesBackend;
import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TableId;
import com.cubeia.backend.cashgame.TransactionId;
import com.cubeia.backend.cashgame.dto.AllowJoinResponse;
import com.cubeia.backend.cashgame.dto.AnnounceTableRequest;
import com.cubeia.backend.cashgame.dto.AnnounceTableResponse;
import com.cubeia.backend.cashgame.dto.BalanceUpdate;
import com.cubeia.backend.cashgame.dto.BatchHandRequest;
import com.cubeia.backend.cashgame.dto.BatchHandResponse;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.HandResult;
import com.cubeia.backend.cashgame.dto.OpenSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.ReserveFailedResponse;
import com.cubeia.backend.cashgame.dto.ReserveRequest;
import com.cubeia.backend.cashgame.dto.ReserveResponse;
import com.cubeia.backend.cashgame.dto.TransactionUpdate;
import com.cubeia.backend.cashgame.exceptions.BatchHandFailedException;
import com.cubeia.backend.cashgame.exceptions.GetBalanceFailedException;
import com.cubeia.backend.cashgame.exceptions.ReserveFailedException;
import com.cubeia.games.poker.common.Money;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class MockBackendAdapter implements CashGamesBackend {

    private Logger log = LoggerFactory.getLogger(CashGamesBackendMock.class);

    private final AtomicInteger idSequence = new AtomicInteger(0);

    private final Multimap<PlayerSessionId, Money> sessionTransactions =
            Multimaps.<PlayerSessionId, Money>synchronizedListMultimap(LinkedListMultimap.<PlayerSessionId, Money>create());

    @Override
    public String generateHandId() {
        return "" + System.currentTimeMillis();
    }

    private int nextId() {
        return idSequence.incrementAndGet();
    }
    
    @Override
    public boolean isSystemShuttingDown() {
    	return false;
    }

    @Override
    public AllowJoinResponse allowJoinTable(int playerId) {
        return new AllowJoinResponse(true, -1);
    }

    @Override
    public AnnounceTableResponse announceTable(AnnounceTableRequest request) {
    	String extid = "MOCK-TABLE-ID-" + System.currentTimeMillis();
        final AnnounceTableResponse response = new AnnounceTableResponse(new TableId(request.tableId, extid));
        response.setProperty(CashGamesBackendService.MARKET_TABLE_REFERENCE_KEY, extid);
        return response;
    }

    /*@Override
    public void closeTable(CloseTableRequest request) {
        log.debug("table removed");
    }*/

    @Override
    public OpenSessionResponse openSession(OpenSessionRequest request) {
        PlayerSessionId sessionId = new PlayerSessionId(request.playerId, null);
        sessionTransactions.put(sessionId, request.getOpeningBalance());

        OpenSessionResponse response = new OpenSessionResponse(sessionId, Collections.<String, String>emptyMap());
        log.debug("new session opened, tId = {}, pId = {}, sId = {}", new Object[]{request.getTableId(), request.getPlayerId(), response.getSessionId()});
        log.debug("currently open sessions: {}", sessionTransactions.size());
        printDiagnostics();
        
        return response;
    }

    @Override
    public void closeSession(CloseSessionRequest request) {
        PlayerSessionId sid = request.getPlayerSessionId();

        if (!sessionTransactions.containsKey(sid)) {
            log.error("error closing session {}: not found", sid);
        } else {
            Money closingBalance = getBalance(sid);
            sessionTransactions.removeAll(sid);
            log.debug("closed session {} with balance: {}", sid, closingBalance);
        }

        printDiagnostics();
    }

    @Override
    public ReserveResponse reserve(ReserveRequest request) throws ReserveFailedException {
        Money amount = request.getAmount();
        PlayerSessionId sid = request.getPlayerSessionId();

        if (!sessionTransactions.containsKey(sid)) {
            log.error("reserve failed, session not found: sId = " + sid);
            throw new ReserveFailedException("session " + sid + " not open", ReserveFailedResponse.ErrorCode.SESSION_NOT_OPEN, true);
        } else if (amount.getAmount() == 66 || amount.getAmount() == 660 || amount.getAmount() == 6600) { // MAGIC FAIL FOR 66 cents BUY-IN
            log.error("Failing reserve with {}ms delay for magic amount 66 cents (hardcoded for debug reasons). sId={}", sid);
            throw new ReserveFailedException("Unknown operator error (magic 66-cent ultra-fail)", ReserveFailedResponse.ErrorCode.UNSPECIFIED_FAILURE, true);
        } else {
            sessionTransactions.put(sid, amount);
            Money newBalance = getBalance(sid);
            // BalanceUpdate balanceUpdate = new BalanceUpdate(request.getPlayerSessionId(), newBalance, nextId());
            final ReserveResponse response = new ReserveResponse(request.getPlayerSessionId(), amount);
            log.debug("reserve successful: sId = {}, amount = {}, new balance = {}", new Object[]{sid, amount, newBalance});
            response.setProperty(CashGamesBackendService.MARKET_TABLE_SESSION_REFERENCE_KEY, "MOCK-MARKET-SID-" + sid.hashCode());
            printDiagnostics();
            return response;
        }
    }

    @Override
    public BatchHandResponse batchHand(BatchHandRequest request) throws BatchHandFailedException {
        int totalBets = 0;
        int totalWins = 0;
        int totalRakes = 0;
        List<TransactionUpdate> resultingBalances = new ArrayList<TransactionUpdate>();
        for (HandResult hr : request.getHandResults()) {
            log.debug("recording hand result: handId = {}, sessionId = {}, bets = {}, wins = {}, rake = {}",
                    new Object[]{request.getHandId(), hr.getPlayerSession(), hr.getAggregatedBet(), hr.getWin(), hr.getRake()});
            long amount = hr.getWin().getAmount() - hr.getAggregatedBet().getAmount();
            sessionTransactions.put(hr.getPlayerSession(), new Money(amount, hr.getWin().getCurrencyCode(), hr.getWin().getFractionalDigits()));
            resultingBalances.add(new TransactionUpdate(new TransactionId(-1), new BalanceUpdate(hr.getPlayerSession(), getBalance(hr.getPlayerSession()), -1)));

            totalBets += hr.getAggregatedBet().getAmount();
            totalWins += hr.getWin().getAmount();
            totalRakes += hr.getRake().getAmount();
        }

        //Sanity check on the sum
        int sum = totalBets - (totalWins + totalRakes);
        if (sum != 0) {
            throw new BatchHandFailedException("sanity check failed on batchHand, totalBets: " + totalBets + " "
                    + "totalWins: " + totalWins + " totalRakes: " + totalRakes + " sum:" + sum);
        } else {
            log.debug("sanity check successful on batchHand, totalBets: " + totalBets + " "
                    + "totalWins: " + totalWins + " totalRakes: " + totalRakes + " sum:" + sum);
        }

        printDiagnostics();
        return new BatchHandResponse(resultingBalances);
    }

    @Override
    public Money getMainAccountBalance(int playerId) {
        log.debug("getMainAccountBalance is not implemented yet! Returning hardcoded value of 1337000");
        return new Money(1337000, "EUR", 2);
    }

    private Money getBalance(PlayerSessionId sid) {
        Money balance = null;

        for (Money tx : sessionTransactions.get(sid)) {
            if (balance == null) {
                balance = tx;
            } else {
                balance = balance.add(tx);
            }
        }
        return balance;
    }

    @Override
    public BalanceUpdate getSessionBalance(PlayerSessionId sessionId)
            throws GetBalanceFailedException {
        printDiagnostics();
        return new BalanceUpdate(sessionId, getBalance(sessionId), nextId());
    }
    
    private void printDiagnostics() {
//      log.debug("wallet session transactions: ");
//      for (PlayerSessionId session : sessionTransactions.keys()) {
//          log.debug("{} (balance: {}) -> {}", 
//              new Object[] {session, getBalance(session), sessionTransactions.get(session)});
//      }
//      log.debug("---");
  }
}
