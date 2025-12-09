package com.pryzmm.magmamacro.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ClientScheduler {

    private final List<ScheduledClientTask> clientTasks = new ArrayList<>();
    private int currentClientTick = 0;

    public void clientTick() {
        currentClientTick++;
        Iterator<ScheduledClientTask> iteratorClient = clientTasks.iterator();
        while (iteratorClient.hasNext()) {
            ScheduledClientTask clientTask = iteratorClient.next();
            if (clientTask.cancelled) {
                iteratorClient.remove();
                continue;
            }
            if (currentClientTick >= clientTask.executeAt) {
                clientTask.action.accept(null);
                iteratorClient.remove();
            }
        }
    }

    public ScheduledClientTask runClientTaskLater(Consumer<Void> consumer, long ticks) {
        ScheduledClientTask clientTask = new ScheduledClientTask(currentClientTick + ticks, consumer);
        clientTasks.add(clientTask);
        return clientTask;
    }

    public interface TaskHandle {
        void cancel();
    }

    public static class ScheduledClientTask implements TaskHandle {
        final long executeAt;
        final Consumer<Void> action;
        boolean cancelled = false;

        ScheduledClientTask(long executeAt, Consumer<Void> action) {
            this.executeAt = executeAt;
            this.action = action;
        }

        @Override
        public void cancel() { cancelled = true; }
    }

}