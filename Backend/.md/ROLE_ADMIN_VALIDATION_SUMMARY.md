# Tổng hợp Validate, Check Trùng & Logic Nghiệp vụ - Module Admin

## Tổng quan
Module Admin được tổ chức theo các phân hệ chính:
1. **Facility** - Quản lý cơ sở
2. **LevelProject** - Quản lý nhóm dự án
3. **Semester** - Quản lý học kỳ
4. **Subject** - Quản lý bộ môn
5. **SubjectFacility** - Quản lý bộ môn cơ sở
6. **UserAdmin** - Quản lý tài khoản admin
7. **UserStaff** - Quản lý nhân viên
8. **Statistics** - Quản lý thống kê
9. **UserActivity** - Quản lý hoạt động người dùng

## Chi tiết Validation từ mã nguồn

### 1. Quản lý Cơ sở (admin/facility)

#### a) AFCreateUpdateFacilityRequest
```java
@NotBlank(message = "Tên cơ sở không được để trống")
@Size(min = 3, message = "Tên cơ sở phải có ít nhất 3 kí tự")
@Size(max = EntityProperties.LENGTH_CODE, message = "Tên cơ sở chỉ có thể có tối đa 50 kí tự")
private String facilityName;
```

#### b) AFAddOrUpdateFacilityLocationRequest
```java
private String id;
private String idFacility;

@NotBlank(message = "Vui lòng nhập tên địa điểm")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên địa điểm phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String name;

@DecimalMin(value = "-90.0", message = "Vĩ độ không được nhỏ hơn -90", inclusive = true)
@DecimalMax(value = "90.0", message = "Vĩ độ không được lớn hơn 90", inclusive = true)
private Double latitude;

@DecimalMin(value = "-180.0", message = "Kinh độ không được nhỏ hơn -180", inclusive = true)
@DecimalMax(value = "180.0", message = "Kinh độ không được lớn hơn 180", inclusive = true)
private Double longitude;

@Min(value = 1, message = "Bán kính tối thiểu là 1m")
@Max(value = 500, message = "Bán kính tối đa là 500m")
private Integer radius = 0;
```

#### c) AFAddOrUpdateFacilityShiftRequest
```java
private String id;
private String idFacility;

@Min(value = 1, message = "Ca sớm nhất là ca 1")
@Max(value = 6, message = "Ca muộn nhất là ca 6")
private Integer shift;

@Min(value = 0, message = "Giờ bắt đầu không hợp lệ")
@Max(value = 23, message = "Giờ bắt đầu không hợp lệ")
private Integer fromHour;

@Min(value = 0, message = "Phút bắt đầu không hợp lệ")
@Max(value = 59, message = "Phút bắt đầu không hợp lệ")
private Integer fromMinute;

@Min(value = 0, message = "Giờ kết thúc không hợp lệ")
@Max(value = 23, message = "Giờ kết thúc không hợp lệ")
private Integer toHour;

@Min(value = 0, message = "Phút kết thúc không hợp lệ")
@Max(value = 59, message = "Phút kết thúc không hợp lệ")
private Integer toMinute;
```

### 6. Quản lý Admin (admin/useradmin)

#### a) ADUserAdminCreateOrUpdateRequest
```java
@NotBlank(message = "Mã admin tạo không được bỏ trống")
@Size(max = EntityProperties.LENGTH_CODE, message = "Mã admin chỉ được tối đa 50 ký tự")
private String staffCode;

@NotBlank(message = "Không được để trống email admin")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Email phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String email;

@NotBlank(message = "Không được để trống tên admin")
@Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên admin phải có ít nhất 2 ký tự và không được vượt quá 255 ký tự")
private String staffName;
```

---

## 1. Quản lý Cơ sở (Facility)

### a) Tạo/Cập nhật cơ sở (AFCreateUpdateFacilityRequest)
| Trường        | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|---------------|----------------------------------|------------|--------------------------------------------|------------|
| facilityName  | @NotBlank (không để trống), @Size(min=3, max=50)  | 3-50       | **Check trùng tên:** Không trùng tên với cơ sở khác (tạo: findByName, sửa: isExistsByName, loại trừ chính nó).<br>**Logic:** Tên được chuẩn hóa, sinh code từ tên. Không cho đổi trạng thái cơ sở cuối cùng còn hoạt động. Không cho đổi trạng thái nhiều lần trong ngày. | "Tên cơ sở đã tồn tại trên hệ thống", "Tên cơ sở không được để trống", "Tên cơ sở phải có ít nhất 3 ký tự", "Tên cơ sở chỉ có thể có tối đa 50 ký tự", "Không thể thay đổi trạng thái cơ sở cuối cùng còn hoạt động", "Chỉ được đổi trạng thái cơ sở ngừng hoạt động 1 lần mỗi ngày" |

### b) Thêm/Cập nhật IP/DNS Suffix (AFAddOrUpdateFacilityIPRequest)
| Trường   | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------|----------------------------------|------------|--------------------------------------------|------------|
| ip       | @NotBlank (không để trống), @Size(min=2, max=255) | 2-255      | **Check trùng:** Không trùng IP/DNS Suffix cùng loại trong cùng cơ sở (isExistsIP).<br>**Logic:** Kiểm tra định dạng hợp lệ (IPv4, IPv6, CIDR, DNS Suffix). Chỉ cho phép thêm/cập nhật khi cơ sở đang ACTIVE. | "IP/DNS Suffix đã tồn tại trong cơ sở", "IP/DNS Suffix không hợp lệ", "Vui lòng điền đầy đủ mục yêu cầu", "Không tìm thấy cơ sở" |
| type     | Bắt buộc (1: IPv4, 2: IPv6, 3: DNS Suffix) | | | |
| idFacility | Không validate (bắt buộc - kiểm tra ở service) | | **Logic:** Phải là cơ sở hợp lệ, đang hoạt động. | "Không tìm thấy cơ sở" |

### c) Thêm/Cập nhật địa điểm (AFAddOrUpdateFacilityLocationRequest)
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | @NotBlank (không để trống), @Size(min=2, max=255) | 2-255      | **Check trùng:** Không trùng tên địa điểm trong cùng cơ sở.<br>**Logic:** | "Tên địa điểm đã tồn tại trong cơ sở", "Vui lòng nhập tên địa điểm", "Tên địa điểm không được vượt quá 255 ký tự" |
| latitude   | @DecimalMin(-90), @DecimalMax(90)|            |                                            | "Vĩ độ không được nhỏ hơn -90/lớn hơn 90" |
| longitude  | @DecimalMin(-180), @DecimalMax(180)|          |                                            | "Kinh độ không được nhỏ hơn -180/lớn hơn 180" |
| radius     | @Min(1), @Max(500)               |            |                                            | "Bán kính tối thiểu là 1m/tối đa là 500m" |
| idFacility | Không validate (bắt buộc - kiểm tra ở service) | | **Logic:** Phải là cơ sở hợp lệ, đang hoạt động. | "Không tìm thấy cơ sở" |

