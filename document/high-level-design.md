# **Phân tích và thiết kế hệ thống phần mềm quản lý bất động sản cho thuê thông minh: Kiến trúc, tính năng và quy trình vận hành toàn diện**

Thị trường bất động sản cho thuê tại Việt Nam đang trải qua một giai đoạn chuyển đổi số mạnh mẽ, dịch chuyển từ các phương thức quản lý truyền thống dựa trên sổ sách và bảng tính sang các hệ thống quản lý tích hợp đa nền tảng. Sự gia tăng nhanh chóng của các mô hình lưu trú như chung cư mini, căn hộ dịch vụ, ký túc xá (dormitory) và nhà trọ sinh viên đã tạo ra áp lực lớn lên các chủ đầu tư và đơn vị vận hành trong việc tối ưu hóa quy trình.1 Một hệ thống phần mềm quản lý phòng trọ hiện đại không chỉ đóng vai trò là một công cụ lưu trữ dữ liệu đơn thuần mà còn phải là một hệ sinh thái kết nối chặt chẽ giữa chủ nhà, người quản lý, khách thuê và các cơ quan quản lý nhà nước thông qua các giải pháp công nghệ như IoT, Fintech và các cổng dịch vụ công trực tuyến.3 Việc thiết kế một hệ thống như vậy đòi hỏi sự hiểu biết sâu sắc về các nghiệp vụ đặc thù tại Việt Nam, từ quy trình tính tiền điện nước lũy tiến, quản lý tạm trú cho đến việc tự động hóa gạch nợ qua ngân hàng.4

## **Kiến trúc tổng thể và mô hình thực thể quan hệ cho trí tuệ nhân tạo**

Để một hệ thống trí tuệ nhân tạo có thể hiểu và tham gia vào quá trình phát triển hoặc vận hành phần mềm, cấu trúc dữ liệu nền tảng phải được thiết kế theo các tiêu chuẩn chuẩn hóa cơ sở dữ liệu (Database Normalization). Kiến trúc của hệ thống quản lý bất động sản cho thuê thường dựa trên mô hình đa thực thể, trong đó các mối quan hệ được định nghĩa chặt chẽ để đảm bảo tính toàn vẹn dữ liệu và khả năng truy vấn phức tạp.7

### **Mô hình thực thể và các thuộc tính dữ liệu cốt lõi**

Cấu trúc cơ sở dữ liệu cần phản ánh chính xác phân cấp của bất động sản. Thực thể cao nhất thường là Tòa nhà hoặc Khu trọ, chứa đựng các thông tin về vị trí địa lý và các cấu hình chung cho tất cả các đơn vị bên trong.9 Dưới cấp độ này là các Phòng hoặc Căn hộ, đây là thực thể trung tâm nơi các giao dịch và hợp đồng phát sinh.9

| Thực thể (Entity) | Thuộc tính chính (Attributes) | Định dạng & Ràng buộc (Constraints) |
| :---- | :---- | :---- |
| **Building (Tòa nhà)** | ID, Name, Address, TotalFloors, ManagerID, UtilityCycleDay | Khóa chính, Không rỗng, Ngày chốt điện nước từ 1-31 |
| **Room (Phòng)** | ID, BuildingID, RoomNumber, Floor, Area, BasePrice, Status | Khóa ngoại, Trạng thái: Trống/Đã thuê/Đang sửa chữa 11 |
| **Tenant (Khách thuê)** | ID, FullName, Phone, Email, IDCard (CCCD), VehiclePlate, Image | Duy nhất cho số điện thoại và CCCD 12 |
| **Contract (Hợp đồng)** | ID, RoomID, TenantID, StartDate, EndDate, Deposit, Cycle | Ngày kết thúc \> Ngày bắt đầu, Tiền cọc \>= 0 7 |
| **UtilityIndex (Chỉ số)** | ID, RoomID, Type (Elec/Water), OldValue, NewValue, Month | NewValue \>= OldValue, Liên kết theo phòng và tháng 5 |
| **Invoice (Hóa đơn)** | ID, ContractID, TotalAmount, Tax, PaymentStatus, DueDate | Trạng thái: Chưa thanh toán/Đã thanh toán/Quá hạn 3 |
| **Service (Dịch vụ)** | ID, ServiceName, UnitPrice, UnitType (Person/Room/Usage) | Đơn giá cố định hoặc lũy tiến 4 |

Mối quan hệ giữa các thực thể này tạo thành một mạng lưới logic. Một phòng có thể có nhiều hợp đồng theo thời gian, nhưng tại một thời điểm chỉ có một hợp đồng hiệu lực.14 Mỗi hợp đồng liên kết một khách thuê chính với một phòng, đồng thời có thể đính kèm danh sách các thành viên cùng ở để phục vụ công tác khai báo cư trú.15 Việc thiết kế khóa ngoại giữa bảng Tenant và Contract đảm bảo rằng mọi dữ liệu tài chính đều được quy chiếu về đúng đối tượng chịu trách nhiệm thanh toán.14

### **Phân tích nghiệp vụ dữ liệu cho AI hiểu**

Để AI có thể tự động hóa các tác vụ, nó cần hiểu các logic chuyển đổi trạng thái. Ví dụ, khi một hợp đồng mới được ký kết, trạng thái của Room phải tự động chuyển từ "Trống" sang "Đã thuê".11 Ngược lại, khi biên bản thanh lý hợp đồng được xác nhận, hệ thống phải thực hiện một chuỗi các hành động: chốt chỉ số điện nước cuối cùng, tất toán tiền cọc, và chuyển trạng thái phòng về "Trống" hoặc "Đang sửa chữa" để chuẩn bị cho khách tiếp theo.17

Các quy tắc kinh doanh (Business Rules) cần được mã hóa dưới dạng các ràng buộc dữ liệu. Chỉ số điện nước (UtilityIndex) là một ví dụ điển hình về việc cần sự kiểm soát chặt chẽ. Hệ thống không được phép cho phép nhập chỉ số mới nhỏ hơn chỉ số cũ của tháng trước đó để tránh sai lệch trong báo cáo doanh thu.4 Hơn nữa, việc tích hợp hình ảnh minh chứng cho chỉ số điện nước giúp tăng cường sự minh bạch, giảm thiểu các tranh chấp giữa chủ nhà và người thuê.4

