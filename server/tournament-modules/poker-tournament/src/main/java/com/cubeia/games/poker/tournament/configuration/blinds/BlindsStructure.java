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

package com.cubeia.games.poker.tournament.configuration.blinds;

import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
public class BlindsStructure implements Serializable {

    private static final Logger log = Logger.getLogger(BlindsStructure.class);

    @Id
    @GeneratedValue
    private Integer id = 0;

    private long timePerLevel;

    private String name;

    @OneToMany(fetch = EAGER, cascade = ALL)
    @OrderColumn
    private List<Level> blindsLevels = new ArrayList<Level>();

    // For Hibernate
    public BlindsStructure() {
    }

    public BlindsStructure(long millisPerLevel, List<Level> blindsLevels) {
        checkNotNull(blindsLevels, "List of blinds levels can't be null");
        checkArgument(millisPerLevel > 0, "Time per level must be > 0");
        checkArgument(!blindsLevels.isEmpty(), "List of blinds levels can't be empty.");

        this.timePerLevel = millisPerLevel;
        this.blindsLevels = blindsLevels;
    }

    public Level getBlindsLevel(int blindsLevel) {
        if (blindsLevel >= blindsLevels.size()) {
            log.debug("blindsLevel " + blindsLevel + " requested, but we only have " + blindsLevels.size() + " levels. Returning last one.");
            return blindsLevels.get(blindsLevels.size() - 1);
        }
        return blindsLevels.get(blindsLevel);
    }

    public Level getFirstBlindsLevel() {
        return blindsLevels.get(0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Level> getBlindsLevels() {
        return blindsLevels;
    }

    public void setBlindsLevels(List<Level> blindsLevels) {
        this.blindsLevels = blindsLevels;
    }

    @Override
    public String toString() {
        return "BlindsStructure{" +
                "id=" + id +
                ", timePerLevel=" + timePerLevel +
                ", name='" + name + '\'' +
                ", blindsLevels=" + blindsLevels +
                '}';
    }

}
