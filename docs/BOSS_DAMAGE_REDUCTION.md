# Giảm Dame Khi Đánh Boss

Tài liệu này giải thích vì sao cùng một lượng sức đánh nhưng khi đánh boss có thể thấy dame thấp hơn. Cách viết ưu tiên để người chơi dễ hiểu, không phải mô tả kỹ thuật nội bộ.

## Cách Đọc

- `Dame nhận`: lượng dame boss thật sự nhận trước khi trừ HP.
- `Giảm dame`: phần dame bị boss giảm bởi cơ chế riêng.
- Ngoài các cơ chế riêng bên dưới, dame vẫn có thể bị ảnh hưởng bởi né đòn, giáp, khiên, random dame skill, trạng thái map và một số cơ chế sự kiện.
- Boss không nằm trong nhóm giảm riêng sẽ dùng cơ chế chung: không giảm % riêng, chỉ tính né đòn/giáp/khiên nếu có.

## Boss Có Giảm Dame Riêng

| Boss | Dame nhận | Giảm dame | Giải thích cho người chơi |
|---|---:|---:|---|
| Xên Bọ Hung | 50% | 50% | Boss có lớp giáp cứng, sát thương đánh vào chỉ còn một nửa. |
| Poc Bunny | 50% | 50% | Poc Bunny giảm một nửa sát thương nhận vào. |
| Ninja Áo Tím | 50% | 50% | Ninja Áo Tím có cơ chế giảm nửa dame. |
| Ninja Clone | 50% | 50% | Ninja Clone nhận một nửa sát thương. |
| Rôbốt Vệ Sĩ | 50% | 50% | Rôbốt Vệ Sĩ có lớp giáp giảm 50% dame. |
| Trung úy Thép | 50% | 50% | Trung úy Thép chống chịu tốt, chỉ nhận 50% dame. |
| Trung úy Trắng | 50% | 50% | Trung úy Trắng giảm một nửa sát thương nhận vào. |
| Trung úy Xanh Lơ | 50% | 50% | Trung úy Xanh Lơ có kháng dame 50%. |
| Siêu Bọ Hung | 33.3% | 66.7% | Siêu Bọ Hung rất trâu, chỉ nhận khoảng 1/3 sát thương. |
| Saibamen Đường Rắn | 14.3% | 85.7% | Saibamen ở phó bản này kháng rất mạnh, chỉ nhận khoảng 1/7 dame. |
| Khỉ Đột Trung Thu | 14.3% | 85.7% | Khỉ Đột sự kiện chỉ nhận khoảng 1/7 sát thương. |
| Bí Ma | 14.3% | 85.7% | Bí Ma giảm rất sâu, chỉ nhận khoảng 1/7 dame. |
| Dơi | 14.3% | 85.7% | Dơi sự kiện chỉ nhận khoảng 1/7 sát thương. |
| Ma Trơi | 14.3% | 85.7% | Ma Trơi chỉ nhận khoảng 1/7 dame. |
| Black Goku | Trừ ngẫu nhiên 0-99,999 dame | Tùy hit | Mỗi hit vào Black Goku bị hao hụt ngẫu nhiên tối đa gần 100k dame. |
| Cumber | Trừ ngẫu nhiên 0-99,999 dame | Tùy hit | Cumber có giáp hỗn loạn, mỗi hit bị trừ ngẫu nhiên tối đa gần 100k dame. |
| Dr Lychee | Giảm theo level | Tùy level | Level boss càng cao thì dame bị trừ thêm càng nhiều. Người đang giữ ngọc Namek chỉ gây 1 dame. |
| Hatchiyack | Giảm theo level mạnh hơn | Tùy level | Hatchiyack giảm dame theo level mạnh hơn Dr Lychee. Người đang giữ ngọc Namek chỉ gây 1 dame. |
| Whis | Chia theo level | Tùy level | Dame đánh vào Whis bị chia theo level, level càng cao càng khó gây sát thương. |
| Super Rank | Giảm theo giáp % | Tối đa khoảng 86% | Boss võ đài có giáp %, có thể bị giảm bởi chỉ số xuyên giáp của người chơi. |
| Broly Cáu Bẩn | Đòn xuyên giáp bị chia 100 | 99% với đòn xuyên | Các đòn xuyên giáp vào boss này bị giảm rất mạnh. |
| Broly Hắc Hóa | Đòn xuyên giáp bị chia 100 | 99% với đòn xuyên | Các đòn xuyên giáp vào boss này bị giảm rất mạnh. |
| Super Broly | Đòn xuyên giáp bị chia 100; hit quá lớn có cap | Tùy tình huống | Đòn xuyên bị giảm mạnh; dame quá lớn có thể bị ép về khoảng 9-10 triệu. |
| Cooler | Đòn xuyên giáp bị chia 100 | 99% với đòn xuyên | Đòn xuyên giáp vào Cooler bị giảm rất mạnh. |
| Thây Ma | Hit thường bị giới hạn | Tùy người đánh | Dame lớn vào Thây Ma có thể bị giới hạn rất thấp; admin có mức riêng. |

