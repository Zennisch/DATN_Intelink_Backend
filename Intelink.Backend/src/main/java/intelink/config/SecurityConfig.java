package intelink.config;

import intelink.security.JwtAuthenticationEntryPoint;
import intelink.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Biểu thị rằng lớp này là một cấu hình Spring
@Configuration

// Bật bảo mật web và phương thức
@EnableWebSecurity

// Bật bảo mật phương thức với hỗ trợ pre/post authorization
// prePostEnabled = true: Bật hỗ trợ @PreAuthorize và @PostAuthorize
// @PreAuthorize: Kiểm tra quyền truy cập trước khi thực hiện phương thức
// @PostAuthorize: Kiểm tra quyền truy cập sau khi thực hiện phương thức
@EnableMethodSecurity(prePostEnabled = true)

// Tự động tạo constructor với các tham số là các trường final
@RequiredArgsConstructor
public class SecurityConfig {

    // Các trường final để inject các bean cần thiết
    // UserDetailsService: Dịch vụ để tải thông tin người dùng từ cơ sở dữ liệu
    private final UserDetailsService userDetailsService;

    // JwtAuthenticationEntryPoint: Điểm vào cho các yêu cầu không được xác thực
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // JwtAuthenticationFilter: Bộ lọc để xác thực JWT trong mỗi yêu cầu
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Bean để mã hóa mật khẩu
    // BCryptPasswordEncoder: Sử dụng thuật toán BCrypt để mã hóa mật khẩu
    // strength: "Cost factor" của thuật toán. Tương đương với số vòng lặp 2^strength.
    // + Mỗi lần tăng strength lên 1, thời gian mã hóa sẽ gấp đôi.
    // + 12 là một giá trị hợp lý cho môi trường sản xuất, cung cấp sự cân bằng giữa bảo mật và hiệu suất.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // Bean để quản lý xác thực
    // AuthenticationManager: Quản lý quá trình xác thực người dùng
    // AuthenticationConfiguration: Cấu hình xác thực được sử dụng để lấy AuthenticationManager
    // config.getAuthenticationManager(): Lấy AuthenticationManager từ cấu hình
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Bean để cung cấp xác thực người dùng
    // DaoAuthenticationProvider: Cung cấp xác thực dựa trên UserDetailsService
    // setUserDetailsService: Thiết lập dịch vụ để tải thông tin người dùng
    // setPasswordEncoder: Thiết lập bộ mã hóa mật khẩu để xác thực
    // setHideUserNotFoundExceptions: Không ẩn ngoại lệ khi người dùng không được tìm thấy
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    // Bean để cấu hình chuỗi bảo mật
    // SecurityFilterChain: Chuỗi bộ lọc bảo mật để xử lý các yêu cầu HTTP
    // HttpSecurity: Cấu hình bảo mật HTTP
    // cors: Cấu hình CORS để cho phép các yêu cầu từ các nguồn khác
    // + Lí do: sử dụng corsConfigurationSource() để cung cấp cấu hình CORS
    // csrf: Vô hiệu hóa CSRF (Cross-Site Request Forgery) vì API sẽ sử dụng JWT
    // + Lí do: CSRF không cần thiết khi sử dụng JWT vì không có trạng thái phiên
    // sessionManagement: Quản lý phiên làm việc, sử dụng SessionCreationPolicy.STATELESS để không lưu trạng thái phiên
    // + Lí do: API sẽ không lưu trạng thái phiên, mỗi yêu cầu đều độc lập
    // exceptionHandling: Thiết lập điểm vào cho các yêu cầu không được xác thực
    // + Lí do: sử dụng jwtAuthenticationEntryPoint để xử lý các yêu cầu không được xác thực
    // authenticationProvider: Thiết lập nhà cung cấp xác thực để sử dụng DaoAuthenticationProvider
    // + Lí do: sử dụng authenticationProvider() để cung cấp xác thực người dùng
    // addFilterBefore: Thêm bộ lọc jwtAuthenticationFilter trước bộ lọc UsernamePasswordAuthenticationFilter
    // + Lí do: jwtAuthenticationFilter sẽ xác thực JWT trong mỗi yêu cầu
    // authorizeHttpRequests: Cấu hình quyền truy cập cho các endpoint
    // + Lí do: xác định các endpoint công khai, yêu cầu quyền truy cập của người dùng, và quyền truy cập của quản trị viên
    // + + requestMatchers: Xác định các endpoint cụ thể và quyền truy cập tương ứng
    // + + permitAll: Cho phép tất cả người dùng truy cập vào các endpoint công khai
    // + + hasRole: Yêu cầu người dùng có vai trò cụ thể để truy cập vào các endpoint quản trị viên hoặc người dùng
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/health/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{shortCode}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/{shortCode}/unlock").permitAll()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // User endpoints - require USER or ADMIN role
                        .requestMatchers("/api/v1/urls/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/analytics/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // Bean để cấu hình CORS (Cross-Origin Resource Sharing)
    // CorsConfigurationSource: Nguồn cấu hình CORS để xử lý các yêu cầu từ các nguồn khác
    // CorsConfiguration: Cấu hình CORS cho phép các nguồn, phương thức, và tiêu đề cụ thể
    // setAllowedOriginPatterns: Cho phép tất cả các nguồn (có thể thay đổi theo yêu cầu bảo mật)
    // setAllowedMethods: Cho phép các phương thức HTTP cụ thể
    // setAllowedHeaders: Cho phép tất cả các tiêu đề trong yêu cầu
    // setAllowCredentials: Cho phép gửi cookie và thông tin xác thực trong yêu cầu CORS
    // setMaxAge: Thiết lập thời gian tối đa cho phép trình duyệt lưu trữ cấu hình CORS
    // UrlBasedCorsConfigurationSource: Nguồn cấu hình CORS dựa trên URL
    // registerCorsConfiguration: Đăng ký cấu hình CORS cho tất cả các endpoint
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}