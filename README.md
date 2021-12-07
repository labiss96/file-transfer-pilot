## File Transfer Server Pilot

- **Verification**

  - username / password authentication
  - public key authentication
  - update configuration dynamically
  - manage multiple folder per user

  

- **FTP Server**

  - Apache FTP Server

    ```xml
    <dependency>
        <groupId>org.apache.ftpserver</groupId>
        <artifactId>ftpserver-core</artifactId>
        <version>1.1.1</version>
    </dependency>
    ```

  - Custom UserManager, FileSystemView

    

- **SFTP Server**

  - Apache SSHD

    ```xml
    <dependency>
        <groupId>org.apache.sshd</groupId>
        <artifactId>sshd-core</artifactId>
        <version>1.7.0</version>
    </dependency>
    ```

  - Custom SftpFileSystemAccessor

