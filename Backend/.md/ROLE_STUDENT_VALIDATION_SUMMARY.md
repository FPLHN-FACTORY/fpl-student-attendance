# Tổng hợp Validation - Module Student

## Tổng quan
Module Student được tổ chức theo các phân hệ chính:
1. **Attendance** - Điểm danh sinh viên
2. **Statistics** - Thống kê
3. **Schedule** - Lịch học
4. **History Attendance** - Lịch sử điểm danh

## Chi tiết Validation từ mã nguồn

### 1. Điểm danh (Attendance)

#### SACheckinAttendanceRequest
```java
private String idPlanDate;

private Double latitude;

private Double longitude;

@NotBlank(message = "Không tìm thấy dữ liệu khuôn mặt")
private String faceEmbedding;
```

#### SAFilterAttendanceRequest
```java
private String idFacility;

private String idUserStudent;

private String keyword;

private Integer status;

private Integer type;
```

---

## Phân hệ Điểm danh (student/attendance)

### 1. Checkin điểm danh
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idPlanDate     | String  |                              |            | Có       | - Kiểm tra ca học tồn tại <br> - Kiểm tra ca học thuộc cơ sở của sinh viên <br> - Kiểm tra ca học còn trong thời gian điểm danh <br> - Kiểm tra ca học đang diễn ra (không quá sớm/muộn) | - "Không tìm thấy lịch" <br> - "Ca học chưa diễn ra hoặc đã kết thúc" <br> - "Đã quá thời gian cho phép điểm danh" |
| latitude       | Double  |                              |            | Không    | - Kiểm tra vị trí trong vùng cho phép (nếu bật) | - "Không thể lấy thông tin vị trí" <br> - "Địa điểm checkin/checkout nằm ngoài vùng cho phép" <br> - "Vị trí không hợp lệ hoặc ngoài phạm vi cho phép" |
| longitude      | Double  |                              |            | Không    | - Kiểm tra vị trí trong vùng cho phép (nếu bật) | - "Không thể lấy thông tin vị trí" <br> - "Địa điểm checkin/checkout nằm ngoài vùng cho phép" <br> - "Vị trí không hợp lệ hoặc ngoài phạm vi cho phép" |
| faceEmbedding  | String  | @NotBlank (không để trống)   |            | Có       | - Kiểm tra định dạng embedding <br> - So sánh với khuôn mặt đã đăng ký <br> - Xác thực khuôn mặt đạt ngưỡng tương đồng | - "Không tìm thấy dữ liệu khuôn mặt" <br> - "Xác thực khuôn mặt thất bại" <br> - "Khuôn mặt không hợp lệ" |

### 2. Lọc danh sách điểm danh
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility     | String  |                              |            | Không    | - Tự động set từ session      |               |
| idUserStudent  | String  |                              |            | Không    | - Tự động set từ session      |               |
| keyword        | String  | @Size(max=255)               | 0-255      | Không    |                               | - "Từ khóa không được quá 255 ký tự" |
| status         | Integer |                              |            | Không    | - Chỉ lấy trạng thái ACTIVE   |               |
| type           | Integer |                              |            | Không    | - Lọc theo loại điểm danh     |               |

---

## Phân hệ Lịch học (student/schedule)

### 1. Tìm kiếm lịch học
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idStudent      | String  |                              |            | Không    | - Tự động set từ session <br> - Chỉ lấy lịch học của sinh viên hiện tại <br> - Chỉ lấy ca/xưởng trạng thái ACTIVE | - "Không có dữ liệu lịch học" |
| now            | Long    |                              |            | Không    | - Thời gian hiện tại, mặc định lấy từ hệ thống nếu không có |               |
| max            | Long    |                              |            | Không    | - Thời gian tối đa, mặc định lấy theo cấu hình nếu không có |               |

---

## Phân hệ Lịch sử điểm danh (student/historyattendance)

### 1. Lọc lịch sử điểm danh
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| studentFactoryId | String |                          |            | Không    | - Tự động trim nếu rỗng <br> - Chỉ lấy lịch sử của sinh viên hiện tại <br> - Chỉ lấy ca/xưởng trạng thái ACTIVE | - "Không có dữ liệu lịch sử" |
| factoryId      | String  |                              |            | Không    | - Tự động trim nếu rỗng       |               |
| semesterId     | String  |                              |            | Không    | - Tự động set học kỳ hiện tại nếu null <br> - Kiểm tra học kỳ tồn tại và trạng thái ACTIVE | - "Không tìm thấy học kỳ" |

## Phân hệ Đăng ký sinh viên (authentication/register)

### 1. Đăng ký thông tin sinh viên
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| code           | String  | @NotBlank <br> @Size(max=50) | 1-50       | Có       | - Kiểm tra mã sinh viên tồn tại <br> - Kiểm tra định dạng mã sinh viên | - "Mã số sinh viên không được bỏ trống" <br> - "Sinh viên không tồn tại" <br> - "Mã sinh viên không hợp lệ" |
| idFacility     | String  | @NotBlank                    |            | Có       | - Kiểm tra cơ sở tồn tại và hoạt động | - "Cơ sở không được bỏ trống" <br> - "Cơ sở không tồn tại hoặc không hoạt động" |
| name           | String  | @NotBlank <br> @Size(min=2, max=255) | 2-255 | Có     | - Kiểm tra định dạng họ tên (2 từ trở lên, không số, không ký tự đặc biệt) | - "Họ và tên sinh viên không được bỏ trống" <br> - "Họ và tên sinh viên phải có ít nhất 2 ký tự" <br> - "Họ và tên không hợp lệ" |
| faceEmbedding  | String  | @NotBlank                    |            | Có       | - Kiểm tra định dạng embedding <br> - Lưu trữ để sử dụng xác thực sau này | - "Thông tin khuôn mặt không được bỏ trống" <br> - "Định dạng khuôn mặt không hợp lệ" |

