package com.example.CLI.Environment;

import com.example.CLI.Commands.Result;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * Класс "информатор", описывающий обмен данными.
 */
public class SimpleInformant implements Informant {

    @NotNull private HashMap<String, byte[]> dataSource;

    public SimpleInformant() {
        dataSource = new HashMap<>();
    }

    @Override @NotNull
    public String createConnection() {
        var name = UUID.randomUUID().toString();
        dataSource.put(name, new byte[0]);
        return name;
    }

    @Override
    public void send(@NotNull String name, byte[] data) throws IOException {
        if (dataSource.containsKey(name)) {
            dataSource.put(name, data);
        } else {
            try (var writer = new FileOutputStream(resolveName(name))) {
                writer.write(data);
            }
        }
    }

    @Override
    public byte[] getAndClose(@NotNull String name) throws IOException {
        if (dataSource.containsKey(name)) {
            var data = dataSource.get(name);
            dataSource.remove(name);
            return data;
        } else {
            try (var reader = new FileInputStream(resolveName(name))) {
                return reader.readAllBytes();
            }
        }
    }

    private File resolveName(String name) {
        String processPath = System.getProperty("user.dir");
        try {
            return Path.of(processPath).resolve(name).toFile();
        } catch (InvalidPathException ex) {
            return new File(processPath.concat(name));
        }
    }
}
