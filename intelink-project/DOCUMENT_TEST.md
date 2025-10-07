# 2. Danh sách các tình huống để kiểm tra ứng dụng

## 2.1. Authentication Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| AUTH_REGISTER_001 | Register | Đăng ký tài khoản thành công với thông tin hợp lệ | Không có user với username và email này | username: "testuser", email: "test@example.com", password: "Password123!" | Status: 200, success: true, message chứa "Registration successful", email verification token được gửi | Happy path |
| AUTH_REGISTER_002 | Register | Đăng ký thất bại với username đã tồn tại | Đã có user với username "existinguser" | username: "existinguser", email: "new@example.com", password: "Password123!" | Status: 400, error message: "Username already exists" | Edge case |
| AUTH_REGISTER_003 | Register | Đăng ký thất bại với email đã tồn tại | Đã có user với email "existing@example.com" | username: "newuser", email: "existing@example.com", password: "Password123!" | Status: 400, error message: "Email already exists" | Edge case |
| AUTH_REGISTER_004 | Register | Đăng ký thất bại với dữ liệu không hợp lệ | Không có user nào | username: "ab", email: "invalid-email", password: "123" | Status: 400, validation errors cho từng field | Validation test |
| AUTH_REGISTER_005 | Register | Đăng ký thất bại với trường bắt buộc trống | Không có user nào | username: "", email: "", password: "" | Status: 400, validation errors cho required fields | Validation test |
| AUTH_VERIFY_001 | Verify Email | Xác thực email thành công với token hợp lệ | User chưa verify email, có valid token | token: "valid_email_verification_token" | Status: 200, success: true, message: "Email verified successfully", user.emailVerified = true | Happy path |
| AUTH_VERIFY_002 | Verify Email | Xác thực email thất bại với token không hợp lệ | User chưa verify email | token: "invalid_token" | Status: 400, error message: "Invalid or expired verification token" | Edge case |
| AUTH_VERIFY_003 | Verify Email | Xác thực email thất bại với token đã sử dụng | User đã verify email, token đã used | token: "used_token" | Status: 400, error message: "Invalid or expired verification token" | Edge case |
| AUTH_VERIFY_004 | Verify Email | Xác thực email thất bại với token hết hạn | User chưa verify email, token expired | token: "expired_token" | Status: 400, error message: "Invalid or expired verification token" | Edge case |
| AUTH_FORGOT_001 | Forgot Password | Gửi email reset password thành công với email tồn tại | User tồn tại với email này | email: "existing@example.com" | Status: 200, success: true, password reset email được gửi | Happy path |
| AUTH_FORGOT_002 | Forgot Password | Gửi email reset password với email không tồn tại | Email không tồn tại trong hệ thống | email: "nonexistent@example.com" | Status: 200, success: true, message tương tự nhưng không gửi email | Security feature |
| AUTH_FORGOT_003 | Forgot Password | Gửi email reset password thất bại với email không hợp lệ | Không có điều kiện trước | email: "invalid-email-format" | Status: 400, validation error | Validation test |
| AUTH_RESET_001 | Reset Password | Đặt lại mật khẩu thành công với token hợp lệ | User có valid password reset token | token: "valid_reset_token", password: "NewPassword123!", confirmPassword: "NewPassword123!" | Status: 200, success: true, message: "Password reset successfully", password được cập nhật | Happy path |
| AUTH_RESET_002 | Reset Password | Đặt lại mật khẩu thất bại với token không hợp lệ | User không có valid token | token: "invalid_token", password: "NewPassword123!", confirmPassword: "NewPassword123!" | Status: 400, error message: "Invalid or expired password reset token" | Edge case |
| AUTH_RESET_003 | Reset Password | Đặt lại mật khẩu thất bại với mật khẩu không khớp | User có valid token | token: "valid_reset_token", password: "NewPassword123!", confirmPassword: "DifferentPassword123!" | Status: 400, error message: "New password and confirmation do not match" | Edge case |
| AUTH_RESET_004 | Reset Password | Đặt lại mật khẩu thất bại với token hết hạn | User có expired token | token: "expired_token", password: "NewPassword123!", confirmPassword: "NewPassword123!" | Status: 400, error message: "Invalid or expired password reset token" | Edge case |
| AUTH_RESET_005 | Reset Password | Đặt lại mật khẩu thất bại với mật khẩu yếu | User có valid token | token: "valid_reset_token", password: "123", confirmPassword: "123" | Status: 400, validation error cho password strength | Validation test |
| AUTH_LOGIN_001 | Login | Đăng nhập thành công với thông tin hợp lệ | User đã verify email | username: "validuser", password: "correctpassword" | Status: 200, AuthTokenResponse với token và refresh token, lastLoginAt được cập nhật | Happy path |
| AUTH_LOGIN_002 | Login | Đăng nhập thất bại với username không tồn tại | Không có user với username này | username: "nonexistentuser", password: "anypassword" | Status: 401, error message: "Invalid username or password" | Edge case |
| AUTH_LOGIN_003 | Login | Đăng nhập thất bại với mật khẩu sai | User tồn tại | username: "validuser", password: "wrongpassword" | Status: 401, error message: "Invalid username or password" | Edge case |
| AUTH_LOGIN_004 | Login | Đăng nhập thất bại với email chưa xác thực | User LOCAL provider chưa verify email | username: "unverifieduser", password: "correctpassword" | Status: 401, error message: "Please verify your email before logging in" | Edge case |
| AUTH_LOGIN_005 | Login | Đăng nhập thành công với user OAuth đã verify | User GOOGLE/GITHUB provider | username: "oauthuser", password: "correctpassword" | Status: 200, AuthTokenResponse với token | OAuth user không cần verify email |
| AUTH_LOGIN_006 | Login | Đăng nhập thất bại với dữ liệu trống | Không có điều kiện trước | username: "", password: "" | Status: 400, validation errors | Validation test |
| AUTH_REFRESH_001 | Refresh Token | Làm mới token thành công với user hợp lệ | User đã đăng nhập, có valid JWT token | Authorization header với valid token | Status: 200, AuthTokenResponse với token và refresh token mới | Happy path |
| AUTH_REFRESH_002 | Refresh Token | Làm mới token thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| AUTH_REFRESH_003 | Refresh Token | Làm mới token thất bại với token không hợp lệ | Không có điều kiện trước | Authorization header với invalid token | Status: 401, error message | Edge case |
| AUTH_REFRESH_004 | Refresh Token | Làm mới token thất bại với token hết hạn | User có expired token | Authorization header với expired token | Status: 401, error message | Edge case |
| AUTH_OAUTH_001 | OAuth Callback | OAuth callback thành công với token hợp lệ | OAuth service trả về valid token | token: "valid_oauth_token" | Status: 200, AuthTokenResponse với JWT token | Happy path |
| AUTH_OAUTH_002 | OAuth Callback | OAuth callback thất bại với token không hợp lệ | OAuth service error | token: "invalid_oauth_token" | Status: 400, error message từ OAuth service | Edge case |
| AUTH_OAUTH_003 | OAuth Callback | OAuth callback thất bại không có token | Không có token | Không có token parameter | Status: 400, error message | Edge case |
| AUTH_PROFILE_001 | Get Profile | Lấy thông tin profile thành công | User đã đăng nhập | Authorization header với valid token | Status: 200, UserProfileResponse với thông tin user và subscription | Happy path |
| AUTH_PROFILE_002 | Get Profile | Lấy thông tin profile thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| AUTH_PROFILE_003 | Get Profile | Lấy thông tin profile thất bại với token không hợp lệ | Không có điều kiện trước | Authorization header với invalid token | Status: 401, error message | Edge case |
| AUTH_PROFILE_004 | Get Profile | Lấy thông tin profile với user không tồn tại | Token hợp lệ nhưng user đã bị xóa | Authorization header với valid token của deleted user | Status: 404, error message | Edge case |
| AUTH_LOGOUT_001 | Logout | Đăng xuất thành công | User đã đăng nhập | Authorization header với valid token | Status: 200, success: true, message: "Logged out successfully", SecurityContext được clear | Happy path |
| AUTH_LOGOUT_002 | Logout | Đăng xuất thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| AUTH_LOGOUT_003 | Logout | Đăng xuất thất bại với token không hợp lệ | Không có điều kiện trước | Authorization header với invalid token | Status: 401, error message | Edge case |