## Boss Hấp Thụ Chưởng

| Boss | Cơ chế | Giải thích cho người chơi |
|---|---|---|
| Android 19 | Chưởng Kamejoko, Masenko, Antomic không gây dame; boss hồi 80% lượng dame đó | Dùng đòn đấm/vật lý sẽ ổn hơn, vì chưởng bị hấp thụ. |
| Dr Kore | Chưởng Kamejoko, Masenko, Antomic không gây dame; boss hồi đúng lượng dame đó | Đánh chưởng vào Dr Kore sẽ thành hồi máu cho boss. |
| Super Rank có vô hiệu chưởng | Một số trạng thái biến chưởng thành hồi MP/vô hiệu sát thương | Khi thấy chưởng không ăn dame, cần đổi loại skill hoặc xử lý xuyên giáp/trạng thái. |

## Boss Không Nhận Sát Thương Trực Tiếp

| Boss | Cơ chế | Giải thích cho người chơi |
|---|---|---|
| Ông Già Noel | Không nhận dame | Đây là boss/sự kiện đặc biệt, không thể đánh mất máu trực tiếp. |
| Tổ Sư Kaio | Không nhận dame | Boss luyện tập/đặc biệt, không nhận sát thương trực tiếp. |
| Death Beam 1 | Không nhận dame | Đối tượng kỹ năng, không phải boss đánh theo cách thông thường. |
| Death Beam 2 | Không nhận dame | Đối tượng kỹ năng, không phải boss đánh theo cách thông thường. |
| Death Beam 3 | Không nhận dame | Đối tượng kỹ năng, không phải boss đánh theo cách thông thường. |
| Death Beam 4 | Không nhận dame | Đối tượng kỹ năng, không phải boss đánh theo cách thông thường. |
| Death Beam 5 | Không nhận dame | Đối tượng kỹ năng, không phải boss đánh theo cách thông thường. |
| Sói Héc Quyn phó bản | Không nhận dame | Boss phó bản đặc biệt, không nhận sát thương người chơi. |
| Xin Ba Tô phó bản | Không nhận dame | Boss phó bản đặc biệt, không nhận sát thương người chơi. |

## Boss Có Điều Kiện Chặn Kết Liễu

| Boss | Cơ chế | Giải thích cho người chơi |
|---|---|---|
| Android 13 | Có thể chặn hit kết liễu nếu boss liên quan chưa chết | Cần xử lý đúng thứ tự boss trong cụm Android. |
| Android 14 | Có thể chặn hit kết liễu trước khi gọi Android 13 | Boss có giai đoạn chuyển pha, không phải bị lỗi mất dame. |
| Android 15 | Có thể chặn hit kết liễu trước khi Android 13 được gọi | Boss có giai đoạn chuyển pha, cần đánh tiếp sau khi cơ chế kích hoạt. |
| Drabura 2 | Có thể chặn hit kết liễu trong một số trạng thái | Khi boss còn 1 ít máu mà chưa chết, có thể là cơ chế pha. |
| Pocolo | Có thể chặn hit kết liễu trong một số trạng thái | Khi boss còn 1 ít máu mà chưa chết, có thể là cơ chế pha. |
| Cađích Đường Rắn | Có thể chặn một số tình huống kết liễu/khiên | Đây là boss phó bản có điều kiện riêng, dame không phải lúc nào cũng trừ thẳng. |

## Boss Có Quy Tắc Đặc Biệt Khác

| Boss | Cơ chế | Giải thích cho người chơi |
|---|---|---|
| Đệ Tử Boss / Gozilaa | Chỉ đệ tử mới gây dame | Sư phụ đánh sẽ không gây dame; phải dùng đệ tử. |
| Boss mặc định khi người chơi giữ ngọc Namek | Người giữ ngọc Namek chỉ gây 1 dame lên một số boss | Khi đang cầm ngọc Namek, dame vào boss có thể bị ép về 1. |
| Boss có khiên | Khi khiên đang bật, dame có thể bị ép rất thấp | Cần phá khiên hoặc chờ hết hiệu ứng. |
| Boss có né đòn | Hit có thể bị hụt và hiện "Xí hụt" | Đây là cơ chế né, không phải giảm dame. |

