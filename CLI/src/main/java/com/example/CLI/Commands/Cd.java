package com.example.CLI.Commands;

import com.example.CLI.Environment.Informant;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Описывает команду 'ls', изменяющую рабочую директорию на свой аргумент
 */
public class Cd implements Command {

    @NotNull
    private ArrayList<Operation> args;

    public Cd() {
        args = new ArrayList<>();
    }

    @Override
    public Result execute() {
        var cdArguments = new ArrayList<String>();

        var cdResult = new Result();

        for (var arg : args) {
            var parsedArg = arg.execute();
            cdResult.addErrors(parsedArg.getErrors());
            cdArguments.addAll(parsedArg.getOutput());
        }

        if (cdArguments.isEmpty()) {
            cdArguments.add(System.getProperty("user.home"));
        }

        if (cdArguments.size() > 1) {
            cdResult.addError("cd accepts one argument max");
            return cdResult;
        }

        doCd(cdArguments.get(0), cdResult);
        return cdResult;
    }

    @Override
    public void setArgs(@NotNull ArrayList<Operation> args) {
        this.args = args;
    }

    private void doCd(String cdString, Result result) {
        Path dirPath;
        try {
            dirPath = getPWD().resolve(cdString).toRealPath();
        } catch (IOException e) {
            result.addError("cd: File does not exist or can not be read");
            return;
        }
        File dir = dirPath.toFile();
        if (!dir.isDirectory()) {
            result.addError("cd: file " + dirPath + " is not a directory");
            return;
        }

        setPWD(dirPath);
    }

    private void setPWD(Path pwd) {
        System.setProperty("user.dir", pwd.toString());
    }

    private Path getPWD() {
        return Path.of(System.getProperty("user.dir"));
    }
}