## 2.2. Short URL Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| URL_CREATE_001 | Create Short URL | Tạo URL rút gọn thành công với thông tin hợp lệ | User đã đăng nhập | originalUrl: "https://example.com", description: "Test URL", maxUsage: 100, availableDays: 30 | Status: 200, CreateShortUrlResponse với shortCode và shortUrl, threat analysis được thực hiện | Happy path |
| URL_CREATE_002 | Create Short URL | Tạo URL rút gọn thành công với custom code | User đã đăng nhập | originalUrl: "https://example.com", customCode: "mycustom", description: "Custom URL" | Status: 200, shortCode = "mycustom" | Happy path |
| URL_CREATE_003 | Create Short URL | Tạo URL rút gọn thành công với password | User đã đăng nhập | originalUrl: "https://example.com", password: "secret123", description: "Protected URL" | Status: 200, URL được tạo với password protection | Happy path |
| URL_CREATE_004 | Create Short URL | Tạo URL rút gọn thất bại với custom code đã tồn tại | User đã đăng nhập, custom code đã tồn tại | customCode: "existing", originalUrl: "https://example.com" | Status: 400, error message về custom code đã tồn tại | Edge case |
| URL_CREATE_005 | Create Short URL | Tạo URL rút gọn thất bại với custom code không hợp lệ | User đã đăng nhập | customCode: "ab", originalUrl: "https://example.com" | Status: 400, error message: "Custom code must be between 4 and 20 characters" | Validation test |
| URL_CREATE_006 | Create Short URL | Tạo URL rút gọn thất bại với custom code chứa ký tự không hợp lệ | User đã đăng nhập | customCode: "test@123", originalUrl: "https://example.com" | Status: 400, error message về ký tự không hợp lệ | Validation test |
| URL_CREATE_007 | Create Short URL | Tạo URL rút gọn thất bại với URL độc hại | User đã đăng nhập | originalUrl: "https://malicious-site.com" | Status: 400, URL bị xóa sau threat analysis, analysis result được lưu | Security test |
| URL_CREATE_008 | Create Short URL | Tạo URL rút gọn thất bại không có token | User chưa đăng nhập | originalUrl: "https://example.com" | Status: 401, error message | Edge case |
| URL_CREATE_009 | Create Short URL | Tạo URL rút gọn thất bại với URL không hợp lệ | User đã đăng nhập | originalUrl: "invalid-url" | Status: 400, validation error | Validation test |
| URL_LIST_001 | Get User URLs | Lấy danh sách URL thành công với pagination | User đã đăng nhập, có URLs | page: 0, size: 10 | Status: 200, PagedResponse với list URL và pagination info | Happy path |
| URL_LIST_002 | Get User URLs | Lấy danh sách URL trống | User đã đăng nhập, chưa có URLs | page: 0, size: 10 | Status: 200, PagedResponse với empty list | Edge case |
| URL_LIST_003 | Get User URLs | Lấy danh sách URL với pagination parameters | User đã đăng nhập, có nhiều URLs | page: 1, size: 5 | Status: 200, correct pagination data | Happy path |
| URL_LIST_004 | Get User URLs | Lấy danh sách URL thất bại không có token | User chưa đăng nhập | page: 0, size: 10 | Status: 401, error message | Edge case |
| URL_DETAIL_001 | Get URL Detail | Lấy chi tiết URL thành công | User đã đăng nhập, URL tồn tại và thuộc user | shortCode: "abc123" | Status: 200, ShortUrlDetailResponse với đầy đủ thông tin | Happy path |
| URL_DETAIL_002 | Get URL Detail | Lấy chi tiết URL thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_DETAIL_003 | Get URL Detail | Lấy chi tiết URL thất bại không thuộc user | User đã đăng nhập, URL thuộc user khác | shortCode: "other123" | Status: 400, error message: "Short URL not found" | Security test |
| URL_DETAIL_004 | Get URL Detail | Lấy chi tiết URL thất bại không có token | User chưa đăng nhập | shortCode: "abc123" | Status: 401, error message | Edge case |
| URL_UPDATE_001 | Update Short URL | Cập nhật URL thành công | User đã đăng nhập, URL tồn tại | shortCode: "abc123", description: "Updated", maxUsage: 200, availableDays: 60 | Status: 200, UpdateShortUrlResponse success | Happy path |
| URL_UPDATE_002 | Update Short URL | Cập nhật URL thành công với một số field | User đã đăng nhập, URL tồn tại | shortCode: "abc123", description: "Updated only" | Status: 200, chỉ description được cập nhật | Happy path |
| URL_UPDATE_003 | Update Short URL | Cập nhật URL thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist", description: "Updated" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_UPDATE_004 | Update Short URL | Cập nhật URL thất bại không thuộc user | User đã đăng nhập, URL thuộc user khác | shortCode: "other123", description: "Updated" | Status: 400, error message | Security test |
| URL_UPDATE_005 | Update Short URL | Cập nhật URL thất bại không có token | User chưa đăng nhập | shortCode: "abc123", description: "Updated" | Status: 401, error message | Edge case |
| URL_DELETE_001 | Delete Short URL | Xóa URL thành công | User đã đăng nhập, URL tồn tại | shortCode: "abc123" | Status: 200, success: true, URL được soft delete, user.totalShortUrls giảm | Happy path |
| URL_DELETE_002 | Delete Short URL | Xóa URL thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_DELETE_003 | Delete Short URL | Xóa URL thất bại không thuộc user | User đã đăng nhập, URL thuộc user khác | shortCode: "other123" | Status: 400, error message | Security test |
| URL_DELETE_004 | Delete Short URL | Xóa URL thất bại không có token | User chưa đăng nhập | shortCode: "abc123" | Status: 401, error message | Edge case |
| URL_ENABLE_001 | Enable Short URL | Kích hoạt URL thành công | User đã đăng nhập, URL bị disable | shortCode: "abc123" | Status: 200, success: true, message: "Short URL enabled successfully", status = ENABLED | Happy path |
| URL_ENABLE_002 | Enable Short URL | Kích hoạt URL thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_ENABLE_003 | Enable Short URL | Kích hoạt URL thất bại không thuộc user | User đã đăng nhập, URL thuộc user khác | shortCode: "other123" | Status: 400, error message | Security test |
| URL_ENABLE_004 | Enable Short URL | Kích hoạt URL thất bại không có token | User chưa đăng nhập | shortCode: "abc123" | Status: 401, error message | Edge case |
| URL_DISABLE_001 | Disable Short URL | Vô hiệu hóa URL thành công | User đã đăng nhập, URL đang enable | shortCode: "abc123" | Status: 200, success: true, message: "Short URL disabled successfully", status = DISABLED | Happy path |
| URL_DISABLE_002 | Disable Short URL | Vô hiệu hóa URL thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_DISABLE_003 | Disable Short URL | Vô hiệu hóa URL thất bại không thuộc user | User đã đăng nhập, URL thuộc user khác | shortCode: "other123" | Status: 400, error message | Security test |
| URL_DISABLE_004 | Disable Short URL | Vô hiệu hóa URL thất bại không có token | User chưa đăng nhập | shortCode: "abc123" | Status: 401, error message | Edge case |
| URL_PASSWORD_001 | Update Password | Cập nhật password thành công | User đã đăng nhập, URL tồn tại, có password cũ | shortCode: "abc123", currentPassword: "old123", newPassword: "new456" | Status: 200, success: true, password được cập nhật | Happy path |
| URL_PASSWORD_002 | Update Password | Thêm password mới thành công | User đã đăng nhập, URL tồn tại, chưa có password | shortCode: "abc123", newPassword: "new456" | Status: 200, success: true, password được thêm | Happy path |
| URL_PASSWORD_003 | Update Password | Xóa password thành công | User đã đăng nhập, URL có password | shortCode: "abc123", currentPassword: "old123", newPassword: "" | Status: 200, success: true, password được xóa | Happy path |
| URL_PASSWORD_004 | Update Password | Cập nhật password thất bại với password cũ sai | User đã đăng nhập, URL có password | shortCode: "abc123", currentPassword: "wrong", newPassword: "new456" | Status: 400, error message: "Current password is incorrect" | Edge case |
| URL_PASSWORD_005 | Update Password | Cập nhật password thất bại không tồn tại | User đã đăng nhập | shortCode: "notexist", newPassword: "new456" | Status: 400, error message: "Short URL not found" | Edge case |
| URL_PASSWORD_006 | Update Password | Cập nhật password thất bại không có token | User chưa đăng nhập | shortCode: "abc123", newPassword: "new456" | Status: 401, error message | Edge case |
| URL_SEARCH_001 | Search URLs | Tìm kiếm URL thành công với query | User đã đăng nhập, có URLs | query: "test", sortBy: "createdAt", sortDirection: "desc", page: 0, size: 10 | Status: 200, PagedResponse với filtered results | Happy path |
| URL_SEARCH_002 | Search URLs | Tìm kiếm URL thành công với status filter | User đã đăng nhập, có URLs | status: "ENABLED", page: 0, size: 10 | Status: 200, chỉ URLs có status ENABLED | Happy path |
| URL_SEARCH_003 | Search URLs | Tìm kiếm URL thành công với sorting | User đã đăng nhập, có URLs | sortBy: "totalClicks", sortDirection: "asc", page: 0, size: 10 | Status: 200, results sorted by totalClicks ascending | Happy path |
| URL_SEARCH_004 | Search URLs | Tìm kiếm URL trả về rỗng | User đã đăng nhập, không có URLs match | query: "nonexistent", page: 0, size: 10 | Status: 200, PagedResponse với empty results | Edge case |
| URL_SEARCH_005 | Search URLs | Tìm kiếm URL thất bại không có token | User chưa đăng nhập | query: "test" | Status: 401, error message | Edge case |
| URL_BATCH_001 | Batch Create URLs | Tạo nhiều URL thành công | User đã đăng nhập | requests: [{"originalUrl": "https://example1.com"}, {"originalUrl": "https://example2.com"}] | Status: 200, BatchCreateShortUrlResponse với array results | Happy path |
| URL_BATCH_002 | Batch Create URLs | Tạo nhiều URL với một số thất bại | User đã đăng nhập | requests: [{"originalUrl": "https://example.com"}, {"originalUrl": "invalid-url"}] | Status: 200, một số success một số error trong results | Mixed case |
| URL_BATCH_003 | Batch Create URLs | Tạo nhiều URL thất bại không có token | User chưa đăng nhập | requests: [{"originalUrl": "https://example.com"}] | Status: 401, error message | Edge case |
| URL_BATCH_004 | Batch Create URLs | Tạo nhiều URL thất bại với array rỗng | User đã đăng nhập | requests: [] | Status: 400, validation error | Validation test |