## Danh Sách Đầy Đủ Theo BossData

Bảng này liệt kê các boss đang được khai báo trong `BossesData`. Nếu cột "Cơ chế dame" ghi "Cơ chế chung" nghĩa là hiện không thấy giảm dame % riêng trong code boss tương ứng.

| Mã boss | Tên hiển thị | Cơ chế dame |
|---|---|---|
| KUKU | Kuku | Cơ chế chung. |
| MAP_DAU_DINH | Mập Đầu Đinh | Cơ chế chung. |
| RAMBO | Rambo | Cơ chế chung. |
| SO_4 | Số 4 | Cơ chế chung. |
| SO_3 | Số 3 | Cơ chế chung. |
| SO_2 | Số 2 | Cơ chế chung. |
| SO_1 | Số 1 | Cơ chế chung. |
| TIEU_DOI_TRUONG | Tiểu đội trưởng | Cơ chế chung. |
| SO_4_NM | Số 4 Namek | Cơ chế chung. |
| SO_3_NM | Số 3 Namek | Cơ chế chung. |
| SO_2_NM | Số 2 Namek | Cơ chế chung. |
| SO_1_NM | Số 1 Namek | Cơ chế chung. |
| TIEU_DOI_TRUONG_NM | Tiểu đội trưởng Namek | Cơ chế chung. |
| XEKO_DRM | Xeko | Cơ chế chung. |
| CHAIEN_DRM | Chaien | Cơ chế chung. |
| XUKA_DRM | Xuka | Cơ chế chung. |
| NOBITA_DRM | Nobita | Cơ chế chung. |
| DOREMON_DATA | Doraemon | Cơ chế chung. |
| FIDE_DAI_CA_1 | Fide đại ca 1 | Cơ chế chung. |
| FIDE_DAI_CA_2 | Fide đại ca 2 | Cơ chế chung. |
| FIDE_DAI_CA_3 | Fide đại ca 3 | Cơ chế chung. |
| DR_KORE | Dr.Kôrê | Hấp thụ chưởng: Kamejoko/Masenko/Antomic không gây dame và hồi máu cho boss. |
| ANDROID_19 | Android 19 | Hấp thụ chưởng: Kamejoko/Masenko/Antomic không gây dame và hồi 80% dame. |
| ANDROID_13 | Android 13 | Có thể chặn hit kết liễu nếu cụm Android chưa đúng điều kiện. |
| ANDROID_14 | Android 14 | Có thể chặn hit kết liễu để chuyển pha/gọi Android 13. |
| ANDROID_15 | Android 15 | Có thể chặn hit kết liễu để chuyển pha/gọi Android 13. |
| PIC | Pic | Cơ chế chung. |
| POC | Poc | Cơ chế chung. |
| KING_KONG | King Kong | Cơ chế chung. |
| XEN_BO_HUNG_1 | Xên bọ hung | Nhận 50% dame, giảm 50%. |
| XEN_BO_HUNG_2 | Xên bọ hung 2 | Nhận 50% dame, giảm 50%. |
| XEN_BO_HUNG_3 | Xên hoàn thiện | Nhận 50% dame, giảm 50%. |
| SIEU_BO_HUNG_1 | Xên Hoàn Thiện | Nhận khoảng 33.3% dame, giảm khoảng 66.7%. |
| SIEU_BO_HUNG_2 | Siêu Bọ Hung | Nhận khoảng 33.3% dame, giảm khoảng 66.7%. |
| XEN_CON_1 | Xên con 1 | Cơ chế chung. |
| XEN_CON_2 | Xên con 2 | Cơ chế chung. |
| XEN_CON_3 | Xên con 3 | Cơ chế chung. |
| XEN_CON_4 | Xên con 4 | Cơ chế chung. |
| XEN_CON_5 | Xên con 5 | Cơ chế chung. |
| XEN_CON_6 | Xên con 6 | Cơ chế chung. |
| XEN_CON_7 | Xên con 7 | Cơ chế chung. |
| BLACK_GOKU | Black Goku | Trừ ngẫu nhiên 0-99,999 dame mỗi hit. |
| SUPER_BLACK_GOKU | Super Black Goku | Cơ chế chung nếu dùng class mặc định. |
| CUMBER | Cumber | Trừ ngẫu nhiên 0-99,999 dame mỗi hit. |
| SUPER_CUMBER | Super Cumber | Cơ chế chung nếu dùng class mặc định. |
| MABU | Mabư mập | Cơ chế chung. |
| SUPER_BU | Super Bư | Cơ chế chung. |
| BU_TENK | Bư Tênk | Cơ chế chung. |
| BU_HAN | Bư Han | Cơ chế chung. |
| KID_BU | Kid Bư | Cơ chế chung. |
| SUPER_BU_BUNG | Super Bư | Cơ chế chung. |
| MABU_12H | Mabư | Cơ chế chung. |
| GOKU | Gôcu | Cơ chế chung. |
| CADIC | Ca Đít | Cơ chế chung. |
| DRABURA | Drabura | Cơ chế chung. |
| DRABURA_2 | Drabura 2 | Có thể chặn hit kết liễu trong một số trạng thái. |
| DRABURA_3 | Drabura 3 | Cơ chế chung. |
| BUI_BUI | Bui Bui | Cơ chế chung. |
| BUI_BUI_2 | Bui Bui 2 | Cơ chế chung. |
| YACON | Yacôn | Cơ chế chung. |
| SOI_HEC_QUYN | Sói Héc Quyn | Cơ chế chung. |
| O_DO | Ở Dơ | Cơ chế chung. |
| XINBATO | Xinbatô | Cơ chế chung. |
| CHA_PA | Cha Pa | Cơ chế chung. |
| PON_PUT | Pon Put | Cơ chế chung. |
| CHAN_XU | Chan Xư | Cơ chế chung. |
| TAU_PAY_PAY | Tàu Pảy Pảy | Cơ chế chung. |
| YAMCHA | Yamcha | Cơ chế chung. |
| JACKY_CHUN | Jacky Chun | Cơ chế chung. |
| THIEN_XIN_HANG | Thiên Xin Hăng | Cơ chế chung. |
| THIEN_XIN_HANG_CLONE | Thiên Xin Hăng Clone | Cơ chế chung. |
| LIU_LIU | Liu Liu | Cơ chế chung. |
| TAU_PAY_PAY_DONG_NAM_KARIN | Tàu Pảy Pảy Đông Nam Karin | Cơ chế chung. |
| BUJIN | Bujin | Cơ chế chung. |
| KOGU | Kogu | Cơ chế chung. |
| ZANGYA | Zangya | Cơ chế chung. |
| BIDO | Bido | Cơ chế chung. |
| BOJACK | Bojack | Cơ chế chung. |
| SUPER_BOJACK | Siêu Bojack | Cơ chế chung. |
| SUPER_BOJACK_2 | Siêu Bojack 2 | Cơ chế chung. |
| TAP_SU_0 | Tập sự 0 | Cơ chế chung. |
| TAP_SU_1 | Tập sự 1 | Cơ chế chung. |
| TAP_SU_2 | Tập sự 2 | Cơ chế chung. |
| TAP_SU_3 | Tập sự 3 | Cơ chế chung. |
| TAP_SU_4 | Tập sự 4 | Cơ chế chung. |
| TAN_BINH_5 | Tân binh 5 | Cơ chế chung. |
| TAN_BINH_0 | Tân binh 0 | Cơ chế chung. |
| TAN_BINH_1 | Tân binh 1 | Cơ chế chung. |
| TAN_BINH_2 | Tân binh 2 | Cơ chế chung. |
| TAN_BINH_3 | Tân binh 3 | Cơ chế chung. |
| TAN_BINH_4 | Tân binh 4 | Cơ chế chung. |
| CHIEN_BINH_5 | Chiến binh 5 | Cơ chế chung. |
| CHIEN_BINH_0 | Chiến binh 0 | Cơ chế chung. |
| CHIEN_BINH_1 | Chiến binh 1 | Cơ chế chung. |
| CHIEN_BINH_2 | Chiến binh 2 | Cơ chế chung. |
| CHIEN_BINH_3 | Chiến binh 3 | Cơ chế chung. |
| CHIEN_BINH_4 | Chiến binh 4 | Cơ chế chung. |
| DOI_TRUONG_5 | Đội trưởng 5 | Cơ chế chung. |
| KHIDOT | Khỉ đột | Nhận khoảng 14.3% dame, giảm khoảng 85.7%. |
| NGUYETTHAN | Nguyệt Thần | Cơ chế chung. |
| NHATTTHAN | Nhật Thần | Cơ chế chung. |
| DRACULA | Đracula | Cơ chế chung. |
| NGUOI_VO_HINH | Người Vô Hình | Cơ chế chung. |
| BONG_BANG | Bông Băng | Cơ chế chung. |
| VUA_QUY_SA_TANG | Vua Quỷ Sa Tăng | Cơ chế chung. |
| THO_DAU_BAC | Thỏ Đầu Bạc | Cơ chế chung. |
| MA_TROI | Ma Trơi | Nhận khoảng 14.3% dame, giảm khoảng 85.7%. |
| DOI | Dơi | Nhận khoảng 14.3% dame, giảm khoảng 85.7%. |
| BI_MA | Bí Ma | Nhận khoảng 14.3% dame, giảm khoảng 85.7%. |
| COOLER | Cooler | Đòn xuyên giáp bị chia 100; đòn thường theo cơ chế chung. |
| COOLER_2 | Cooler 2 | Đòn xuyên giáp bị chia 100 nếu dùng class Cooler. |
| ONG_GIA_NOEL | Ông Già Noel | Không nhận sát thương trực tiếp. |
| THUY_TINH | Thủy Tinh | Cơ chế chung. |
| SON_TINH | Sơn Tinh | Cơ chế chung. |
| KARIN | Karin | Cơ chế chung. |
| TAUPAYPAY | Tàu Pảy Pảy | Cơ chế chung. |
| TAUPAYPAY_AFTER_KARIN | Tàu Pảy Pảy sau Karin | Cơ chế chung. |
| YAJIRO | Yajirô | Cơ chế chung. |
| MRPOPO | Mr.PôPô | Cơ chế chung. |
| THUONG_DE | Thượng Đế | Cơ chế chung. |
| KHI_BUBBLES | Khỉ Bubbles | Cơ chế chung. |
| THAN_VU_TRU | Thần Vũ Trụ | Cơ chế chung. |
| TO_SU_KAIO | Tổ Sư Kaio | Không nhận sát thương trực tiếp. |
| WHIS | Whis | Dame bị chia theo level. |
| GOLDEN_FRIEZA | Fide Vàng | Cơ chế chung. |
| DEATH_BEAM | Death Beam | Không nhận sát thương trực tiếp. |
| LAN_CON | Lân Con | Cơ chế chung. |
| SUPPER | Super Broly | Đòn xuyên giáp bị chia 100; hit quá lớn có thể bị cap khoảng 9-10 triệu. |
| BROLYANRGY | Broly Anrgy | Đòn xuyên giáp bị chia 100; đòn thường theo cơ chế chung. |
| BROLYHACHOA | Broly Hắc Hóa | Đòn xuyên giáp bị chia 100; đòn thường theo cơ chế chung. |
| THAYMA | Thây Ma | Hit thường bị giới hạn rất thấp; admin có mức riêng. |
| AN_TROM_TV | Ăn Trộm TV | Cơ chế chung. |

