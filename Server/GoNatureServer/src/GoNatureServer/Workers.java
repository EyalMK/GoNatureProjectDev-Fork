package GoNatureServer;

import CommonServer.ocsf.AbstractServer;
import CommonServer.ocsf.ConnectionToClient;
import DataBase.DBConnection;
import ServerUIPageController.ServerUIFrameController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Workers {

    private static final List<ScheduledExecutorService> executors = new ArrayList<>();

    public static void startClientProcessingThread(ServerUIFrameController controller, AbstractServer server) {
        new Thread(() -> {
            try {
                while (server != null && server.isListening()) {
                    Thread[] clientConnections = server.getClientConnections();
                    controller.resetTableClients();
                    for (Thread clientThread : clientConnections) {
                        if (clientThread instanceof ConnectionToClient) {
                            ConnectionToClient client = (ConnectionToClient) clientThread;
                            controller.addRow(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress());
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("The client processing thread was interrupted.");
                    }
                }
            } catch (NullPointerException e) {

            }
        }, "Client Processing Thread").start();
    }

    public static void SendReminderDayBeforeWorker(DBConnection db, ServerUIFrameController controller) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 24);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            try {
                db.updateOrderStatusForUpcomingVisits();
                db.cancelOrdersInWaitlist24HoursBefore();
            } catch (Exception e) {
                controller.addtolog("Sending Reminders for Orders in  " + calendar.getTime());
            }
        };

        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
//        long initialDelay = 5000;
//        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.SECONDS.toMillis(10), TimeUnit.MILLISECONDS);
        executors.add(scheduler);
    }

    public static void CancelOrdersThatDidntConfirmWorker(DBConnection db, ServerUIFrameController controller) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try{
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, 22);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                db.ChangeLatePendingConfirmationToCancelled();
                controller.addtolog("Checking Confirmation for Orders in  " + calendar.getTime());
            }
            catch (Exception e){
                controller.addtolog("failed CancelOrdersThatDidntConfirmWorker");
            }

        };

        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
//        long initialDelay = 5000;
//        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.SECONDS.toMillis(5), TimeUnit.MILLISECONDS);
        executors.add(scheduler);
    }

    public static void enterOrdersFromWaitList48HoursBeforeWorker(DBConnection db, ServerUIFrameController controller) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, 48);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                db.enterOrdersInWaitlist48HoursBefore();
                controller.addtolog("Entering orders from the waitlist  " + calendar.getTime());
            }
            catch (Exception e){
                controller.addtolog("failed enterOrdersFromWaitList48HoursBeforeWorker");
            }
        };

        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
//        long initialDelay = 5000;
//        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.SECONDS.toMillis(5), TimeUnit.MILLISECONDS);
        executors.add(scheduler);
    }

    public static void changeToAbsentOrders(DBConnection db, ServerUIFrameController controller) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                db.ChangeToAbsent();
                controller.addtolog("Update status for absent ordets  " + calendar.getTime());
            }
            catch (Exception e){
                controller.addtolog("failed changeToAbsentOrders");
            }
        };

        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS);
//        long initialDelay = 5000;
//        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.SECONDS.toMillis(5), TimeUnit.MILLISECONDS);
        executors.add(scheduler);
    }

    private static long calculateInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long nextHour = calendar.getTimeInMillis();
        return nextHour - now;
    }

    public static void shutdownExecutors() {
        executors.forEach(executor -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Executor service did not terminate");
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}

