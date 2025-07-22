# TỔNG HỢP VALIDATION MODULE GIÁO VIÊN (TEACHER)

## Tổng quan
Module Teacher được tổ chức theo các phân hệ chính:
1. **Statistics** - Quản lý thống kê
2. **TeachingSchedule** - Quản lý lịch dạy
3. **StudentAttendance** - Quản lý điểm danh sinh viên
4. **Factory** - Quản lý nhóm xưởng

## Chi tiết Validation từ mã nguồn

### 1. Điểm danh sinh viên (StudentAttendance)

#### TeacherStudentAttendanceRequest
```java
private String idAttendance;

private String idUserStudent;

private String idPlanDate;

private Integer status;
```

---

## Phân hệ Thống kê (teacher/statistics)

### 1. Lọc thống kê nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSemester  | String  |                              |            | Không    | - Kiểm tra học kỳ tồn tại và hoạt động nếu có | - "Không tìm thấy học kỳ" |
| idFacility  | String  |                              |            | Không    | - Kiểm tra cơ sở tồn tại và hoạt động nếu có | - "Cơ sở không tồn tại hoặc đã ngừng hoạt động" |
| idUserStaff | String  |                              |            | Không    | - Tự động set giảng viên từ session |               |

### 2. Gửi email thống kê
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSemester  | String  |                              |            | Có       | - Kiểm tra học kỳ tồn tại và hoạt động <br> - Kiểm tra cơ sở tồn tại và hoạt động <br> - Kiểm tra khoảng ngày trong học kỳ | - "Không tìm thấy học kỳ" <br> - "Cơ sở không tồn tại hoặc đã ngừng hoạt động" <br> - "Khoảng ngày thống kê phải nằm trong học kỳ" |
| emailAdmin  | Set<String> |                          |            | Không    | - Kiểm tra định dạng email hợp lệ | - "Email không hợp lệ" |
| emailStaff  | Set<String> |                          |            | Không    | - Kiểm tra định dạng email hợp lệ | - "Email không hợp lệ" |
| rangeDate   | List<Long> |                          |            | Có       | - Kiểm tra khoảng ngày hợp lệ <br> - Kiểm tra trong học kỳ <br> - Kiểm tra fromDate < toDate | - "Khoảng ngày thống kê phải nằm trong học kỳ" <br> - "Ngày bắt đầu phải trước ngày kết thúc" |

## Phân hệ Lịch dạy (teacher/teachingschedule)

### 1. Tìm kiếm lịch dạy
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSubject   | String  |                              |            | Không    | - Kiểm tra bộ môn tồn tại nếu có | - "Bộ môn không tồn tại" |
| idFactory   | String  |                              |            | Không    | - Factory phải thuộc quyền giáo viên, trạng thái ACTIVE | - "Không tìm thấy xưởng hoặc không có quyền truy cập" |
| idProject   | String  |                              |            | Không    | - Dự án phải thuộc giáo viên và đang hoạt động | - "Dự án không tồn tại hoặc không có quyền truy cập" |
| shift       | Integer |                              |            | Không    | - Kiểm tra ca học hợp lệ (1-6) | - "Ca học không hợp lệ" |
| startDate   | Long    |                              |            | Không    | - Kiểm tra thời gian hợp lệ | - "Thời gian không hợp lệ" |
| endDate     | Long    |                              |            | Không    | - Kiểm tra thời gian hợp lệ <br> - Kiểm tra endDate > startDate nếu cả hai đều có | - "Thời gian không hợp lệ" <br> - "Ngày kết thúc phải sau ngày bắt đầu" |
| shiftType   | Integer |                              |            | Không    | - Kiểm tra hình thức học hợp lệ | - "Hình thức học không hợp lệ" |

### 2. Cập nhật thông tin ca học
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| idPlanDate  | String  |                              |            | Có       | - Kiểm tra ca học tồn tại <br> - Kiểm tra ca học thuộc giảng viên <br> - Kiểm tra chưa quá giờ cập nhật (trước khi diễn ra) | - "Không tìm thấy lịch dạy" <br> - "Lịch dạy không phải của bạn" <br> - "Đã quá giờ cập nhật ca dạy" |
| description | String  |                              |            | Không    | - Mô tả ca học, có thể để trống |               |
| lateArrival | Integer | @Min(0)                    | 0+         | Không    | - Kiểm tra thời gian muộn tối đa <br> - Không được quá quy định (thường 30 phút) | - "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0" <br> - "Thời gian điểm danh muộn nhất không quá {MAX} phút" |
| link        | String  |                              |            | Không    | - Kiểm tra URL hợp lệ nếu có <br> - Phải là URL meeting hợp lệ | - "Link online không hợp lệ" <br> - "Link họp không hợp lệ" |
| room        | String  | @Size(min=2, max=EntityProperties.LENGTH_NAME) | 2-255 | Không | - Phòng học hợp lệ nếu có | - "Phòng học phải có ít nhất 2 ký tự" <br> - "Phòng học không được vượt quá 255 ký tự" |