### d) Thêm/Cập nhật ca xưởng (AFAddOrUpdateFacilityShiftRequest)
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| shift      | @Min(1), @Max(6)                 |            |                                            | "Ca học sớm nhất là ca 1", "Ca học muộn nhất là ca 6" |
| fromHour   | @Min(0), @Max(23)                |            |                                            | "Giờ bắt đầu không hợp lệ" |
| fromMinute | @Min(0), @Max(59)                |            |                                            | "Phút bắt đầu không hợp lệ" |
| toHour     | @Min(0), @Max(23)                |            |                                            | "Giờ kết thúc không hợp lệ" |
| toMinute   | @Min(0), @Max(59)                |            |                                            | "Phút kết thúc không hợp lệ" |
| idFacility | Không validate (bắt buộc - kiểm tra ở service) | | **Logic:** Phải là cơ sở hợp lệ, đang hoạt động. | "Không tìm thấy cơ sở" |

## 2. Quản lý IP/DNS Suffix của Cơ sở
| Trường   | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------|----------------------------------|------------|--------------------------------------------|------------|
| ip       | Bắt buộc, không để trống         | 2-255      | Không trùng IP/DNS Suffix cùng loại trong cùng cơ sở | "IP/DNS Suffix đã tồn tại trong cơ sở"<br>"IP/DNS Suffix không hợp lệ"<br>"Vui lòng điền đầy đủ mục yêu cầu" |
|          | @NotBlank, @Size(min=2, max=255) |            | Kiểm tra định dạng IPv4/IPv6/CIDR/DNS Suffix hợp lệ |            |
| type     | Bắt buộc (1: IPv4, 2: IPv6, 3: DNS Suffix) | | | |

**Các logic khác:**
- Khi cập nhật: kiểm tra tồn tại theo id, nếu không có trả về "Không tìm thấy IP/DNS Suffix muốn cập nhật".
- Khi đổi trạng thái: nếu IP/DNS Suffix đã được áp dụng thì không cho đổi trạng thái.

## 3. Quản lý Địa điểm của Cơ sở
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | Bắt buộc, không để trống         | 2-100      | Không trùng tên địa điểm trong cùng cơ sở  | "Tên địa điểm đã tồn tại trong cơ sở"<br>"Vui lòng nhập tên địa điểm"<br>"Tên địa điểm không được vượt quá 100 ký tự" |
|            | @NotBlank, @Size(min=2, max=100) |            |                                            |            |
| latitude   | Bắt buộc, [-90, 90]              |            |                                            | "Vĩ độ không được nhỏ hơn -90/lớn hơn 90" |
| longitude  | Bắt buộc, [-180, 180]            |            |                                            | "Kinh độ không được nhỏ hơn -180/lớn hơn 180" |
| radius     | Bắt buộc, [1, 500]               |            |                                            | "Bán kính tối thiểu là 1m/tối đa là 500m" |

**Các logic khác:**
- Khi cập nhật: kiểm tra tồn tại theo id, nếu không có trả về "Không tìm thấy địa điểm muốn cập nhật".
- Khi đổi trạng thái: nếu địa điểm đã được áp dụng thì không cho đổi trạng thái.

## 4. Quản lý Nhóm dự án (Level Project)

### a) Tạo/Cập nhật Level Project
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | @NotBlank (không để trống), @Size(max=50)         | max 50     | **Check trùng mã:** Sinh code từ tên, không trùng mã nhóm dự án (isExistsLevelProject, loại trừ chính nó).<br>**Logic:** Kiểm tra định dạng tên (không khoảng trắng, không ký tự đặc biệt ngoài . và _). Khi đổi trạng thái sang ACTIVE: gọi disableAllStudentDuplicateShiftByIdLevelProject. | "Tên cấp độ dự án không được bỏ trống", "Tên cấp độ dự án không được vượt quá 50 ký tự", "Nhóm dự án đã tồn tại trong hệ thống", "Mã dự án đã tồn tại vui lòng sửa mã dự án khác", "Tên nhóm dự án không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |
| description| Không bắt buộc                   |            |                                            |            |

### b) Tìm kiếm Level Project
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | @Size(max=255)                   | max 255    |                                            | "Từ khóa không được quá:255" |
| status     | Không validate                   |            |                                            |            |
| page, size, orderBy, sortBy, q | Không validate | | | |

## 5. Quản lý Học kỳ (Semester)

### a) Tạo/Cập nhật Học kỳ
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| semesterId     | Không validate                   |            |                                            |            |
| facilityId     | Không validate                   |            |                                            |            |
| semesterName   | @NotBlank (không để trống), @Size(min=2, max=255) | 2-255      | **Check trùng:** Không trùng tên học kỳ cùng năm, cùng trạng thái (checkSemesterExistNameAndYear, loại trừ chính nó).<br>**Check trùng thời gian:** Không trùng thời gian với học kỳ khác (checkConflictTime).<br>**Logic:** fromDate < toDate, cùng 1 năm, cách nhau tối thiểu 3 tháng, không cho sửa học kỳ đã kết thúc, không cho tạo học kỳ quá khứ. | "Học kỳ đã tồn tại", "Tên học kỳ không được để trống", "Tên học kỳ không được quá:255", "Đã có học kỳ trong khoảng thời gian này!", "Khoảng thời gian học kỳ phải tối thiểu 3 tháng", "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm", "Không thể sửa học kỳ đã kết thúc", "Ngày bắt đầu học kỳ không thể là ngày quá khứ hoặc hiện tại" |
| fromDate       | @NotNull (không để trống)        |            |                                            |            |
| toDate         | @NotNull (không để trống)        |            |                                            |            |

### b) Tìm kiếm Học kỳ
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| semesterCode   | @Size(max=255)                   | max 255    |                                            | "Từ khóa không được quá:255" |
| fromDateSemester| Không validate                  |            |                                            |            |
| toDateSemester | Không validate                   |            |                                            |            |
| status         | Không validate                   |            |                                            |            |
| page, size, orderBy, sortBy, q | Không validate | | | |

