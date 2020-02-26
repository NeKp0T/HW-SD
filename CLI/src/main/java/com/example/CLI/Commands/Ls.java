package com.example.CLI.Commands;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ls implements Command {

    @NotNull private ArrayList<Operation> args;

    public Ls() {
        args = new ArrayList<>();
    }

    @Override
    public Result execute() {
        var lsArguments = new ArrayList<String>();

        var errors = new ArrayList<String>();
        var output = new ArrayList<String>();
        var lsResult = new Result(output, errors);

        for (var arg : args) {
            var parsedArg = arg.execute();
            errors.addAll(parsedArg.getErrors());
            lsArguments.addAll(parsedArg.getOutput());
        }

        if (lsArguments.isEmpty()) {
            lsArguments.add("."); // crossplatform current directory
        }

        if (lsArguments.size() > 1) {
            errors.add("ls accepts one argument max");
            return lsResult;
        }

        doLs(lsArguments.get(0), lsResult);
        return lsResult;
    }

    @Override
    public void setArgs(@NotNull ArrayList<Operation> args) {
        this.args = args;
    }

    private void doLs(String lsString, Result result) {
        Path dirPath = Path.of(System.getProperty("user.dir")).resolve(lsString);
        File dir = dirPath.toFile();
        if (!dir.exists()) {
            result.addError("ls: file " + dirPath + " does not exist");
            return;
        }
        if (!dir.isDirectory()) {
            result.addError("ls: file " + dirPath + " is not a directory");
            return;
        }

        try {
            Stream<Path> contents = Files.list(dirPath);
            result.addOutputLine(contents.map(this::pathToString).collect(Collectors.joining(" ")));
        } catch (IOException e) {
            result.addError("ls: can't read contents of " + dirPath);
        }
    }

    private String pathToString(Path path) {
        if (path.toFile().isDirectory()) {
            return path.getFileName().toString() + File.separator;
        }
        return path.getFileName().toString();
    }
}
