package com.example.CLI.Commands;

import com.example.CLI.Environment.Informant;
import com.example.CLI.Environment.Informed;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Класс, описывающий поведение внешней команды.
 */
public class External implements Command, Informed {

    @NotNull private String name;
    @NotNull private ArrayList<Operation> args;
    @NotNull private ArrayList<String> connections;
    @NotNull private Informant informant;

    public External(@NotNull String fullName, @NotNull Informant informant) {
        name = fullName;
        args = new ArrayList<>();
        connections = new ArrayList<>();
        this.informant = informant;
    }

    @Override
    public Result execute() {
        var result = new Result();

        try {
            var process = createProcess(result);
            var stdout = new Scanner(process.getInputStream());
            var stderr = new Scanner(process.getErrorStream());

            while (stdout.hasNext()) {
                result.addOutputLine(stdout.nextLine());
            }
            while (stderr.hasNext()) {
                result.addError(stderr.nextLine());
            }

            return result;
        } catch (IOException e) {
            result.addError("Can't run external program \'" + name + "\'.");
            return result;
        }
    }

    /**
     * Создает процесс, исполняющий внешнюю команду.
     * @param result - объект класса Result, в который будут класть ошибки при запуске аргументов {@link External#args}
     * @return созданный процесс
     * @throws IOException - если внешнюю команду не удалось запустить
     */
    @NotNull
    private Process createProcess(@NotNull Result result) throws IOException {
        var command = new ArrayList<String>();
        command.add(name);
        for (var operation: args) {
            var argResult = operation.execute();
            if (argResult.getOutput().size() > 0) {
                command.add(argResult.getOutput().get(0));
            }
            result.addErrors(result.getErrors());
        }
        var processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(System.getProperty("user.dir")));

        if (connections.size() > 0) {
            try {
                var tempFile = File.createTempFile("cli-", "-temp");
                tempFile.deleteOnExit();
                var writer = new FileWriter(tempFile);
                for (var conn: connections) {
                    writer.write(new String(informant.getAndClose(conn)));
                }
                writer.close();

                processBuilder.redirectInput(tempFile);
            } catch (IOException e) {
                result.addError("Can't create temp file for connection.");
            }
        }

        return processBuilder.start();
    }

    @Override
    public void setArgs(@NotNull ArrayList<Operation> args) {
        this.args = args;
    }

    @Override
    public void addConnection(@NotNull String name) {
        connections.add(name);
    }
}