**Các logic khác:**
- Khi cập nhật: kiểm tra tồn tại theo id, nếu không có trả về "Không tìm thấy học kỳ".
- Khi đổi trạng thái: chuyển ACTIVE/INACTIVE, nếu ACTIVE thì gọi disableAllStudentDuplicateShiftByIdSemester.

## 6. Quản lý Bộ môn (Subject)

### a) Tạo/Cập nhật Bộ môn
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | @NotBlank (không để trống), @Size(min=2, max=255) | 2-255      | **Check trùng:** Không trùng tên bộ môn (isExistsNameSubject, loại trừ chính nó).<br>**Logic:** | "Tên bộ môn không được bỏ trống", "Tên bộ môn không được vượt quá 255 ký tự", "Tên bộ môn đã tồn tại trên hệ thống" |
| code       | @NotBlank (không để trống), @Size(max=50)         | max 50     | **Check trùng:** Không trùng mã bộ môn (isExistsCodeSubject, loại trừ chính nó).<br>**Logic:** Kiểm tra định dạng code (không khoảng trắng, không ký tự đặc biệt ngoài . và _). | "Mã bộ môn không được bỏ trống", "Mã bộ môn không được vượt quá 50 ký tự", "Mã bộ môn đã tồn tại trên hệ thống", "Mã bộ môn không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |

### b) Tìm kiếm Bộ môn
| Trường     | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|------------|----------------------------------|------------|--------------------------------------------|------------|
| name       | @Size(max=255)                   | max 255    |                                            | "Từ khóa không được quá:255" |
| status     | Không validate                   |            |                                            |            |
| page, size, orderBy, sortBy, q | Không validate | | | |

**Các logic khác:**
- Khi cập nhật: kiểm tra tồn tại theo id, nếu không có trả về "Không tìm thấy bộ môn".
- Khi đổi trạng thái: chuyển ACTIVE/INACTIVE, nếu ACTIVE thì gọi disableAllStudentDuplicateShiftByIdSubject.

## 7. Quản lý Bộ môn cơ sở (Subject Facility)

### a) Tạo/Cập nhật Bộ môn cơ sở
| Trường       | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|--------------|----------------------------------|------------|--------------------------------------------|------------|
| facilityId   | Không validate (bắt buộc - kiểm tra ở service) |            | **Logic:** Phải là cơ sở hợp lệ, đang hoạt động. | "Không tìm thấy cơ sở" |
| subjectId    | Không validate (bắt buộc - kiểm tra ở service) |            | **Logic:** Phải là bộ môn hợp lệ, đang hoạt động. | "Không tìm thấy bộ môn" |
| status       | (Chỉ có ở update)                |            |                                            |            |
| (cặp facilityId + subjectId) | Không validate (bắt buộc - kiểm tra ở service) | | **Check trùng:** Không trùng bộ môn trong cùng cơ sở (isExistsSubjectFacility, loại trừ chính nó). | "Bộ môn đã tồn tại trong cơ sở này" |

### b) Tìm kiếm Bộ môn cơ sở
| Trường       | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|--------------|----------------------------------|------------|--------------------------------------------|------------|
| name         | Không validate                   |            |                                            |            |
| facilityId   | Không validate                   |            |                                            |            |
| subjectId    | Không validate                   |            |                                            |            |
| status       | Không validate                   |            |                                            |            |
| page, size, orderBy, sortBy, q | Không validate | | | |

## 8. Quản lý Admin (UserAdmin)

### a) Tạo/Cập nhật Admin
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| staffCode      | @NotBlank (không để trống), @Size(max=50)     | max 50     | **Check trùng:** Không trùng mã admin (getUserAdminByCode, isExistCodeUpdate, loại trừ chính mình).<br>**Logic:** Kiểm tra định dạng code (không khoảng trắng, không ký tự đặc biệt ngoài . và _). Không được sửa trạng thái hoặc xóa chính bản thân. | "Mã admin đã tồn tại", "Mã admin không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |
| email          | @NotBlank (không để trống), @Size(max=255)    | max 255    | **Check trùng:** Không trùng email (getUserAdminByEmail, isExistEmailUpdate, loại trừ chính mình).<br>**Logic:** Kiểm tra định dạng email (gmail, fe, fpt). Không cho phép chuyển quyền nếu email đã tồn tại ở admin khác. | "Email của admin đã tồn tại", "Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn", "Đã có admin khác dùng email fe này" |
| staffName      | @NotBlank (không để trống), @Size(max=255)    | 2-255      | **Logic:** Kiểm tra định dạng tên (tối thiểu 2 từ, chỉ ký tự chữ, không số, không ký tự đặc biệt). | "Tên admin không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", "Tên admin không được bỏ trống", "Tên admin không được vượt quá 255 ký tự" |

### b) Các logic khác:
- Không được sửa trạng thái hoặc xóa chính bản thân.
- Khi đổi trạng thái: gửi email thông báo nếu chuyển sang INACTIVE.
- Khi chuyển quyền: không cho phép chuyển nếu email đã tồn tại ở admin khác.

## 9. Quản lý Nhân viên (UserStaff)

### a) Tạo/Cập nhật Nhân viên
| Trường      | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|-------------|----------------------------------|------------|--------------------------------------------|------------|
| name        | @NotBlank (không để trống), @Size(max=255)    | max 255    | **Logic:** Kiểm tra định dạng tên (tối thiểu 2 từ, chỉ ký tự chữ, không số, không ký tự đặc biệt). | "Tên nhân viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt.", "Tên không được để trống", "Tên chỉ được tối đa 255 ký tự" |
| staffCode   | @NotBlank (không để trống), @Size(max=50)     | max 50     | **Check trùng:** Không trùng mã nhân viên (isStaffExist, loại trừ chính mình).<br>**Logic:** Kiểm tra định dạng code (không khoảng trắng, không ký tự đặc biệt ngoài . và _). | "Mã nhân viên đã tồn tại", "Mã nhân viên không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |
| emailFe     | @NotBlank (không để trống), @Size(max=255)    | max 255    | **Check trùng:** Không trùng email FE nhân viên khác (isStaffExist, loại trừ chính mình).<br>**Logic:** Kiểm tra định dạng email FE. | "Email FE đã tồn tại", "Email FE không hợp lệ", "Email FE không được chứa khoảng trắng và phải kết thúc bằng @fe.edu.vn" |
| emailFpt    | @NotBlank (không để trống), @Size(max=255)    | max 255    | **Check trùng:** Không trùng email FPT nhân viên khác (isStaffExist, loại trừ chính mình).<br>**Logic:** Kiểm tra định dạng email FPT. | "Email FPT đã tồn tại", "Email FPT không hợp lệ", "Email FPT không được chứa khoảng trắng và phải kết thúc bằng @fpt.edu.vn" |
| facilityId  | @NotBlank (không để trống)       | 36         | **Logic:** Kiểm tra tồn tại cơ sở. | "Cơ sở không tồn tại" |
| roleCodes   | @NotBlank (không để trống)       |            | **Logic:** Phải chọn ít nhất 1 vai trò, kiểm tra hợp lệ enum RoleConstant. | "Phải chọn ít nhất một vai trò", "Vai trò không hợp lệ: ..." |

