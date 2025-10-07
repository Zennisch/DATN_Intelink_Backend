# 2. Đặc tả Class

## 2.1. Thực thể User

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | Long | Khóa chính, tự động tăng, không null, duy nhất | ID định danh người dùng |
| 2 | username | String | 3-16 ký tự, không null, duy nhất | Tên đăng nhập |
| 3 | email | String | Email hợp lệ, không null, duy nhất, tối đa 128 ký tự | Địa chỉ email |
| 4 | passwordHash | String | Tối đa 255 ký tự, có thể null | Mật khẩu đã mã hóa |
| 5 | emailVerified | Boolean | Không null, mặc định false | Trạng thái xác thực email |
| 6 | lastLoginAt | Instant | Có thể null | Thời điểm đăng nhập cuối |
| 7 | role | UserRole | Không null, mặc định USER | Vai trò người dùng |
| 8 | provider | UserProvider | Không null, mặc định LOCAL | Nhà cung cấp xác thực |
| 9 | providerUserId | String | Tối đa 128 ký tự, có thể null | ID từ nhà cung cấp bên ngoài |
| 10 | displayName | String | Tối đa 64 ký tự, có thể null | Tên hiển thị |
| 11 | bio | String | Tối đa 512 ký tự, có thể null | Tiểu sử người dùng |
| 12 | profilePictureUrl | String | Tối đa 512 ký tự, có thể null | URL ảnh đại diện |
| 13 | totalShortUrls | Integer | Không null, mặc định 0 | Tổng số URL rút gọn |
| 14 | totalClicks | Long | Không null, mặc định 0 | Tổng số lượt click |
| 15 | status | UserStatus | Không null, mặc định ACTIVE | Trạng thái tài khoản |
| 16 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 17 | updatedAt | Instant | Không null | Thời điểm cập nhật cuối |

## 2.2. Thực thể OAuthAccount

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh tài khoản OAuth |
| 2 | user | User | ManyToOne, không null | Người dùng sở hữu |
| 3 | provider | UserProvider | Không null | Nhà cung cấp OAuth |
| 4 | providerUserId | String | Không null | ID người dùng từ nhà cung cấp |
| 5 | providerUsername | String | Có thể null | Tên đăng nhập từ nhà cung cấp |
| 6 | providerEmail | String | Có thể null | Email từ nhà cung cấp |
| 7 | accessToken | String | Có thể null, kiểu TEXT | Token truy cập |
| 8 | refreshToken | String | Có thể null, kiểu TEXT | Token làm mới |
| 9 | tokenExpiresAt | Instant | Có thể null | Thời điểm hết hạn token |
| 10 | createdAt | Instant | Không null | Thời điểm tạo |
| 11 | updatedAt | Instant | Không null | Thời điểm cập nhật cuối |

## 2.3. Thực thể VerificationToken

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh token |
| 2 | user | User | ManyToOne, không null | Người dùng sở hữu |
| 3 | token | String | Không null, duy nhất | Mã token |
| 4 | type | VerificationTokenType | Không null | Loại token xác thực |
| 5 | used | Boolean | Không null, mặc định false | Trạng thái đã sử dụng |
| 6 | expiresAt | Instant | Không null | Thời điểm hết hạn |
| 7 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |

## 2.4. Thực thể ShortUrl

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | Long | Khóa chính, tự động tăng, không null, duy nhất | ID định danh URL rút gọn |
| 2 | user | User | ManyToOne, không null | Người dùng sở hữu |
| 3 | customDomain | CustomDomain | ManyToOne, có thể null | Tên miền tùy chỉnh |
| 4 | shortCode | String | 6-32 ký tự, không null, duy nhất | Mã rút gọn |
| 5 | originalUrl | String | Không null | URL gốc |
| 6 | passwordHash | String | Tối đa 255 ký tự, có thể null | Mật khẩu bảo vệ đã mã hóa |
| 7 | alias | String | Tối đa 64 ký tự, có thể null | Tên gọi khác |
| 8 | description | String | Tối đa 255 ký tự, có thể null | Mô tả URL |
| 9 | status | ShortUrlStatus | Không null, mặc định ENABLED | Trạng thái URL |
| 10 | maxUsage | Long | Có thể null, mặc định 0 | Giới hạn số lần sử dụng |
| 11 | totalClicks | Long | Không null, mặc định 0 | Tổng số lượt click |
| 12 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 13 | updatedAt | Instant | Không null | Thời điểm cập nhật cuối |
| 14 | expiresAt | Instant | Có thể null | Thời điểm hết hạn |
| 15 | deletedAt | Instant | Có thể null | Thời điểm xóa (soft delete) |

