package com.datasync.ytdownloader.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.function.Consumer;

@Component
public class CommandRunner {

    public static class CommandResult {
        public final int exitCode;
        public final String output;

        public CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }

    public CommandResult runCommandAndWaitWithOutput(String... command) throws Exception {
        return runCommandAndWaitWithOutput(null, command);
    }

    public CommandResult runCommandAndWaitWithOutput(Consumer<String> lineCallback, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.environment().put("PYTHONUTF8", "1");
        pb.redirectErrorStream(true); // merge stderr into stdout
        Process process = pb.start();

        LinkedList<String> lastLines = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastLines.add(line);
                if (lastLines.size() > 50) {
                    lastLines.removeFirst();
                }
                if (lineCallback != null) {
                    try {
                        lineCallback.accept(line);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        int exitCode = process.waitFor();
        return new CommandResult(exitCode, String.join("\n", lastLines));
    }

    public int runCommandAndWait(String... command) throws Exception {
        return runCommandAndWaitWithOutput(command).exitCode;
    }
}