## 2.3. Redirect Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| REDIRECT_001 | Redirect | Redirect thành công với URL không có password | ShortUrl tồn tại, enabled, chưa hết hạn, chưa đạt max usage | shortCode: "abc123" | Status: 302, Location header = originalUrl, ClickLog được tạo | Happy path |
| REDIRECT_002 | Redirect | Redirect thất bại với shortCode không tồn tại | ShortCode không tồn tại trong hệ thống | shortCode: "notexist" | Status: 404, NoResourceFoundException | Edge case |
| REDIRECT_003 | Redirect | Redirect thất bại với URL đã hết hạn | ShortUrl tồn tại nhưng đã hết hạn | shortCode: "expired123" | Status: 410, ShortUrlUnavailableException với message về URL expired | Edge case |
| REDIRECT_004 | Redirect | Redirect thất bại với URL bị disabled | ShortUrl tồn tại nhưng status = DISABLED | shortCode: "disabled123" | Status: 410, ShortUrlUnavailableException với message về URL disabled | Edge case |
| REDIRECT_005 | Redirect | Redirect thất bại với URL đã đạt max usage | ShortUrl tồn tại nhưng đã đạt maxUsage | shortCode: "maxused123" | Status: 410, ShortUrlUnavailableException với message về max usage reached | Edge case |
| REDIRECT_006 | Redirect | Redirect với URL có password - chuyển đến unlock page | ShortUrl tồn tại, có password, không truyền password param | shortCode: "protected123" | Status: 302, Location header = password unlock URL template | Happy path |
| REDIRECT_007 | Redirect | Redirect thất bại với URL có password và password sai | ShortUrl tồn tại, có password | shortCode: "protected123", password: "wrongpass" | Status: 400, IncorrectPasswordException | Edge case |
| REDIRECT_008 | Redirect | Redirect thành công với URL có password và password đúng | ShortUrl tồn tại, có password | shortCode: "protected123", password: "correctpass" | Status: 302, Location header = originalUrl, ClickLog được tạo | Happy path |
| REDIRECT_009 | Redirect | Redirect với URL có threat analysis result | ShortUrl tồn tại, có analysis result với isMalicious = true | shortCode: "malicious123" | Status: 410, ShortUrlUnavailableException với security warning | Security test |
| REDIRECT_010 | Redirect | Redirect với click log recording | ShortUrl hợp lệ, có HttpServletRequest với headers | shortCode: "abc123", User-Agent, X-Forwarded-For headers | Status: 302, ClickLog được tạo với đúng thông tin từ request | Integration test |
| UNLOCK_INFO_001 | Get Unlock Info | Lấy thông tin unlock thành công | ShortUrl tồn tại, có password | shortCode: "protected123" | Status: 200, UnlockUrlResponse với thông tin cần thiết để unlock | Happy path |
| UNLOCK_INFO_002 | Get Unlock Info | Lấy thông tin unlock thất bại với shortCode không tồn tại | ShortCode không tồn tại | shortCode: "notexist" | Status: 400, error message: "Short URL not found" | Edge case |
| UNLOCK_INFO_003 | Get Unlock Info | Lấy thông tin unlock với URL không có password | ShortUrl tồn tại nhưng không có password | shortCode: "public123" | Status: 200, thông tin về URL không cần unlock | Edge case |
| UNLOCK_INFO_004 | Get Unlock Info | Lấy thông tin unlock với URL bị disabled | ShortUrl tồn tại, có password nhưng bị disabled | shortCode: "disabled123" | Status: 400, error message về URL unavailable | Edge case |
| UNLOCK_URL_001 | Unlock URL | Unlock URL thành công với password đúng | ShortUrl tồn tại, có password | shortCode: "protected123", password: "correctpass" | Status: 200, UnlockUrlResponse với success = true, ClickLog được tạo | Happy path |
| UNLOCK_URL_002 | Unlock URL | Unlock URL thất bại với password sai | ShortUrl tồn tại, có password | shortCode: "protected123", password: "wrongpass" | Status: 401, UnlockUrlResponse với success = false, message về incorrect password | Edge case |
| UNLOCK_URL_003 | Unlock URL | Unlock URL thất bại với shortCode không tồn tại | ShortCode không tồn tại | shortCode: "notexist", password: "anypass" | Status: 400, error message: "Short URL not found" | Edge case |
| UNLOCK_URL_004 | Unlock URL | Unlock URL thất bại với URL không có password | ShortUrl tồn tại nhưng không có password | shortCode: "public123", password: "anypass" | Status: 400, error message về URL không cần password | Edge case |
| UNLOCK_URL_005 | Unlock URL | Unlock URL thất bại với URL bị disabled | ShortUrl tồn tại, có password nhưng bị disabled | shortCode: "disabled123", password: "correctpass" | Status: 400, error message về URL unavailable | Edge case |
| UNLOCK_URL_006 | Unlock URL | Unlock URL thất bại với URL đã hết hạn | ShortUrl tồn tại, có password nhưng đã hết hạn | shortCode: "expired123", password: "correctpass" | Status: 400, error message về URL expired | Edge case |
| UNLOCK_URL_007 | Unlock URL | Unlock URL thất bại với URL đã đạt max usage | ShortUrl tồn tại, có password nhưng đã đạt maxUsage | shortCode: "maxused123", password: "correctpass" | Status: 400, error message về max usage reached | Edge case |
| UNLOCK_URL_008 | Unlock URL | Unlock URL thất bại với request body không hợp lệ | ShortUrl tồn tại, có password | shortCode: "protected123", request body không có password field | Status: 400, validation error | Validation test |
| UNLOCK_URL_009 | Unlock URL | Unlock URL thất bại với password trống | ShortUrl tồn tại, có password | shortCode: "protected123", password: "" | Status: 400, validation error về required password | Validation test |
| UNLOCK_URL_010 | Unlock URL | Unlock URL với click log recording | ShortUrl hợp lệ, có password, có HttpServletRequest | shortCode: "protected123", password: "correctpass", request với headers | Status: 200, ClickLog được tạo với đúng thông tin từ request | Integration test |