## 2.5. Thực thể ClickLog

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh log click |
| 2 | shortUrl | ShortUrl | ManyToOne, không null | URL rút gọn được click |
| 3 | ipVersion | IpVersion | Không null | Phiên bản IP |
| 4 | ipAddress | String | Không null | Địa chỉ IP |
| 5 | userAgent | String | Có thể null, kiểu TEXT | Thông tin trình duyệt |
| 6 | referrer | String | Có thể null, kiểu TEXT | Trang web giới thiệu |
| 7 | timestamp | Instant | Không null | Thời điểm click |

## 2.6. Thực thể ClickStat

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | Long | Khóa chính, tự động tăng, không null, duy nhất | ID định danh thống kê |
| 2 | shortUrl | ShortUrl | ManyToOne, không null | URL rút gọn |
| 3 | granularity | Granularity | Không null | Mức độ chi tiết thống kê |
| 4 | bucket | Instant | Không null | Khoảng thời gian thống kê |
| 5 | totalClicks | Long | Không null, mặc định 0 | Tổng số click trong khoảng |

## 2.7. Thực thể DimensionStat

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh thống kê chiều |
| 2 | shortUrl | ShortUrl | ManyToOne, không null | URL rút gọn |
| 3 | type | DimensionType | Không null | Loại chiều thống kê |
| 4 | value | String | Không null | Giá trị của chiều |
| 5 | totalClicks | Long | Không null, mặc định 0 | Tổng số click theo chiều |

## 2.8. Thực thể ShortUrlAnalysisResult

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh kết quả phân tích |
| 2 | shortUrl | ShortUrl | ManyToOne, không null | URL rút gọn được phân tích |
| 3 | status | ShortUrlAnalysisStatus | Không null | Trạng thái phân tích |
| 4 | engine | ShortUrlAnalysisEngine | Không null | Công cụ phân tích |
| 5 | threatType | String | Không null | Loại mối đe dọa |
| 6 | platformType | String | Không null | Loại nền tảng |
| 7 | cacheDuration | String | Có thể null | Thời gian cache |
| 8 | details | String | Có thể null, kiểu TEXT | Chi tiết kết quả |
| 9 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |

## 2.9. Thực thể SubscriptionPlan

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | Long | Khóa chính, tự động tăng, không null, duy nhất | ID định danh gói dịch vụ |
| 2 | type | SubscriptionPlanType | Không null, duy nhất, tối đa 32 ký tự | Loại gói dịch vụ |
| 3 | description | String | Có thể null, tối đa 1024 ký tự | Mô tả gói dịch vụ |
| 4 | price | BigDecimal | Không null, ≥ 0.00, 10 chữ số, 2 số thập phân | Giá gói dịch vụ |
| 5 | billingInterval | SubscriptionPlanBillingInterval | Không null | Chu kỳ thanh toán |
| 6 | maxShortUrls | Integer | Không null | Số URL rút gọn tối đa |
| 7 | shortCodeCustomizationEnabled | Boolean | Không null | Cho phép tùy chỉnh mã rút gọn |
| 8 | statisticsEnabled | Boolean | Không null | Cho phép thống kê |
| 9 | customDomainEnabled | Boolean | Không null | Cho phép tên miền tùy chỉnh |
| 10 | apiAccessEnabled | Boolean | Không null | Cho phép truy cập API |
| 11 | active | Boolean | Không null, mặc định true | Trạng thái hoạt động |
| 12 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 13 | maxUsagePerUrl | Integer | Không null | Số lần sử dụng tối đa mỗi URL |

