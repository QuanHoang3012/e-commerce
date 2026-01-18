1. Đánh giá sơ bộ và phân tích yêu cầu
1.1. Xác định Scope 
1.1.1 MUST-HAVE 
Catalog & Search: Hiển thị sản phẩm, chi tiết SKU (Màu/Size). Phân trang, lọc cơ bản.
Cart: Giỏ hàng lưu trữ vĩnh viễn (kể cả khi khách chưa login/tắt web) để tăng tỷ lệ quay lại mua.
Inventory Core: Cơ chế "Giữ chỗ" (Reservation) trong 15 phút. Chống bán lố (Overselling), ngăn chặn race-condition.
Checkout: Tạo đơn hàng, ghi nhận thông tin giao hàng.
Tracking: Xem trạng thái đơn hàng qua Public Link (UUID).
Admin Basic: Danh sách đơn hàng, đổi trạng thái đơn (Vận hành kho).
1.1.2 Nice-to-have
SePay Full Auto: Tự động đổi trạng thái khi tiền về (Sẽ làm nếu dư thời gian, backup bằng Confirm thủ công).
Product CRUD: API quản lý sản phẩm (Phase 1 nhập liệu trực tiếp vào Database).
1.2. Gap Analysis 

Yêu cầu nghiệp vụ (Business)
Thực tế kỹ thuật (Technical Gap)
Giải pháp chuyển đổi
Giữ hàng 15 phút, hết giờ nhả ra
Không thể treo Database Transaction suốt 15 phút (gây sập DB).
Sử dụng Soft Reservation: Tạo bản ghi trong bảng InventoryReservations với thời gian hết hạn. Job ngầm sẽ dọn dẹp sau.
Khách chuyển khoản xong hệ thống tự biết
Cần Webhook từ Gateway (SePay), đòi hỏi Server có Public IP/Domain thật để nhận tin hiệu.
Trong môi trường Dev/Local, sẽ giả lập Webhook. Trên Production, cần cấu hình Domain và SSL.
Tải nhanh, phân trang rõ ràng, không load 1 cục 1000 cái. Lọc theo giá/loại
Nếu Backend trả về toàn bộ 1000 sản phẩm một lúc (fetch all), Server sẽ tốn RAM và Browser khách sẽ bị treo.
API chỉ trả về tối đa 20 sản phẩm/trang, đánh chỉ mục (INDEX) cho các cột price và category
Last item trong kho, phải xử lý cho chuẩn. phải xử lý chuẩn
Database Transaction thông thường có độ trễ, nếu không lock kỹ thì 2 request cùng đọc thấy còn 1, cùng bán thì tồn kho âm 
 - Soft Reservation: Ngay khi vào checkout, bản ghi giữ chỗ trong bảng riêng.
- Sử dụng Optimistic Locking (version) để đảm bảo chỉ 1 người ghi được dữ liệu cuối cùng.
Không cần đăng nhập, bấm link là xem được đơn.
Nếu dùng Order ID (VD: đơn hàng số 100), kẻ gian có thể đoán mò sang đơn 101, 102 để xem trộm thông tin khách khác
UUID Token
- Mỗi đơn hàng tạo ra một mã ngẫu nhiên duy nhất (VD: track_a8b9-c7d6...).
- Tracking Link sẽ dựa trên mã này. Không thể đoán mò.


Lưu giỏ hàng
Server không biết user là ai nếu không Login. Session mất khi tắt trình duyệt.
Guest Token + DB Persistence
- Lưu guest_token ở LocalStorage Client.
- Backend lưu giỏ hàng vào Database map với token đó.

1.3 Đánh giá hoàn thiện
Tính năng Cốt lõi (Must-have): Cam kết hoàn thành 100%.
Rủi ro trễ tiến độ: Tính năng "Tự động xác nhận thanh toán qua SePay".
Đề xuất cắt giảm (Backup Plan): Nếu không kịp cấu hình Domain/SSL để nhận tín hiệu thật từ Ngân hàng sẽ chuyển sang Giả lập Webhook (Mocking).

2. Giải pháp kỹ thuật
2.1. Kiến trúc hệ thống
Mô hình: Monolithic (Phase 1 để phát triển nhanh), thiết kế Modular (để dễ tách Microservices sau này).
Backend: Java Spring Boot (Tận dụng khả năng quản lý Transaction mạnh mẽ).
Database: PostgreSQL (Đảm bảo tính nhất quán dữ liệu - ACID).
2.2 Giải trình kỹ thuật
2.2.1 Vấn đề Race-condition
Lý thuyết (Best Practice):
Trong các hệ thống lớn (Shopee/Tiki), giải pháp tốt nhất cho Race-condition là Redis.
Redis hoạt động đơn luồng (Single-threaded) và xử lý trên RAM, hỗ trợ các lệnh Atomic (nguyên tử) như DECR. Nó xử lý hàng nghìn request mua hàng cùng lúc mà không bao giờ sai lệch số liệu.
Thực tế dự án (Context):
Chúng ta chỉ có 2 tuần và nhân sự giới hạn.
Việc triển khai Redis đòi hỏi xử lý bài toán đồng bộ dữ liệu (Consistency) giữa Redis và database rất phức tạp (Ví dụ: Redis trừ kho xong nhưng DB sập thì sao?). Nếu làm không khéo, rủi ro lệch kho còn cao hơn.
Quyết định: Sử dụng Optimistic Locking (PostgreSQL)
Sử dụng cột version (@Version) trong bảng product_variants.
Nếu 2 người cùng mua, người đến sau sẽ sai version và bị DB từ chối.
Tuy chậm hơn Redis, nhưng với quy mô hiện tại, giải pháp này phù hợp hơn, dễ triển khai kịp deadline.
2.2.2 Vấn đề "Last-Item" (Cái cuối cùng)
Để tránh bán lố món hàng cuối cùng, hệ thống không trừ kho ngay mà dùng cơ chế "Phòng chờ" (Reservation):
Công thức kiểm tra: AvailableStock = PhysicalStock -  ActiveReservations
Khi khách bấm Checkout, hệ thống tính AvailableStock.
Nếu AvailableStock > 0 $\rightarrow$ Tạo ngay một bản ghi trong bảng InventoryReservations (có hạn 15 phút). Lúc này, AvailableStock sẽ giảm về 0.
Khách hàng thứ 2 vào mua, hệ thống tính ra AvailableStock = 0 $\rightarrow$ Báo lỗi "Hết hàng".
Kết quả: Giải quyết triệt để vấn đề 2 người cùng bấm mua. Người bấm trước (dù chỉ vài mili-giây) sẽ được giữ hàng.