## 2.4. Statistics Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| STAT_DEVICE_001 | Get Device Stats | Lấy thống kê thiết bị thành công | ShortUrl tồn tại, có DimensionStat cho BROWSER, OS, DEVICE_TYPE | shortCode: "abc123" | Status: 200, Map với browser, os, deviceType statistics và percentages | Happy path |
| STAT_DEVICE_002 | Get Device Stats | Lấy thống kê thiết bị với URL không có data | ShortUrl tồn tại, không có DimensionStat | shortCode: "abc123" | Status: 200, Map với empty data arrays, totalClicks = 0 | Edge case |
| STAT_DEVICE_003 | Get Device Stats | Lấy thống kê thiết bị thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_DEVICE_004 | Get Device Stats | Lấy thống kê thiết bị với data phức tạp | ShortUrl tồn tại, có nhiều DimensionStat với các browser/os khác nhau | shortCode: "abc123" | Status: 200, đúng tính toán percentage cho từng category | Data validation |
| STAT_LOCATION_001 | Get Location Stats | Lấy thống kê địa lý thành công | ShortUrl tồn tại, có DimensionStat cho COUNTRY, CITY | shortCode: "abc123" | Status: 200, Map với country, city statistics và percentages | Happy path |
| STAT_LOCATION_002 | Get Location Stats | Lấy thống kê địa lý với URL không có data | ShortUrl tồn tại, không có DimensionStat địa lý | shortCode: "abc123" | Status: 200, Map với empty data arrays, totalClicks = 0 | Edge case |
| STAT_LOCATION_003 | Get Location Stats | Lấy thống kê địa lý thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_LOCATION_004 | Get Location Stats | Lấy thống kê địa lý với data đa dạng | ShortUrl tồn tại, có DimensionStat từ nhiều quốc gia/thành phố | shortCode: "abc123" | Status: 200, đúng sắp xếp theo totalClicks desc và tính percentage | Data validation |
| STAT_TIME_001 | Get Time Stats | Lấy thống kê thời gian thành công với default params | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, TimeStatsResponse với 24 buckets, granularity HOURLY | Happy path |
| STAT_TIME_002 | Get Time Stats | Lấy thống kê thời gian với custom time range | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", customFrom: "2023-01-01T00:00:00Z", customTo: "2023-01-02T00:00:00Z", granularity: "HOURLY" | Status: 200, TimeStatsResponse với correct buckets từ customFrom đến customTo | Happy path |
| STAT_TIME_003 | Get Time Stats | Lấy thống kê thời gian với granularity DAILY | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "DAILY" | Status: 200, TimeStatsResponse với 30 buckets, granularity DAILY | Happy path |
| STAT_TIME_004 | Get Time Stats | Lấy thống kê thời gian với granularity MONTHLY | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "MONTHLY" | Status: 200, TimeStatsResponse với 12 buckets, granularity MONTHLY | Happy path |
| STAT_TIME_005 | Get Time Stats | Lấy thống kê thời gian với granularity YEARLY | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "YEARLY" | Status: 200, TimeStatsResponse với 10 buckets, granularity YEARLY | Happy path |
| STAT_TIME_006 | Get Time Stats | Lấy thống kê thời gian với URL không có data | ShortUrl tồn tại, không có ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, TimeStatsResponse với buckets có clicks = 0, totalClicks = 0 | Edge case |
| STAT_TIME_007 | Get Time Stats | Lấy thống kê thời gian thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist", granularity: "HOURLY" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_TIME_008 | Get Time Stats | Lấy thống kê thời gian thất bại với granularity không hợp lệ | ShortUrl tồn tại | shortCode: "abc123", granularity: "INVALID" | Status: 400, error message về invalid granularity | Validation test |
| STAT_TIME_009 | Get Time Stats | Lấy thống kê thời gian với custom time range không hợp lệ | ShortUrl tồn tại | shortCode: "abc123", customFrom: "invalid-date", customTo: "2023-01-02T00:00:00Z" | Status: 400, error message về invalid date format | Validation test |
| STAT_DIMENSION_001 | Get Dimension Stats | Lấy thống kê dimension thành công với type hợp lệ | ShortUrl tồn tại, có DimensionStat cho type này | shortCode: "abc123", type: "browser" | Status: 200, StatisticsResponse với correct category và data | Happy path |
| STAT_DIMENSION_002 | Get Dimension Stats | Lấy thống kê dimension với type alias | ShortUrl tồn tại, có DimensionStat | shortCode: "abc123", type: "device" | Status: 200, StatisticsResponse với type mapped thành DEVICE_TYPE | Happy path |
| STAT_DIMENSION_003 | Get Dimension Stats | Lấy thống kê dimension với UTM parameters | ShortUrl tồn tại, có DimensionStat cho UTM | shortCode: "abc123", type: "utm_source" | Status: 200, StatisticsResponse với UTM_SOURCE data | Happy path |
| STAT_DIMENSION_004 | Get Dimension Stats | Lấy thống kê dimension với URL không có data | ShortUrl tồn tại, không có DimensionStat cho type này | shortCode: "abc123", type: "browser" | Status: 200, StatisticsResponse với empty data, totalClicks = 0 | Edge case |
| STAT_DIMENSION_005 | Get Dimension Stats | Lấy thống kê dimension thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist", type: "browser" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_DIMENSION_006 | Get Dimension Stats | Lấy thống kê dimension thất bại với type không hợp lệ | ShortUrl tồn tại | shortCode: "abc123", type: "invalid_type" | Status: 400, error message về unsupported dimension type | Validation test |
| STAT_DIMENSION_007 | Get Dimension Stats | Lấy thống kê dimension với data đa dạng | ShortUrl tồn tại, có nhiều DimensionStat cho type này | shortCode: "abc123", type: "country" | Status: 200, data sắp xếp theo totalClicks desc, đúng tính percentage | Data validation |
| STAT_OVERVIEW_001 | Get Overview Stats | Lấy thống kê tổng quan thành công | ShortUrl tồn tại, có đủ data cho device, location, time | shortCode: "abc123" | Status: 200, Map với device, location, time statistics combined | Happy path |
| STAT_OVERVIEW_002 | Get Overview Stats | Lấy thống kê tổng quan với URL không có data | ShortUrl tồn tại, không có statistics data | shortCode: "abc123" | Status: 200, Map với các category có empty data | Edge case |
| STAT_OVERVIEW_003 | Get Overview Stats | Lấy thống kê tổng quan thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_PEAK_001 | Get Peak Time Stats | Lấy thống kê giờ peak thành công | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, Map với peakTime, clicks, granularity | Happy path |
| STAT_PEAK_002 | Get Peak Time Stats | Lấy thống kê giờ peak với custom time range | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", customFrom: "2023-01-01T00:00:00Z", customTo: "2023-01-02T00:00:00Z", granularity: "HOURLY" | Status: 200, peak time trong range được chỉ định | Happy path |
| STAT_PEAK_003 | Get Peak Time Stats | Lấy thống kê giờ peak với granularity DAILY | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "DAILY" | Status: 200, peak day với highest clicks | Happy path |
| STAT_PEAK_004 | Get Peak Time Stats | Lấy thống kê giờ peak với URL không có data | ShortUrl tồn tại, không có ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, peakTime = null, clicks = 0 | Edge case |
| STAT_PEAK_005 | Get Peak Time Stats | Lấy thống kê giờ peak thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist", granularity: "HOURLY" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_PEAK_006 | Get Peak Time Stats | Lấy thống kê giờ peak thất bại với granularity không hợp lệ | ShortUrl tồn tại | shortCode: "abc123", granularity: "INVALID" | Status: 400, error message về invalid granularity | Validation test |
| STAT_TOP_PEAK_001 | Get Top Peak Times | Lấy top 10 giờ peak thành công | ShortUrl tồn tại, có nhiều ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, TopPeakTimesResponse với top 10 peak times sorted by clicks desc | Happy path |
| STAT_TOP_PEAK_002 | Get Top Peak Times | Lấy top peak times với granularity DAILY | ShortUrl tồn tại, có ClickStat | shortCode: "abc123", granularity: "DAILY" | Status: 200, TopPeakTimesResponse với daily granularity | Happy path |
| STAT_TOP_PEAK_003 | Get Top Peak Times | Lấy top peak times với ít hơn 10 records | ShortUrl tồn tại, có 5 ClickStat records | shortCode: "abc123", granularity: "HOURLY" | Status: 200, TopPeakTimesResponse với 5 items, count = 5 | Edge case |
| STAT_TOP_PEAK_004 | Get Top Peak Times | Lấy top peak times với URL không có data | ShortUrl tồn tại, không có ClickStat | shortCode: "abc123", granularity: "HOURLY" | Status: 200, TopPeakTimesResponse với empty list, count = 0 | Edge case |
| STAT_TOP_PEAK_005 | Get Top Peak Times | Lấy top peak times thất bại với shortCode không tồn tại | ShortUrl không tồn tại | shortCode: "notexist", granularity: "HOURLY" | Status: 400, error message: "Short code not found" | Edge case |
| STAT_TOP_PEAK_006 | Get Top Peak Times | Lấy top peak times thất bại với granularity không hợp lệ | ShortUrl tồn tại | shortCode: "abc123", granularity: "INVALID" | Status: 400, error message về invalid granularity | Validation test |