## Phân hệ Điểm danh sinh viên (teacher/studentattendance)

### 1. Điểm danh sinh viên
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idAttendance | String |                          |            | Không    | - Kiểm tra điểm danh tồn tại (nếu có) <br> - Kiểm tra thuộc ca học của giảng viên <br> - Nếu có thì cập nhật, nếu không có thì tạo mới | - "Không tìm thấy điểm danh" <br> - "Điểm danh không thuộc ca học của bạn" |
| idUserStudent | String |                        |            | Có       | - Kiểm tra sinh viên tồn tại <br> - Kiểm tra sinh viên trong nhóm xưởng <br> - Kiểm tra sinh viên trạng thái ACTIVE | - "Không tìm thấy sinh viên" <br> - "Sinh viên không thuộc nhóm xưởng này" <br> - "Sinh viên đã bị khóa" |
| idPlanDate  | String  |                              |            | Có       | - Kiểm tra ca học tồn tại <br> - Kiểm tra ca học thuộc giảng viên <br> - Kiểm tra ca học đang diễn ra | - "Không tìm thấy ca học" <br> - "Ca học không thuộc của bạn" <br> - "Ca học chưa diễn ra hoặc đã kết thúc" |
| status      | Integer |                              |            | Có       | - Kiểm tra trạng thái hợp lệ (PRESENT, ABSENT, NOT_CHECKIN) <br> - Kiểm tra không cho phép điểm danh nhiều lần với cùng trạng thái | - "Trạng thái điểm danh không hợp lệ" <br> - "Sinh viên đã được điểm danh với trạng thái này" |

### 2. Sửa đổi điểm danh nhiều sinh viên
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| students    | List<TeacherStudentAttendanceRequest> | |            | Có       | - Kiểm tra danh sách không rỗng <br> - Kiểm tra từng sinh viên hợp lệ (validate từng item) <br> - Kiểm tra có thay đổi nào <br> - Kiểm tra tất cả sinh viên thuộc cùng 1 ca học | - "Danh sách sinh viên không được để trống" <br> - "Dữ liệu sinh viên không hợp lệ" <br> - "Không có thay đổi nào" <br> - "Sinh viên phải thuộc cùng một ca học" |

## Phân hệ Quản lý Nhóm xưởng (teacher/factory)

### 1. Tìm kiếm ca học theo nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFactory   | String  |                              |            | Không    | - Kiểm tra nhóm xưởng thuộc giảng viên | - "Nhóm xưởng không tồn tại hoặc không thuộc quyền quản lý của bạn" |
| idFacility  | String  |                              |            | Không    | - Kiểm tra cơ sở tồn tại và hoạt động | - "Cơ sở không tồn tại hoặc đã ngừng hoạt động" |
| keyword     | String  | @Size(max=EntityProperties.LENGTH_NAME) | 0-255 | Không | | - "Từ khóa không được quá: 255" |
| shift       | Integer |                              |            | Không    | - Kiểm tra ca học hợp lệ (1-6) | - "Ca học không hợp lệ" |
| type        | Integer |                              |            | Không    | - Kiểm tra loại hình học hợp lệ | - "Loại hình học không hợp lệ" |
| startDate   | Long    |                              |            | Không    | - Kiểm tra thời gian hợp lệ | - "Thời gian không hợp lệ" |
| status      | String  |                              |            | Không    | - Kiểm tra trạng thái hợp lệ | - "Trạng thái không hợp lệ" |

### 2. Tìm kiếm sinh viên trong nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| studentId   | String  |                              |            | Không    | - Kiểm tra sinh viên tồn tại nếu có | - "Sinh viên không tồn tại" |
| factoryId   | String  |                              |            | Không    | - Kiểm tra nhóm xưởng tồn tại và thuộc giảng viên | - "Nhóm xưởng không tồn tại hoặc không thuộc quyền quản lý của bạn" |
| studentFactoryId | String |                          |            | Không    | - Kiểm tra sinh viên thuộc nhóm xưởng nếu có | - "Sinh viên không thuộc nhóm xưởng" |
| searchQuery | String  | @Size(max=EntityProperties.LENGTH_NAME) | 0-255 | Không | | - "Từ khóa không được quá: 255" |
| status      | Integer |                              |            | Không    | - Kiểm tra trạng thái hợp lệ | - "Trạng thái không hợp lệ" |