## 2.10. Thực thể Subscription

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh đăng ký |
| 2 | user | User | ManyToOne, không null | Người dùng đăng ký |
| 3 | subscriptionPlan | SubscriptionPlan | ManyToOne, không null | Gói dịch vụ đăng ký |
| 4 | status | SubscriptionStatus | Không null | Trạng thái đăng ký |
| 5 | active | Boolean | Không null | Trạng thái hoạt động |
| 6 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 7 | startsAt | Instant | Không null | Thời điểm bắt đầu |
| 8 | expiresAt | Instant | Có thể null | Thời điểm hết hạn |

## 2.11. Thực thể Payment

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh thanh toán |
| 2 | subscription | Subscription | OneToOne, không null, duy nhất | Đăng ký dịch vụ |
| 3 | provider | PaymentProvider | Không null | Nhà cung cấp thanh toán |
| 4 | status | PaymentStatus | Không null, mặc định PENDING | Trạng thái thanh toán |
| 5 | amount | BigDecimal | Không null, > 0.0 | Số tiền thanh toán |
| 6 | currency | String | Không null, đúng 3 ký tự, mã ISO 4217 | Đơn vị tiền tệ |
| 7 | transactionId | String | Có thể null, duy nhất | Mã giao dịch |
| 8 | metadata | String | Có thể null, kiểu TEXT | Thông tin bổ sung |
| 9 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 10 | updatedAt | Instant | Có thể null | Thời điểm cập nhật cuối |
| 11 | processedAt | Instant | Có thể null | Thời điểm xử lý |
| 12 | expiresAt | Instant | Có thể null | Thời điểm hết hạn |

## 2.12. Thực thể ApiKey

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh API key |
| 2 | user | User | ManyToOne, không null | Người dùng sở hữu |
| 3 | name | String | Không null, tối đa 128 ký tự | Tên API key |
| 4 | keyHash | String | Không null, duy nhất, tối đa 255 ký tự | Mã băm của key |
| 5 | keyPrefix | String | Không null, tối đa 20 ký tự | Tiền tố của key |
| 6 | rateLimitPerHour | Integer | Không null | Giới hạn tốc độ mỗi giờ |
| 7 | active | Boolean | Không null | Trạng thái hoạt động |
| 8 | expiresAt | Instant | Có thể null | Thời điểm hết hạn |
| 9 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 10 | updatedAt | Instant | Không null | Thời điểm cập nhật cuối |
| 11 | lastUsedAt | Instant | Có thể null | Lần sử dụng cuối |

## 2.13. Thực thể CustomDomain

| STT | Tên thuộc tính | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|----------------|--------------|-----------|---------|
| 1 | id | UUID | Khóa chính, tự động tạo, không null, duy nhất | ID định danh tên miền |
| 2 | user | User | ManyToOne, không null | Người dùng sở hữu |
| 3 | domain | String | Không null, duy nhất, tối đa 255 ký tự | Tên miền chính |
| 4 | subdomain | String | Có thể null, tối đa 100 ký tự | Tên miền phụ |
| 5 | status | CustomDomainStatus | Không null, mặc định PENDING_VERIFICATION | Trạng thái tên miền |
| 6 | verified | Boolean | Không null | Trạng thái xác minh |
| 7 | verificationMethod | CustomDomainVerificationMethod | Không null, mặc định TXT_RECORD | Phương thức xác minh |
| 8 | sslEnabled | Boolean | Không null, mặc định false | Trạng thái SSL |
| 9 | verificationToken | String | Không null | Token xác minh |
| 10 | active | Boolean | Không null, mặc định true | Trạng thái hoạt động |
| 11 | createdAt | Instant | Không null, không cập nhật | Thời điểm tạo |
| 12 | updatedAt | Instant | Không null | Thời điểm cập nhật cuối |

## Định nghĩa Enum

### UserRole
- **ADMIN**: Quản trị viên hệ thống
- **USER**: Người dùng thông thường
- **GUEST**: Khách

### UserProvider
- **GOOGLE**: Xác thực qua Google
- **GITHUB**: Xác thực qua GitHub
- **LOCAL**: Xác thực nội bộ

