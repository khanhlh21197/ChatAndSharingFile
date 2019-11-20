# ChatAndSharingFile<br />

### 1. Thành viên<br />
  1.1 Lê Hồng Khánh<br />
- MSV: B15DCCN287<br />
- Email: lek21197@gmail.com<br />
- Phone: 0963003197<br />
  1.2 Nguyễn Hải Anh<br />
  MSV: B15DCCN037<br />
<br />
### 2. Chức năng <br />
- [chức năng 1]: Chat giữa các Client với nhau sử dụng Socket, In/OutputStream trong java<br />
- [chức năng 2]: Chia sẻ File giữa 2 Client với nhau thông qua Server <br />
	Cách thức hoạt động: Client 1 gửi File tới Server, Server đọc và gửi File tới Client 2

```
InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int cnt;
                                //send File to Client
                                while ((cnt = input.read(buffer)) > 0) {
                                    sendFile.write(buffer, 0, cnt);
                                }
                                //xoa bo dem 
                                sendFile.flush();
                                sendFile.close();
```

### 3. Kiến thức cần tìm hiểu: <br />
- Java Swing<br />
- Socket<br />
- Thread<br />
<br />
### 4. Môi trường sử dụng:<br />
- IDE: Netbeans 8.2<br />
- Java 8<br />
<br />
### 5. Hạn chế/Hướng phát triển<br />
- Hạn chế:<br />
	+ Chưa thể gửi File trong Group (1 - n)<br />
	+ Chưa gửi được Emoji<br />
- Hướng phát triển/ý tưởng<br />
	+ Gửi emoji sử dụng thư viện : https://github.com/vdurmont/emoji-java<br />
	+ <br />
<br />
### 6. Hướng dẫn sử dụng: <br />
Ứng dụng Chat và Share File thông qua Socket sử dụng ngôn ngữ Java <br />
Khởi động Server/MainForm và Click "Khởi động máy chủ" ở Port tùy chỉnh tại jTextField<br />
Bắt đầu Client tại Client/LoginForm để vào màn hình đăng nhập<br />
Nhập tên vào ô Tên và bấm Login để bắt đầu Chat và chia sẻ File (Tên không được có khoảng trống hoặc quá 15 ký tự)<br />
<br />
Click Menu Chia Sẻ File -> Gửi File, Chọn File và Nhập tên tài khoản người nhận để Gửi file<br />

Tài liệu tham khảo:
https://www.c-sharpcorner.com/article/how-to-make-a-chat-application-using-sockets-in-java/