### 3. Tìm kiếm nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| factoryId   | String  |                              |            | Không    | - Kiểm tra nhóm xưởng tồn tại nếu có | - "Nhóm xưởng không tồn tại" |
| userStaffId | String  |                              |            | Không    | - Tự động set từ session nếu không có | |
| factoryName | String  | @Size(max=EntityProperties.LENGTH_NAME) | 0-255 | Không | | - "Từ khóa không được quá: 255" |
| projectId   | String  |                              |            | Không    | - Kiểm tra dự án tồn tại nếu có | - "Dự án không tồn tại" |
| factoryStatus | String |                          |            | Không    | - Kiểm tra trạng thái hợp lệ | - "Trạng thái không hợp lệ" |
| semesterId  | String  |                              |            | Không    | - Kiểm tra học kỳ tồn tại nếu có | - "Học kỳ không tồn tại" |

### 4. Tìm kiếm điểm danh ca học
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| keyword     | String  | @Size(max=EntityProperties.LENGTH_NAME) | 0-255 | Không | | - "Từ khóa không được quá: 255" |
| status      | Integer |                              |            | Không    | - Kiểm tra trạng thái điểm danh hợp lệ | - "Trạng thái điểm danh không hợp lệ" |
| idPlanDate  | String  |                              |            | Không    | - Kiểm tra ca học tồn tại và thuộc giảng viên | - "Ca học không tồn tại hoặc không thuộc quyền quản lý của bạn" |
| idFacility  | String  |                              |            | Không    | - Kiểm tra cơ sở tồn tại và hoạt động | - "Cơ sở không tồn tại hoặc đã ngừng hoạt động" |
| idUserStudent | String |                          |            | Không    | - Kiểm tra sinh viên tồn tại nếu có | - "Sinh viên không tồn tại" |

### 5. Tìm kiếm sinh viên theo ca học
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFactory   | String  |                              |            | Không    | - Kiểm tra nhóm xưởng tồn tại và thuộc giảng viên | - "Nhóm xưởng không tồn tại hoặc không thuộc quyền quản lý của bạn" |
| idUserStudent | String |                          |            | Không    | - Kiểm tra sinh viên tồn tại nếu có | - "Sinh viên không tồn tại" |

### 6. Xuất danh sách điểm danh
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idPlanDate  | String  |                              |            | Có       | - Kiểm tra ca học tồn tại và thuộc giảng viên | - "Ca học không tồn tại hoặc không thuộc quyền quản lý của bạn" |
| type        | Integer |                              |            | Có       | - Kiểm tra loại xuất hợp lệ (Excel, PDF) | - "Loại xuất không hợp lệ" |

---

## Tổng kết Validation Patterns

### Validation Annotations sử dụng:
- **@Min(0)**: Kiểm tra giá trị tối thiểu là 0
- **@Size(min=2, max=255)**: Kiểm tra độ dài chuỗi từ 2-255 ký tự
- **@Size(max=255)**: Kiểm tra độ dài chuỗi tối đa 255 ký tự
- **@NotBlank**: Kiểm tra chuỗi không rỗng và không chỉ chứa khoảng trắng
- **@NotNull**: Kiểm tra giá trị không null

### Business Logic Validations:
1. **Quyền truy cập**: Kiểm tra giảng viên có quyền truy cập dữ liệu
2. **Thời gian**: Kiểm tra chưa quá giờ cập nhật/cập nhật
3. **Tồn tại**: Kiểm tra entity tồn tại trong hệ thống
4. **Định dạng**: Kiểm tra URL, email hợp lệ
5. **Giới hạn**: Kiểm tra thời gian muộn tối đa theo cấu hình
6. **Sở hữu**: Kiểm tra dữ liệu thuộc về giảng viên đang đăng nhập
7. **Trạng thái**: Kiểm tra trạng thái entity (ACTIVE/INACTIVE)
8. **Phạm vi**: Kiểm tra khoảng thời gian trong học kỳ
9. **Thay đổi**: Kiểm tra có thay đổi nào trong dữ liệu

### Additional Service Validations:
- **Tạo điểm danh**: Kiểm tra có sinh viên cho ngày điểm danh (phải có ít nhất 1 sinh viên trong nhóm xưởng)
- **Cập nhật điểm danh**: Kiểm tra có thay đổi nào trước khi lưu (tránh cập nhật không cần thiết)
- **Xóa sinh viên**: Kiểm tra sinh viên tồn tại trước khi xóa
- **Thống kê**: Kiểm tra học kỳ và cơ sở hoạt động
- **Gửi email**: Kiểm tra khoảng ngày trong học kỳ và định dạng email hợp lệ
- **Cập nhật ca học**: Kiểm tra ca học chưa diễn ra (chỉ cho phép cập nhật trước khi diễn ra)
- **Điểm danh sinh viên**: Kiểm tra sinh viên thuộc nhóm xưởng của ca học và ca học đang diễn ra

### Error Messages:
- Tất cả thông báo lỗi đều bằng tiếng Việt
- Thông báo rõ ràng về nguyên nhân lỗi
- Hướng dẫn cách khắc phục khi có thể
- Thông báo cụ thể về điều kiện và ràng buộc của dữ liệu 