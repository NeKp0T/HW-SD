package com.example.CLI.Commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LsAndCdTest {

    private Path root;
    private Path rootDir1;
    private Path rootDir1Dir11;
    private Path rootDir2;
    private Path rootFile1;
    private Path rootFile2;

    private void createTestFileTree() throws IOException {
        root = Files.createTempDirectory("CLI_TEST");
        rootDir1 = Files.createDirectory(root.resolve("dir1"));
        rootDir1Dir11 = Files.createDirectory(root.resolve(Path.of("dir1", "dir11")));
        rootDir2 = Files.createDirectory(root.resolve("dir2"));
        rootFile1 = Files.createFile(root.resolve("file1"));
        rootFile2 = Files.createFile(root.resolve("file2"));

        root.toFile().deleteOnExit();
        rootDir1.toFile().deleteOnExit();
        rootDir1Dir11.toFile().deleteOnExit();
        rootDir2.toFile().deleteOnExit();
        rootFile1.toFile().deleteOnExit();
        rootFile2.toFile().deleteOnExit();
    }

    private Cd cd;
    private Ls ls;

    private void setCmdArg(Command cmd, String arg) {
        var list = new ArrayList<Operation>();
        list.add(new Literal(arg));
        cmd.setArgs(list);
    }
    private void setCmdArg(Command cmd) { ;
        cmd.setArgs(new ArrayList<>());
    }
    private void setCdArg(String arg) { setCmdArg(cd, arg); }
    private void setLsArg(String arg) { setCmdArg(ls, arg); }
    private void setLsArg() { setCmdArg(ls); }

    @BeforeEach
    public void createFiles() throws IOException {
        createTestFileTree();
        cd = new Cd();
        ls = new Ls();
    }

    @Test
    public void cdAcceptsAbsolutePath() {
        setCdArg(root.toAbsolutePath().toString());
        cd.execute();
        assertEquals(root.toAbsolutePath().normalize().toString(), getPWDString());
    }

    @Test
    public void cdAcceptsRelativePath() {
        setCdArg(root.toAbsolutePath().toString());
        cd.execute();
        setCdArg("dir1");
        cd.execute();
        assertEquals(rootDir1.toAbsolutePath().normalize().toString(), getPWDString());
    }

    @Test
    public void cdAcceptsLongRelativePath() {
        setCdArg(root.toAbsolutePath().toString());
        cd.execute();
        setCdArg(Path.of("dir1", "dir11").toString());
        cd.execute();
        assertEquals(rootDir1Dir11.toAbsolutePath().normalize().toString(), getPWDString());
    }

    @Test
    public void cdWithNoArguments() {
        cd.setArgs(new ArrayList<>());
        cd.execute();
        assertEquals(System.getProperty("user.home"), getPWDString());
    }

    private void assertLsRoot(Result result) {
        var lsOutput = Arrays.stream(result.getOutput().get(0).split(" ")).sorted().collect(Collectors.toList());
        assertEquals(List.of("dir1" + File.separator, "dir2" + File.separator, "file1", "file2"), lsOutput);
    }

    @Test
    public void lsAbsoluteShowsEverything() {
        setLsArg(root.toAbsolutePath().toString());
        assertLsRoot(ls.execute());
    }

    @Test
    public void lsNoArgs() {
        setCdArg(root.toAbsolutePath().toString());
        cd.execute();
        setLsArg();
        assertLsRoot(ls.execute());
    }

    @Test
    public void lsRelative() {
        setCdArg(rootDir1.toAbsolutePath().toString());
        cd.execute();
        setLsArg("..");
        assertLsRoot(ls.execute());
    }

    private void setPWD(Path pwd) {
        System.setProperty("user.dir", pwd.toString());
    }

    private String getPWDString() {
        return System.getProperty("user.dir");
    }
}