## **Thiết kế hệ thống tính năng chuyên sâu và quy trình vận hành**

Một phần mềm quản lý chuyên nghiệp phải bao quát toàn bộ vòng đời của việc cho thuê, từ quảng bá phòng trống đến khi khách rời đi và tất toán tài chính.3

### **Quản lý danh mục bất động sản và sơ đồ phòng thông minh**

Tính năng này cho phép chủ nhà quản lý nhiều tòa nhà tại các địa điểm khác nhau trên một tài khoản duy nhất.2 Sơ đồ phòng nên được hiển thị dưới dạng lưới (Grid View) để người dùng có cái nhìn tổng quan tức thì về hiệu suất lấp đầy.3

Cơ chế lọc và tìm kiếm nâng cao là cần thiết để xử lý các hệ thống có hàng trăm phòng. Chủ nhà có thể lọc theo tầng, theo trạng thái thanh toán hoặc theo thời hạn hợp đồng sắp hết hạn.9 Đối với AI, việc cung cấp một API trả về danh sách các phòng trống kèm theo các thuộc tính như giá cả và tiện ích sẽ giúp tích hợp các chatbot tư vấn cho khách thuê một cách tự động.21

### **Quản lý khách thuê và hồ sơ định danh điện tử**

Trong bối cảnh quy định pháp luật về cư trú ngày càng chặt chẽ, việc quản lý thông tin khách thuê không chỉ dừng lại ở họ tên và số điện thoại. Hệ thống cần tích hợp công nghệ OCR (Optical Character Recognition) để quét thông tin từ ảnh chụp CCCD, tự động điền vào các trường dữ liệu và lưu trữ bản sao số hóa của giấy tờ tùy thân.23

Việc phân loại khách thuê theo các tiêu chí như sinh viên, người đi làm hoặc hộ gia đình giúp chủ nhà có các chiến lược chăm sóc khách hàng phù hợp.12 Thông tin phương tiện (biển số xe, loại xe) cũng cực kỳ quan trọng để quản lý an ninh bãi giữ xe, một trong những dịch vụ đi kèm phổ biến nhất tại các khu trọ Việt Nam.9

### **Chu kỳ tài chính và thuật toán tính toán hóa đơn**

Quy trình tài chính hàng tháng thường bắt đầu bằng việc chốt chỉ số điện nước. Hệ thống cần hỗ trợ cả hai phương thức: nhập thủ công qua ứng dụng di động hoặc tự động lấy dữ liệu từ công tơ thông minh.3

Tại Việt Nam, biểu giá điện sinh hoạt của EVN áp dụng cơ chế lũy tiến bậc thang nhằm khuyến khích tiết kiệm năng lượng.5 Phần mềm phải cho phép cấu hình các bậc giá này một cách linh hoạt.

Công thức tính tiền điện theo bậc thang lũy tiến được xác định như sau:

![][image1]  
Trong đó ![][image2] là lượng điện năng tiêu thụ thuộc bậc ![][image3], ![][image4] là đơn giá tương ứng của bậc đó, và ![][image5] là thuế suất giá trị gia tăng, hiện nay thường áp dụng mức 8% hoặc 10% tùy theo quy định từng thời kỳ.5

Hệ thống phải tự động phân bổ lượng điện tiêu thụ vào các bậc dựa trên cấu hình đã thiết lập. Ví dụ, nếu khách dùng 250 kWh, hệ thống sẽ tự động tính 50 số đầu giá bậc 1, 50 số tiếp theo giá bậc 2, 100 số tiếp theo giá bậc 3 và 50 số cuối giá bậc 4\.5 Kết quả tính toán này sau đó được tổng hợp cùng với tiền thuê phòng cố định và các dịch vụ khác như phí vệ sinh, phí internet, phí gửi xe để tạo thành một hóa đơn tổng thể.4

### **Tích hợp thanh toán Fintech và tự động hóa gạch nợ**

Sự kết hợp giữa quản lý nhà trọ và công nghệ thanh toán là yếu tố then chốt để giảm chi phí vận hành. Hệ thống nên tích hợp giải pháp VietQR động, tạo ra mã QR riêng biệt cho mỗi hóa đơn.3 Khi khách thuê nhận được hóa đơn qua ứng dụng hoặc tin nhắn Zalo, họ chỉ cần quét mã QR để thanh toán. Mã QR này đã chứa sẵn số tiền, số tài khoản chủ nhà và nội dung chuyển khoản theo cú pháp định sẵn.29

Quy trình gạch nợ tự động được thực hiện thông qua các Webhook kết nối với hệ thống ngân hàng hoặc trung gian thanh toán như SePay hay VietQR.io.31 Ngay khi tiền về tài khoản chủ nhà, hệ thống nhận được thông báo và tự động cập nhật trạng thái hóa đơn là "Đã thanh toán", đồng thời sinh ra phiếu thu và gửi xác nhận cho khách thuê mà không cần sự can thiệp của con người.3

## **Hệ thống màn hình giao diện (UI/UX) và luồng người dùng**

Thiết kế giao diện cần được tối ưu cho cả phiên bản web (dành cho chủ nhà quản lý số lượng lớn) và ứng dụng di động (dành cho quản lý tại chỗ và khách thuê).2

### **Màn hình Dashboard: Trung tâm điều hành chiến lược**

Dashboard không chỉ là nơi hiển thị các con số mà còn là công cụ hỗ trợ ra quyết định. Một Dashboard hiệu quả cần trực quan hóa các chỉ số hiệu suất chính (KPI) thông qua biểu đồ tròn, biểu đồ cột và các thẻ chỉ số (Scorecards).33

Các chỉ số cần có trên Dashboard bao gồm:

* **Tỷ lệ lấp đầy (Occupancy Rate):** So sánh số phòng đang có khách với tổng số phòng để đánh giá hiệu quả kinh doanh.  
* **Dòng tiền (Cash Flow):** Tổng số tiền dự kiến thu so với số tiền thực tế đã thu trong tháng hiện tại.33  
* **Biến động doanh thu:** Biểu đồ đường thể hiện doanh thu qua các tháng để nhận diện xu hướng mùa vụ.  
* **Danh sách việc cần làm (Task List):** Các hóa đơn quá hạn, các sự cố kỹ thuật khách vừa báo cáo, và các hợp đồng sắp hết hạn trong vòng 30 ngày.22