### b) Các logic khác:
- Không được sửa trạng thái hoặc xóa chính bản thân.
- Khi đổi trạng thái: gửi email thông báo nếu chuyển sang INACTIVE.

## 10. Quản lý Sinh viên (Staff/Student)
| Trường      | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|-------------|----------------------------------|------------|--------------------------------------------|------------|
| code        | Bắt buộc, không để trống         | max 50     | Không trùng mã sinh viên, định dạng: không khoảng trắng, không ký tự đặc biệt ngoài . và _ | "Mã sinh viên đã tồn tại", "Mã sinh viên không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." |
| name        | Bắt buộc, không để trống         | 2-255      | Định dạng: tối thiểu 2 từ, chỉ ký tự chữ, không số, không ký tự đặc biệt | "Tên sinh viên không hợp lệ", "Tên sinh viên không được bỏ trống", "Tên sinh viên không được vượt quá 255 ký tự" |
| email       | Bắt buộc, không để trống, đúng định dạng email   | 2-255    | Không trùng email, đúng domain FE/FPT/Gmail, có thể bắt buộc fpt.edu.vn theo cấu hình | "Email đã tồn tại", "Email không hợp lệ", "Email phải kết thúc bằng fpt.edu.vn" |

## 11. Quản lý Khôi phục điểm danh (Staff/AttendanceRecovery)

### a) Tạo/Cập nhật sự kiện khôi phục điểm danh
| Trường       | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|--------------|----------------------------------|------------|--------------------------------------------|------------|
| name         | Bắt buộc, không để trống, @Size(min=2, max=255) | 2-255      | Không trùng tên trong cùng cơ sở (nếu có)  | "Không được để trống tên sự kiện", "Tên tối đa 255 ký tự" |
| description  | Không bắt buộc                   |            |                                            |            |
| day          | Không validate annotation, kiểu Long (epoch millis) |            | Phải là ngày hợp lệ, không trùng event (nếu có) |            |

### b) Thêm sinh viên vào sự kiện khôi phục điểm danh
| Trường              | Validate/Bắt buộc | Biên ký tự | Check trùng/Logic nghiệp vụ                                                                 | Lỗi trả về                                                                                 |
|---------------------|------------------|------------|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| day                 | Không validate   | epoch millis| Phải có ca học hợp lệ trong ngày này                                                       | "Ngày ... không có ca học nào"                                                            |
| studentCode         | Không validate   |            | Phải tồn tại sinh viên với mã này, phải thuộc nhóm xưởng, phải có kế hoạch, không được điểm danh trùng | "Không tìm thấy mã sinh viên", "Chưa tham gia nhóm xưởng", "Chưa có kế hoạch", "Đã điểm danh đủ" |
| attendanceRecoveryId| Không validate   |            | Phải tồn tại sự kiện khôi phục điểm danh                                                   | "Không tìm thấy sự kiện khôi phục điểm danh"                                              |

### c) Lấy danh sách sự kiện khôi phục điểm danh
| Trường       | Validate/Bắt buộc | Biên ký tự | Check trùng/Logic nghiệp vụ | Lỗi trả về |
|--------------|------------------|------------|-----------------------------|------------|
| searchQuery  | Không validate   |            |                             |            |
| fromDate     | Không validate   | epoch millis|                            |            |
| toDate       | Không validate   | epoch millis|                            |            |
| semesterId   | Không validate   |            |                             |            |
| page, size, orderBy, sortBy, q | Không validate | | | |

## 12. Quản lý Sinh viên xưởng (Staff/StudentFactory)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| studentId      | Bắt buộc, phải là sinh viên hợp lệ|            | Kiểm tra tồn tại sinh viên                 | "Không tìm thấy sinh viên" |
| factoryId      | Bắt buộc, phải là xưởng hợp lệ   |            | Kiểm tra tồn tại xưởng                     | "Không tìm thấy xưởng" |
| (cặp studentId + factoryId) | Duy nhất |            | Không trùng sinh viên trong cùng xưởng     | "Sinh viên đã thuộc xưởng này" |

## 13. Quản lý Dự án (Staff/Project)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| projectName    | Bắt buộc, không để trống         | 3-100      | Không trùng tên dự án trong cùng xưởng     | "Tên dự án đã tồn tại trong xưởng" |
| code           | Bắt buộc, không để trống         | max 20     | Không trùng mã dự án, định dạng: không khoảng trắng, không ký tự đặc biệt ngoài . và _ | "Mã dự án đã tồn tại", "Mã dự án không hợp lệ" |
| factoryId      | Bắt buộc, phải là xưởng hợp lệ   |            | Kiểm tra tồn tại xưởng                     | "Không tìm thấy xưởng" |

## 14. Quản lý Kế hoạch (Staff/Plan)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| planName       | Bắt buộc, không để trống         | 3-100      | Không trùng tên kế hoạch trong cùng xưởng  | "Tên kế hoạch đã tồn tại trong xưởng" |
| factoryId      | Bắt buộc, phải là xưởng hợp lệ   |            | Kiểm tra tồn tại xưởng                     | "Không tìm thấy xưởng" |
| fromDate, toDate| Bắt buộc, đúng định dạng        |            | fromDate < toDate, không trùng lịch kế hoạch| "Ngày bắt đầu phải trước ngày kết thúc", "Trùng lịch kế hoạch" |

## 15. Quản lý Xưởng (Staff/Factory)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| factoryName    | Bắt buộc, không để trống         | 3-100      | Không trùng tên xưởng                      | "Tên xưởng đã tồn tại" |
| code           | Bắt buộc, không để trống         | max 20     | Không trùng mã xưởng, định dạng: không khoảng trắng, không ký tự đặc biệt ngoài . và _ | "Mã xưởng đã tồn tại", "Mã xưởng không hợp lệ" |

