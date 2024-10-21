package Fianco.GameEngine;

import java.util.ArrayList;
import java.util.List;

// LoggerSingleton to handle logging and notifying subscribers
public class Logger {
    private static Logger instance = null;
    private final List<LogListener> listeners;
    private final List<String> logs;

    // Private constructor to prevent instantiation
    private Logger() {
        listeners = new ArrayList<>();
        logs = new ArrayList<>();
    }

    // Method to get the singleton instance
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    // Method to add a log and notify listeners
    public void log(String message) {
        logs.add(message);
        notifyListeners(message);
    }

    // Register a listener
    public void registerListener(LogListener listener) {
        listeners.add(listener);
    }

    // Notify all listeners about a new log
    private void notifyListeners(String message) {
        for (LogListener listener : listeners) {
            listener.onLogEvent(message);
        }
    }
}