## 2.5. Subscription Plan Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| PLAN_GET_ALL_001 | Get All Plans | Lấy danh sách tất cả subscription plans thành công | Có subscription plans trong database | Không có parameters | Status: 200, GetAllSubscriptionPlansResponse với list plans | Happy path |
| PLAN_GET_ALL_002 | Get All Plans | Lấy danh sách plans khi không có data | Không có subscription plans trong database | Không có parameters | Status: 200, GetAllSubscriptionPlansResponse với empty list | Edge case |
| PLAN_GET_BY_ID_001 | Get Plan By ID | Lấy plan theo ID thành công | Plan tồn tại với ID này | id: 1 | Status: 200, SubscriptionPlanResponse với plan details | Happy path |
| PLAN_GET_BY_ID_002 | Get Plan By ID | Lấy plan thất bại với ID không tồn tại | Plan không tồn tại với ID này | id: 999 | Status: 404, error message: "Subscription plan not found with ID: 999" | Edge case |
| PLAN_CREATE_001 | Create Plan | Tạo subscription plan thành công | Admin có quyền tạo plan | type: "PREMIUM", description: "Premium Plan", price: 9.99, billingInterval: "MONTHLY", maxShortUrls: 100, features enabled | Status: 200, SubscriptionPlanResponse với plan được tạo | Happy path |
| PLAN_CREATE_002 | Create Plan | Tạo plan thất bại với type không hợp lệ | Admin có quyền tạo plan | type: "INVALID_TYPE", description: "Test", price: 9.99, billingInterval: "MONTHLY" | Status: 400, validation error về invalid type | Validation test |
| PLAN_CREATE_003 | Create Plan | Tạo plan thất bại với price âm | Admin có quyền tạo plan | type: "PREMIUM", description: "Test", price: -5.0, billingInterval: "MONTHLY" | Status: 400, validation error về negative price | Validation test |
| PLAN_CREATE_004 | Create Plan | Tạo plan thất bại với billingInterval không hợp lệ | Admin có quyền tạo plan | type: "PREMIUM", description: "Test", price: 9.99, billingInterval: "INVALID" | Status: 400, validation error về invalid billing interval | Validation test |
| PLAN_CREATE_005 | Create Plan | Tạo plan thất bại với required fields trống | Admin có quyền tạo plan | type: "", description: "", price: null, billingInterval: "" | Status: 400, validation errors cho required fields | Validation test |
| PLAN_UPDATE_001 | Update Plan | Cập nhật plan thành công | Plan tồn tại, admin có quyền | id: 1, type: "PREMIUM", description: "Updated Premium", price: 12.99, features updated | Status: 200, SubscriptionPlanResponse với plan được cập nhật | Happy path |
| PLAN_UPDATE_002 | Update Plan | Cập nhật plan thất bại với ID không tồn tại | Plan không tồn tại | id: 999, type: "PREMIUM", description: "Updated" | Status: 404, error message: "Subscription plan not found with ID: 999" | Edge case |
| PLAN_UPDATE_003 | Update Plan | Cập nhật plan thất bại với dữ liệu không hợp lệ | Plan tồn tại | id: 1, type: "INVALID", price: -10, billingInterval: "WRONG" | Status: 400, validation errors | Validation test |
| PLAN_DELETE_001 | Delete Plan | Xóa plan thành công | Plan tồn tại, không có subscriptions active sử dụng plan này | id: 1 | Status: 200, DeleteSubscriptionPlanResponse với success = true | Happy path |
| PLAN_DELETE_002 | Delete Plan | Xóa plan thất bại với ID không tồn tại | Plan không tồn tại | id: 999 | Status: 404, error message: "Subscription plan not found with ID: 999" | Edge case |
| PLAN_TOGGLE_001 | Toggle Plan Status | Toggle status plan thành công | Plan tồn tại với active = true | id: 1 | Status: 200, SubscriptionPlanResponse với active = false | Happy path |
| PLAN_TOGGLE_002 | Toggle Plan Status | Toggle status plan thành công từ inactive | Plan tồn tại với active = false | id: 1 | Status: 200, SubscriptionPlanResponse với active = true | Happy path |
| PLAN_TOGGLE_003 | Toggle Plan Status | Toggle status thất bại với ID không tồn tại | Plan không tồn tại | id: 999 | Status: 404, error message: "Subscription plan not found with ID: 999" | Edge case |

## 2.6. Subscription Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| SUB_GET_ALL_001 | Get All Subscriptions | Lấy danh sách subscriptions của user thành công | User đã đăng nhập, có subscriptions | Authorization header với valid token | Status: 200, GetAllSubscriptionsResponse với subscriptions của user | Happy path |
| SUB_GET_ALL_002 | Get All Subscriptions | Lấy danh sách subscriptions trống | User đã đăng nhập, chưa có subscriptions | Authorization header với valid token | Status: 200, GetAllSubscriptionsResponse với empty list | Edge case |
| SUB_GET_ALL_003 | Get All Subscriptions | Lấy danh sách subscriptions thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| SUB_GET_CURRENT_001 | Get Current Subscription | Lấy subscription hiện tại thành công | User đã đăng nhập, có active subscription | Authorization header với valid token | Status: 200, SubscriptionResponse với current active subscription | Happy path |
| SUB_GET_CURRENT_002 | Get Current Subscription | Lấy subscription hiện tại khi không có active | User đã đăng nhập, không có active subscription | Authorization header với valid token | Status: 200, response = null hoặc empty | Edge case |
| SUB_GET_CURRENT_003 | Get Current Subscription | Lấy subscription hiện tại thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| SUB_REGISTER_001 | Register Subscription | Đăng ký subscription thành công với payment | User đã đăng nhập, plan tồn tại, cần payment | subscriptionPlanId: 2, applyImmediately: true | Status: 200, subscriptionId và paymentUrl cho VNPay | Happy path |
| SUB_REGISTER_002 | Register Subscription | Đăng ký subscription thành công với credit balance | User đã đăng nhập, có đủ credit balance | subscriptionPlanId: 2, applyImmediately: true, user có credit balance cao | Status: 200, subscriptionId và paymentUrl trống, subscription active ngay | Happy path |
| SUB_REGISTER_003 | Register Subscription | Đăng ký subscription với pro-rate calculation | User đã đăng nhập, có current subscription | subscriptionPlanId: 3, applyImmediately: true, có current active subscription | Status: 200, pro-rate được tính đúng, current subscription expired | Integration test |
| SUB_REGISTER_004 | Register Subscription | Đăng ký subscription thất bại với plan không tồn tại | User đã đăng nhập | subscriptionPlanId: 999, applyImmediately: true | Status: 400, error message: "Subscription plan not found" | Edge case |
| SUB_REGISTER_005 | Register Subscription | Đăng ký subscription thất bại không có token | User chưa đăng nhập | subscriptionPlanId: 2, applyImmediately: true | Status: 401, error message | Edge case |
| SUB_REGISTER_006 | Register Subscription | Đăng ký subscription thất bại với dữ liệu không hợp lệ | User đã đăng nhập | subscriptionPlanId: null, applyImmediately: true | Status: 400, validation error | Validation test |
| SUB_CANCEL_001 | Cancel Subscription | Hủy subscription thành công | User đã đăng nhập, có subscription chưa bắt đầu | id: "subscription-uuid", subscription thuộc user và chưa active | Status: 200, CancelSubscriptionResponse với success = true | Happy path |
| SUB_CANCEL_002 | Cancel Subscription | Hủy subscription thất bại với ID không tồn tại | User đã đăng nhập | id: "non-existent-uuid" | Status: 400, error message: "Subscription not found" | Edge case |
| SUB_CANCEL_003 | Cancel Subscription | Hủy subscription thất bại không thuộc user | User đã đăng nhập, subscription thuộc user khác | id: "other-user-subscription-uuid" | Status: 400, error message: "Subscription does not belong to user" | Security test |
| SUB_CANCEL_004 | Cancel Subscription | Hủy subscription thất bại với subscription đã active | User đã đăng nhập, subscription đã bắt đầu và active | id: "active-subscription-uuid" | Status: 400, error message: "Cannot cancel active subscription that has already started" | Edge case |
| SUB_CANCEL_005 | Cancel Subscription | Hủy subscription thất bại không có token | User chưa đăng nhập | id: "subscription-uuid" | Status: 401, error message | Edge case |
| SUB_COST_001 | Get Subscription Cost | Tính cost thành công với new subscription | User đã đăng nhập, plan tồn tại, không có current subscription | subscriptionPlanId: 2, applyImmediately: false | Status: 200, SubscriptionCostResponse với full price | Happy path |
| SUB_COST_002 | Get Subscription Cost | Tính cost thành công với pro-rate | User đã đăng nhập, có current subscription | subscriptionPlanId: 3, applyImmediately: true | Status: 200, SubscriptionCostResponse với pro-rate calculation | Happy path |
| SUB_COST_003 | Get Subscription Cost | Tính cost thành công với credit balance | User đã đăng nhập, có credit balance | subscriptionPlanId: 2, applyImmediately: true, user có credit balance | Status: 200, SubscriptionCostResponse với credit balance consideration | Happy path |
| SUB_COST_004 | Get Subscription Cost | Tính cost thất bại với plan không tồn tại | User đã đăng nhập | subscriptionPlanId: 999, applyImmediately: false | Status: 400, error message: "Subscription plan not found" | Edge case |
| SUB_COST_005 | Get Subscription Cost | Tính cost thất bại không có token | User chưa đăng nhập | subscriptionPlanId: 2, applyImmediately: false | Status: 401, error message | Edge case |
| SUB_COST_006 | Get Subscription Cost | Tính cost với schedule start (không apply immediately) | User đã đăng nhập, có current subscription | subscriptionPlanId: 2, applyImmediately: false | Status: 200, cost calculation cho scheduled start | Happy path |