## 16. Teacher - Điểm danh sinh viên (TeacherStudentAttendance)
| Trường             | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|--------------------|----------------------------------|------------|--------------------------------------------|------------|
| idAttendance       | Không bắt buộc (update mới cần)  |            | Nếu null sẽ tạo mới, nếu có sẽ update      |            |
| idUserStudent      | Bắt buộc                         |            | Phải là sinh viên hợp lệ, sinh viên thuộc ca/xưởng, trạng thái ACTIVE | "Không tìm thấy sinh viên", "Sinh viên không thuộc ca học này" |
| idPlanDate         | Bắt buộc                         |            | Phải là ca học hợp lệ, ca học thuộc quyền giáo viên hiện tại, ca học trạng thái ACTIVE, chỉ thao tác khi ca học đã bắt đầu và chưa kết thúc | "Không tìm thấy ca học", "Lịch dạy không phải của bạn", "Ca học chưa diễn ra hoặc đã kết thúc" |
| status             | Bắt buộc                         |            | Giá trị hợp lệ (PRESENT/ABSENT/...), không cho phép điểm danh nhiều lần, chỉ cho phép điểm danh khi chưa có bản ghi | "Trạng thái điểm danh không hợp lệ", "Đã điểm danh rồi" |

## 17. Teacher - Quản lý xưởng (TCFactory)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| factoryId      | Không bắt buộc (filter)           |            | Xưởng phải thuộc quyền giáo viên, trạng thái ACTIVE | "Không tìm thấy xưởng hoặc không có quyền truy cập" |
| userStaffId    | Không bắt buộc (filter)           |            |                                            |            |
| factoryName    | Không bắt buộc, @Size(max=255)    | max 255    |                                            | "Tên xưởng không được vượt quá 255 ký tự" |
| projectId      | Không bắt buộc (filter)           |            |                                            |            |
| factoryStatus  | Không bắt buộc (filter)           |            |                                            |            |
| semesterId     | Không bắt buộc (filter)           |            |                                            |            |

## 18. Teacher - Quản lý sinh viên xưởng (TCStudentFactory)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| studentId      | Không bắt buộc (filter)           |            | Sinh viên phải thuộc xưởng, trạng thái ACTIVE | "Sinh viên không thuộc xưởng hoặc đã bị khóa" |
| factoryId      | Không bắt buộc (filter)           |            | Xưởng phải thuộc quyền giáo viên, trạng thái ACTIVE | "Không tìm thấy xưởng hoặc không có quyền truy cập" |
| studentFactoryId| Không bắt buộc (filter)           |            |                                            |            |
| searchQuery    | Không bắt buộc, @Size(max=255)    | max 255    |                                            | "Tìm kiếm không được vượt quá 255 ký tự" |
| status         | Không bắt buộc (filter)           |            |                                            |            |

## 19. Teacher - Lọc ca học, ca xưởng (TCFilterPlanDateAttendance, TCFilterShiftFactory)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| keyword        | Không bắt buộc, @Size(max=255)    | max 255    |                                            | "Từ khóa không được vượt quá 255 ký tự" |
| status         | Không bắt buộc (filter)           |            | Lọc chỉ các ca học/xưởng trạng thái ACTIVE |            |
| idPlanDate     | Không bắt buộc (filter)           |            | Ca học phải thuộc quyền giáo viên, trạng thái ACTIVE | "Không tìm thấy ca học hoặc không có quyền truy cập" |
| idFacility     | Không bắt buộc (filter)           |            |                                            |            |
| idUserStudent  | Không bắt buộc (filter)           |            |                                            |            |
| shift          | Không bắt buộc (filter)           |            |                                            |            |
| type           | Không bắt buộc (filter)           |            |                                            |            |
| startDate      | Không bắt buộc (filter)           |            |                                            |            |

## 20. Teacher - Lịch giảng dạy (TCTeachingSchedule)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| idSubject      | Không bắt buộc (filter)           |            |                                            |            |
| idFactory      | Không bắt buộc (filter)           |            | Factory phải thuộc quyền giáo viên, trạng thái ACTIVE | "Không tìm thấy xưởng hoặc không có quyền truy cập" |
| idProject      | Không bắt buộc (filter)           |            |                                            |            |
| shift          | Không bắt buộc (filter)           |            |                                            |            |
| startDate      | Không bắt buộc (filter)           |            |                                            |            |
| endDate        | Không bắt buộc (filter)           |            |                                            |            |
| shiftType      | Không bắt buộc (filter)           |            |                                            |            |

## 21. Teacher - Thống kê (TSFilterFactoryStats, TSSendMailStats)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| idSemester     | Không bắt buộc (filter)           |            | Học kỳ phải tồn tại, trạng thái ACTIVE     | "Không tìm thấy học kỳ" |
| idFacility     | Không bắt buộc (filter)           |            | Cơ sở phải tồn tại, trạng thái ACTIVE      | "Cơ sở không tồn tại hoặc đã ngừng hoạt động" |
| idUserStaff    | Không bắt buộc (filter)           |            |                                            |            |
| emailAdmin     | Không bắt buộc (gửi mail)         |            |                                            |            |
| emailStaff     | Không bắt buộc (gửi mail)         |            |                                            |            |
| rangeDate      | Không bắt buộc (gửi mail)         |            | Khoảng ngày phải nằm trong học kỳ          | "Khoảng ngày thống kê phải nằm trong học kỳ" |

## 22. Student - Điểm danh (SACheckinAttendance, SAFilterAttendance)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| idPlanDate     | Bắt buộc                         |            | Phải là ca học hợp lệ, ca học trạng thái ACTIVE, chỉ cho phép điểm danh khi ca học đang diễn ra | "Không tìm thấy ca học", "Ca học chưa diễn ra hoặc đã kết thúc" |
| latitude       | Không bắt buộc                    |            | Nếu ca học yêu cầu vị trí, phải kiểm tra vị trí hợp lệ | "Vị trí không hợp lệ hoặc ngoài phạm vi cho phép" |
| longitude      | Không bắt buộc                    |            |                                            |            |
| faceEmbedding  | Bắt buộc, @NotBlank              |            | Không được để trống, kiểm tra hợp lệ khuôn mặt | "Không tìm thấy dữ liệu khuôn mặt", "Khuôn mặt không hợp lệ" |
| idFacility     | Không bắt buộc (filter)           |            |                                            |            |
| idUserStudent  | Không bắt buộc (filter)           |            |                                            |            |
| keyword        | Không bắt buộc (filter)           |            |                                            |            |
| status         | Không bắt buộc (filter)           |            |                                            |            |
| type           | Không bắt buộc (filter)           |            |                                            |            |

