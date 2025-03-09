package org.rubix.redfile.profiler;

import net.minecraft.util.TimeHelper;

import java.util.Timer;
import java.util.TimerTask;

public interface ProfileEnder {
    default void stop() {}

    boolean tick();

    final class TickProfileEnder implements ProfileEnder {
        private long ticksRemaining;

        public TickProfileEnder(long ticks) {
            this.ticksRemaining = ticks;
        }

        @Override
        public boolean tick() {
            if (ticksRemaining == 0) return true;
            --ticksRemaining;
            return false;
        }
    }

    final class TimeProfileEnder implements ProfileEnder {
        private boolean finished = false;
        private final Timer timer = new Timer();
        private final TimerTask timeoutTask = new TimerTask() {
            @Override
            public void run() {
                finished = true;
            }
        };

        public TimeProfileEnder(long seconds) {
            timer.schedule(timeoutTask,  seconds * TimeHelper.SECOND_IN_MILLIS);
        }

        @Override
        public void stop() {
            timeoutTask.cancel();
        }

        @Override
        public boolean tick() {
            return finished;
        }

    }

    final class IndefiniteProfileEnder implements ProfileEnder {

        @Override
        public boolean tick() {
            return false;
        }
    }
}