## 2.7. Payment Controller Test Cases (VNPay Callback Only)

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| PAY_CALLBACK_001 | VNPay Callback | Xử lý callback thành công với payment hợp lệ | User đã đăng nhập, có pending payment và subscription | params với vnp_TxnRef hợp lệ, vnp_ResponseCode: "00" | Status: 200, payment status = COMPLETED, subscription status = ACTIVE | Happy path |
| PAY_CALLBACK_002 | VNPay Callback | Xử lý callback thất bại với payment không tồn tại | User đã đăng nhập | params với vnp_TxnRef không tồn tại | Status: 400, error message: "Payment not found" | Edge case |
| PAY_CALLBACK_003 | VNPay Callback | Xử lý callback với user có current subscription | User đã đăng nhập, có current active subscription và pending payment | params với vnp_TxnRef hợp lệ, vnp_ResponseCode: "00" | Status: 200, current subscription expired, new subscription active | Integration test |
| PAY_CALLBACK_004 | VNPay Callback | Xử lý callback với user không có current subscription | User đã đăng nhập, không có current subscription, có pending payment | params với vnp_TxnRef hợp lệ, vnp_ResponseCode: "00" | Status: 200, new subscription được activate | Happy path |
| PAY_CALLBACK_005 | VNPay Callback | Xử lý callback thất bại không có token | User chưa đăng nhập | params với vnp_TxnRef hợp lệ | Status: 401, error message | Edge case |
| PAY_CALLBACK_006 | VNPay Callback | Xử lý callback với invalid signature | User đã đăng nhập, có pending payment | params với invalid signature/checksum | Status: 400, error message về invalid signature | Security test |
| PAY_CALLBACK_007 | VNPay Callback | Xử lý callback với payment đã processed | User đã đăng nhập, payment đã COMPLETED | params với vnp_TxnRef của completed payment | Status: 400, error message về payment already processed | Edge case |
| PAY_CALLBACK_008 | VNPay Callback | Xử lý callback với VNPay error response | User đã đăng nhập, có pending payment | params với vnp_ResponseCode: "24" (cancelled) | Status: 400, payment status updated theo VNPay response | Edge case |
| PAY_CALLBACK_009 | VNPay Callback | Xử lý callback với subscription activation failure | User đã đăng nhập, có pending payment nhưng subscription không thể activate | params hợp lệ nhưng subscription có vấn đề | Status: 400, error message về subscription activation failure | Edge case |
| PAY_CALLBACK_010 | VNPay Callback | Xử lý callback với missing required parameters | User đã đăng nhập | params thiếu vnp_TxnRef hoặc parameters required | Status: 400, validation error về missing parameters | Validation test |

## 2.8. API Key Controller Test Cases

| TestID | Chức năng | Mô tả | Điều kiện trước | Dữ liệu Test | Kết quả mong muốn | Ghi chú |
|--------|-----------|-----------|-----------------|--------------|-------------------|---------|
| API_LIST_001 | List API Keys | Lấy danh sách API keys của user thành công | User đã đăng nhập, có API keys | Authorization header với valid token | Status: 200, List<ApiKeyResponse> với API keys của user | Happy path |
| API_LIST_002 | List API Keys | Lấy danh sách API keys trống | User đã đăng nhập, chưa tạo API keys | Authorization header với valid token | Status: 200, empty list | Edge case |
| API_LIST_003 | List API Keys | Lấy danh sách API keys thất bại không có token | User chưa đăng nhập | Không có Authorization header | Status: 401, error message | Edge case |
| API_LIST_004 | List API Keys | Lấy danh sách API keys với multiple keys | User đã đăng nhập, có nhiều API keys với trạng thái khác nhau | Authorization header với valid token | Status: 200, list với đúng số lượng keys, không expose raw key | Security test |
| API_GET_001 | Get API Key | Lấy API key theo ID thành công | User đã đăng nhập, API key tồn tại và thuộc user | id: "valid-uuid", Authorization header | Status: 200, ApiKeyResponse với key details | Happy path |
| API_GET_002 | Get API Key | Lấy API key thất bại với ID không tồn tại | User đã đăng nhập | id: "non-existent-uuid", Authorization header | Status: 200, success: false, message: "API key not found" | Edge case |
| API_GET_003 | Get API Key | Lấy API key thất bại không thuộc user | User đã đăng nhập, API key thuộc user khác | id: "other-user-key-uuid", Authorization header | Status: 200, success: false, message: "API key not found" | Security test |
| API_GET_004 | Get API Key | Lấy API key thất bại không có token | User chưa đăng nhập | id: "valid-uuid" | Status: 401, error message | Edge case |
| API_GET_005 | Get API Key | Lấy API key thất bại với ID format không hợp lệ | User đã đăng nhập | id: "invalid-uuid-format", Authorization header | Status: 400, validation error | Validation test |
| API_CREATE_001 | Create API Key | Tạo API key thành công với thông tin đầy đủ | User đã đăng nhập, có quyền tạo API key | name: "Production API", rateLimitPerHour: 5000, active: true | Status: 200, ApiKeyResponse với generated key, rawKey được expose một lần | Happy path |
| API_CREATE_002 | Create API Key | Tạo API key thành công với thông tin tối thiểu | User đã đăng nhập | name: "Test API" | Status: 200, ApiKeyResponse với default rateLimitPerHour: 1000, active: true | Happy path |
| API_CREATE_003 | Create API Key | Tạo API key thất bại với name trống | User đã đăng nhập | name: "", rateLimitPerHour: 1000 | Status: 400, validation error về required name | Validation test |
| API_CREATE_004 | Create API Key | Tạo API key thất bại với rateLimitPerHour âm | User đã đăng nhập | name: "Test API", rateLimitPerHour: -100 | Status: 400, validation error về invalid rate limit | Validation test |
| API_CREATE_005 | Create API Key | Tạo API key thất bại không có token | User chưa đăng nhập | name: "Test API", rateLimitPerHour: 1000 | Status: 401, error message | Edge case |
| API_CREATE_006 | Create API Key | Tạo API key với key generation và hashing | User đã đăng nhập | name: "Security Test API" | Status: 200, key được generate unique, hash đúng cách, prefix correct | Security test |
| API_CREATE_007 | Create API Key | Tạo API key với subscription limit check | User đã đăng nhập, plan có giới hạn API keys | name: "Limited API", user đã đạt max API keys | Status: 400, error message về subscription limit | Business rule test |
| API_UPDATE_001 | Update API Key | Cập nhật API key thành công | User đã đăng nhập, API key tồn tại và thuộc user | id: "valid-uuid", name: "Updated API", rateLimitPerHour: 2000, active: false | Status: 200, ApiKeyResponse với thông tin updated | Happy path |
| API_UPDATE_002 | Update API Key | Cập nhật API key thành công với partial update | User đã đăng nhập, API key tồn tại | id: "valid-uuid", name: "New Name Only" | Status: 200, chỉ name được update, rateLimitPerHour và active giữ nguyên | Happy path |
| API_UPDATE_003 | Update API Key | Cập nhật API key thất bại với ID không tồn tại | User đã đăng nhập | id: "non-existent-uuid", name: "Updated" | Status: 200, success: false, message: "API key not found or not owned by user" | Edge case |
| API_UPDATE_004 | Update API Key | Cập nhật API key thất bại không thuộc user | User đã đăng nhập, API key thuộc user khác | id: "other-user-key-uuid", name: "Hacked" | Status: 200, success: false, message: "API key not found or not owned by user" | Security test |
| API_UPDATE_005 | Update API Key | Cập nhật API key thất bại không có token | User chưa đăng nhập | id: "valid-uuid", name: "Updated" | Status: 401, error message | Edge case |
| API_UPDATE_006 | Update API Key | Cập nhật API key thất bại với dữ liệu không hợp lệ | User đã đăng nhập, API key tồn tại | id: "valid-uuid", name: "", rateLimitPerHour: -500 | Status: 400, validation errors | Validation test |
| API_UPDATE_007 | Update API Key | Cập nhật API key với updatedAt timestamp | User đã đăng nhập, API key tồn tại | id: "valid-uuid", name: "Timestamp Test" | Status: 200, updatedAt được cập nhật đúng | Integration test |
| API_DELETE_001 | Delete API Key | Xóa API key thành công | User đã đăng nhập, API key tồn tại và thuộc user | id: "valid-uuid", Authorization header | Status: 200, success: true, message: "API key deleted successfully" | Happy path |
| API_DELETE_002 | Delete API Key | Xóa API key thất bại với ID không tồn tại | User đã đăng nhập | id: "non-existent-uuid", Authorization header | Status: 200, success: false, message: "API key not found or not owned by user" | Edge case |
| API_DELETE_003 | Delete API Key | Xóa API key thất bại không thuộc user | User đã đăng nhập, API key thuộc user khác | id: "other-user-key-uuid", Authorization header | Status: 200, success: false, message: "API key not found or not owned by user" | Security test |
| API_DELETE_004 | Delete API Key | Xóa API key thất bại không có token | User chưa đăng nhập | id: "valid-uuid" | Status: 401, error message | Edge case |
| API_DELETE_005 | Delete API Key | Xóa API key thất bại với ID format không hợp lệ | User đã đăng nhập | id: "invalid-uuid-format", Authorization header | Status: 400, validation error | Validation test |
| API_DELETE_006 | Delete API Key | Xóa API key với cascade effects | User đã đăng nhập, API key đang được sử dụng | id: "active-key-uuid", key đang có active requests | Status: 200, key deleted, active sessions invalidated | Integration test |
| API_VALIDATE_001 | Validate API Key | Validate API key thành công với key hợp lệ | API key tồn tại, active, chưa hết hạn | X-API-Key header với valid key | Key validation successful, lastUsed updated | Integration test |
| API_VALIDATE_002 | Validate API Key | Validate API key thất bại với key không tồn tại | API key không tồn tại trong database | X-API-Key header với non-existent key | Key validation failed, null returned | Integration test |
| API_VALIDATE_003 | Validate API Key | Validate API key thất bại với key inactive | API key tồn tại nhưng active = false | X-API-Key header với inactive key | Key validation failed, null returned | Integration test |
| API_VALIDATE_004 | Validate API Key | Validate API key với rate limit check | API key hợp lệ nhưng đã đạt hourly limit | X-API-Key header với rate-limited key | Key validation considering rate limit | Integration test |
| API_SECURITY_001 | API Key Security | Key generation uniqueness test | Multiple API key creation requests | Concurrent create requests | Tất cả keys generated unique, không duplicate | Security test |
| API_SECURITY_002 | API Key Security | Key hashing consistency test | API key được tạo và validate | Tạo key, sau đó validate với raw key | Hash consistent, validation successful | Security test |
| API_SECURITY_003 | API Key Security | Raw key exposure limitation | API key created và retrieved | Create API key, sau đó get by ID | Raw key chỉ expose khi create, không expose trong get/list | Security test |