## 23. Student - Lịch sử điểm danh (STDHistoryAttendance)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| studentFactoryId| Không bắt buộc (filter)           |            | Chỉ lấy lịch sử của sinh viên hiện tại, chỉ lấy ca/xưởng trạng thái ACTIVE | "Không có dữ liệu lịch sử" |
| factoryId      | Không bắt buộc (filter)           |            |                                            |            |
| semesterId     | Không bắt buộc (filter)           |            |                                            |            |

## 24. Student - Lịch học (STDScheduleAttendanceSearch)
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| idStudent      | Không bắt buộc (filter)           |            | Chỉ lấy lịch học của sinh viên hiện tại, chỉ lấy ca/xưởng trạng thái ACTIVE | "Không có dữ liệu lịch học" |
| now            | Không bắt buộc (filter)           |            |                                            |            |
| max            | Không bắt buộc (filter)           |            |                                            |            |

## 25. Student - Thống kê
| Trường         | Validate/Bắt buộc                | Biên ký tự | Check trùng/Logic nghiệp vụ                | Lỗi trả về |
|----------------|----------------------------------|------------|--------------------------------------------|------------|
| idSemester     | Không bắt buộc (filter)           |            | Học kỳ phải tồn tại, trạng thái ACTIVE     | "Không tìm thấy học kỳ" |

Nếu cần chi tiết validate cho các màn khác hoặc muốn bổ sung thêm, hãy phản hồi! 

## Quản lý Học Kỳ (admin/semester)

### 1. Thêm/Cập nhật học kỳ
**API:** `POST /api/admin/semester-management`<br>**API:** `PUT /api/admin/semester-management`

| Trường         | Kiểu     | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                                       | Thông báo lỗi |
|---------------|----------|--------------------------------------|------------|----------|---------------------------------------------------------------------------------------------------------------------|---------------|
| semesterId    | String   |                                      |            | Không    | (Chỉ dùng khi cập nhật)                                                                                              |               |
| facilityId    | String   |                                      |            | Không    |                                                                                                                     |               |
| semesterName  | String   | @NotBlank, @Size(min=2, max=255)     | 2-255      | Có       | - Không trùng tên học kỳ cùng năm (active) <br> - Đúng enum tên học kỳ <br> - Không trùng thời gian với học kỳ khác | - "Tên học kỳ không được để trống" <br> - "Tên học kỳ không được quá:255" <br> - "Học kỳ đã tồn tại" <br> - "Đã có học kỳ trong khoảng thời gian này!" <br> - "Tên học kỳ không hợp lệ" |
| fromDate      | Long     | @NotNull                             |            | Có       | - Không ở quá khứ/hiện tại (tạo mới) <br> - Tối thiểu 3 tháng <br> - Cùng năm với toDate                            | - "Thời gian không được để trống" <br> - "Ngày bắt đầu học kỳ không thể là ngày quá khứ hoặc hiện tại" <br> - "Khoảng thời gian học kỳ phải tối thiểu 3 tháng" <br> - "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm" |
| toDate        | Long     | @NotNull                             |            | Có       | - Tối thiểu 3 tháng <br> - Cùng năm với fromDate                                                                     | - "Thời gian không được để trống" <br> - "Khoảng thời gian học kỳ phải tối thiểu 3 tháng" <br> - "Thời gian bắt đầu và kết thúc của học kỳ phải cùng 1 năm" |

**Lưu ý:**
- Không thể sửa học kỳ đã kết thúc: "Không thể sửa học kỳ đã kết thúc"
- Nếu fromDate đã qua, không cho sửa fromDate.

### 2. Tìm kiếm học kỳ
**API:** `GET /api/admin/semester-management`

| Trường            | Kiểu         | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------------|--------------|------------------------------|------------|----------|-------------------------------|---------------|
| semesterCode      | String       | @Size(max=255)               | <= 255     | Không    |                               | "Từ khóa không được quá:255" |
| fromDateSemester  | Long         |                              |            | Không    |                               |               |
| toDateSemester    | Long         |                              |            | Không    |                               |               |
| status            | EntityStatus |                              |            | Không    |                               |               | 

## Quản lý Bộ Môn (admin/subject)

### 1. Thêm/Cập nhật bộ môn
**API:** `POST /api/admin/subject-management`<br>**API:** `PUT /api/admin/subject-management/{id}`

| Trường   | Kiểu   | Validation annotation            | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                 | Thông báo lỗi |
|----------|--------|----------------------------------|------------|----------|-----------------------------------------------------------------------------------------------|---------------|
| name     | String | @NotBlank, @Size(min=2, max=255) | 2-255      | Có       | - Không trùng tên bộ môn (kiểm tra DB)                                                        | - "Tên bộ môn không được bỏ trống" <br> - "Tên bộ môn không được vượt quá 255 ký tự" <br> - "Tên bộ môn đã tồn tại trên hệ thống" |
| code     | String | @NotBlank, @Size(max=50)         | <= 50      | Có       | - Không trùng mã bộ môn (kiểm tra DB) <br> - Đúng định dạng code (không khoảng trắng, ký tự đặc biệt ngoài . và _) | - "Mã bộ môn không được bỏ trống" <br> - "Mã bộ môn không được vượt quá 50 ký tự" <br> - "Mã bộ môn không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." <br> - "Mã bộ môn đã tồn tại trên hệ thống" |

### 2. Tìm kiếm bộ môn
**API:** `GET /api/admin/subject-management/list`

| Trường   | Kiểu   | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------|--------|------------------------------|------------|----------|-------------------------------|---------------|
| name     | String | @Size(max=255)               | <= 255     | Không    |                               | "Từ khóa không được quá:255" |
| status   | Int    |                              |            | Không    |                               |               | 

## Quản lý Bộ Môn Cơ Sở (admin/subjectfacility)

### 1. Thêm/Cập nhật bộ môn cơ sở
**API:** `POST /api/admin/subject-facility-management`<br>**API:** `PUT /api/admin/subject-facility-management/{id}`