## Boss Class Riêng Chưa Nằm Rõ Trong Bảng BossData

| Boss / class | Cơ chế dame |
|---|---|
| Along | Cơ chế chung. |
| MORO / Bossgido | Cơ chế chung. |
| Kong Thanos | Cơ chế chung. |
| Tài Lộc Quá Lớn | Cơ chế chung. |
| Gozilaa / Đệ Tử Boss | Chỉ đệ tử mới gây dame. |
| Ăn Trộm | Cơ chế chung. |
| Ăn Trộm TV | Cơ chế chung. |
| Ở Dơ phó bản | Cơ chế chung. |
| Sói Héc Quên phó bản | Không nhận sát thương trực tiếp. |
| Xin Ba Tô phó bản | Không nhận sát thương trực tiếp. |
| Poc Bunny | Nhận 50% dame, giảm 50%. |
| Cađích Đường Rắn | Có điều kiện riêng; khi có khiên/pha phụ có thể giảm mạnh. |
| Nađíc Đường Rắn | Cơ chế chung. |
| Saibamen Đường Rắn | Nhận khoảng 14.3% dame, giảm khoảng 85.7%. |
| Pocolo | Có thể chặn hit kết liễu trong một số trạng thái; người giữ ngọc Namek chỉ gây 1 dame. |
| Tester 001 | Cơ chế chung. |
| Boss đại hội võ thuật 23 | Cơ chế chung nếu không có override riêng ở class con. |
| Training Boss | Cơ chế chung; người giữ ngọc Namek chỉ gây 1 dame. |
| Trung úy Xanh Lơ bản đồ kho báu | Nhận 50% dame nếu dùng chung logic Trung úy Xanh Lơ. |
| Yardart | Cơ chế chung. |