### UserStatus
- **ACTIVE**: Tài khoản hoạt động
- **INACTIVE**: Tài khoản không hoạt động
- **BANNED**: Tài khoản bị cấm

### VerificationTokenType
- **EMAIL_VERIFICATION**: Xác thực email
- **PASSWORD_RESET**: Đặt lại mật khẩu
- **OAUTH_STATE**: Trạng thái OAuth

### ShortUrlStatus
- **ENABLED**: URL hoạt động
- **DISABLED**: URL bị vô hiệu hóa

### IpVersion
- **IPv4**: Địa chỉ IP phiên bản 4
- **IPv6**: Địa chỉ IP phiên bản 6
- **UNKNOWN**: Không xác định được phiên bản

### Granularity
- **HOURLY**: Thống kê theo giờ
- **DAILY**: Thống kê theo ngày
- **MONTHLY**: Thống kê theo tháng
- **YEARLY**: Thống kê theo năm

### DimensionType
- **REFERRER**: Trang web giới thiệu
- **REFERRER_TYPE**: Loại nguồn giới thiệu
- **UTM_SOURCE**: Nguồn UTM
- **UTM_MEDIUM**: Phương tiện UTM
- **UTM_CAMPAIGN**: Chiến dịch UTM
- **UTM_TERM**: Từ khóa UTM
- **UTM_CONTENT**: Nội dung UTM
- **COUNTRY**: Quốc gia
- **REGION**: Vùng/Khu vực
- **CITY**: Thành phố
- **TIMEZONE**: Múi giờ
- **BROWSER**: Trình duyệt
- **OS**: Hệ điều hành
- **DEVICE_TYPE**: Loại thiết bị
- **ISP**: Nhà cung cấp Internet
- **LANGUAGE**: Ngôn ngữ
- **CUSTOM**: Chiều tùy chỉnh

### ShortUrlAnalysisStatus
- **PENDING**: Đang chờ phân tích
- **SAFE**: An toàn
- **MALICIOUS**: Độc hại
- **SUSPICIOUS**: Khả nghi
- **MALWARE**: Phần mềm độc hại
- **SOCIAL_ENGINEERING**: Kỹ thuật xã hội
- **UNKNOWN**: Không xác định

### ShortUrlAnalysisEngine
- **GOOGLE_SAFE_BROWSING**: Google Safe Browsing
- **VIRUSTOTAL**: VirusTotal
- **PHISHING_DETECTOR**: Trình phát hiện lừa đảo

### SubscriptionPlanType
- **FREE**: Miễn phí
- **PRO**: Chuyên nghiệp
- **ENTERPRISE**: Doanh nghiệp

### SubscriptionPlanBillingInterval
- **MONTHLY**: Hàng tháng
- **YEARLY**: Hàng năm
- **NONE**: Không có

### SubscriptionStatus
- **ACTIVE**: Đang hoạt động
- **TRIALING**: Đang dùng thử
- **PAST_DUE**: Quá hạn
- **CANCELED**: Đã hủy
- **EXPIRED**: Đã hết hạn

### PaymentProvider
- **CREDIT_CARD**: Thẻ tín dụng
- **PAYPAL**: PayPal
- **BANK_TRANSFER**: Chuyển khoản ngân hàng
- **VNPAY**: VNPay
- **MOMO**: MoMo
- **ZALOPAY**: ZaloPay

### PaymentStatus
- **PENDING**: Đang chờ
- **COMPLETED**: Hoàn thành
- **FAILED**: Thất bại
- **CANCELLED**: Đã hủy
- **REFUNDED**: Đã hoàn tiền

### CustomDomainStatus
- **PENDING_VERIFICATION**: Chờ xác minh
- **VERIFIED**: Đã xác minh
- **FAILED_VERIFICATION**: Xác minh thất bại
- **SUSPENDED**: Bị tạm ngưng
- **DELETED**: Đã xóa

### CustomDomainVerificationMethod
- **TXT_RECORD**: Bản ghi TXT
- **CNAME_RECORD**: Bản ghi CNAME
- **HTML_FILE**: Tệp HTML