| Trường      | Kiểu   | Validation annotation | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                 | Thông báo lỗi |
|-------------|--------|----------------------|----------|-----------------------------------------------------------------------------------------------|---------------|
| facilityId  | String |                      | Có       | - Phải là cơ sở hợp lệ, đang hoạt động                                                        | "Không tìm thấy cơ sở" |
| subjectId   | String |                      | Có       | - Phải là bộ môn hợp lệ, đang hoạt động                                                       | "Không tìm thấy bộ môn" |
| status      | String |                      | Không    | (Chỉ khi cập nhật)                                                                            |               |
| (cặp facilityId + subjectId) |        |                      | Có       | - Không trùng bộ môn-cơ sở (không được phép tạo/cập nhật trùng)                               | "Bộ môn đã tồn tại trong cơ sở này" |

- Nếu không tìm thấy bộ môn cơ sở khi cập nhật/xoá: "Không tìm thấy bộ môn cơ sở"

### 2. Tìm kiếm bộ môn cơ sở
**API:** `POST /api/admin/subject-facility-management/list`

| Trường      | Kiểu   | Validation annotation | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|--------|----------------------|----------|-------------------------------|---------------|
| name        | String |                      | Không    |                               |               |
| facilityId  | String |                      | Không    |                               |               |
| subjectId   | String |                      | Không    |                               |               |
| status      | Int    |                      | Không    |                               |               | 

## Quản lý Tài Khoản Admin (admin/useradmin)

### 1. Thêm/Cập nhật tài khoản admin
**API:** `POST /api/admin/admin-management`<br>**API:** `PUT /api/admin/admin-management/{id}`

| Trường      | Kiểu   | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                 | Thông báo lỗi |
|-------------|--------|--------------------------------------|------------|----------|-----------------------------------------------------------------------------------------------|---------------|
| staffCode   | String | @NotBlank, @Size(max=50)             | <= 50      | Có       | - Không trùng mã admin (kiểm tra DB) <br> - Đúng định dạng code (không khoảng trắng, ký tự đặc biệt ngoài . và _) | - "Mã admin tạo không được bỏ trống" <br> - "Mã admin chỉ được tối đa 50 ký tự" <br> - "Mã admin không hợp lệ: không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." <br> - "Mã của admin đã tồn tại" <br> - "Mã admin đã tồn tại" |
| email       | String | @NotBlank, @Size(min=2, max=255)     | 2-255      | Có       | - Không trùng email (kiểm tra DB) <br> - Đúng định dạng email (gmail.com, fe.edu.vn, fpt.edu.vn) | - "Không được để trống email admin" <br> - "Email chỉ được tối đa 255 ký tự" <br> - "Email phải có định dạng @gmail.com hoặc kết thúc bằng edu.vn" <br> - "Email của admin đã tồn tại" <br> - "Đã có admin khác dùng email fe này" |
| staffName   | String | @NotBlank, @Size(min=2, max=255)     | 2-255      | Có       | - Đúng định dạng tên (tối thiểu 2 từ, chỉ ký tự chữ, không số/ký tự đặc biệt)                  | - "Không được để trống tên admin" <br> - "Tên admin chỉ được tối đa 255 ký tự" <br> - "Tên admin không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt." |

- Không được sửa trạng thái/xoá chính mình: "Không được sửa trạng thái của chính bản thân", "Không thể xóa tài khoản của chính mình"
- Nếu không tìm thấy nhân viên: "Không tìm thấy nhân viên"

### 2. Tìm kiếm tài khoản admin
**API:** `GET /api/admin/admin-management`

| Trường        | Kiểu   | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|---------------|--------|------------------------------|------------|----------|-------------------------------|---------------|
| searchQuery   | String | @Size(max=255)               | <= 255     | Không    |                               | "Từ khóa không được quá:255" |
| status        | Int    |                              |            | Không    |                               |               | 

## Quản lý Nhân Viên (admin/userstaff)

### 1. Thêm/Cập nhật nhân viên
**API:** `POST /api/admin/staff-management`<br>**API:** `PUT /api/admin/staff-management/{id}`

| Trường      | Kiểu         | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                 | Thông báo lỗi |
|-------------|--------------|------------------------------|------------|----------|-----------------------------------------------------------------------------------------------|---------------|
| name        | String       | @NotBlank, @Length(max=255)  | <= 255     | Có       | - Đúng định dạng tên (tối thiểu 2 từ, chỉ ký tự chữ, không số/ký tự đặc biệt)                | - "Tên không được để trống" <br> - "Tên chỉ được tối đa 255 ký tự" <br> - "Tên nhân viên không hợp lệ: Tối thiểu 2 từ, cách nhau bởi khoảng trắng và Chỉ gồm ký tự chữ không chứa số hay ký tự đặc biệt." |
| staffCode   | String       | @NotBlank, @Length(max=50)   | <= 50      | Có       | - Không trùng mã nhân viên (kiểm tra DB) <br> - Đúng định dạng code (không khoảng trắng, ký tự đặc biệt ngoài . và _) | - "Mã nhân viên không được để trống" <br> - "Mã nhân viên chỉ được tối đa 50 ký tự" <br> - "Mã nhân viên không hợp lệ: Không có khoảng trắng, không có ký tự đặc biệt ngoài dấu chấm . và dấu gạch dưới _." <br> - "Mã nhân viên đã tồn tại" |
| emailFe     | String       | @NotBlank, @Length(max=255)  | <= 255     | Có       | - Không trùng email FE (kiểm tra DB) <br> - Đúng định dạng email FE (không khoảng trắng, kết thúc @fe.edu.vn) | - "Tài khoản FE không được để trống" <br> - "Tài khoản FE chỉ được tối đa 255 ký tự" <br> - "Email FE không được chứa khoảng trắng và phải kết thúc bằng @fe.edu.vn" <br> - "Đã có nhân viên khác dùng email fe này" |
| emailFpt    | String       | @NotBlank, @Length(max=255)  | <= 255     | Có       | - Không trùng email FPT (kiểm tra DB) <br> - Đúng định dạng email FPT (không khoảng trắng, kết thúc @fpt.edu.vn) | - "Tài khoản FPT không được để trống" <br> - "Tài khoản FPT chỉ được tối đa 255 ký tự" <br> - "Email FPT không được chứa khoảng trắng và phải kết thúc bằng @fpt.edu.vn" <br> - "Đã có nhân viên khác dùng email fpt này" |
| facilityId  | String       |                              |            | Có       | - Phải là cơ sở hợp lệ (kiểm tra DB)                                                        | - "Cơ sở không tồn tại" |
| roleCodes   | List<String> |                              |            | Có       | - Phải chọn ít nhất một vai trò <br> - Vai trò hợp lệ                                        | - "Phải chọn ít nhất một vai trò" <br> - "Vai trò không hợp lệ: ..." |

