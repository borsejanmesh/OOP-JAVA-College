import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Practical5{
    enum MessagePriority {
        HIGH(0),
        NORMAL(1);

        final int order;

        MessagePriority(int order) {
            this.order = order;
        }
    }

    static class Message implements Comparable<Message> {
        private final String sender;
        private final String text;
        private final MessagePriority priority;
        private final long sequence;

        Message(String sender, String text, MessagePriority priority, long sequence) {
            this.sender = sender;
            this.text = text;
            this.priority = priority;
            this.sequence = sequence;
        }

        @Override
        public int compareTo(Message other) {
            int byPriority = Integer.compare(priority.order, other.priority.order);
            return byPriority != 0
                    ? byPriority
                    : Long.compare(sequence, other.sequence);
        }

        @Override
        public String toString() {
            return "[" + priority + "] " + sender + ": " + text;
        }
    }

    static class ChatRoom {
        private final PriorityBlockingQueue<Message> messages =
                new PriorityBlockingQueue<>();
        private final AtomicLong sequence = new AtomicLong();
        private volatile boolean open = true;

        void send(String sender, String text, MessagePriority priority) {
            messages.add(new Message(sender, text, priority, sequence.getAndIncrement()));
        }

        void close() {
            open = false;
        }

        void receiveMessages() {
            while (open || !messages.isEmpty()) {
                try {
                    Message message = messages.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        System.out.println("Server received: " + message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    static class ChatUser extends Thread {
        private final ChatRoom room;
        private final List<String> messages;
        private final MessagePriority messagePriority;
        private final Object pauseLock = new Object();

        private volatile boolean running = true;
        private boolean paused;

        final CountDownLatch firstMessageSent = new CountDownLatch(1);
        final CountDownLatch secondMessageSent = new CountDownLatch(1);

        ChatUser(String name, ChatRoom room, List<String> messages,
                 MessagePriority messagePriority, int threadPriority) {
            super(name);
            this.room = room;
            this.messages = messages;
            this.messagePriority = messagePriority;
            setPriority(threadPriority);
        }

        void pauseUser() {
            synchronized (pauseLock) {
                paused = true;
            }
        }

        void resumeUser() {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
        }

        void stopUser() {
            running = false;
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
        }

        private void waitIfPaused() {
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                    }
                }
            }
        }

        @Override
        public void run() {
            for (int i = 0; i < messages.size() && running; i++) {
                waitIfPaused();
                if (!running) {
                    break;
                }

                room.send(getName(), messages.get(i), messagePriority);

                if (i == 0) {
                    firstMessageSent.countDown();
                } else if (i == 1) {
                    secondMessageSent.countDown();
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }

            System.out.println(getName() + " has left the chat.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ChatRoom room = new ChatRoom();

        Thread server = new Thread(room::receiveMessages, "ChatServer");
        server.start();

        ChatUser alice = new ChatUser(
                "Alice", room,
                List.of("Hello everyone!", "This is a high-priority message.",
                        "Goodbye!"),
                MessagePriority.HIGH,
                Thread.MAX_PRIORITY
        );

        ChatUser bob = new ChatUser(
                "Bob", room,
                List.of("Hi Alice!", "Nice to meet you."),
                MessagePriority.NORMAL,
                Thread.NORM_PRIORITY
        );

        alice.start();
        bob.start();

        // Wait for Alice's first message, then pause her thread.
        alice.firstMessageSent.await();
        alice.pauseUser();
        Thread.sleep(400);
        System.out.println("Alice is alive while paused: " + alice.isAlive());

        // Resume Alice, then stop her cooperatively while paused again.
        alice.resumeUser();
        alice.secondMessageSent.await();
        alice.pauseUser();
        alice.stopUser();

        alice.join();
        bob.join();

        room.close();
        server.join();

        System.out.println("Chat simulation finished.");
    }
}