### **Màn hình Quản lý Sơ đồ phòng và Tương tác nhanh**

Màn hình này thường là nơi chủ nhà dành nhiều thời gian nhất. Mỗi phòng được đại diện bởi một ô màu sắc thể hiện trạng thái.12 Việc sử dụng màu sắc đồng nhất (ví dụ: xanh dương cho phòng trống, đỏ cho phòng nợ tiền, xanh lá cho phòng đã thanh toán) giúp người dùng xử lý thông tin cực nhanh.11

Khi nhấp vào một phòng, một bảng thông tin nhanh (Side-drawer hoặc Modal) sẽ hiện ra, cho phép thực hiện các thao tác như: chốt điện nước, lập hóa đơn, xem thông tin khách thuê hoặc gửi tin nhắn nhắc nợ trực tiếp qua Zalo.9 Luồng người dùng này giúp giảm thiểu số lần chuyển trang, tăng năng suất quản lý cho những khu trọ quy mô lớn.2

### **Màn hình Quản lý Hợp đồng và Hồ sơ số**

Màn hình này liệt kê tất cả các hợp đồng hiện có, phân loại theo trạng thái: Đang hiệu lực, Chờ ký, Sắp hết hạn và Đã thanh lý.17 Mỗi hồ sơ hợp đồng cần đính kèm các tệp tin quét (scan) của hợp đồng giấy hoặc lưu trữ chữ ký số của hợp đồng điện tử.3

Tính năng gia hạn hợp đồng tự động hoặc thanh lý hợp đồng nhanh là cực kỳ quan trọng. Quy trình thanh lý cần dẫn dắt người dùng qua các bước kiểm tra tài sản và chốt công nợ để đảm bảo không bỏ sót bất kỳ khoản phí nào trước khi trả cọc cho khách.17

### **Ứng dụng dành cho khách thuê (Tenant Mobile App)**

Giao diện cho người thuê cần tập trung vào sự đơn giản và minh bạch. Màn hình trang chủ của khách thuê nên hiển thị trạng thái hóa đơn hiện tại và nút thanh toán nhanh.3 Khách thuê cũng cần một khu vực riêng để báo cáo sự cố (ví dụ: hỏng điện, nước) kèm theo ảnh chụp thực tế. Thông tin này sẽ được đồng bộ ngay lập tức tới màn hình quản lý của chủ nhà để kịp thời xử lý.3

## **Nghiệp vụ pháp lý, Quản lý tạm trú và Tuân thủ**

Quản lý nhà trọ tại Việt Nam chịu sự điều chỉnh của nhiều văn bản pháp luật về an ninh trật tự và cư trú. Phần mềm cần tích hợp các công cụ hỗ trợ tuân thủ để bảo vệ chủ nhà khỏi các rủi ro pháp lý.24

### **Tích hợp khai báo lưu trú qua VNeID và Cổng dịch vụ công**

Theo Nghị định 96/2016/NĐ-CP, các cơ sở cho thuê lưu trú có trách nhiệm thông báo thông tin khách thuê cho cơ quan công an địa phương.24 Phần mềm quản lý cần hỗ trợ xuất file dữ liệu theo đúng định dạng yêu cầu của hệ thống quản lý cư trú quốc gia.24

Quy trình đăng ký tạm trú trực tuyến có thể được thiết kế như sau:

1. **Thu thập dữ liệu:** Khi khách vào ở, thông tin CCCD được quét và lưu trữ vào hệ thống.24  
2. **Tạo hồ sơ:** Hệ thống tự động điền dữ liệu vào mẫu CT01 (Tờ khai thay đổi thông tin cư trú).40  
3. **Kết nối cổng dịch vụ công:** Cung cấp liên kết hoặc API (nếu có) để chủ nhà tải hồ sơ lên Cổng dịch vụ công Bộ Công an hoặc thực hiện thông báo lưu trú qua ứng dụng VNeID của khách hàng.16  
4. **Theo dõi kết quả:** Lưu trữ trạng thái đã khai báo và ngày hết hạn tạm trú để nhắc nhở chủ nhà gia hạn khi cần thiết.43

Việc thực hiện đăng ký tạm trú trực tuyến không chỉ giúp chủ nhà tuân thủ pháp luật mà còn giúp cơ quan chức năng quản lý dân cư hiệu quả hơn, đảm bảo an ninh trật tự tại địa phương.16

### **Quản lý nội quy và biên bản bàn giao tài sản**

Hợp đồng thuê nhà chuẩn năm 2024 cần quy định rõ trách nhiệm của các bên về bảo quản tài sản.45 Phần mềm nên cho phép đính kèm phụ lục danh mục trang thiết bị (nội thất, đồ điện tử) kèm theo tình trạng khi bàn giao phòng.9 Khi thanh lý hợp đồng, hệ thống sẽ hiển thị lại danh mục này để đối chiếu, từ đó tính toán chính xác các khoản bồi thường nếu có hư hỏng, mất mát tài sản.17

## **Tích hợp công nghệ IoT và Hệ sinh thái truyền thông**

Sự khác biệt giữa một phần mềm quản lý thông thường và một giải pháp quản lý thông minh nằm ở khả năng tương tác với môi trường vật lý và các kênh liên lạc phổ biến.1

### **Hệ thống giám sát năng lượng và khóa cửa thông minh**

Việc tích hợp công tơ điện tử qua giao thức Wi-Fi hoặc Zigbee cho phép hệ thống tự động chốt số điện nước hàng ngày.3 Điều này không chỉ giúp loại bỏ sai sót mà còn cho phép chủ nhà phát hiện sớm các bất thường như rò rỉ nước hoặc quá tải điện năng.3

Khóa cửa thông minh tích hợp phần mềm giúp quản lý quyền truy cập một cách linh hoạt. Chủ nhà có thể tạo mã mở cửa tạm thời cho khách xem phòng hoặc tự động vô hiệu hóa mã vân tay của khách thuê khi hợp đồng hết hạn mà không cần có mặt trực tiếp.3 Các dữ liệu ra vào này cũng là nguồn dữ liệu quý giá để giám sát an ninh tòa nhà.3

### **Truyền thông tự động qua Zalo OA và ZNS**