- Nếu không tìm thấy nhân viên: "Không tìm thấy nhân viên"

### 2. Tìm kiếm nhân viên
**API:** `GET /api/admin/staff-management`

| Trường        | Kiểu         | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|---------------|--------------|------------------------------|------------|----------|-------------------------------|---------------|
| searchQuery   | String       | @Size(max=255)               | <= 255     | Không    |                               | "Từ khóa không được quá:255" |
| idFacility    | String       |                              |            | Không    |                               |               |
| status        | EntityStatus |                              |            | Không    |                               |               |
| roleCodeFilter| Integer      |                              |            | Không    |                               |               | 

## Quản lý Cơ Sở (admin/facility)

### 1. Thêm/Cập nhật cơ sở
**API:** `POST /api/admin/facility-management`<br>**API:** `PUT /api/admin/facility-management/{id}`

| Trường        | Kiểu   | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ                                                                 | Thông báo lỗi |
|---------------|--------|--------------------------------------|------------|----------|-----------------------------------------------------------------------------------------------|---------------|
| facilityName  | String | @NotBlank, @Size(min=3), @Size(max=50) | 3-50     | Có       | - Không trùng tên cơ sở (kiểm tra DB, cả khi cập nhật)                                        | - "Tên cơ sở không được để trống" <br> - "Tên cơ sở phải có ít nhất 3 kí tự" <br> - "Tên cơ sở chỉ có thể có tối đa 50 kí tự" <br> - "Tên cơ sở đã tồn tại trên hệ thống" |

- Nếu không tìm thấy cơ sở khi cập nhật/xoá: "Không tìm thấy cơ sở"

### 2. Tìm kiếm cơ sở
**API:** `GET /api/admin/facility-management`

| Trường   | Kiểu         | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|----------|--------------|------------------------------|------------|----------|-------------------------------|---------------|
| name     | String       | @Size(max=50)                | <= 50      | Không    |                               | "Từ khóa không được vượt quá 50 ký tự" |
| status   | EntityStatus |                              |            | Không    |                               |               | 

## Quản lý Địa Điểm Cơ Sở (admin/facility/location)

### 1. Thêm/Cập nhật địa điểm
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| name        | String  | @NotBlank, @Size(min=2, max=255)    | 2-255      | Có       |                               | - "Vui lòng nhập tên địa điểm" <br> - "Tên địa điểm không được vượt quá 255 ký tự" |
| latitude    | Double  | @DecimalMin(-90), @DecimalMax(90)   |            | Có       |                               | - "Vĩ độ không được nhỏ hơn -90" <br> - "Vĩ độ không được lớn hơn 90" |
| longitude   | Double  | @DecimalMin(-180), @DecimalMax(180) |            | Có       |                               | - "Kinh độ không được nhỏ hơn -180" <br> - "Kinh độ không được lớn hơn 180" |
| radius      | Int     | @Min(1), @Max(500)                  | 1-500      | Có       |                               | - "Bán kính tối thiểu là 1m" <br> - "Bán kính tối đa là 500m" |

### 2. Tìm kiếm địa điểm
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| keyword     | String  | @Size(max=255)               | <= 255     | Không    |                               | "Keyword không được vượt quá 255 ký tự" |
| idFacility  | String  |                              |            | Không    |                               |               |
| status      | Int     |                              |            | Không    |                               |               | 

## Quản lý IP/DNS Suffix Cơ Sở (admin/facility/ip)

### 1. Thêm/Cập nhật IP/DNS Suffix
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| ip          | String  | @NotBlank, @Size(min=2, max=255)     | 2-255      | Có       | - Đúng định dạng IP hoặc DNS Suffix (logic service) <br> - Không trùng IP/DNS Suffix trong cùng cơ sở | - "Vui lòng điền đầy đủ mục yêu cầu" <br> - "IP không được vượt quá 255 ký tự" <br> - "IP/DNS Suffix đã tồn tại trong cơ sở này" <br> - "IP/DNS Suffix không hợp lệ" |
| type        | Int     |                                      |            | Có       | - Phân biệt loại: IPv4/IPv6/DNS Suffix           |               |
| idFacility  | String  |                                      | Có         | - Phải là cơ sở hợp lệ (kiểm tra DB)            | - "Cơ sở không tồn tại" |

### 2. Tìm kiếm IP/DNS Suffix
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| keyword     | String  | @Size(max=255)               | <= 255     | Không    |                               | "Keyword không được vượt quá 255 ký tự" |
| idFacility  | String  |                              |            | Không    |                               |               |
| status      | Int     |                              |            | Không    |                               |               |
| type        | Int     |                              |            | Không    |                               |               |

## Quản lý Ca Học Cơ Sở (admin/facility/shift)

### 1. Thêm/Cập nhật ca học
| Trường      | Kiểu    | Validation annotation                | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|--------------------------------------|------------|----------|-------------------------------|---------------|
| shift       | Int     | @Min(1), @Max(6)                     | 1-6        | Có       |                               | - "Ca học sớm nhất là ca 1" <br> - "Ca học muộn nhất là ca 6" |
| fromHour    | Int     | @Min(0), @Max(23)                    | 0-23       | Có       |                               | - "Giờ bắt đầu không hợp lệ" |
| fromMinute  | Int     | @Min(0), @Max(59)                    | 0-59       | Có       |                               | - "Phút bắt đầu không hợp lệ" |
| toHour      | Int     | @Min(0), @Max(23)                    | 0-23       | Có       |                               | - "Giờ kết thúc không hợp lệ" |
| toMinute    | Int     | @Min(0), @Max(59)                    | 0-59       | Có       |                               | - "Phút kết thúc không hợp lệ" |
| idFacility  | String  |                                      | Có         | - Phải là cơ sở hợp lệ (kiểm tra DB) | - "Cơ sở không tồn tại" |

### 2. Tìm kiếm ca học
| Trường      | Kiểu    | Validation annotation         | Biên ký tự | Bắt buộc | Kiểm tra trùng/logic nghiệp vụ | Thông báo lỗi |
|-------------|---------|------------------------------|------------|----------|-------------------------------|---------------|
| idFacility  | String  |                              |            | Không    |                               |               |
| shift       | Int     |                              |            | Không    |                               |               |
| status      | Int     |                              |            | Không    |                               |               |