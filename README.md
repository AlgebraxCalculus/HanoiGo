# HanoiGo
BTL môn Lập trình mobile
# 🔑 Thiết lập Mapbox Access Token
Repo này không chứa file token để tránh lộ key. Cần tự tạo file ```mapbox_access_token.xml trước khi chạy project:
1. Vào thư mục:
```bash
app/src/main/res/values/
2. Tạo file mới tên:
```bash
mapbox_access_token.xml
3. Thêm nội dung sau vào file vừa tạo, và thay YOUR_MAPBOX_TOKEN_HERE (token do Mapbox tạo) bằng token thật:
```bash
<resources>
    <string name="mapbox_access_token">YOUR_MAPBOX_TOKEN_HERE</string>
</resources>
4. Lưu file và build lại project.
