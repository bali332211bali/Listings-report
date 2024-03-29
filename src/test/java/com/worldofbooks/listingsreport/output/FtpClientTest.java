package com.worldofbooks.listingsreport.output;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class FtpClientTest {

    private FakeFtpServer fakeFtpServer;

    private FtpClient ftpClient;

    @ClassRule
    public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("ftpUser", "ftpPassword", "/test"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/test"));
        fileSystem.add(new FileEntry("/test/ftpTest.txt", "test file content"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.start();

        ftpClient = new FtpClient("localhost", fakeFtpServer.getServerControlPort(), "ftpUser", "ftpPassword");
        ftpClient.open();
    }

    @After
    public void teardown() throws IOException {
        ftpClient.close();
        fakeFtpServer.stop();
    }

    @Test
    public void sendToFtp() throws IOException {
        Path pathToSend = TEMPORARY_FOLDER.newFile("sendToFtpTest.txt").toPath();
        ftpClient.sendToFtp(pathToSend, "/uploadTest.txt");
        ftpClient.close();
        Path pathToSend2 = TEMPORARY_FOLDER.newFile("sendToFtpTest2.txt").toPath();
        ftpClient.sendToFtp(pathToSend2, "/uploadTest.txt");
        assertTrue(fakeFtpServer.getFileSystem().exists("/uploadTest.txt"));
    }

    @Test(expected = IOException.class)
    public void sendToFtpNull() throws IOException {
        ftpClient = new FtpClient("badServer", fakeFtpServer.getServerControlPort(), "ftpUser", "ftpPassword");
        Path pathToSend = TEMPORARY_FOLDER.newFile("ftpTest.txt").toPath();
        ftpClient.sendToFtp(pathToSend, "/uploadTest.txt");
    }

    @Test
    public void closeNull() throws IOException {
        ftpClient = new FtpClient("badServer", fakeFtpServer.getServerControlPort(), "ftpUser", "ftpPassword");
        ftpClient.close();
    }

}