---

## Phân hệ Thống kê (student/statistics)

### 1. Thống kê điểm danh
| Trường         | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSemester     | String  |                              |            | Không    | - Học kỳ phải tồn tại, trạng thái ACTIVE <br> - Mặc định lấy học kỳ hiện tại nếu null | - "Không tìm thấy học kỳ" |

---

## Business Logic Validations

### 1. Điểm danh (Attendance)
- **Kiểm tra ca học**: Ca học phải tồn tại và thuộc cơ sở của sinh viên
- **Kiểm tra sinh viên trong nhóm xưởng**: Sinh viên phải thuộc nhóm xưởng của ca học
- **Kiểm tra IP**: Nếu bật yêu cầu IP, phải kết nối từ mạng trường (kiểm tra IP trong danh sách cho phép)
- **Kiểm tra vị trí**: Nếu bật yêu cầu vị trí, phải trong vùng cho phép (tính toán khoảng cách GPS trong bán kính)
- **Kiểm tra khuôn mặt**: Sinh viên phải đã đăng ký thông tin khuôn mặt và phải đạt ngưỡng tương đồng với khuôn mặt đã lưu
- **Kiểm tra trạng thái điểm danh**: Không cho phép điểm danh lại nếu đã điểm danh hoặc bị hủy
- **Kiểm tra thời gian**: 
  - Chưa đến giờ điểm danh (kiểm tra theo thời gian bắt đầu ca học)
  - Đã quá giờ điểm danh (kiểm tra thời gian hiện tại vs thời gian kết thúc + thời gian muộn cho phép)
  - Thời gian checkout hợp lệ (sau thời gian checkin và trước khi kết thúc ca học)
- **Kiểm tra xác thực khuôn mặt**: So sánh với embedding đã lưu (sử dụng thuật toán cosine similarity với ngưỡng cấu hình)
- **Cache Redis**: Sử dụng cache để tránh điểm danh trùng lặp quá nhanh

### 2. Lịch học (Schedule)
- **Cache Redis**: Sử dụng cache để tối ưu hiệu suất
- **Phân trang**: Hỗ trợ phân trang với sắp xếp theo ID
- **Export PDF**: Xuất lịch học ra file PDF
- **Lọc thời gian**: Lọc theo ngày hiện tại và x ngày tiếp theo (x theo cấu hình)

### 3. Lịch sử điểm danh (History)
- **Tự động set học kỳ**: Nếu không chọn học kỳ, tự động set học kỳ hiện tại
- **Lọc theo sinh viên**: Chỉ hiển thị lịch sử của sinh viên đang đăng nhập
- **Export PDF**: Xuất lịch sử điểm danh ra file PDF
- **Phân trang**: Hỗ trợ phân trang kết quả

### 4. Đăng ký sinh viên (Register)
- **Kiểm tra sinh viên**: Phải có trong database và chưa đăng ký
- **Kiểm tra cơ sở**: Cơ sở phải đang hoạt động
- **Kiểm tra khuôn mặt**: Lưu embedding khuôn mặt để dùng cho quá trình điểm danh
- **Lưu token**: Tạo JWT token cho phiên đăng nhập của sinh viên

---

## Tổng kết Validation Patterns

### Validation Annotations sử dụng:
- **@NotBlank**: Kiểm tra chuỗi không rỗng và không chỉ chứa khoảng trắng
- **@Size**: Kiểm tra độ dài chuỗi trong giới hạn min-max
- **@Min/@Max**: Kiểm tra giá trị số trong giới hạn

### Business Logic Validations:
1. **Quyền truy cập**: Kiểm tra sinh viên có quyền truy cập dữ liệu
2. **Thời gian**: Kiểm tra thời gian điểm danh hợp lệ
3. **Tồn tại**: Kiểm tra entity tồn tại trong hệ thống
4. **Định dạng**: Kiểm tra IP, vị trí, khuôn mặt hợp lệ
5. **Giới hạn**: Kiểm tra thời gian muộn tối đa theo cấu hình
6. **Sở hữu**: Kiểm tra dữ liệu thuộc về sinh viên đang đăng nhập
7. **Trạng thái**: Kiểm tra trạng thái điểm danh (PRESENT, ABSENT, NOTCHECKIN)
8. **Xác thực**: Kiểm tra khuôn mặt, IP, vị trí theo yêu cầu
9. **Thời gian**: Kiểm tra giờ checkin/checkout hợp lệ

### Additional Service Validations:
- **Checkin**: Kiểm tra đầy đủ điều kiện trước khi điểm danh
- **IP Validation**: Kiểm tra IP trong danh sách cho phép
- **Location Validation**: Kiểm tra vị trí trong vùng cho phép
- **Face Recognition**: So sánh khuôn mặt với ngưỡng chấp nhận
- **Time Validation**: Kiểm tra thời gian điểm danh hợp lệ
- **Recovery System**: Cho phép điểm danh muộn với số lần có hạn

### Error Messages:
- Tất cả thông báo lỗi đều bằng tiếng Việt
- Thông báo rõ ràng về nguyên nhân lỗi
- Hướng dẫn cách khắc phục khi có thể
- Thông báo về giới hạn thời gian và điều kiện điểm danh 