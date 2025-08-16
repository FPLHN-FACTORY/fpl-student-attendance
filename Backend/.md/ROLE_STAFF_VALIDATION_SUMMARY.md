# TỔNG HỢP VALIDATION HỆ THỐNG QUẢN LÝ ĐIỂM DANH SINH VIÊN

## MỤC LỤC
1. [Module Staff](#module-staff)

---

# MODULE STAFF

## Tổng quan
Module Staff được tổ chức theo các phân hệ chính:
1. **Student** - Quản lý sinh viên
2. **Statistics** - Quản lý thống kê
3. **UserActivity** - Quản lý hoạt động người dùng
4. **AttendanceRecovery** - Quản lý phục hồi điểm danh
5. **StudentFactory** - Quản lý sinh viên xưởng
6. **Project** - Quản lý dự án
7. **Plan** - Quản lý kế hoạch
8. **Factory** - Quản lý nhóm xưởng

## Chi tiết Validation từ mã nguồn

### 1. Quản lý Sinh viên (Student)

#### USStudentCreateUpdateRequest
```java
private String id;

@NotBlank(message = "Không được để trống mã sinh viên")
@Size(max = EntityProperties.LENGTH_CODE, message = "Mã sinh viên không được vượt quá 50 ký tự")
private String code;

@NotBlank(message = "Không được để trống mã sinh viên")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên sinh viên phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String name;

@NotBlank(message = "Không được để trống email sinh viên")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Email sinh viên phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String email;
```

### 2. Quản lý Dự án (Project)

#### USProjectCreateOrUpdateRequest
```java
@NotBlank(message = "Tên dự án không được bỏ trống")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên dự án phải có ít nhất 2 ký tự và chỉ được tối đa 255 ký tự")
private String name;

private String description;

private String levelProjectId;

private String semesterId;

private String subjectFacilityId;
```

### 3. Quản lý Kế hoạch (Plan)

#### SPDAddOrUpdatePlanRequest
```java
private String id;

private String idProject;

private List<Long> rangeDate;

@Max(value = 50, message = "Số buổi checkin/checkout muộn không được vượt quá 50% tổng số buổi")
@Min(value = 0, message = "Số buổi checkin/checkout muộn không được nhỏ hơn 0%")
private Integer maxLateArrival;

@NotBlank(message = "Vui lòng nhập tên kế hoạch")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên kế hoạch phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String name;

private String description;
```

#### SPDAddOrUpdatePlanDateRequest
```java
private String idFacility;

private String idPlanFactory;

private String id;

private Long startDate;

private String link;

@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Phòng học phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String room;

private Integer requiredIp = StatusType.ENABLE.getKey();

private Integer requiredLocation = StatusType.ENABLE.getKey();

private Integer requiredCheckin = StatusType.ENABLE.getKey();

private Integer requiredCheckout = StatusType.ENABLE.getKey();

private List<Integer> shift;

private Integer type;

@Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0")
private Integer lateArrival;

private String description;

private List<Long> customTime;
```

#### SPDAddPlanFactoryRequest
```java
private String idPlan;

private String idFactory;

private List<Integer> days;

private Integer type;

private String link;

@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Phòng học phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String room;

private Integer requiredIp = StatusType.ENABLE.getKey();

private Integer requiredLocation = StatusType.ENABLE.getKey();

private Integer requiredCheckin = StatusType.ENABLE.getKey();

private Integer requiredCheckout = StatusType.ENABLE.getKey();

private List<Integer> shift;

@Min(value = 0, message = "Thời gian điểm danh muộn nhất phải lớn hơn 0")
private Integer lateArrival;
```

---

## Phân hệ Quản lý Sinh viên (staff/student)

### 1. Thêm/Cập nhật sinh viên
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| code        | String  | @NotBlank (không để trống), @Size(max = 50)          | 1-50       | Có       | - Kiểm tra trùng mã sinh viên  | - "Không được để trống mã sinh viên" <br> - "Mã sinh viên không được vượt quá 50 ký tự" <br> - "Mã sinh viên đã tồn tại" <br> - "Mã sinh viên không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |
| name        | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       | - Kiểm tra định dạng họ tên    | - "Không được để trống mã sinh viên" <br> - "Tên sinh viên không được vượt quá 255 ký tự" <br> - "Họ Tên sinh viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt." |
| email       | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       | - Kiểm tra trùng email <br> - Kiểm tra định dạng email | - "Không được để trống email sinh viên" <br> - "Email sinh viên không được vượt quá 255 ký tự" <br> - "Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn" <br> - "Email phải kết thúc bằng fpt.edu.vn" <br> - "Email sinh viên đã tồn tại" <br> - "Đã có sinh viên khác dùng email này" |

### 2. Tìm kiếm sinh viên
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| studentId   | String  |                              |            | Không    |                               |               |
| searchQuery | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Từ khóa không được quá: 255" |
| studentStatus | EntityStatus |                          |            | Không    |                               |               |

## Phân hệ Quản lý Thống kê (staff/statistics)

### 1. Lọc thống kê cơ sở
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSemester  | String  |                              |            | Không    |                               |               |
| idFacility  | String  |                              |            | Không    |                               |               |

### 2. Gửi email thống kê
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idSemester  | String  |                              |            | Có       | - Kiểm tra học kỳ tồn tại và hoạt động | - "Không tìm thấy học kỳ" |
| emailAdmin  | Set<String> |                          |            | Không    |                               |               |
| emailStaff  | Set<String> |                          |            | Không    |                               |               |
| emailTeacher | Set<String> |                         |            | Không    |                               |               |
| rangeDate   | List<Long> |                          |            | Có       | - Kiểm tra khoảng ngày trong học kỳ | - "Khoảng ngày thống kê phải nằm trong học kỳ" |

## Phân hệ Quản lý Hoạt động Người dùng (staff/useractivity)

### 1. Tìm kiếm hoạt động
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| facilityId  | String  |                              |            | Không    |                               |               |
| userId      | String  |                              |            | Không    |                               |               |
| role        | Integer |                              |            | Không    |                               |               |
| searchQuery | String  |                              |            | Không    |                               |               |
| fromDate    | Long    |                              |            | Không    |                               |               |
| toDate      | Long    |                              |            | Không    |                               |               |

## Phân hệ Quản lý Phục hồi Điểm danh (staff/attendancerecovery)

### 1. Tạo/Cập nhật sự kiện phục hồi điểm danh
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| name        | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       |                               | - "Không được để trống tên sự kiện" <br> - "Tên sụ kiện chỉ được tối đa 255 ký tự" |
| description | String  |                              |            | Không    |                               |               |
| day         | Long    |                              |            | Không    |                               |               |

### 2. Thêm sinh viên vào sự kiện phục hồi
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| day         | Long    |                              |            | Có       | - Kiểm tra sinh viên có ca học trong ngày | - "Dữ liệu đầu vào không hợp lệ" <br> - "Không tìm thấy mã sinh viên {code}" <br> - "Sinh viên {code} - {name} chưa tham gia nhóm xưởng nào" <br> - "Nhóm xưởng của sinh viên {code} - {name} chưa có kế hoạch hoặc không tồn tại" <br> - "Ngày {date} - Sinh viên {code} - {name} không có ca học nào" <br> - "Sinh viên đã được điểm danh có mặt cho tất cả ca học trong ngày {date}" |
| studentCode | String  |                              |            | Có       | - Kiểm tra sinh viên tồn tại   |               |
| attendanceRecoveryId | String |                      |            | Có       | - Kiểm tra sự kiện tồn tại    |               |

### 3. Tìm kiếm sự kiện phục hồi
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| searchQuery | String  |                              |            | Không    |                               |               |
| fromDate    | Long    |                              |            | Không    |                               |               |
| toDate      | Long    |                              |            | Không    |                               |               |
| semesterId  | String  |                              |            | Không    |                               |               |

## Phân hệ Quản lý Sinh viên - Nhóm xưởng (staff/studentfactory)

### 1. Thêm sinh viên vào nhóm xưởng
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| studentCode | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       | - Kiểm tra sinh viên tồn tại   | - "Không được để trống mã sinh viên" <br> - "Mã sih viên 255 ký tự" <br> - "Thêm sinh viên thất bại: Sinh viên không tồn tại trong cơ sở" <br> - "Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng" <br> - "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm vượt quá 20" <br> - "Thêm sinh viên thất bại: Sinh viên tồn tại ở ca khác" |
| factoryId   | String  |                              |            | Có       | - Kiểm tra nhóm xưởng tồn tại |               |

### 2. Tạo/Cập nhật sinh viên - nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| studentId   | String  |                              |            | Có       | - Kiểm tra sinh viên tồn tại   | - "Thêm sinh viên thất bại: Sinh viên đã có trong nhóm xưởng" <br> - "Thêm sinh viên thất bại: Số lượng sinh viên trong nhóm vượt quá 20" <br> - "Thêm sinh viên thất bại: Sinh viên tồn tại ở ca khác" |
| factoryId   | String  |                              |            | Có       | - Kiểm tra nhóm xưởng tồn tại |               |

### 3. Tìm kiếm sinh viên trong nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| studentId   | String  |                              |            | Không    |                               |               |
| factoryId   | String  |                              |            | Không    |                               |               |
| studentFactoryId | String |                          |            | Không    |                               |               |
| searchQuery | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Tìm kiếm 255 ký tự" |
| status      | Integer |                              |            | Không    |                               |               |

### 4. Tìm kiếm sinh viên theo cơ sở
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| searchQuery | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Keyword không được vượt quá 255 ký tự" |
| factoryId   | String  |                              |            | Không    |                               |               |

### 5. Tìm kiếm ca học theo sinh viên
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| keyword     | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Keyword không được vượt quá 255 ký tự" |
| shift       | Integer |                              |            | Không    |                               |               |
| type        | Integer |                              |            | Không    |                               |               |
| startDate   | Long    |                              |            | Không    |                               |               |
| status      | String  |                              |            | Không    |                               |               |

## Phân hệ Quản lý Dự án (staff/project)

### 1. Tạo/Cập nhật dự án
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                             | Thông báo lỗi                                                                                                                                                                                                                                                                                                             |
|-------------|---------|--------------------------------------|------------|----------|----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name        | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       | - Kiểm tra định dạng tên dự án <br> - Kiểm tra trùng dự án trong cùng nhóm | - "Tên dự án không được bỏ trống" <br> - "Tên dự án chỉ được tối đa 255 ký tự" <br> - "Tên dự án không hợp lệ: Chỉ được chứa ký tự chữ và các ký tự đặc biệt _ - #" <br> - "Tên dự án không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng" <br> - "Dự án này đã tồn tại ở {level} - {semester} - {year} - {subject}" |
| description | String  |                              |            | Không    |                                                                            |                                                                                                                                                                                                                                                                                                                           |
| levelProjectId | String |                          |            | Có       | - Kiểm tra nhóm dự án tồn tại                                              | - "Không tìm thấy nhóm dự án"                                                                                                                                                                                                                                                                                             |
| semesterId  | String  |                              |            | Có       | - Kiểm tra học kỳ tồn tại <br> - Kiểm tra học kỳ chưa kết thúc             | - "Không tìm thấy học kỳ" <br> - "Vui lòng chọn học kỳ đang hoặc chưa diễn ra cho dự án mới"                                                                                                                                                                                                                              |
| subjectFacilityId | String |                      |            | Có       | - Kiểm tra bộ môn tồn tại                                                  | - "Không tìm thấy bộ môn"                                                                                                                                                                                                                                                                                                 |

### 2. Tìm kiếm dự án
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| name        | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Từ khóa không được quá: 255" |
| levelProjectId | String |                          |            | Không    |                               |               |
| semesterId  | String  |                              |            | Không    |                               |               |
| subjectId   | String  |                              |            | Không    |                               |               |
| facilityId  | String  |                              |            | Không    |                               |               |
| status      | Integer |                              |            | Không    |                               |               |

## Phân hệ Quản lý Kế hoạch (staff/plan)

### 1. Tạo/Cập nhật kế hoạch
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| idProject   | String  |                              |            | Có       | - Kiểm tra dự án tồn tại và hoạt động <br> - Kiểm tra dự án thuộc cơ sở <br> - Kiểm tra học kỳ của dự án hoạt động <br> - Kiểm tra dự án chưa được triển khai trong kế hoạch khác | - "Không tìm thấy dự án" <br> - "Dự án {name} đã được triển khai trong 1 kế hoạch khác" |
| rangeDate   | List<Long> |                          |            | Có       | - Kiểm tra khoảng ngày hợp lệ <br> - Kiểm tra ngày trong học kỳ <br> - Kiểm tra ngày bắt đầu và kết thúc hợp lệ | - "Ngày bắt đầu diễn ra phải lớn hơn hoặc bằng ngày hiện tại" <br> - "Ngày kết thúc diễn ra phải lớn hơn hoặc bằng ngày hiện tại" <br> - "Thời gian diễn ra phải trong khoảng từ {fromDate} đến {toDate}" |
| maxLateArrival | Integer | @Min(0), @Max(50)              | 0-50       | Không    |                               | - "Số buổi checkin/checkout muộn không được nhỏ hơn 0%" <br> - "Số buổi checkin/checkout muộn không được vượt quá 50% tổng số buổi" |
| name        | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       |                               | - "Vui lòng nhập tên kế hoạch" <br> - "Tên kế hoạch không được vượt quá 255 ký tự" |
| description | String  |                              |            | Không    |                               |               |

### 2. Tìm kiếm kế hoạch
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Không    |                               |               |
| keyword     | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Keyword không được vượt quá 255 ký tự" |
| level       | String  |                              |            | Không    |                               |               |
| status      | Integer |                              |            | Không    |                               |               |
| semester    | String  |                              |            | Không    |                               |               |
| year        | Integer |                              |            | Không    |                               |               |
| subject     | String  |                              |            | Không    |                               |               |

### 3. Thêm nhóm xưởng vào kế hoạch
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| idPlan      | String  |                              |            | Có       | - Kiểm tra kế hoạch tồn tại và thuộc cơ sở | - "Không tìm thấy kế hoạch" |
| idFactory   | String  |                              |            | Có       | - Kiểm tra nhóm xưởng tồn tại và thuộc cơ sở <br> - Kiểm tra nhóm xưởng chưa có sinh viên <br> - Kiểm tra nhóm xưởng chưa được thêm vào kế hoạch khác | - "Không tìm thấy nhóm xưởng" <br> - "Chỉ có thể thêm nhóm xưởng khi chưa có sinh viên nào" <br> - "Nhóm xưởng {name} đã được triển khai trong một kế hoạch này" |
| days        | List<Integer> |                        |            | Có       | - Kiểm tra ít nhất 1 ca học   | - "Vui lòng chọn ít nhất 1 ca học" |
| type        | Integer |                              |            | Có       | - Kiểm tra hình thức học hợp lệ | - "Hinh thức học không hợp lệ" |
| link        | String  |                              |            | Không    | - Kiểm tra URL hợp lệ         | - "Link học online không hợp lệ" |
| room        | String  | @Size(min = 2, max = 255)     | 2-255      | Không    |                               | - "Phòng học không được vượt quá 255 ký tự" |
| requiredIp  | Integer |                              |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredLocation | Integer |                      |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredCheckin | Integer |                       |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredCheckout | Integer |                      |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| shift       | List<Integer> |                       |            | Có       | - Kiểm tra ít nhất 1 ca học <br> - Kiểm tra ca học tồn tại <br> - Kiểm tra không trùng lịch giảng viên | - "Vui lòng chọn ít nhất 1 ca học" <br> - "Ca học {shift} không tồn tại" <br> - "Giảng viên {name} - {code} đã đứng lớp tại ca {shift} trong ngày {date}" |
| lateArrival | Integer | @Min(0)                    | 0+         | Không    | - Kiểm tra thời gian muộn tối đa | - "Thời gian điểm danh muộn nhất phải lớn hơn 0" <br> - "Thời gian điểm danh muộn nhất không quá {MAX} phút" |

### 4. Tạo/Cập nhật ca học chi tiết
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Có       | - Kiểm tra cơ sở tồn tại      | - "Không tìm thấy kế hoạch" <br> - "Không tìm thấy kế hoạch chi tiết" |
| idPlanFactory | String |                          |            | Có       | - Kiểm tra kế hoạch nhóm xưởng tồn tại | - "Không tìm thấy kế hoạch nhóm xưởng" |
| startDate   | Long    |                              |            | Có       | - Kiểm tra thời gian hợp lệ   | - "Không thể cập nhật kế hoạch đã diễn ra" |
| link        | String  |                              |            | Không    | - Kiểm tra URL hợp lệ         | - "Link online không hợp lệ" |
| room        | String  | @Size(min = 2, max = 255)     | 2-255      | Không    |                               | - "Phòng học không được vượt quá 255 ký tự" |
| requiredIp  | Integer |                              |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredLocation | Integer |                      |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredCheckin | Integer |                       |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| requiredCheckout | Integer |                      |            | Không    | - Kiểm tra điều kiện điểm danh | - "Điều kiện điểm danh không hợp lệ" |
| shift       | List<Integer> |                       |            | Có       | - Kiểm tra ít nhất 1 ca học <br> - Kiểm tra ca học tồn tại <br> - Kiểm tra không trùng lịch giảng viên | - "Vui lòng chọn ít nhất 1 ca học" <br> - "Ca học {shift} không tồn tại" <br> - "Giảng viên {name} - {code} đã đứng lớp tại ca {shift} trong ngày {date}" |
| type        | Integer |                              |            | Có       | - Kiểm tra hình thức học hợp lệ | - "Hình thức học không hợp lệ" |
| lateArrival | Integer | @Min(0)                    | 0+         | Không    | - Kiểm tra thời gian muộn tối đa | - "Thời gian điểm danh muộn nhất phải lớn hơn hoặc bằng 0" <br> - "Thời gian điểm danh muộn nhất không quá {MAX} phút" |
| description | String  |                              |            | Không    |                               |               |
| customTime  | List<Long> |                         |            | Không    |                               |               |

## Phân hệ Quản lý Cơ sở (staff/factory)

### 1. Tạo/Cập nhật nhóm xưởng
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| id          | String  |                              |            | Không    |                               |               |
| factoryName | String  | @NotBlank (không để trống), @Size(min = 2, max = 255) | 2-255      | Có       | - Kiểm tra định dạng tên nhóm xưởng <br> - Kiểm tra trùng tên trong cùng dự án | - "Tên nhóm xưởng không được để trống" <br> - "Tên nhóm xưởng chỉ được tối đa 255 ký tự" <br> - "Tên nhóm xưởng không hợp lệ: Chỉ được chứa ký tự chữ và các ký tự đặc biệt _ - #" <br> - "Nhóm xưởng đã tồn tại trong dự án này" |
| factoryDescription | String |                          |            | Không    |                               |               |
| idProject   | String  |                              |            | Có       | - Kiểm tra dự án tồn tại      | - "Dự án không tồn tại" |
| idUserStaff | String  |                              |            | Có       | - Kiểm tra giảng viên tồn tại | - "Giảng viên không tồn tại" |

### 2. Tìm kiếm nhóm xưởng
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idProject   | String  |                              |            | Không    |                               |               |
| idFacility  | String  |                              |            | Không    |                               |               |
| factoryName | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Tên nhóm xưởng không được vượt quá 255 ký tự" |
| idSemester  | String  |                              |            | Không    |                               |               |
| status      | Integer |                              |            | Không    |                               |               |

### 9. Tìm kiếm ca học chi tiết
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idPlanFactory | String |                          |            | Không    |                               |               |
| idFacility  | String  |                              |            | Không    |                               |               |
| keyword     | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Keyword không được vượt quá 255 ký tự" |
| shift       | Integer |                              |            | Không    |                               |               |
| type        | Integer |                              |            | Không    |                               |               |
| startDate   | Long    |                              |            | Không    |                               |               |
| status      | String  |                              |            | Không    |                               |               |

### 10. Tìm kiếm nhóm xưởng trong kế hoạch
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Không    |                               |               |
| idPlan      | String  |                              |            | Không    |                               |               |
| keyword     | String  | @Size(max = 255)             | 0-255      | Không    |                               | - "Keyword không được vượt quá 255 ký tự" |
| status      | Integer |                              |            | Không    |                               |               |
| fromDate    | Long    |                              |            | Không    |                               |               |
| toDate      | Long    |                              |            | Không    |                               |               |

### 11. Tìm kiếm tạo ca học
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Không    |                               |               |
| idFactory   | String  |                              |            | Không    |                               |               |
| level       | String  |                              |            | Không    |                               |               |
| semester    | String  |                              |            | Không    |                               |               |
| year        | Integer |                              |            | Không    |                               |               |
| subject     | String  |                              |            | Không    |                               |               |

### 12. Tìm kiếm tạo kế hoạch
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Không    |                               |               |
| idProject   | String  |                              |            | Không    |                               |               |
| level       | String  |                              |            | Không    |                               |               |
| semester    | String  |                              |            | Không    |                               |               |
| year        | Integer |                              |            | Không    |                               |               |
| subject     | String  |                              |            | Không    |                               |               |

 