Zalo là kênh liên lạc chính thức và hiệu quả nhất tại Việt Nam. Thay vì sử dụng SMS tốn phí cao, phần mềm cần tích hợp Zalo Notification Service (ZNS) để gửi thông báo hóa đơn và nhắc nợ.3

Quy trình gửi tin nhắn ZNS tự động:

* **Thiết lập mẫu tin:** Chủ nhà đăng ký mẫu tin với Zalo (ví dụ: thông báo tiền nhà, xác nhận thanh toán).47  
* **Kích hoạt điều kiện:** Khi hóa đơn được tạo trên phần mềm, hệ thống gọi API của Zalo để gửi tin nhắn đến số điện thoại khách thuê.46  
* **Tương tác hai chiều:** Khách thuê có thể phản hồi trực tiếp hoặc nhấn vào liên kết trong tin nhắn để xem hóa đơn và thanh toán qua mã QR tích hợp sẵn.3

Sử dụng Zalo ZNS giúp tăng tính chuyên nghiệp, đảm bảo thông báo đến tay khách thuê một cách tức thì và giảm tỷ lệ nợ đọng đáng kể.4

## **Phân tích quản trị và Hệ thống báo cáo đa chiều**

Đối với các nhà đầu tư sở hữu chuỗi nhà trọ, việc theo dõi các con số tài chính là ưu tiên hàng đầu. Hệ thống cần cung cấp các loại báo cáo chuyên sâu giúp phân tích hiệu quả kinh doanh.20

| Loại báo cáo | Nội dung cung cấp | Ý nghĩa quản trị |
| :---- | :---- | :---- |
| **Báo cáo Lãi/Lỗ (P\&L)** | Tổng thu (Tiền phòng \+ Dịch vụ) \- Tổng chi (Vận hành \+ Sửa chữa) | Đánh giá lợi nhuận thực tế của từng tòa nhà 12 |
| **Báo cáo Nợ quá hạn** | Danh sách phòng nợ tiền, số ngày quá hạn, lịch sử nhắc nợ | Kiểm soát rủi ro dòng tiền và kế hoạch thu hồi nợ 3 |
| **Báo cáo Hiệu suất phòng** | Tỷ lệ phòng trống theo thời gian, thời gian trống trung bình | Tối ưu hóa giá thuê và chiến lược marketing tìm khách 9 |
| **Báo cáo Tiêu thụ Dịch vụ** | So sánh tổng điện nước thu từ khách vs hóa đơn trả cho nhà nước | Phát hiện thất thoát năng lượng và rò rỉ hệ thống 12 |
| **Báo cáo Sự cố & Bảo trì** | Tần suất hư hỏng trang thiết bị, chi phí sửa chữa trung bình | Lên kế hoạch mua sắm và thay thế tài sản định kỳ 10 |

Bên cạnh đó, việc đồng bộ dữ liệu với các hệ thống kế toán doanh nghiệp như MISA giúp đảm bảo tính minh bạch và phục vụ công tác quyết toán thuế của các công ty bất động sản.3

## **Giải pháp triển khai và Bảo mật thông tin**

Hệ thống quản lý nhà trọ chứa đựng nhiều thông tin cá nhân nhạy cảm, do đó vấn đề bảo mật và hạ tầng triển khai cần được chú trọng đặc biệt.3

### **Kiến trúc đa người dùng (Multi-tenancy)**

Để phục vụ nhiều chủ nhà trên cùng một nền tảng, hệ thống cần được thiết kế theo mô hình Multi-tenant. Có ba cách tiếp cận chính:

1. **Shared Database, Shared Schema:** Tiết kiệm tài nguyên nhưng yêu cầu kiểm soát quyền truy cập cực kỳ chặt chẽ bằng cách gắn ID chủ nhà vào mọi bản ghi dữ liệu.51  
2. **Shared Database, Separate Schema:** Cân bằng giữa bảo mật và hiệu suất, mỗi chủ nhà có một không gian lưu trữ riêng.51  
3. **Separate Database:** Bảo mật cao nhất, phù hợp cho các doanh nghiệp lớn yêu cầu sự độc lập tuyệt đối về dữ liệu.51

### **Các yêu cầu phi chức năng và bảo mật**

Phần mềm cần tuân thủ các tiêu chuẩn bảo mật hiện đại như mã hóa dữ liệu đầu cuối (End-to-end encryption) cho thông tin cá nhân khách thuê và mật khẩu.11 Cơ chế phân quyền nhân viên (Role-based Access Control) cho phép chủ nhà giới hạn quyền truy cập của quản lý tòa nhà, nhân viên kế toán hoặc nhân viên kỹ thuật vào các vùng dữ liệu cụ thể.3

Về mặt hiệu suất, hệ thống phải đảm bảo thời gian phản hồi nhanh (dưới 2 giây) ngay cả khi số lượng phòng và giao dịch tăng lên đến hàng nghìn đơn vị.11 Khả năng sao lưu dữ liệu tự động (Auto-backup) hàng ngày là bắt buộc để ngăn ngừa mất mát thông tin do sự cố kỹ thuật.51

## **Kết luận và Khuyến nghị cho quá trình phát triển**

Việc thiết kế một phần mềm quản lý phòng trọ cho AI hiểu và thực thi đòi hỏi một tài liệu mô tả nghiệp vụ (SRS) cực kỳ chi tiết và có tính logic cao. Một hệ thống thành công không chỉ nằm ở những tính năng bề nổi mà còn ở khả năng xử lý các nghiệp vụ ngầm như tính toán giá điện bậc thang, tự động gạch nợ ngân hàng và tuân thủ các quy định pháp luật về cư trú tại Việt Nam.5

Các nhà phát triển nên ưu tiên việc xây dựng một lõi dữ liệu (Data Core) vững chắc trước khi mở rộng các tính năng IoT hay AI. Việc tích hợp sâu với các hệ sinh thái phổ biến như Zalo và các cổng thanh toán ngân hàng sẽ là lợi thế cạnh tranh lớn, giúp chủ nhà giảm bớt 50-70% khối lượng công việc hành chính và tập trung vào việc tối ưu hóa tỷ lệ lấp đầy bất động sản.3 Cuối cùng, giao diện người dùng phải được tinh chỉnh để đơn giản hóa các thao tác phức tạp, giúp những chủ nhà không rành về công nghệ vẫn có thể vận hành hệ thống một cách trơn tru và hiệu quả.2