# 3. Báo cáo kết quả test (Test Report)

| Test ID | Ngày Testing | Người tham gia Test | Pass/Fail | Độ nghiêm trọng | Tóm tắt lỗi | Ghi chú |
|---------|--------------|-------------------|-----------|----------------|-------------|---------|
| AUTH_REGISTER_001 | 01/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Registration flow hoạt động tốt |
| AUTH_REGISTER_002 | 01/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Validation username duplicate đúng |
| AUTH_REGISTER_003 | 01/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Validation email duplicate đúng |
| AUTH_REGISTER_004 | 01/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Input validation hoạt động tốt |
| AUTH_REGISTER_005 | 01/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Required field validation đúng |
| AUTH_VERIFY_001 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Email verification thành công |
| AUTH_VERIFY_002 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid token handling đúng |
| AUTH_VERIFY_003 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Used token validation đúng |
| AUTH_VERIFY_004 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Expired token handling đúng |
| AUTH_FORGOT_001 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password reset email gửi thành công |
| AUTH_FORGOT_002 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security feature hoạt động đúng |
| AUTH_FORGOT_003 | 02/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Email format validation đúng |
| AUTH_RESET_001 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password reset thành công |
| AUTH_RESET_002 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid token handling đúng |
| AUTH_RESET_003 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password mismatch validation đúng |
| AUTH_RESET_004 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Expired token handling đúng |
| AUTH_RESET_005 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password strength validation đúng |
| AUTH_LOGIN_001 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Login flow hoạt động tốt |
| AUTH_LOGIN_002 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent user handling đúng |
| AUTH_LOGIN_003 | 03/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Wrong password handling đúng |
| AUTH_LOGIN_004 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Unverified email check đúng |
| AUTH_LOGIN_005 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | OAuth user login thành công |
| AUTH_LOGIN_006 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty data validation đúng |
| AUTH_REFRESH_001 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Token refresh thành công |
| AUTH_REFRESH_002 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Missing token handling đúng |
| AUTH_REFRESH_003 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid token handling đúng |
| AUTH_REFRESH_004 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Expired token handling đúng |
| AUTH_OAUTH_001 | 04/09/2025 | Nguyễn Thiên Phú | Pass | - |  | OAuth callback thành công |
| AUTH_OAUTH_002 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid OAuth token handling đúng |
| AUTH_OAUTH_003 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Missing OAuth token handling đúng |
| AUTH_PROFILE_001 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Profile retrieval thành công |
| AUTH_PROFILE_002 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Missing token handling đúng |
| AUTH_PROFILE_003 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid token handling đúng |
| AUTH_PROFILE_004 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Deleted user handling đúng |
| AUTH_LOGOUT_001 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Logout thành công |
| AUTH_LOGOUT_002 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Missing token handling đúng |
| AUTH_LOGOUT_003 | 05/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid token handling đúng |
| URL_CREATE_001 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL creation thành công |
| URL_CREATE_002 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Custom code creation thành công |
| URL_CREATE_003 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password protection thành công |
| URL_CREATE_004 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Duplicate custom code handling đúng |
| URL_CREATE_005 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Custom code validation đúng |
| URL_CREATE_006 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid character validation đúng |
| URL_CREATE_007 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Threat analysis hoạt động tốt |
| URL_CREATE_008 | 06/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_CREATE_009 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL validation đúng |
| URL_LIST_001 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Pagination hoạt động tốt |
| URL_LIST_002 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty list handling đúng |
| URL_LIST_003 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Pagination parameters đúng |
| URL_LIST_004 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_DETAIL_001 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL detail retrieval thành công |
| URL_DETAIL_002 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_DETAIL_003 | 07/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| URL_DETAIL_004 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_UPDATE_001 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL update thành công |
| URL_UPDATE_002 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Partial update thành công |
| URL_UPDATE_003 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_UPDATE_004 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| URL_UPDATE_005 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_DELETE_001 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL deletion thành công |
| URL_DELETE_002 | 08/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_DELETE_003 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| URL_DELETE_004 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_ENABLE_001 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL enable thành công |
| URL_ENABLE_002 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_ENABLE_003 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| URL_ENABLE_004 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_DISABLE_001 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL disable thành công |
| URL_DISABLE_002 | 09/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_DISABLE_003 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| URL_DISABLE_004 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_PASSWORD_001 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password update thành công |
| URL_PASSWORD_002 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password addition thành công |
| URL_PASSWORD_003 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password removal thành công |
| URL_PASSWORD_004 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Incorrect password handling đúng |
| URL_PASSWORD_005 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent URL handling đúng |
| URL_PASSWORD_006 | 10/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_SEARCH_001 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Search với query thành công |
| URL_SEARCH_002 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Status filter thành công |
| URL_SEARCH_003 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Sorting thành công |
| URL_SEARCH_004 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty result handling đúng |
| URL_SEARCH_005 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_BATCH_001 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Batch creation thành công |
| URL_BATCH_002 | 11/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Mixed results handling đúng |
| URL_BATCH_003 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| URL_BATCH_004 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty array validation đúng |
| REDIRECT_001 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Basic redirect thành công |
| REDIRECT_002 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent code handling đúng |
| REDIRECT_003 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Expired URL handling đúng |
| REDIRECT_004 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Disabled URL handling đúng |
| REDIRECT_005 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Max usage handling đúng |
| REDIRECT_006 | 12/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Password redirect thành công |
| REDIRECT_007 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Wrong password handling đúng |
| REDIRECT_008 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Correct password redirect thành công |
| REDIRECT_009 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Threat analysis handling đúng |
| REDIRECT_010 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Click logging thành công |
| UNLOCK_INFO_001 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Unlock info retrieval thành công |
| UNLOCK_INFO_002 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent code handling đúng |
| UNLOCK_INFO_003 | 13/09/2025 | Nguyễn Thiên Phú | Pass | - |  | No password URL handling đúng |
| UNLOCK_INFO_004 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Disabled URL handling đúng |
| UNLOCK_URL_001 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | URL unlock thành công |
| UNLOCK_URL_002 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Wrong password handling đúng |
| UNLOCK_URL_003 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent code handling đúng |
| UNLOCK_URL_004 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | No password URL handling đúng |
| UNLOCK_URL_005 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Disabled URL handling đúng |
| UNLOCK_URL_006 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Expired URL handling đúng |
| UNLOCK_URL_007 | 14/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Max usage handling đúng |
| UNLOCK_URL_008 | 15/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid request handling đúng |
| UNLOCK_URL_009 | 15/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty password validation đúng |
| UNLOCK_URL_010 | 15/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Click logging thành công |
| STAT_DEVICE_001 | 15/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Device stats retrieval thành công |
| STAT_DEVICE_002 | 15/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_DEVICE_003 | 15/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_DEVICE_004 | 15/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Complex data calculation đúng |
| STAT_LOCATION_001 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Location stats retrieval thành công |
| STAT_LOCATION_002 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_LOCATION_003 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_LOCATION_004 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Diverse data validation đúng |
| STAT_TIME_001 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Hourly time stats thành công |
| STAT_TIME_002 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Custom time range thành công |
| STAT_TIME_003 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Daily granularity thành công |
| STAT_TIME_004 | 16/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Monthly granularity thành công |
| STAT_TIME_005 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Yearly granularity thành công |
| STAT_TIME_006 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_TIME_007 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_TIME_008 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid granularity validation đúng |
| STAT_TIME_009 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid date format validation đúng |
| STAT_DIMENSION_001 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Dimension stats retrieval thành công |
| STAT_DIMENSION_002 | 17/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Type alias mapping đúng |
| STAT_DIMENSION_003 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | UTM parameters handling đúng |
| STAT_DIMENSION_004 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_DIMENSION_005 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_DIMENSION_006 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid type validation đúng |
| STAT_DIMENSION_007 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Diverse data sorting đúng |
| STAT_OVERVIEW_001 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Overview stats thành công |
| STAT_OVERVIEW_002 | 18/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_OVERVIEW_003 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_PEAK_001 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Peak time stats thành công |
| STAT_PEAK_002 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Custom range peak stats thành công |
| STAT_PEAK_003 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Daily peak stats thành công |
| STAT_PEAK_004 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data peak handling đúng |
| STAT_PEAK_005 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_PEAK_006 | 19/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid granularity validation đúng |
| STAT_TOP_PEAK_001 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Top peak times thành công |
| STAT_TOP_PEAK_002 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Daily top peaks thành công |
| STAT_TOP_PEAK_003 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Limited records handling đúng |
| STAT_TOP_PEAK_004 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty data handling đúng |
| STAT_TOP_PEAK_005 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent code handling đúng |
| STAT_TOP_PEAK_006 | 20/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid granularity validation đúng |
| PLAN_GET_ALL_001 | 20/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Plans retrieval thành công |
| PLAN_GET_ALL_002 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty plans handling đúng |
| PLAN_GET_BY_ID_001 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Plan by ID retrieval thành công |
| PLAN_GET_BY_ID_002 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| PLAN_CREATE_001 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Plan creation thành công |
| PLAN_CREATE_002 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid type validation đúng |
| PLAN_CREATE_003 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Negative price validation đúng |
| PLAN_CREATE_004 | 21/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid billing interval validation đúng |
| PLAN_CREATE_005 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Required fields validation đúng |
| PLAN_UPDATE_001 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Plan update thành công |
| PLAN_UPDATE_002 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| PLAN_UPDATE_003 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Invalid data validation đúng |
| PLAN_DELETE_001 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Plan deletion thành công |
| PLAN_DELETE_002 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| PLAN_TOGGLE_001 | 22/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Status toggle thành công |
| PLAN_TOGGLE_002 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Inactive to active toggle thành công |
| PLAN_TOGGLE_003 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| SUB_GET_ALL_001 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Subscriptions retrieval thành công |
| SUB_GET_ALL_002 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Empty subscriptions handling đúng |
| SUB_GET_ALL_003 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| SUB_GET_CURRENT_001 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Current subscription retrieval thành công |
| SUB_GET_CURRENT_002 | 23/09/2025 | Nguyễn Thiên Phú | Pass | - |  | No active subscription handling đúng |
| SUB_GET_CURRENT_003 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| SUB_REGISTER_001 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Subscription registration thành công |
| SUB_REGISTER_002 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Credit balance registration thành công |
| SUB_REGISTER_003 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Pro-rate calculation đúng |
| SUB_REGISTER_004 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| SUB_REGISTER_005 | 24/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| SUB_REGISTER_006 | 24/09/2025 | Nguyễn Thiên Phú | Fail | Medium | Validation không bắt được null planId | Cần cải thiện input validation |
| SUB_CANCEL_001 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Subscription cancellation thành công |
| SUB_CANCEL_002 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent subscription handling đúng |
| SUB_CANCEL_003 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Security check đúng |
| SUB_CANCEL_004 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Active subscription handling đúng |
| SUB_CANCEL_005 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| SUB_COST_001 | 25/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Cost calculation thành công |
| SUB_COST_002 | 26/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Pro-rate cost calculation đúng |
| SUB_COST_003 | 26/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Credit balance consideration đúng |
| SUB_COST_004 | 26/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Non-existent plan handling đúng |
| SUB_COST_005 | 26/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Authentication check đúng |
| SUB_COST_006 | 26/09/2025 | Nguyễn Thiên Phú | Pass | - |  | Scheduled start calculation đúng |
| PAY_CALLBACK_001 | 26/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | VNPay callback processing thành công |
| PAY_CALLBACK_002 | 26/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent payment handling đúng |
| PAY_CALLBACK_003 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Current subscription handling đúng |
| PAY_CALLBACK_004 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | New subscriber activation thành công |
| PAY_CALLBACK_005 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| PAY_CALLBACK_006 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid signature handling đúng |
| PAY_CALLBACK_007 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Processed payment handling đúng |
| PAY_CALLBACK_008 | 27/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | VNPay error response handling đúng |
| PAY_CALLBACK_009 | 27/09/2025 | Lê Nguyễn Duy Khang | Fail | High | Subscription activation timeout khi có system overload | Cần tối ưu hóa activation process |
| PAY_CALLBACK_010 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Missing parameters validation đúng |
| API_LIST_001 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | API keys listing thành công |
| API_LIST_002 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty keys handling đúng |
| API_LIST_003 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| API_LIST_004 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Multiple keys security đúng |
| API_GET_001 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | API key retrieval thành công |
| API_GET_002 | 28/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent key handling đúng |
| API_GET_003 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Security check đúng |
| API_GET_004 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| API_GET_005 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid UUID validation đúng |
| API_CREATE_001 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | API key creation thành công |
| API_CREATE_002 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Minimal info creation thành công |
| API_CREATE_003 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Empty name validation đúng |
| API_CREATE_004 | 29/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Negative rate limit validation đúng |
| API_CREATE_005 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| API_CREATE_006 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Key generation security đúng |
| API_CREATE_007 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Subscription limit check đúng |
| API_UPDATE_001 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | API key update thành công |
| API_UPDATE_002 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Partial update thành công |
| API_UPDATE_003 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent key handling đúng |
| API_UPDATE_004 | 30/09/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Security check đúng |
| API_UPDATE_005 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| API_UPDATE_006 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid data validation đúng |
| API_UPDATE_007 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Timestamp update đúng |
| API_DELETE_001 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | API key deletion thành công |
| API_DELETE_002 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent key handling đúng |
| API_DELETE_003 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Security check đúng |
| API_DELETE_004 | 01/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Authentication check đúng |
| API_DELETE_005 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Invalid UUID validation đúng |
| API_DELETE_006 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Cascade effects handling đúng |
| API_VALIDATE_001 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Key validation thành công |
| API_VALIDATE_002 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Non-existent key validation đúng |
| API_VALIDATE_003 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Inactive key validation đúng |
| API_VALIDATE_004 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Rate limit check đúng |
| API_SECURITY_001 | 02/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Key uniqueness thành công |
| API_SECURITY_002 | 03/10/2025 | Lê Nguyễn Duy Khang | Pass | - |  | Hashing consistency đúng |
| API_SECURITY_003 | 03/10/2025 | Lê Nguyễn Duy Khang | Fail | Critical | Raw key bị expose trong response của get endpoint | Cần fix ngay security vulnerability |
