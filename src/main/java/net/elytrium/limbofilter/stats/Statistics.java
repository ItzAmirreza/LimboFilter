/*
 * Copyright (C) 2021 - 2022 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limbofilter.stats;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import net.elytrium.limbofilter.Settings;

public class Statistics {

  private final AtomicLong blockedConnections = new AtomicLong();
  private final AtomicLong connections = new AtomicLong();
  private final AtomicLong pings = new AtomicLong();
  private final AtomicLong interpolatedCpsBefore = new AtomicLong();
  private final AtomicLong interpolatedPpsBefore = new AtomicLong();

  public void addBlockedConnection() {
    this.blockedConnections.incrementAndGet();
  }

  public void addConnection() {
    this.connections.addAndGet(Settings.IMP.MAIN.UNIT_OF_TIME_CPS * 2L);
  }

  public void addPing() {
    this.pings.addAndGet(Settings.IMP.MAIN.UNIT_OF_TIME_CPS * 2L);
  }

  public long getBlockedConnections() {
    return this.blockedConnections.get();
  }

  public long getConnections() {
    return this.connections.get() / Settings.IMP.MAIN.UNIT_OF_TIME_CPS / 2L;
  }

  public long getPings() {
    return this.pings.get() / Settings.IMP.MAIN.UNIT_OF_TIME_CPS / 2L;
  }

  public long getTotalConnection() {
    return this.getPings() + this.getConnections();
  }

  public void startUpdating() {
    this.startUpdatingCps();
    this.startUpdatingPps();
  }

  private void startUpdatingCps() {
    long delayInterpolate = Settings.IMP.MAIN.UNIT_OF_TIME_CPS * 1000L;

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        Statistics.this.interpolatedCpsBefore.set(Statistics.this.connections.get() / Settings.IMP.MAIN.UNIT_OF_TIME_CPS / 2L);
      }
    }, delayInterpolate, delayInterpolate);

    long delay = delayInterpolate / Settings.IMP.MAIN.UNIT_OF_TIME_CPS / 2L;

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        long current = Statistics.this.connections.get();
        long before = Statistics.this.interpolatedCpsBefore.get();

        if (current >= before) {
          Statistics.this.connections.set(current - before);
        }
      }
    }, delay, delay);
  }

  private void startUpdatingPps() {
    long delayInterpolate = Settings.IMP.MAIN.UNIT_OF_TIME_PPS * 1000L;

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        Statistics.this.interpolatedPpsBefore.set(Statistics.this.pings.get() / Settings.IMP.MAIN.UNIT_OF_TIME_PPS / 2L);
      }
    }, delayInterpolate, delayInterpolate);

    long delay = delayInterpolate / Settings.IMP.MAIN.UNIT_OF_TIME_PPS / 2L;

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        long current = Statistics.this.pings.get();
        long before = Statistics.this.interpolatedPpsBefore.get();

        if (current >= before) {
          Statistics.this.pings.set(current - before);
        }
      }
    }, delay, delay);
  }
}