#### **Nguồn trích dẫn**

1. Top 10 phần mềm quản lý nhà trọ hiệu quả nhất năm 2026 (so sánh chi tiết) \- Resident, truy cập vào tháng 4 22, 2026, [https://resident.vn/top-10-phan-mem-quan-ly-nha-tro/](https://resident.vn/top-10-phan-mem-quan-ly-nha-tro/)  
2. Top 12 Phần Mềm Quản Lý Nhà Trọ Hiệu Quả Và Phổ Biến Nhất \- Magenest, truy cập vào tháng 4 22, 2026, [https://magenest.com/vi/phan-mem-quan-ly-nha-tro/](https://magenest.com/vi/phan-mem-quan-ly-nha-tro/)  
3. So Sánh Chi Tiết 10 Phần Mềm Quản Lý Phòng Trọ, Ưu Nhược ..., truy cập vào tháng 4 22, 2026, [https://resident.vn/so-sanh-phan-mem-quan-ly-phong-tro/](https://resident.vn/so-sanh-phan-mem-quan-ly-phong-tro/)  
4. Giải pháp 5 phút tính tiền điện nước với Tính năng Quản lý Chỉ số điện nước của App Trọ Mới Host, truy cập vào tháng 4 22, 2026, [https://host.tromoi.com/blog/tin-tro-moi/giai-phap-5-phut-tinh-tien-dien-nuoc-voi-tinh-nang-quan-ly-chi-so-dien-nuoc-cua-app-tro-moi-host](https://host.tromoi.com/blog/tin-tro-moi/giai-phap-5-phut-tinh-tien-dien-nuoc-voi-tinh-nang-quan-ly-chi-so-dien-nuoc-cua-app-tro-moi-host)  
5. Cách tính tiền điện sinh hoạt đơn giản, chính xác nhất 2025 \- Zalopay, truy cập vào tháng 4 22, 2026, [https://zalopay.vn/cach-tinh-tien-dien-2655](https://zalopay.vn/cach-tinh-tien-dien-2655)  
6. Mở TK VietQR và liên kết, truy cập vào tháng 4 22, 2026, [https://api.vietqr.vn/vi/mo-tk-vietqr-va-lien-ket](https://api.vietqr.vn/vi/mo-tk-vietqr-va-lien-ket)  
7. 1010sb/DataMart\_SQL: This project offers a DataMart rental service similar to AirBNB, but specifically designed for MySQL Relational Database Management System (RDBMS). \- GitHub, truy cập vào tháng 4 22, 2026, [https://github.com/1010sb/DataMart\_SQL](https://github.com/1010sb/DataMart_SQL)  
8. ER Diagram for a Hotel Management System: Guide with Examples | Creately, truy cập vào tháng 4 22, 2026, [https://creately.com/guides/er-diagram-for-hotel-management-system/](https://creately.com/guides/er-diagram-for-hotel-management-system/)  
9. CÁC BƯỚC KHỞI TẠO DỮ LIỆU NHÀ TRỌ TẠI PHẦN MỀM ITRO TRÊN MÁY TÍNH, truy cập vào tháng 4 22, 2026, [https://quanlynhatro.com/blog/cac-buoc-khoi-tao-du-lieu-nha-tro-tai-phan-mem-itro-tren-may-tinh-97](https://quanlynhatro.com/blog/cac-buoc-khoi-tao-du-lieu-nha-tro-tai-phan-mem-itro-tren-may-tinh-97)  
10. Rental Management Database Database Structure and Schema, truy cập vào tháng 4 22, 2026, [https://www.databasesample.com/database/rental-management-database-database](https://www.databasesample.com/database/rental-management-database-database)  
11. MotelManager SRS: Hệ Thống Quản Lý Nhà Trọ Sinh Viên V1.0 \- Studocu, truy cập vào tháng 4 22, 2026, [https://www.studocu.vn/vn/document/dai-hoc-studocu-viet-nam/hanh-chinh-van-phong/srs-xay-dung-he-thong-quan-ly-phong-tro-cho-sinh-vien/98090910](https://www.studocu.vn/vn/document/dai-hoc-studocu-viet-nam/hanh-chinh-van-phong/srs-xay-dung-he-thong-quan-ly-phong-tro-cho-sinh-vien/98090910)  
12. 5+ Mẫu File Excel quản lý phòng trọ hiệu quả, truy cập vào tháng 4 22, 2026, [https://blog.slimcrm.vn/quan-tri/mau-file-excel-quan-ly-phong-tro](https://blog.slimcrm.vn/quan-tri/mau-file-excel-quan-ly-phong-tro)  
13. SRS \- Yêu Cầu Phần Mềm cho Website Quản Lý Đặt Phòng Khách Sạn \- Studocu Vietnam, truy cập vào tháng 4 22, 2026, [https://www.studocu.vn/vn/document/vietnam-national-university/cong-nghe/srs-example/88918560](https://www.studocu.vn/vn/document/vietnam-national-university/cong-nghe/srs-example/88918560)  
14. Hostel Management System ER Diagram | PDF \- Scribd, truy cập vào tháng 4 22, 2026, [https://www.scribd.com/document/858642680/Hostel-Management-System-Assignment2](https://www.scribd.com/document/858642680/Hostel-Management-System-Assignment2)  
15. PHẦN MỀM QUẢN LÝ NHÀ TRỌ, CHUNG CƯ MINI, CĂN HỘ, truy cập vào tháng 4 22, 2026, [https://phanmemnhatro.com/](https://phanmemnhatro.com/)  
16. 2 cách đăng ký tạm trú online NHANH CHÓNG \- CHÍNH XÁC \- MISA meInvoice, truy cập vào tháng 4 22, 2026, [https://www.meinvoice.vn/tin-tuc/26823/dang-ky-tam-tru-online/](https://www.meinvoice.vn/tin-tuc/26823/dang-ky-tam-tru-online/)  
17. Mẫu biên bản thanh lý hợp đồng thuê nhà chuẩn nhất 2024 \- Chothuenha.com.vn, truy cập vào tháng 4 22, 2026, [https://chothuenha.com.vn/mau-bien-ban-thanh-ly-hop-dong-thue-nha-chuan-nhat-2024](https://chothuenha.com.vn/mau-bien-ban-thanh-ly-hop-dong-thue-nha-chuan-nhat-2024)  
18. Thanh Lý Hợp Đồng Là Gì? Mẫu Thanh Lý Hợp Đồng Thuê Nhà 2022 \- Wiki BĐS, truy cập vào tháng 4 22, 2026, [https://wiki.batdongsan.com.vn/wiki/thanh-ly-hop-dong-la-gi-110129](https://wiki.batdongsan.com.vn/wiki/thanh-ly-hop-dong-la-gi-110129)  
19. Lập hóa đơn \- Chốt tiền điện, nước, tiền phòng \- YouTube, truy cập vào tháng 4 22, 2026, [https://www.youtube.com/watch?v=sUy1ED7oCQs](https://www.youtube.com/watch?v=sUy1ED7oCQs)  
20. Top 15 phần mềm quản lý nhà trọ miễn phí tốt nhất hiện nay \- POS365, truy cập vào tháng 4 22, 2026, [https://www.pos365.vn/phan-mem-quan-ly-nha-tro-6790.html](https://www.pos365.vn/phan-mem-quan-ly-nha-tro-6790.html)  
21. Top 8 ứng dụng tìm phòng trọ tốt nhất hiện nay \- Maytech, truy cập vào tháng 4 22, 2026, [https://software.maytech.vn/top-8-ung-dung-tim-phong-tro-tot-nhat-hien-nay/](https://software.maytech.vn/top-8-ung-dung-tim-phong-tro-tot-nhat-hien-nay/)  
22. Top 10 phần mềm quản lý KPI tối ưu hoá hiệu suất doanh nghiệp \- Base.vn, truy cập vào tháng 4 22, 2026, [https://base.vn/blog/phan-mem-kpi/](https://base.vn/blog/phan-mem-kpi/)  
23. Demo App Quản Lý Phòng Trọ | Appsheet | Nghia IT 40 \- YouTube, truy cập vào tháng 4 22, 2026, [https://www.youtube.com/watch?v=28Tvdq9IeEA](https://www.youtube.com/watch?v=28Tvdq9IeEA)  
24. Quản lý khách lưu trú trên phần mềm KiotViet: Scan nhanh hàng loạt giấy tờ, khai báo lưu trú online dễ dàng, truy cập vào tháng 4 22, 2026, [https://www.kiotviet.vn/quan-ly-khach-luu-tru-tren-phan-mem-kiotviet-scan-nhanh-hang-loat-giay-to-khai-bao-luu-tru-online-de-dang/](https://www.kiotviet.vn/quan-ly-khach-luu-tru-tren-phan-mem-kiotviet-scan-nhanh-hang-loat-giay-to-khai-bao-luu-tru-online-de-dang/)  
25. Chốt điện nước tự động hiệu quả cùng phần mềm Resident, truy cập vào tháng 4 22, 2026, [https://resident.vn/chot-dien-nuoc-tu-dong-hieu-qua-cung-phan-mem-resident/](https://resident.vn/chot-dien-nuoc-tu-dong-hieu-qua-cung-phan-mem-resident/)  
26. Cách tính tiền điện & thanh toán tiền điện sinh hoạt đơn giản 2026 \- Techcombank, truy cập vào tháng 4 22, 2026, [https://techcombank.com/thong-tin/blog/cach-tinh-tien-dien-sinh-hoat](https://techcombank.com/thong-tin/blog/cach-tinh-tien-dien-sinh-hoat)  
27. Cách tính tiền điện sinh hoạt, phòng trọ trong 1 tháng mới nhất 2025 \- Phong Vũ, truy cập vào tháng 4 22, 2026, [https://phongvu.vn/cong-nghe/cach-tinh-tien-dien-sinh-hoat-2025/](https://phongvu.vn/cong-nghe/cach-tinh-tien-dien-sinh-hoat-2025/)  
28. Phiếu Thu Tiền Phòng Trọ: Mẫu chuẩn và hướng dẫn chi tiết \- Resident, truy cập vào tháng 4 22, 2026, [https://resident.vn/phieu-thu-tien-phong-tro-mau-chuan-va-huong-dan-chi-tiet/](https://resident.vn/phieu-thu-tien-phong-tro-mau-chuan-va-huong-dan-chi-tiet/)  
29. Hướng dẫn sử dụng Thanh toán QR code \- KiotViet, truy cập vào tháng 4 22, 2026, [https://www.kiotviet.vn/huong-dan-su-dung-kiotviet/thanh-toan-qr-code-web-fnb/](https://www.kiotviet.vn/huong-dan-su-dung-kiotviet/thanh-toan-qr-code-web-fnb/)  
30. Hướng dẫn thanh toán chuyển khoản qua QR Code VietQR \- CellphoneS, truy cập vào tháng 4 22, 2026, [https://cellphones.com.vn/huong-dan-thanh-toan-chuyen-khoan-qua-viet-qr](https://cellphones.com.vn/huong-dan-thanh-toan-chuyen-khoan-qua-viet-qr)  
31. Cẩm nang sử dụng mã VietQR từ A \- Z \- payOS, truy cập vào tháng 4 22, 2026, [https://payos.vn/cam-nang-ma-vietqr/](https://payos.vn/cam-nang-ma-vietqr/)  
32. Cổng thanh toán HostBill \- Thanh toán QR Code VietQR \- SePay, truy cập vào tháng 4 22, 2026, [https://sepay.vn/cong-thanh-toan-hostbill.html](https://sepay.vn/cong-thanh-toan-hostbill.html)  
33. Dashboard là gì? Hướng dẫn toàn diện, lợi ích và cách xây dựng hiệu quả | Lark Blog, truy cập vào tháng 4 22, 2026, [https://www.larksuite.com/vi\_vn/blog/what-is-dashboard](https://www.larksuite.com/vi_vn/blog/what-is-dashboard)  
34. Dashboard là gì? Ứng dụng và 5 bước xây dựng dashboard nhanh chóng \- Vieclam24h, truy cập vào tháng 4 22, 2026, [https://vieclam24h.vn/nghe-nghiep/tram-sac-ky-nang/dashboard-la-gi](https://vieclam24h.vn/nghe-nghiep/tram-sac-ky-nang/dashboard-la-gi)  
35. Cách Xây Dựng Hệ Thống Báo Cáo Hiệu Quả Bằng KPI Và Dashboard, truy cập vào tháng 4 22, 2026, [https://www.mcivietnam.com/blog-detail/cach-xay-dung-he-thong-bao-cao-hieu-qua-bang-kpi-va-dashboard-C185AB/](https://www.mcivietnam.com/blog-detail/cach-xay-dung-he-thong-bao-cao-hieu-qua-bang-kpi-va-dashboard-C185AB/)  
36. Hướng dẫn lập hóa đơn dịch vụ và thanh toán hóa đơn trên phần mềm quản lý nhà trọ ITRO phiên bản APP, truy cập vào tháng 4 22, 2026, [https://quanlynhatro.com/blog/huong-dan-lap-hoa-don-dich-vu-va-thanh-toan-hoa-don-tren-phan-mem-quan-ly-nha-tro-itro-phien-ban-app-52](https://quanlynhatro.com/blog/huong-dan-lap-hoa-don-dich-vu-va-thanh-toan-hoa-don-tren-phan-mem-quan-ly-nha-tro-itro-phien-ban-app-52)  
37. Thanh lý hợp đồng thuê nhà và những điều quan trọng cần biết \- RESIDENT, truy cập vào tháng 4 22, 2026, [https://resident.vn/thanh-ly-hop-dong-thue-nha-va-nhung-dieu-quan-trong-can-biet/](https://resident.vn/thanh-ly-hop-dong-thue-nha-va-nhung-dieu-quan-trong-can-biet/)  
38. imtharun/apartment-management-system-dbms: This is our mini-project for the course Database Management System. \- GitHub, truy cập vào tháng 4 22, 2026, [https://github.com/imtharun/apartment-management-system-dbms](https://github.com/imtharun/apartment-management-system-dbms)  
39. Cách Đăng Ký Tạm Trú cho người thuê nhà online \- trực tiếp \- Kế toán Anpha, truy cập vào tháng 4 22, 2026, [https://ketoananpha.vn/thu-tuc-dang-ky-tam-tru-cho-nguoi-thue-nha.html](https://ketoananpha.vn/thu-tuc-dang-ky-tam-tru-cho-nguoi-thue-nha.html)  
40. thủ tục: Đăng ký tạm trú \- Cổng Dịch vụ công Quốc gia, truy cập vào tháng 4 22, 2026, [https://dichvucong.gov.vn/p/home/dvc-chi-tiet-thu-tuc-nganh-doc.html?ma\_thu\_tuc=1.004194](https://dichvucong.gov.vn/p/home/dvc-chi-tiet-thu-tuc-nganh-doc.html?ma_thu_tuc=1.004194)  
41. Đăng ký tạm trú \- Dịch vụ công \- Bộ Công an, truy cập vào tháng 4 22, 2026, [https://dichvucong.bocongan.gov.vn/bocongan/bothutuc/tthc?matt=26356](https://dichvucong.bocongan.gov.vn/bocongan/bothutuc/tthc?matt=26356)  
42. Cách đăng ký tạm trú online cho người thuê nhà mới nhất năm 2025 \- Bách hóa XANH, truy cập vào tháng 4 22, 2026, [https://www.bachhoaxanh.com/kinh-nghiem-hay/cach-dang-ky-tam-tru-online-1584293](https://www.bachhoaxanh.com/kinh-nghiem-hay/cach-dang-ky-tam-tru-online-1584293)  
43. Hướng dẫn đăng ký thường trú, tạm trú trên ứng dụng VNeID \- Công an tỉnh Thanh Hóa, truy cập vào tháng 4 22, 2026, [https://conganthanhhoa.gov.vn/phong-chong-toi-pham/quan-ly-hanh-chinh-ve-trat-tu-xa-hoi/huong-dan-dang-ky-thuong-tru-tam-tru-tren-ung-dung-vneid.html](https://conganthanhhoa.gov.vn/phong-chong-toi-pham/quan-ly-hanh-chinh-ve-trat-tu-xa-hoi/huong-dan-dang-ky-thuong-tru-tam-tru-tren-ung-dung-vneid.html)  
44. Đăng ký tạm trú \- Cổng Dịch vụ công Quốc gia, truy cập vào tháng 4 22, 2026, [https://dichvucong.gov.vn/p/home/dvc-tthc-thu-tuc-hanh-chinh-chi-tiet.html?ma\_thu\_tuc=5974](https://dichvucong.gov.vn/p/home/dvc-tthc-thu-tuc-hanh-chinh-chi-tiet.html?ma_thu_tuc=5974)  
45. Hợp đồng thuê nhà trọ là gì? Tải về mẫu hợp đồng chuẩn nhất \- eBH, truy cập vào tháng 4 22, 2026, [https://ebh.vn/tin-tuc/hop-dong-thue-nha-tro](https://ebh.vn/tin-tuc/hop-dong-thue-nha-tro)  
46. Hướng dẫn sử dụng tính năng gửi tin nhắn Zalo ZNS \- KiotViet, truy cập vào tháng 4 22, 2026, [https://www.kiotviet.vn/huong-dan-su-dung-kiotviet/khach-hang-web-fnb/gui-tin-nhan-zns/](https://www.kiotviet.vn/huong-dan-su-dung-kiotviet/khach-hang-web-fnb/gui-tin-nhan-zns/)  
47. Zalo ZNS \- Giải pháp chăm sóc khách hàng tự động GIÁ RẺ \- CNV CDP, truy cập vào tháng 4 22, 2026, [https://cnv.vn/zalo-zns-la-gi/](https://cnv.vn/zalo-zns-la-gi/)  
48. Zalo ZNS là gì? 4 Lợi ích tuyệt vời của giải pháp dịch vụ ZNS \- Worldfone, truy cập vào tháng 4 22, 2026, [https://worldfone.cloud/vi/blog/zns-giai-phap-giup-tang-giao-tiep-toi-da-voi-khach-hang-qua-zalo](https://worldfone.cloud/vi/blog/zns-giai-phap-giup-tang-giao-tiep-toi-da-voi-khach-hang-qua-zalo)  
49. Chi tiết về ZNS – Dịch vụ nhắn tin thông qua tài khoản Zalo OA \- Zafago Agency, truy cập vào tháng 4 22, 2026, [https://zafago.com/chi-tiet-ve-zns-dich-vu-nhan-tin-thong-qua-tai-khoan-zalo-oa/](https://zafago.com/chi-tiet-ve-zns-dich-vu-nhan-tin-thong-qua-tai-khoan-zalo-oa/)  
50. Top 15 Phần Mềm Quản Lý Nhà Trọ Tốt Nhất Năm 2026 \- Mona Media, truy cập vào tháng 4 22, 2026, [https://mona.media/phan-mem-quan-ly-nha-tro/](https://mona.media/phan-mem-quan-ly-nha-tro/)  
51. How To Design Database Schema Principles for Multi-Tenancy Models, truy cập vào tháng 4 22, 2026, [https://ajayreddychinthala.medium.com/how-to-design-database-schema-principles-for-multi-tenancy-models-6344583ea955](https://ajayreddychinthala.medium.com/how-to-design-database-schema-principles-for-multi-tenancy-models-6344583ea955)  
52. Top 12 phần mềm quản lý nhà trọ \- nhà cho thuê (2025) \- PosApp, truy cập vào tháng 4 22, 2026, [https://posapp.vn/top-phan-mem-quan-ly-nha-tro](https://posapp.vn/top-phan-mem-quan-ly-nha-tro)

[image1]: <data:image/png;base64,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>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABUAAAAYCAYAAAAVibZIAAABk0lEQVR4Xu2UvytGURjHH2EQkoiUkcGPRCEKg0EpBkURFoPIaJDtlQwGBinFH2AymAwkxSAzE0LJophMCt+v55yc+7y/rv391Kf3vfd7z7nnnOecK5IjAxVwAI7CBpgfjeOTB/vgFTyCk85TeAc7/h6NRyFchw+S3JjZLnyHbSZLCxvtwDfYaTJPs2in26Izysoc/HK/6aiBT/AGVposiTr4Aq9hlclCfKeU/zOSgN/uNxO18FlidFoCz+An7IlGSTDnc+ew1GQR/jOlJUk9oxY4LkHxfKfZ3l4GL+ErbDLZPJwNb7CKrOY9rA4Dw5To7li0QSoK4L7o/uM+5H5dFT09G+6a+5Y5Nz+vPcVwBe6JziQCT4hvNAHH3P1puAAf4SYscvc9I7BV9Dh3m+wXHstb0equiZ73Q9ER94sWgR+UcKT1sAsew/LgfgQ2aBf9Kg2LLj5f4BlyeUjCGZsZ+CFanC14INElYJEvREfLZbLLk5JB0X1Jueb2Q8Pv7YloYXtNlhZWd1l0lI0m83DJ+FwO5QcldkkN6TqZRgAAAABJRU5ErkJggg==>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAcAAAAXCAYAAADHhFVIAAAAlUlEQVR4XmNgGHjADcSFQKyGLgECRUD8H4jT0SVAQASIHYCYFU0cN2AGYmMgtoGy4QBkxAQgrgXi00DciyzpCsQ1QMwHxAeAeCUDku5MINYHYksg/gbEETAJGAC58ioQTwFiRjQ5Bg8g/gXELkCsDsQNyJIzGCCOEWaABATIHXDgB8RPgHgDEBcwYDGaB4gF0AWHDgAAPfUSVNIdKk0AAAAASUVORK5CYII=>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABIAAAAYCAYAAAD3Va0xAAABMUlEQVR4Xu2TvUoDQRRGr2Bh4Q82EVFMLQSUYCNoIdhoIb6DlUUaGyFVXiAgaiWCWNjZaqEWgo3gMwimUVASGxsR1HOdHZi9s8ZsrAQPHHbZb2Z29t5ZkX/ysoAP+BHYwsfk/gW3cNBP+Il9fMM587wsbtEz7DdZxABe4S2OmEwnX+I7LqajmEls4jH2mmwYbyR7txEr4uqxYQOYxVe8xiGTRWxL9ht14gU+4YzJInwN9K1HuJd4iPd4gON+cDt8fc6xiKOBfcE4RXe4nlwjfH2qNshgCnflm2Og9emote3w5+cOx9JRih5cxVOcNtkXJXzGE4nrEaJ1XBL3+bUwmMeGxP/XWjgooIAT4nZkj0hu9GBqZzM7lgdtyiYuiytJ19RxByviit81Olm7/KtF/gifT5k5dVpTL2EAAAAASUVORK5CYII=>

[image5]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACkAAAAYCAYAAABnRtT+AAAB9ElEQVR4Xu2VTyhlYRjGn4kR+ZdITUZSNrKYpGzMwkIxRUkWiq2YZqcks7qUBTMLZiOaSRZW7JUsSFmMmmHBgqSZTFaTspXyPN5znO8czp2JW3dzfvXr3Pud95zv/b73vd8FEhISEhL+hxL6jlZ733XtpK/uI7JMEV2gE/ScfqHzdIwe0dogNHt00CH6hl7RJVpGN+lfWh+EPplCPHOx72EJ9tBr+pa+gJVbC9AEc7BdlTM07+5JoJ8e0++02xuLonfN0ilnTG20C3tuG/aOfbrljela4cWGUJkPEXOTfKUrsEl9yukkrGXiaKaXdNkZ66V9sHepanuwBQt9/wzbnBDFdAcPk3BRj24hSEhxw7TRD3iEApqin+gazfUcpaVeTBM9QdBWSnLE+xxCAeo/9WYcagd3pxvoIOIXJdQCrQgvUElWBSF3O6idVHJCC6sJbge4/RhHGz2lr+lLOg4rdxyVsOQU24VwIj5aoH6oarV/kqIHSD+pyvLbu/o7FIcmV8k+wvpvmv7Cw3M32o9pyUf65hfaQU2kPlQCKlscaoUPsKRkO6wK0eMs2o/PRr2onvxB6yL3XFTeFKzcPm4VXPQbeKwNnoyOhA06EL3hoL/Wn/QP7JwV2qVVekPXaQvs+FmkZ/SCfoM9mxF0VOVEBxMSMsgtJzdQsHo9EQYAAAAASUVORK5CYII=>